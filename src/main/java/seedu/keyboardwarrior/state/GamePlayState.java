package seedu.keyboardwarrior.state;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.logging.Logger;

import seedu.keyboardwarrior.GameDifficulty;
import seedu.keyboardwarrior.GameSave;
import seedu.keyboardwarrior.combat.EnemyFactory;
import seedu.keyboardwarrior.combat.Player;
import seedu.keyboardwarrior.combat.Skill;
import seedu.keyboardwarrior.encounter.CombatEncounter;
import seedu.keyboardwarrior.encounter.Encounter;
import seedu.keyboardwarrior.encounter.EncounterFactory;
import seedu.keyboardwarrior.encounter.EncounterSnapshot;
import seedu.keyboardwarrior.encounter.EventEncounter;
import seedu.keyboardwarrior.encounter.PuzzleEncounter;
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
import seedu.keyboardwarrior.ui.EventUi;
import seedu.keyboardwarrior.ui.StateUi;

/**
 * Runs five encounter choices and a two-phase final boss while keeping typing
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
    /**
     * Null for a fresh run; otherwise supplies the initial stable turn to restore.
     */
    private final GameSave restoredSave;
    /**
     * Shares prompt selection history across regular scenarios and both boss
     * phases.
     */
    private final TypingChallengeFactory challengeFactory;
    /**
     * Shares both random choices and previous-passage exclusions with saved runs.
     */
    private final PromptBank promptBank;
    /** Displays the existing puzzle, reaction and merchant event interfaces. */
    private final EventUi eventUi = new EventUi();
    /** Shares event content and unclaimed rewards across the player's entire run. */
    private EncounterFactory encounterFactory;
    /** Nonempty only while choosing the next regular encounter. */
    private List<Encounter> choices = List.of();
    /** The same player's remaining health carries into the next scenario. */
    private Player player;
    private Encounter encounter;
    /**
     * Null at encounter/skill selection or during a reaction wait; otherwise holds the active answer target.
     */
    private TypingChallenge challenge;
    /**
     * Zero-based regular scenario index; equals the scenario count during the boss
     * fight.
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
     * @param saveStore    history store retained when returning to the menu
     */
    public GamePlayState(StateMachine stateMachine, Random masterRandom, SaveStore saveStore) {
        this(stateMachine, new ChallengeClock(), new CombatUi(), masterRandom, saveStore);
    }

    /**
     * Starts a seeded run with the menu's selected difficulty and history location.
     *
     * @param stateMachine machine that controls this state
     * @param masterRandom shared source for enemy and prompt selection
     * @param saveStore    history store retained when returning to the menu
     * @param difficulty   selected difficulty for health, damage and typing
     *                     deadlines
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
     * @param clock        clock used to measure typing duration
     * @param combatUi     interface used to display combat and typing challenges
     */
    GamePlayState(StateMachine stateMachine, ChallengeClock clock, CombatUi combatUi) {
        this(stateMachine, clock, combatUi, SeedGenerator.createRandom());
    }

    /**
     * Accepts a shared random source for reproducible enemy and prompt selection.
     *
     * @param stateMachine machine that controls this state
     * @param clock        clock used to measure typing duration
     * @param combatUi     interface used to display combat and typing challenges
     * @param random       shared source for enemy and prompt selection
     */
    GamePlayState(StateMachine stateMachine, ChallengeClock clock, CombatUi combatUi, Random random) {
        this(stateMachine, clock, combatUi, random, new SaveStore());
    }

    /**
     * Accepts a save location so tests do not overwrite the player's history.
     *
     * @param stateMachine machine that controls this state
     * @param clock        clock used to measure typing duration
     * @param combatUi     interface used to display combat and typing challenges
     * @param random       shared source for enemy and prompt selection
     * @param saveStore    history store retained when returning to the menu
     */
    GamePlayState(StateMachine stateMachine, ChallengeClock clock, CombatUi combatUi,
            Random random, SaveStore saveStore) {
        this(stateMachine, clock, combatUi, random, saveStore, GameDifficulty.HARD);
    }

    /**
     * Accepts controlled dependencies and run settings for testing difficulty
     * effects and progression.
     */
    GamePlayState(StateMachine stateMachine, ChallengeClock clock, CombatUi combatUi,
            Random random, SaveStore saveStore, GameDifficulty difficulty) {
        this(stateMachine, clock, combatUi, random, saveStore, difficulty,
                new GameSaveStore(saveStore.getDirectory().resolve("game-save.properties")), null);
    }

    /**
     * Accepts run storage and an optional snapshot for deterministic restoration
     * and failure tests.
     */
    GamePlayState(StateMachine stateMachine, ChallengeClock clock, CombatUi combatUi,
            Random random, SaveStore saveStore, GameDifficulty difficulty, GameSaveStore gameSaveStore,
            GameSave restoredSave) {
        super("Gameplay", stateMachine);
        this.clock = Objects.requireNonNull(clock, "Challenge clock must not be null");
        this.combatUi = Objects.requireNonNull(combatUi, "Combat interface must not be null");
        Objects.requireNonNull(random, "Random generator must not be null");
        this.random = restoredSave != null && restoredSave.randomSnapshot() != null
                ? restoreRandom(restoredSave)
                : random;
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

    /**
     * Older saves still load, but only new saves contain the original random
     * sequence position.
     */
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
        encounterFactory = new EncounterFactory(random, player);
        scenarioIndex = 0;
        bossPhase = 0;
        challenge = null;
        StateUi.showGameplayStart();
        StateUi.printLine("Difficulty: " + difficulty.getDisplayName());
        startScenario();
    }

    /**
     * Restores health and attack rotation directly, without applying difficulty
     * multipliers again.
     */
    private void restoreRun() {
        assert restoredSave != null : "Restoration requires a validated run snapshot";
        restoreRunProgress();
        showRestoredProgress();
        restorePendingEncounter();
        assert player.getHealth() == restoredSave.playerHealth() : "Restoration must preserve health";
    }

    /** Restores the player and progression before reconstructing offered or active encounters. */
    private void restoreRunProgress() {
        player = restoredSave.restorePlayer();
        encounterFactory = new EncounterFactory(random, player);
        scenarioIndex = restoredSave.scenarioIndex();
        bossPhase = restoredSave.bossPhase();
        challenge = null;
    }

    /** Announces the saved difficulty and current scenario or boss phase. */
    private void showRestoredProgress() {
        StateUi.printLine("Resuming saved run. Difficulty: " + difficulty.getDisplayName());
        if (bossPhase == 0) {
            StateUi.showScenario(scenarioIndex + 1, SCENARIO_COUNT);
        } else {
            StateUi.showBossPhase(bossPhase, BOSS_PHASE_COUNT);
        }
    }

    /** Recreates saved choices or resumes the selected encounter without regenerating content. */
    private void restorePendingEncounter() {
        if (restoredSave.activeEncounter() == null) {
            choices = restoredSave.choices().stream().map(this::restoreEncounter).toList();
            StateUi.showEncounterChoices(choices);
        } else {
            resumeActiveEncounter(restoredSave.activeEncounter());
        }
    }

    /** Opens the saved combat turn or event stage without replaying its preceding effects. */
    private void resumeActiveEncounter(EncounterSnapshot savedEncounter) {
        encounter = restoreEncounter(savedEncounter);
        if (encounter instanceof CombatEncounter combat) {
            combat.begin(Integer.parseInt(savedEncounter.data().get("nextEnemySkillIndex")));
        } else {
            EventEncounter event = (EventEncounter) encounter;
            activateEventChallenge(savedEncounter.data().get("phase").equals("NOT_STARTED")
                    ? event.prepareEvent(challengeFactory) : event.resumeEvent(challengeFactory));
        }
    }

    /** Restores a previously generated choice without consuming the shared random sequence. */
    private Encounter restoreEncounter(EncounterSnapshot snapshot) {
        return encounterFactory.restore(snapshot, player, combatUi, eventUi, challengeFactory, difficulty,
                scenarioIndex + 1, bossPhase);
    }

    /**
     * Clears the live countdown when gameplay ends.
     */
    @Override
    public void exit() {
        StateUi.clearCountdown();
    }

    /**
     * Handles an exit command, skill selection or typing submission for the active
     * turn.
     *
     * @param input input interpreted according to the current state
     */
    @Override
    public void handleInput(String input) {
        if (input.trim().equalsIgnoreCase("exit")) {
            if (encounter instanceof EventEncounter && challenge == null) {
                // Preserve the remaining reaction wait without saving unfinished combat input.
                saveCurrentRun();
            }
            stateMachine.stop();
            return;
        }
        if (!choices.isEmpty()) {
            chooseEncounter(input);
            return;
        }
        if (challenge == null) {
            if (encounter instanceof CombatEncounter) {
                chooseSkill(input);
            } else {
                StateUi.printLine("Wait for the reaction command, or type Exit to quit.");
            }
            return;
        }
        double elapsedSeconds = Math.max(0.000000001, clock.getElapsedSeconds());
        if (elapsedSeconds > challenge.getTime()) {
            StateUi.showTimeUp();
        }
        finishAttempt(input, elapsedSeconds);
    }

    /**
     * Refreshes the countdown and scores unfinished input once the deadline has
     * passed.
     */
    @Override
    public void update() {
        updateWaiting();
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

    /** Advances delayed events even in plain consoles, without auto-submitting buffered input. */
    @Override
    public void updateWaiting() {
        if (choices.isEmpty() && challenge == null && encounter instanceof EventEncounter event) {
            TypingChallenge nextChallenge = event.updateEvent(challengeFactory);
            if (nextChallenge != null) {
                activateEventChallenge(nextChallenge);
                saveCurrentRun();
            }
        }
    }

    /**
     * Starts the next numbered encounter using the player's current health.
     */
    private void startScenario() {
        assert scenarioIndex >= 0 && scenarioIndex < SCENARIO_COUNT : "Scenario index must be in the run";
        assert player != null && player.getHealth() > 0 : "A scenario needs a living player";
        StateUi.showScenario(scenarioIndex + 1, SCENARIO_COUNT);
        encounter = null;
        choices = encounterFactory.generateChoices(player, combatUi, eventUi, challengeFactory,
                difficulty, scenarioIndex + 1);
        StateUi.showEncounterChoices(choices);
        saveCurrentRun();
    }

    /** Starts exactly one of the already generated choices; invalid input changes no run state. */
    private void chooseEncounter(String input) {
        int selected;
        try {
            selected = Integer.parseInt(input.trim()) - 1;
        } catch (NumberFormatException exception) {
            StateUi.showInvalidEncounterChoice();
            return;
        }
        if (selected < 0 || selected >= choices.size()) {
            StateUi.showInvalidEncounterChoice();
            return;
        }
        encounter = choices.get(selected);
        choices = List.of();
        if (encounter instanceof CombatEncounter combat) {
            combat.begin();
        } else {
            activateEventChallenge(((EventEncounter) encounter).prepareEvent(challengeFactory));
        }
        saveCurrentRun();
    }

    /** Reports whether the state is waiting for an encounter selection. */
    boolean isChoosingEncounter() {
        return !choices.isEmpty();
    }

    /** Starts an event stage's clock without displaying a puzzle's hidden answer. */
    private void activateEventChallenge(TypingChallenge nextChallenge) {
        challenge = nextChallenge;
        if (challenge != null) {
            StateUi.showEventChallenge(challenge, encounter instanceof PuzzleEncounter);
            clock.start(challenge.getTime());
            lastDisplayedSeconds = clock.getRemainingSeconds();
        }
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
        CombatEncounter combat = new CombatEncounter(player, EnemyFactory.createBossPhase(bossPhase), combatUi,
                challengeFactory, difficulty);
        encounter = combat;
        choices = List.of();
        combat.begin();
        saveCurrentRun();
    }

    /**
     * Starts a timed typing attempt only after an available skill is selected.
     *
     * @param input skill display name, compared without case or surrounding
     *              whitespace
     */
    private void chooseSkill(String input) {
        for (Skill skill : player.getActiveSkills()) {
            if (skill.getName().equalsIgnoreCase(input.trim())) {
                challenge = ((CombatEncounter) encounter).preparePlayerTurn(skill);
                combatUi.showTypingChallenge(challenge);
                clock.start(challenge.getTime());
                lastDisplayedSeconds = clock.getRemainingSeconds();
                return;
            }
        }
        combatUi.showInvalidSkill();
    }

    /**
     * Scores and saves one submission, then resolves combat and advances after
     * victory.
     * A storage failure is reported without preventing the combat turn.
     *
     * @param typedText      text captured or submitted for evaluation
     * @param elapsedSeconds elapsed typing duration in seconds
     */
    private void finishAttempt(String typedText, double elapsedSeconds) {
        assert challenge != null : "An active challenge is required to finish a typing attempt";
        boolean overdue = elapsedSeconds > challenge.getTime();
        TypingResult result = evaluateAttempt(typedText, elapsedSeconds);
        saveTypingHistory(result);
        resolveAttempt(result, typedText, overdue);
        advanceAfterAttempt();
    }

    /** Scores the active challenge and replaces its countdown with typing feedback. */
    private TypingResult evaluateAttempt(String typedText, double elapsedSeconds) {
        TypingResult result = TypingEvaluator.evaluateTyping(challenge.getText(), typedText,
                elapsedSeconds, challenge.getTime());
        challenge = null;
        StateUi.clearCountdown();
        StateUi.showTypingResult(result);
        return result;
    }

    /** Reports history-write failures without preventing the encounter from resolving. */
    private void saveTypingHistory(TypingResult result) {
        try {
            saveStore.save(result);
        } catch (IOException e) {
            StateUi.printLine("Could not save typing history: " + e.getMessage());
        }
    }

    /** Applies combat performance or an event answer, treating overdue event answers as failures. */
    private void resolveAttempt(TypingResult result, String typedText, boolean overdue) {
        if (encounter instanceof CombatEncounter combat) {
            combat.submitTypingResult(result);
        } else {
            TypingChallenge nextChallenge = ((EventEncounter) encounter)
                    .submitAnswer(overdue ? "" : typedText, challengeFactory);
            activateEventChallenge(nextChallenge);
        }
    }

    /** Saves an unfinished encounter, starts the next encounter or ends the run. */
    private void advanceAfterAttempt() {
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
        finishRun();
    }

    /** Removes the finished run's save and returns to the menu even if deletion fails. */
    private void finishRun() {
        try {
            gameSaveStore.delete();
        } catch (IOException exception) {
            StateUi.printLine("Could not remove finished game save: " + exception.getMessage());
        }
        stateMachine.transitState(new MainMenuState(stateMachine, saveStore, difficulty));
    }

    /**
     * Persists only stable player turns; storage failures leave combat playable.
     */
    private void saveCurrentRun() {
        GameSave save = createRunSnapshot();
        try {
            gameSaveStore.save(save);
        } catch (IOException exception) {
            StateUi.printLine("Could not save game: " + exception.getMessage());
        }
    }

    /** Captures stable progression, encounters and random history without writing to storage. */
    private GameSave createRunSnapshot() {
        assert player != null && player.getHealth() > 0 : "Autosave requires a living player";
        assert !choices.isEmpty() || encounter != null && !encounter.getIsCompleted()
                : "Autosave requires encounter choices or an active encounter";
        assert (bossPhase == 0 && scenarioIndex >= 0 && scenarioIndex < SCENARIO_COUNT)
                || (bossPhase >= 1 && bossPhase <= BOSS_PHASE_COUNT && scenarioIndex == SCENARIO_COUNT)
                : "Autosave progression must identify a regular scenario or a final boss phase";
        RandomSnapshot randomSnapshot = random instanceof RunRandom runRandom
                ? new RandomSnapshot(runRandom.snapshot(), promptBank.snapshotHistory()) : null;
        return !choices.isEmpty()
                ? GameSave.forChoices(difficulty, player, scenarioIndex, randomSnapshot,
                        choices.stream().map(encounterFactory::snapshot).toList())
                : GameSave.forEncounter(difficulty, player, scenarioIndex, bossPhase, randomSnapshot,
                        encounterFactory.snapshot(encounter));
    }

    /**
     * Advances regular encounters and boss phases without giving a transition an
     * enemy turn.
     *
     * @return true when another encounter started, or false after the final boss
     *         phase
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
