package seedu.keyboardwarrior.state;

import java.io.IOException;
import java.util.Objects;
import java.util.Random;
import java.util.logging.Logger;

import seedu.keyboardwarrior.GameDifficulty;
import seedu.keyboardwarrior.GameSave;
import seedu.keyboardwarrior.combat.EnemyFactory;
import seedu.keyboardwarrior.combat.Player;
import seedu.keyboardwarrior.combat.Skill;
import seedu.keyboardwarrior.encounter.CombatEncounter;
import seedu.keyboardwarrior.seed.RandomSnapshot;
import seedu.keyboardwarrior.seed.RunRandom;
import seedu.keyboardwarrior.seed.SeedGenerator;
import seedu.keyboardwarrior.storage.GameSaveStore;
import seedu.keyboardwarrior.storage.SaveStore;
import seedu.keyboardwarrior.typing.ChallengeClock;
import seedu.keyboardwarrior.typing.PromptBank;
import seedu.keyboardwarrior.typing.TypingChallenge;
import seedu.keyboardwarrior.typing.TypingChallengeFactory;
import seedu.keyboardwarrior.typing.TypingEvaluator;
import seedu.keyboardwarrior.typing.TypingResult;
import seedu.keyboardwarrior.ui.CombatUi;
import seedu.keyboardwarrior.ui.StateUi;

/**
 * Runs five combat scenarios and a two-phase final boss while keeping typing
 * countdowns live.
 */
public class GamePlayState extends State {
    private static final int SCENARIO_COUNT = 5;
    private static final int BOSS_PHASE_COUNT = 2;
    /**
     * Records progression without adding messages to normal gameplay output.
     */
    private static final Logger LOGGER = Logger.getLogger(GamePlayState.class.getName());
    private final ChallengeClock clock;
    private final CombatUi combatUi;
    /**
     * Shared sequence consumed by both enemy selection and typing-prompt selection.
     */
    private final Random random;
    private final SaveStore saveStore;
    private final GameDifficulty difficulty;
    private final GameSaveStore gameSaveStore;
    /** Null for a fresh run; otherwise supplies the initial stable turn to restore. */
    private final GameSave restoredSave;
    /** Shares prompt selection history across regular scenarios and both boss phases. */
    private final TypingChallengeFactory challengeFactory;
    /** Shares both random choices and previous-passage exclusions with saved runs. */
    private final PromptBank promptBank;
    /** The same player's remaining health carries into the next scenario. */
    private Player player;
    private CombatEncounter encounter;
    /**
     * Null while choosing a skill; otherwise input is the current typing answer.
     */
    private TypingChallenge challenge;
    /**
     * Zero-based regular scenario index; equals the scenario count during the boss fight.
     */
    private int scenarioIndex;
    /**
     * Zero during regular scenarios, then one or two during the final boss.
     */
    private int bossPhase;
    /**
     * Last countdown value drawn, used to avoid redundant terminal updates.
     */
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
     * Creates gameplay with a shared random source and the menu's history store.
     *
     * @param stateMachine machine that controls this state
     * @param masterRandom shared source for enemy and prompt selection
     * @param saveStore history store retained when returning to the menu
     */
    public GamePlayState(StateMachine stateMachine, Random masterRandom, SaveStore saveStore) {
        this(stateMachine, new ChallengeClock(), new CombatUi(), masterRandom, saveStore);
    }

    /**
     * Starts a seeded run with the menu's selected difficulty and history location.
     *
     * @param stateMachine machine that controls this state
     * @param masterRandom shared source for enemy and prompt selection
     * @param saveStore history store retained when returning to the menu
     * @param difficulty selected difficulty for health, damage and typing deadlines
     */
    GamePlayState(StateMachine stateMachine, Random masterRandom, SaveStore saveStore, GameDifficulty difficulty) {
        this(stateMachine, new ChallengeClock(), new CombatUi(), masterRandom, saveStore, difficulty);
    }

    /** Starts a run using the menu's selected difficulty and history location. */
    GamePlayState(StateMachine stateMachine, SaveStore saveStore, GameDifficulty difficulty) {
        this(stateMachine, new ChallengeClock(), new CombatUi(), SeedGenerator.createRandom(), saveStore, difficulty);
    }

    /** Restores a saved run using new input, timing and combat objects. */
    GamePlayState(StateMachine stateMachine, SaveStore saveStore, GameSave save) {
        this(stateMachine, new ChallengeClock(), new CombatUi(), restoreRandom(save), saveStore, save.difficulty(),
                new GameSaveStore(saveStore.getDirectory().resolve("game-save.properties")), save);
    }

    /**
     * Accepts a controllable clock and interface for testing turns and scenario
     * progression.
     *
     * @param stateMachine machine that controls this state
     * @param clock clock used to measure typing duration
     * @param combatUi interface used to display combat and typing challenges
     */
    GamePlayState(StateMachine stateMachine, ChallengeClock clock, CombatUi combatUi) {
        this(stateMachine, clock, combatUi, SeedGenerator.createRandom());
    }

    /**
     * Accepts a shared random source for reproducible enemy and prompt selection.
     *
     * @param stateMachine machine that controls this state
     * @param clock clock used to measure typing duration
     * @param combatUi interface used to display combat and typing challenges
     * @param random shared source for enemy and prompt selection
     */
    GamePlayState(StateMachine stateMachine, ChallengeClock clock, CombatUi combatUi, Random random) {
        this(stateMachine, clock, combatUi, random, new SaveStore());
    }

    /**
     * Accepts a save location so tests do not overwrite the player's history.
     *
     * @param stateMachine machine that controls this state
     * @param clock clock used to measure typing duration
     * @param combatUi interface used to display combat and typing challenges
     * @param random shared source for enemy and prompt selection
     * @param saveStore history store retained when returning to the menu
     */
    GamePlayState(StateMachine stateMachine, ChallengeClock clock, CombatUi combatUi,
            Random random, SaveStore saveStore) {
        this(stateMachine, clock, combatUi, random, saveStore, GameDifficulty.HARD);
    }

    /** Accepts controlled dependencies and run settings for testing difficulty effects and progression. */
    GamePlayState(StateMachine stateMachine, ChallengeClock clock, CombatUi combatUi,
            Random random, SaveStore saveStore, GameDifficulty difficulty) {
        this(stateMachine, clock, combatUi, random, saveStore, difficulty,
                new GameSaveStore(saveStore.getDirectory().resolve("game-save.properties")), null);
    }

    /** Accepts run storage and an optional snapshot for deterministic restoration and failure tests. */
    GamePlayState(StateMachine stateMachine, ChallengeClock clock, CombatUi combatUi,
            Random random, SaveStore saveStore, GameDifficulty difficulty, GameSaveStore gameSaveStore,
            GameSave restoredSave) {
        super("Gameplay", stateMachine);
        this.clock = Objects.requireNonNull(clock, "Challenge clock must not be null");
        this.combatUi = Objects.requireNonNull(combatUi, "Combat interface must not be null");
        Objects.requireNonNull(random, "Random generator must not be null");
        this.random = restoredSave != null && restoredSave.randomSnapshot() != null
                ? restoreRandom(restoredSave) : random;
        this.saveStore = Objects.requireNonNull(saveStore, "Save store must not be null");
        this.difficulty = Objects.requireNonNull(difficulty, "Game difficulty must not be null");
        this.gameSaveStore = Objects.requireNonNull(gameSaveStore, "Game save store must not be null");
        if (restoredSave != null && restoredSave.difficulty() != difficulty) {
            throw new IllegalArgumentException("Restored difficulty must match the saved run");
        }
        this.restoredSave = restoredSave;
        this.promptBank = new PromptBank(this.random);
        if (restoredSave != null && restoredSave.randomSnapshot() != null) {
            promptBank.restoreHistory(restoredSave.randomSnapshot().previousPromptIndices());
        }
        this.challengeFactory = new TypingChallengeFactory(promptBank, difficulty);
    }

    /** Older saves still load, but only new saves contain the original random sequence position. */
    private static Random restoreRandom(GameSave save) {
        return save.randomSnapshot() == null ? SeedGenerator.createRandom()
                : RunRandom.restore(save.randomSnapshot().state());
    }

    /**
     * Initializes a full-health player and starts the first regular scenario.
     */
    @Override
    public void enter() {
        if (restoredSave != null) {
            restoreRun();
            return;
        }
        player = new Player("Player", difficulty.getStartingHealth());
        scenarioIndex = 0;
        bossPhase = 0;
        challenge = null;
        StateUi.showGameplayStart();
        StateUi.printLine("Difficulty: " + difficulty.getDisplayName());
        startScenario();
    }

    /** Restores health and attack rotation directly, without applying difficulty multipliers again. */
    private void restoreRun() {
        assert restoredSave != null : "Restoration requires a validated run snapshot";
        player = new Player("Player", restoredSave.playerHealth());
        scenarioIndex = restoredSave.scenarioIndex();
        bossPhase = restoredSave.bossPhase();
        challenge = null;
        StateUi.printLine("Resuming saved run. Difficulty: " + difficulty.getDisplayName());
        if (bossPhase == 0) {
            StateUi.showScenario(scenarioIndex + 1, SCENARIO_COUNT);
        } else {
            StateUi.showBossPhase(bossPhase, BOSS_PHASE_COUNT);
        }
        encounter = new CombatEncounter(player, restoredSave.restoreEnemy(), combatUi, challengeFactory, difficulty);
        encounter.begin(restoredSave.nextEnemySkillIndex());
        assert player.getHealth() == restoredSave.playerHealth() && challenge == null
                : "Restoration must preserve health and return to skill selection";
        assert encounter.snapshot().equals(new CombatEncounter.CombatSnapshot(restoredSave.enemyType(),
                restoredSave.enemyHealth(), restoredSave.nextEnemySkillIndex()))
                : "Restoration must preserve the opponent's health and next attack";
    }

    /**
     * Clears the live countdown when gameplay ends.
     */
    @Override
    public void exit() {
        StateUi.clearCountdown();
    }

    /**
     * Handles an exit command, skill selection or typing submission for the active turn.
     *
     * @param input input interpreted according to the current state
     */
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

    /**
     * Refreshes the countdown and scores unfinished input once the deadline has passed.
     */
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
        encounter = new CombatEncounter(player, EnemyFactory.createRandomEnemy(random, scenarioIndex + 1),
                combatUi, challengeFactory, difficulty);
        encounter.begin();
        saveCurrentRun();
    }

    /**
     * Starts a fresh boss phase, retaining player health and discarding the
     * previous attack's overkill.
     */
    private void startBossPhase() {
        assert scenarioIndex == SCENARIO_COUNT : "The boss follows all five regular scenarios";
        assert bossPhase >= 1 && bossPhase <= BOSS_PHASE_COUNT : "Boss phase must be in the final fight";
        assert player != null && player.getHealth() > 0 : "A boss phase needs a living player";
        LOGGER.fine(() -> "Starting final boss phase " + bossPhase);
        StateUi.showBossPhase(bossPhase, BOSS_PHASE_COUNT);
        encounter = new CombatEncounter(player, EnemyFactory.createBossPhase(bossPhase), combatUi,
                challengeFactory, difficulty);
        encounter.begin();
        saveCurrentRun();
    }

    /**
     * Starts a timed typing attempt only after an available skill is selected.
     *
     * @param input skill display name, compared without case or surrounding whitespace
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
     * Scores and saves one submission, then resolves combat and advances after victory.
     * A storage failure is reported without preventing the combat turn.
     *
     * @param typedText text captured or submitted for evaluation
     * @param elapsedSeconds elapsed typing duration in seconds
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
            saveCurrentRun();
            return;
        }
        if (encounter.getEncounterOutcome().getIsVictory()) {
            if (advanceAfterVictory()) {
                return;
            }
            StateUi.showRunVictory();
        } else {
            StateUi.showRunDefeat();
        }
        try {
            gameSaveStore.delete();
        } catch (IOException exception) {
            StateUi.printLine("Could not remove finished game save: " + exception.getMessage());
        }
        stateMachine.transitState(new MainMenuState(stateMachine, saveStore, difficulty));
    }

    /** Persists only stable player turns; storage failures leave combat playable. */
    private void saveCurrentRun() {
        assert challenge == null : "Autosave must not capture an unfinished typing attempt";
        assert player != null && player.getHealth() > 0 : "Autosave requires a living player";
        assert encounter != null && !encounter.getIsCompleted() : "Autosave requires an active encounter";
        assert (bossPhase == 0 && scenarioIndex >= 0 && scenarioIndex < SCENARIO_COUNT)
                || (bossPhase >= 1 && bossPhase <= BOSS_PHASE_COUNT && scenarioIndex == SCENARIO_COUNT)
                : "Autosave progression must identify a regular scenario or a final boss phase";
        CombatEncounter.CombatSnapshot snapshot = encounter.snapshot();
        GameSave save = new GameSave(GameSave.CURRENT_VERSION, difficulty, player.getHealth(), scenarioIndex,
                bossPhase, snapshot.enemyType(), snapshot.enemyHealth(), snapshot.nextEnemySkillIndex(),
                random instanceof RunRandom runRandom
                        ? new RandomSnapshot(runRandom.snapshot(), promptBank.snapshotHistory()) : null);
        try {
            gameSaveStore.save(save);
        } catch (IOException exception) {
            StateUi.printLine("Could not save game: " + exception.getMessage());
        }
    }

    /**
     * Advances regular encounters and boss phases without giving a transition an
     * enemy turn.
     *
     * @return true when another encounter started, or false after the final boss
     *     phase
     */
    private boolean advanceAfterVictory() {
        if (bossPhase == 0) {
            scenarioIndex++;
            if (scenarioIndex < SCENARIO_COUNT) {
                startScenario();
                return true;
            }
        }
        if (bossPhase < BOSS_PHASE_COUNT) {
            bossPhase++;
            startBossPhase();
            return true;
        }
        assert scenarioIndex == SCENARIO_COUNT && bossPhase == BOSS_PHASE_COUNT
                : "Run victory requires all scenarios and both boss phases";
        return false;
    }
}
