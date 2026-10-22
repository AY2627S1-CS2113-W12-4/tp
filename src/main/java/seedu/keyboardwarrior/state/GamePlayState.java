package seedu.keyboardwarrior.state;

import java.io.IOException;
import java.util.Objects;
import java.util.Random;

import seedu.keyboardwarrior.combat.EnemyFactory;
import seedu.keyboardwarrior.combat.Player;
import seedu.keyboardwarrior.combat.Skill;
import seedu.keyboardwarrior.encounter.CombatEncounter;
import seedu.keyboardwarrior.storage.SaveStore;
import seedu.keyboardwarrior.typing.ChallengeClock;
import seedu.keyboardwarrior.typing.TypingChallenge;
import seedu.keyboardwarrior.typing.TypingChallengeFactory;
import seedu.keyboardwarrior.typing.TypingEvaluator;
import seedu.keyboardwarrior.typing.TypingResult;
import seedu.keyboardwarrior.ui.CombatUi;
import seedu.keyboardwarrior.ui.StateUi;

/**
 * Runs five combat scenarios while the game loop keeps each typing countdown live.
 */
public class GamePlayState extends State {
    private static final int SCENARIO_COUNT = 5;
    private final ChallengeClock clock;
    private final CombatUi combatUi;
    private final Random random;
    private final SaveStore saveStore;
    /** Shares prompt selection history across all five scenarios in a run. */
    private final TypingChallengeFactory challengeFactory = new TypingChallengeFactory();
    /** The same player's remaining health carries into the next scenario. */
    private Player player;
    private CombatEncounter encounter;
    /** Null while choosing a skill; otherwise input is the current typing answer. */
    private TypingChallenge challenge;
    private int scenarioIndex;
    private int lastDisplayedSeconds;

    /**
     * Creates the gameplay state.
     *
     * @param stateMachine the machine that controls this state
     */
    public GamePlayState(StateMachine stateMachine) {
        this(stateMachine, new ChallengeClock(), new CombatUi());
    }

    /**
     * Accepts a controllable clock and interface for testing turns and scenario progression.
     */
    GamePlayState(StateMachine stateMachine, ChallengeClock clock, CombatUi combatUi) {
        this(stateMachine, clock, combatUi, new Random());
    }

    /** Accepts controlled enemy selection so gameplay tests remain reproducible. */
    GamePlayState(StateMachine stateMachine, ChallengeClock clock, CombatUi combatUi, Random random) {
        this(stateMachine, clock, combatUi, random, new SaveStore());
    }

    /** Accepts a save location so tests do not overwrite the player's history. */
    GamePlayState(StateMachine stateMachine, ChallengeClock clock, CombatUi combatUi,
            Random random, SaveStore saveStore) {
        super("Gameplay", stateMachine);
        this.clock = Objects.requireNonNull(clock, "Challenge clock must not be null");
        this.combatUi = Objects.requireNonNull(combatUi, "Combat interface must not be null");
        this.random = Objects.requireNonNull(random, "Random generator must not be null");
        this.saveStore = Objects.requireNonNull(saveStore, "Save store must not be null");
    }

    @Override
    public void enter() {
        player = new Player("Player", 100);
        scenarioIndex = 0;
        challenge = null;
        StateUi.showGameplayStart();
        startScenario();
    }

    @Override
    public void exit() {
        StateUi.clearCountdown();
    }

    @Override
    public void handleInput(String input) {
        if (input.trim().equalsIgnoreCase("exit")) {
            stateMachine.stop();
            return;
        }
        if (challenge == null) {
            chooseSkill(input);
            return;
        }
        double elapsedSeconds = Math.max(0.000000001, clock.getElapsedSeconds());
        if (elapsedSeconds > challenge.getTime()) {
            StateUi.showTimeUp();
        }
        finishAttempt(input, elapsedSeconds);
    }

    @Override
    public void update() {
        if (challenge == null) {
            return;
        }
        int remainingSeconds = clock.getRemainingSeconds();
        if (remainingSeconds != lastDisplayedSeconds) {
            StateUi.updateCountdown(remainingSeconds);
            lastDisplayedSeconds = remainingSeconds;
        }
        double elapsedSeconds = clock.getElapsedSeconds();
        if (elapsedSeconds > challenge.getTime()) {
            String typedText = StateUi.takeCurrentInput();
            StateUi.showTimeUp();
            finishAttempt(typedText, elapsedSeconds);
            StateUi.showInputPrompt();
        }
    }

    /**
     * Starts the next numbered encounter using the player's current health.
     */
    private void startScenario() {
        assert scenarioIndex >= 0 && scenarioIndex < SCENARIO_COUNT : "Scenario index must be in the run";
        assert player != null && player.getHealth() > 0 : "A scenario needs a living player";
        StateUi.showScenario(scenarioIndex + 1, SCENARIO_COUNT);
        encounter = new CombatEncounter(player, EnemyFactory.createRandomEnemy(random), combatUi, challengeFactory);
        encounter.begin();
    }

    /**
     * Starts a timed typing attempt only after an available skill is selected.
     */
    private void chooseSkill(String input) {
        for (Skill skill : player.getActiveSkills()) {
            if (skill.getName().equalsIgnoreCase(input.trim())) {
                challenge = encounter.preparePlayerTurn(skill);
                combatUi.showTypingChallenge(challenge);
                clock.start(challenge.getTime());
                lastDisplayedSeconds = clock.getRemainingSeconds();
                return;
            }
        }
        combatUi.showInvalidSkill();
    }

    /**
     * Scores one submission, resolves both turns and advances only after victory.
     */
    private void finishAttempt(String typedText, double elapsedSeconds) {
        assert challenge != null : "An active challenge is required to finish a typing attempt";
        TypingResult result = TypingEvaluator.evaluateTyping(challenge.getText(), typedText,
                elapsedSeconds, challenge.getTime());
        challenge = null;
        StateUi.clearCountdown();
        StateUi.showTypingResult(result);
        try {
            saveStore.save(result);
        } catch (IOException e) {
            StateUi.printLine("Could not save typing history: " + e.getMessage());
        }
        encounter.submitTypingResult(result);
        if (!encounter.getIsCompleted()) {
            return;
        }
        if (encounter.getEncounterOutcome().getIsVictory()) {
            scenarioIndex++;
            if (scenarioIndex < SCENARIO_COUNT) {
                startScenario();
                return;
            }
            StateUi.showRunVictory();
        } else {
            StateUi.showRunDefeat();
        }
        stateMachine.transitState(new MainMenuState(stateMachine));
    }
}
