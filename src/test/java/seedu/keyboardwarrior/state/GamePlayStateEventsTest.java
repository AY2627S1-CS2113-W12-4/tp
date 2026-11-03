package seedu.keyboardwarrior.state;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.keyboardwarrior.GameDifficulty;
import seedu.keyboardwarrior.GameSave;
import seedu.keyboardwarrior.combat.Enemy;
import seedu.keyboardwarrior.combat.EnemyFactory;
import seedu.keyboardwarrior.combat.Player;
import seedu.keyboardwarrior.combat.SkillRegistry;
import seedu.keyboardwarrior.encounter.EncounterSnapshot;
import seedu.keyboardwarrior.seed.RandomSnapshot;
import seedu.keyboardwarrior.seed.RunRandom;
import seedu.keyboardwarrior.storage.GameSaveStore;
import seedu.keyboardwarrior.storage.SaveStore;
import seedu.keyboardwarrior.typing.ChallengeClock;
import seedu.keyboardwarrior.typing.TypingChallenge;
import seedu.keyboardwarrior.ui.CombatUi;
import seedu.keyboardwarrior.ui.StateUi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Exercises event selection, timed answers, progression and save restoration through the real controller. */
class GamePlayStateEventsTest {
    @TempDir
    private Path tempDir;
    private ByteArrayOutputStream output;
    private PrintStream originalOutput;
    private PrintStream capturedOutput;
    private SaveStore history;
    private GameSaveStore runStore;
    private RecordingStateMachine machine;
    private ControlledClock clock;
    private RecordingCombatUi combatUi;

    @BeforeEach
    void setUp() {
        originalOutput = System.out;
        output = new ByteArrayOutputStream();
        capturedOutput = new PrintStream(output, true, StandardCharsets.UTF_8);
        System.setOut(capturedOutput);
        StateUi.detachTerminal();
        history = new SaveStore(tempDir.resolve("history.txt"));
        runStore = new GameSaveStore(tempDir.resolve("game-save.properties"));
    }

    @AfterEach
    void tearDown() {
        if (machine != null && machine.isRunning()) {
            machine.stop();
        }
        StateUi.detachTerminal();
        System.setOut(originalOutput);
        capturedOutput.close();
    }

    @Test
    void enter_freshRun_offersThreeChoicesAndInvalidInputPreservesTheirRandomState() throws IOException {
        start(GameDifficulty.HARD, new RunRandom(42), null);
        GameSave before = saved();

        assertEquals(3, before.choices().size());
        assertEquals("combat", before.choices().get(0).type());
        assertFalse(before.choices().get(1).type().equals("combat"));
        assertTrue(report().contains("Scenario 1/5"));
        assertEquals(0, combatUi.encounters, "An offered combat must not start before it is selected.");
        for (String invalid : List.of("0", "4", "Ping", "", "-1", "999999999999999")) {
            machine.handleInput(invalid);
            assertEquals(before, saved(), "Invalid input must not regenerate choices or consume randomness.");
        }
        assertEquals(0, clock.starts);
    }

    @Test
    void puzzle_rewardAndContinue_preserveHiddenAnswerSkillsBonusAndFutureChoices() throws IOException {
        Player player = new Player("Player", 80);
        player.setBonusTime(7);
        startSaved(GameSave.forChoices(GameDifficulty.NORMAL, player, 0, randomSnapshot(),
                puzzleChoices("What token names the hidden protocol?", "saffron", 30)));
        output.reset();

        machine.handleInput("2");

        assertTrue(report().contains("What token names the hidden protocol?"));
        assertFalse(report().contains("saffron"), "The generic typing display must not disclose a puzzle answer.");
        assertEquals(74, clock.duration);
        submit("saffron", 1.0);
        GameSave checkpoint = saved();
        assertEquals(1, checkpoint.scenarioIndex());
        assertEquals(80, checkpoint.playerHealth());
        assertEquals(7, checkpoint.bonusTime());
        assertTrue(checkpoint.skillIds().contains("overclock"));
        assertEquals(6, checkpoint.skillIds().size());

        playSlowEncryptionTurn();
        GameSave uninterrupted = saved();
        assertEquals(15, uninterrupted.enemyHealth(), "NORMAL must apply twenty damage at 10.25 WPM.");
        int enemyDamage = EnemyFactory.createScenarioEnemy(checkpoint.choices().get(0).data().get("enemyType"), 2)
                .getActiveSkills().get(0).getBaseDamage();
        assertEquals(80 - enemyDamage, uninterrupted.playerHealth());
        machine.stop();
        startSaved(checkpoint);

        assertTrue(report().contains("Resuming saved run. Difficulty: Normal"));
        playSlowEncryptionTurn();
        assertEquals(uninterrupted, saved(),
                "Continue must preserve offered enemies, the prompt sequence, loadout and countdown effects.");
    }

    @Test
    void dialogue_tradeBranchContinue_resumesSecondAnswerAndGrantsRewardOnce() throws IOException {
        Player player = new Player("Player", 90);
        player.setBonusTime(3);
        startSaved(activeEvent(player, 1, dialogue(0)));
        submit("I will not be extorted", 1.0);
        GameSave trade = saved();
        assertEquals("1", trade.activeEncounter().data().get("node"));
        assertEquals(5, trade.skillIds().size(), "Choosing the trade branch must not award its reward yet.");
        machine.stop();
        output.reset();
        startSaved(trade);

        assertTrue(report().contains("Secure the deal"));
        assertFalse(report().contains("Stand your ground and haggle"));
        assertEquals(10, clock.duration);
        submit("Show me your rare wares", 1.0);
        GameSave completed = saved();
        assertEquals(2, completed.scenarioIndex());
        assertEquals(90, completed.playerHealth());
        assertEquals(3, completed.bonusTime());
        assertTrue(completed.skillIds().contains("overclock"));
        machine.handleInput("Show me your rare wares");
        assertEquals(completed, saved(), "A consumed response cannot grant another reward at the next choice menu.");
    }

    @Test
    void dialogue_escapeBranchContinue_retainsBranchAndRejectsLateCorrectEscape() throws IOException {
        Player player = new Player("Player", 90);
        startSaved(activeEvent(player, 1, dialogue(0)));
        submit("refuse", 1.0);
        GameSave escape = saved();
        assertEquals("2", escape.activeEncounter().data().get("node"));
        machine.stop();
        output.reset();
        startSaved(escape);

        assertTrue(report().contains("Dodge the guards:"));
        assertEquals(5, clock.duration);
        submit("flee", 6.0);

        assertEquals(70, saved().playerHealth(), "A correct escape after its deadline must still fail.");
        assertEquals(5, saved().skillIds().size());
        assertEquals(2, saved().scenarioIndex());
        assertTrue(report().contains("Time's up"));
    }

    @Test
    void puzzle_correctAnswerAfterDeadline_appliesPenaltyWithoutReward() throws IOException {
        startSaved(activeEvent(new Player("Player", 100), 0,
                puzzle("Name the secret token", "saffron", "ACCEPTING_ANSWER")));

        submit("saffron", 31.0);

        GameSave next = saved();
        assertEquals(1, next.scenarioIndex());
        assertEquals(-2, next.bonusTime());
        assertEquals(5, next.skillIds().size());
        assertFalse(next.skillIds().contains("overclock"));
        assertTrue(report().contains("Time's up"));
    }

    @Test
    void reaction_waitingUpdate_revealsCommandWithoutInputAndExpiryAppliesDamage() throws Exception {
        startSaved(activeEvent(new Player("Player", 100), 0, reaction("WAITING", 0)));
        assertEquals(0, clock.starts);
        machine.handleInput("premature answer");
        assertEquals("WAITING", saved().activeEncounter().data().get("phase"));
        // Restoration starts a wall-clock wait; its zero remaining delay needs one elapsed millisecond.
        Thread.sleep(2);

        machine.updateWaiting();

        assertEquals(1, clock.starts);
        assertEquals(2, clock.duration);
        assertTrue(report().contains("TYPE 'DODGE' NOW"));
        assertEquals("ACCEPTING_ANSWER", saved().activeEncounter().data().get("phase"));
        clock.elapsed = 3.0;
        machine.update();

        assertEquals(70, saved().playerHealth());
        assertEquals(1, saved().scenarioIndex());
        assertTrue(report().contains("Time's up"));
    }

    @Test
    void reaction_correctOnTimeResponse_healsAndPersistsHealthAboveStartingMaximum() throws IOException {
        Player player = new Player("Player", 100);
        player.addSkill(SkillRegistry.getSkill("overclock"));
        startSaved(activeEvent(player, 0, reaction("ACCEPTING_ANSWER", 0)));

        submit("dodge", 1.0);

        GameSave healed = saved();
        assertEquals(110, healed.playerHealth());
        assertTrue(healed.skillIds().contains("overclock"));
        machine.stop();
        startSaved(healed);
        machine.handleInput("1");
        assertEquals(110, combatUi.player.getHealth());
        assertEquals(35, combatUi.enemy.getHealth());
    }

    @Test
    void reaction_exitDuringWait_savesRemainingDelayWithoutChangingRandomState() throws Exception {
        startSaved(activeEvent(new Player("Player", 100), 0, reaction("WAITING", 20_000)));
        GameSave before = saved();
        Thread.sleep(2);

        machine.handleInput("exit");

        GameSave after = saved();
        assertFalse(machine.isRunning());
        assertEquals(before.randomSnapshot(), after.randomSnapshot());
        assertEquals("WAITING", after.activeEncounter().data().get("phase"));
        long remaining = Long.parseLong(after.activeEncounter().data().get("delayMillis"));
        assertTrue(remaining >= 0 && remaining < 20_000, "Quit must retain the current remaining wait.");
        assertEquals(100, after.playerHealth());
    }

    @Test
    void dialogue_lethalGuardAttack_returnsToMenuAndRemovesActiveSave() throws IOException {
        startSaved(activeEvent(new Player("Player", 20), 0, dialogue(2)));

        submit("wrong response", 1.0);

        assertTrue(runStore.load().isEmpty());
        assertInstanceOf(MainMenuState.class, machine.nextState);
        assertTrue(report().contains("Game over."));
        assertFalse(report().contains("Scenario 2/5"));
        assertFalse(report().contains("Final Boss"));
    }

    @Test
    void progression_fivePuzzleEvents_requireBothBossPhasesBeforeRunVictory() throws IOException {
        start(GameDifficulty.HARD, new PuzzleOnlyRandom(), null);
        for (int scenario = 0; scenario < 5; scenario++) {
            GameSave offer = saved();
            assertEquals(scenario, offer.scenarioIndex());
            assertEquals("puzzle", offer.choices().get(1).type());
            machine.handleInput("2");
            submit(saved().activeEncounter().data().get("answer"), 1.0);
            assertFalse(report().contains("You win!"));
        }
        assertEquals(1, saved().bossPhase());
        assertEquals(180, saved().playerHealth(), "Later puzzles heal after the single skill reward is earned.");
        assertEquals(1, combatUi.encounters);

        oneShotBoss();

        assertEquals(2, saved().bossPhase());
        assertEquals(180, saved().playerHealth());
        assertEquals(70, combatUi.enemy.getHealth());
        assertFalse(report().contains("You win!"));
        oneShotBoss();
        assertTrue(runStore.load().isEmpty());
        assertInstanceOf(MainMenuState.class, machine.nextState);
        assertTrue(report().contains("You win!"));
        assertEquals(2, combatUi.encounters);
    }

    /** Starts a controlled controller while leaving the real encounter factory and storage in use. */
    private void start(GameDifficulty difficulty, Random random, GameSave saved) {
        machine = new RecordingStateMachine();
        clock = new ControlledClock();
        combatUi = new RecordingCombatUi();
        machine.start(new GamePlayState(machine, clock, combatUi, random, history, difficulty, runStore, saved));
    }

    /** Reopens a previously persisted encounter or offer through the real restore path. */
    private void startSaved(GameSave snapshot) throws IOException {
        runStore.save(snapshot);
        start(snapshot.difficulty(), new RunRandom(999), snapshot);
    }

    private GameSave saved() throws IOException {
        return runStore.load().orElseThrow();
    }

    private void submit(String answer, double elapsed) {
        clock.elapsed = elapsed;
        machine.handleInput(answer);
    }

    /** Uses a slow, exact Encryption response to check the retained NORMAL damage/countdown settings. */
    private void playSlowEncryptionTurn() {
        machine.handleInput("1");
        assertEquals(35, combatUi.enemy.getHealth());
        assertEquals(80, combatUi.player.getHealth());
        machine.handleInput("Encryption");
        assertNotNull(combatUi.challenge);
        assertEquals(134, clock.duration);
        String answer = combatUi.challenge.getText();
        submit(answer, answer.length() * 60.0 / (5.0 * 10.25));
    }

    private void oneShotBoss() {
        machine.handleInput("Ping");
        submit(combatUi.challenge.getText(), 1.0);
    }

    private String report() {
        return output.toString(StandardCharsets.UTF_8);
    }

    private static RandomSnapshot randomSnapshot() {
        return new RandomSnapshot(new RunRandom(42).snapshot(), Map.of());
    }

    private static GameSave activeEvent(Player player, int scenario, EncounterSnapshot event) {
        return GameSave.forEncounter(GameDifficulty.HARD, player, scenario, 0, randomSnapshot(), event);
    }

    private static List<EncounterSnapshot> puzzleChoices(String question, String answer, int enemyHealth) {
        EncounterSnapshot combat = new EncounterSnapshot("combat", Map.of("enemyType", "rogue_ai",
                "enemyHealth", Integer.toString(enemyHealth), "nextEnemySkillIndex", "0"));
        return List.of(combat, puzzle(question, answer, "NOT_STARTED"), combat);
    }

    private static EncounterSnapshot puzzle(String question, String answer, String phase) {
        return new EncounterSnapshot("puzzle", Map.of("phase", phase, "question", question, "answer", answer));
    }

    private static EncounterSnapshot dialogue(int node) {
        return new EncounterSnapshot("dialogue", Map.of("phase", "ACCEPTING_ANSWER", "node", Integer.toString(node)));
    }

    private static EncounterSnapshot reaction(String phase, int delayMillis) {
        return new EncounterSnapshot("reaction", Map.of("phase", phase, "targetWord", "dodge",
                "delayMillis", Integer.toString(delayMillis)));
    }

    /** Selects puzzle events deterministically while retaining actual encounter generation. */
    private static class PuzzleOnlyRandom extends Random {
        @Override
        public int nextInt(int bound) {
            return 0;
        }

        @Override
        public boolean nextBoolean() {
            return true;
        }
    }

    /** Controls elapsed typing time without sleeping through challenge deadlines. */
    private static class ControlledClock extends ChallengeClock {
        private double elapsed;
        private int duration;
        private int starts;

        @Override
        public void start(int seconds) {
            elapsed = 0.0;
            duration = seconds;
            starts++;
        }

        @Override
        public double getElapsedSeconds() {
            return elapsed;
        }

        @Override
        public int getRemainingSeconds() {
            return (int) Math.max(0, Math.ceil(duration - elapsed));
        }
    }

    /** Records actual combat participants and prompts without replacing encounter behavior. */
    private static class RecordingCombatUi extends CombatUi {
        private Player player;
        private Enemy enemy;
        private TypingChallenge challenge;
        private int encounters;

        @Override
        public void showCombatStart(Enemy startedEnemy) {
            encounters++;
            super.showCombatStart(startedEnemy);
        }

        @Override
        public void showPlayerTurn(Player currentPlayer, Enemy currentEnemy) {
            player = currentPlayer;
            enemy = currentEnemy;
            super.showPlayerTurn(currentPlayer, currentEnemy);
        }

        @Override
        public void showTypingChallenge(TypingChallenge selectedChallenge) {
            challenge = selectedChallenge;
            super.showTypingChallenge(selectedChallenge);
        }
    }

    /** Keeps final menu transitions observable while executing the real transition lifecycle. */
    private static class RecordingStateMachine extends StateMachine {
        private State nextState;

        @Override
        public void transitState(State next) {
            nextState = next;
            super.transitState(next);
        }
    }
}
