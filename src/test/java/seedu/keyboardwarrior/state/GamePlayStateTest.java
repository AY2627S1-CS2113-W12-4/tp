package seedu.keyboardwarrior.state;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import org.jline.terminal.Size;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.keyboardwarrior.CountdownReader;
import seedu.keyboardwarrior.GameDifficulty;
import seedu.keyboardwarrior.GameSave;
import seedu.keyboardwarrior.combat.Enemy;
import seedu.keyboardwarrior.combat.Player;
import seedu.keyboardwarrior.combat.Skill;
import seedu.keyboardwarrior.storage.GameSaveStore;
import seedu.keyboardwarrior.storage.SaveStore;
import seedu.keyboardwarrior.typing.ChallengeClock;
import seedu.keyboardwarrior.typing.TypingChallenge;
import seedu.keyboardwarrior.typing.TypingEvaluator;
import seedu.keyboardwarrior.typing.TypingResult;
import seedu.keyboardwarrior.ui.CombatUi;
import seedu.keyboardwarrior.ui.StateUi;

/**
 * Tests timed attacks, five scenarios, both final-boss phases, defeat and
 * quitting.
 */
class GamePlayStateTest {
    @TempDir
    Path tempDir;
    private ByteArrayOutputStream output;
    private PrintStream originalOutput;
    private PrintStream capturedOutput;
    private RecordingStateMachine machine;
    private ControlledClock clock;
    private RecordingCombatUi ui;

    @Test
    void save_resume_restoresHealthDamageAndNextEnemyAttackWithoutReplayingATurn() throws IOException {
        GameSaveStore runStore = new GameSaveStore(tempDir.resolve("game-save.properties"));
        startGame();
        assertEquals(100, runStore.load().orElseThrow().playerHealth());
        machine.handleInput("Ping");
        String answer = ui.challenge.getText();
        clock.elapsedSeconds = answer.length() * 60.0 / (5.0 * 5.25);
        machine.handleInput(answer);
        GameSave saved = runStore.load().orElseThrow();
        assertEquals(90, saved.playerHealth());
        assertEquals(25, saved.enemyHealth());
        assertEquals(1, saved.nextEnemySkillIndex());
        machine.handleInput("save");
        assertEquals(1, ui.invalidSkills, "Save is no longer a skill-selection command.");
        assertEquals(saved, runStore.load().orElseThrow());
        machine.handleInput("Encryption");
        machine.handleInput("exit");
        assertEquals(saved, runStore.load().orElseThrow(), "Exiting mid-challenge must retain the stable turn.");

        machine = new RecordingStateMachine();
        ui = new RecordingCombatUi();
        machine.start(new GamePlayState(machine, clock, ui, new FixedEnemyRandom(),
                new SaveStore(tempDir.resolve("history.txt")), saved.difficulty(), runStore, saved));
        assertEquals(90, ui.player.getHealth());
        assertEquals("Script Kiddie", ui.enemy.getName());
        assertEquals(25, ui.enemy.getHealth());
        assertEquals(0, ui.enemyTurns);
        machine.handleInput("Ping");
        submitAfterTenSeconds("");
        assertEquals(70, ui.player.getHealth(), "The restored opponent must use its second, 20-damage attack.");
        assertEquals(2, runStore.load().orElseThrow().nextEnemySkillIndex());
    }

    @Test
    void save_progressionOverwritesSlotAndVictoryRemovesIt() throws IOException {
        GameSaveStore runStore = new GameSaveStore(tempDir.resolve("game-save.properties"));
        runStore.save(new GameSave(1, GameDifficulty.CODER, 25, 5, 2, "kernel_overlord", 17, 0));
        startGame();
        assertEquals(GameDifficulty.HARD, runStore.load().orElseThrow().difficulty(),
                "Starting a fresh run must replace the previous slot with the selected difficulty.");
        for (int scenario = 0; scenario < 5; scenario++) {
            GameSave save = runStore.load().orElseThrow();
            assertEquals(scenario, save.scenarioIndex());
            assertEquals(0, save.bossPhase());
            oneShotCurrentEncounter();
        }
        assertEquals(1, runStore.load().orElseThrow().bossPhase());
        oneShotCurrentEncounter();
        assertEquals(2, runStore.load().orElseThrow().bossPhase());
        oneShotCurrentEncounter();
        assertTrue(runStore.load().isEmpty());
    }

    @Test
    void save_resumeBothBossPhases_preservesDifficultyCountdownAndFixedEnemyAttack() throws IOException {
        GameSaveStore runStore = new GameSaveStore(tempDir.resolve("game-save.properties"));
        for (int phase = 1; phase <= 2; phase++) {
            machine = new RecordingStateMachine();
            ui = new RecordingCombatUi();
            GameSave save = new GameSave(1, GameDifficulty.CODER, 55, 5, phase, "kernel_overlord", 17, 0);
            runStore.save(save);
            machine.start(new GamePlayState(machine, clock, ui, new FixedEnemyRandom(),
                    new SaveStore(tempDir.resolve("history.txt")), save.difficulty(), runStore, save));
            assertEquals(55, ui.player.getHealth(), "Saved health must not be multiplied again.");
            assertEquals(17, ui.enemy.getHealth());
            assertEquals(0, ui.enemyTurns);
            machine.handleInput("Encryption");
            assertEquals(48, clock.durationSeconds);
            submitAfterTenSeconds("");
            assertEquals(phase == 1 ? 45 : 35, ui.player.getHealth());
            assertEquals(0, runStore.load().orElseThrow().nextEnemySkillIndex());
            ui.player.setHealth(1);
            machine.handleInput("Ping");
            submitAfterTenSeconds("");
            assertTrue(runStore.load().isEmpty(), "Defeat must remove the active save.");
        }
    }

    @Test
    void save_storageFailure_reportsErrorWithoutDiscardingCombatTurn() {
        GameSaveStore failingStore = new GameSaveStore(tempDir.resolve("game-save.properties")) {
            @Override
            public void save(GameSave save) throws IOException {
                throw new IOException("Storage unavailable");
            }
        };
        machine.start(new GamePlayState(machine, clock, ui, new FixedEnemyRandom(),
                new SaveStore(tempDir.resolve("history.txt")), GameDifficulty.HARD, failingStore, null));
        assertTrue(report().contains("Could not save game: Storage unavailable"));
        machine.handleInput("Ping");
        submitAfterTenSeconds("");
        assertEquals(90, ui.player.getHealth());
        assertEquals(1, ui.enemyTurns);
        assertTrue(machine.isRunning());
    }

    @Test
    void difficulty_eachMode_scalesHealthAndEncryptionAndRetainsSelectionAfterDefeat() {
        int[] expectedHealth = {400, 200, 100, 80};
        int[] expectedSeconds = {240, 120, 60, 48};
        GameDifficulty[] difficulties = GameDifficulty.values();
        for (int index = 0; index < difficulties.length; index++) {
            machine = new RecordingStateMachine();
            ui = new RecordingCombatUi();
            machine.start(new GamePlayState(machine, clock, ui, new FixedEnemyRandom(),
                    new SaveStore(tempDir.resolve("history.txt")), difficulties[index]));
            assertEquals(expectedHealth[index], ui.player.getHealth());
            machine.handleInput("Encryption");
            assertEquals(expectedSeconds[index], clock.durationSeconds);
            submitAfterTenSeconds("");
            assertEquals(expectedHealth[index] - 10, ui.player.getHealth());

            ui.player.setHealth(1);
            machine.handleInput("Ping");
            submitAfterTenSeconds("");
            assertTrue(machine.nextState instanceof MainMenuState);
            assertTrue(report().contains("Current difficulty: " + difficulties[index].getDisplayName()));
            output.reset();
            machine.handleInput("1");
            machine.handleInput("");
            assertTrue(report().contains("HP: " + expectedHealth[index]));
            machine.stop();
        }
    }

    @Test
    void difficulty_coder_retainsDamageAndCountdownAcrossScenariosAndBossPhases() {
        machine.start(new GamePlayState(machine, clock, ui, new FixedEnemyRandom(),
                new SaveStore(tempDir.resolve("history.txt")), GameDifficulty.CODER));
        for (int encounterIndex = 0; encounterIndex < 7; encounterIndex++) {
            assertEquals(80, ui.player.getHealth(), "Killing blows must not heal or allow retaliation.");
            machine.handleInput("Encryption");
            assertEquals(48, clock.durationSeconds);
            String text = ui.challenge.getText();
            double elapsed = text.length() * 60.0 / (5.0 * 10.25);
            clock.elapsedSeconds = elapsed;
            machine.handleInput(text);
            assertTrue(report().contains("Dealt 8 damage to"), "10.25 WPM in Coder must deal eight damage.");
            assertEquals(encounterIndex == 6 ? 60 : 70, ui.player.getHealth());
            ui.player.setHealth(80);
            oneShotCurrentEncounter();
            output.reset();
        }
        assertTrue(machine.nextState instanceof MainMenuState);
        machine.handleInput("1");
        machine.handleInput("");
        assertTrue(report().contains("Difficulty: Coder"));
        assertTrue(report().contains("HP: 80"));
    }

    /**
     * Captures console output and creates fresh state, timing and combat fixtures.
     */
    @BeforeEach
    void setUp() {
        StateUi.detachTerminal();
        output = new ByteArrayOutputStream();
        originalOutput = System.out;
        capturedOutput = new PrintStream(output, true, StandardCharsets.UTF_8);
        System.setOut(capturedOutput);
        machine = new RecordingStateMachine();
        clock = new ControlledClock();
        ui = new RecordingCombatUi();
    }

    /**
     * Detaches the terminal and restores standard output after each test.
     */
    @AfterEach
    void tearDown() {
        StateUi.detachTerminal();
        System.setOut(originalOutput);
        capturedOutput.close();
    }

    /**
     * Checks that Start followed by an empty seed enters the first scenario.
     */
    @Test
    void mainMenu_start_entersFirstScenario() {
        machine.start(new MainMenuState(machine, new SaveStore(tempDir.resolve("history.txt"))));
        machine.handleInput(" Start ");
        machine.handleInput("");

        assertTrue(machine.nextState instanceof GamePlayState);
        assertTrue(report().contains("Scenario 1/5"));
        assertTrue(report().contains("==== COMBAT STARTED ===="));
        machine.handleInput("exit");
        assertFalse(machine.isRunning());
    }

    /**
     * Checks that Exit remains a command while the menu is waiting for a seed.
     */
    @Test
    void mainMenu_exitAtSeedPrompt_stopsWithoutStartingGame() {
        machine.start(new MainMenuState(machine, new SaveStore(tempDir.resolve("history.txt"))));
        machine.handleInput("start");
        machine.handleInput(" EXIT ");

        assertFalse(machine.isRunning());
        assertTrue(machine.nextState == null);
        assertFalse(report().contains("Scenario 1/5"));
        assertFalse(Files.exists(tempDir.resolve("history.txt")));
    }

    /**
     * Checks reproducible full runs and history retention across victory and a restarted seeded run.
     *
     * @throws IOException if the temporary history cannot be read
     */
    @Test
    void handleInput_sameSeed_repeatsRunAndRetainsHistory() throws IOException {
        Path file = tempDir.resolve("seeded-history.txt");
        RecordingSaveStore firstStore = new RecordingSaveStore(file);
        List<String> firstRun = completeSeededRun(42, firstStore);
        machine.handleInput("Records");
        assertEquals(1, firstStore.loadCount);
        List<SaveStore.SavedRecord> firstRecords = new SaveStore(file).load();
        assertEquals(7, firstRecords.size());
        machine.handleInput("exit");

        RecordingSaveStore reopenedStore = new RecordingSaveStore(file);
        List<String> secondRun = completeSeededRun(42, reopenedStore);
        machine.handleInput("Records");
        assertEquals(1, reopenedStore.loadCount);
        assertEquals(firstRun, secondRun);
        List<SaveStore.SavedRecord> combinedRecords = new ArrayList<>(firstRecords);
        combinedRecords.addAll(firstRecords);
        assertEquals(combinedRecords.subList(4, 14), new SaveStore(file).load());
        machine.handleInput("exit");
    }

    /**
     * Completes five scenarios and both boss phases with reproducible one-second submissions.
     *
     * @param seed seed shared by enemy and prompt selection
     * @param history history store used throughout the run and returned menu
     * @return enemy names, prompt text and deadlines in turn order
     */
    private List<String> completeSeededRun(long seed, SaveStore history) {
        machine = new RecordingStateMachine();
        clock = new ControlledClock();
        ui = new RecordingCombatUi();
        machine.start(new GamePlayState(machine, clock, ui, new Random(seed), history));
        List<String> selections = new ArrayList<>();
        String[] skills = {"Ping", "Brute Force", "Buffer Overflow", "SIGKILL"};
        for (int encounter = 0; encounter < 7; encounter++) {
            machine.handleInput(skills[encounter % skills.length]);
            selections.add(ui.enemy.getName());
            selections.add(ui.challenge.getText());
            selections.add(Integer.toString(ui.challenge.getTime()));
            clock.elapsedSeconds = 1.0;
            clock.remainingSeconds = clock.durationSeconds - 1;
            machine.handleInput(ui.challenge.getText());
        }
        assertEquals(7, ui.encounters);
        assertTrue(machine.nextState instanceof MainMenuState);
        assertTrue(report().contains("You win! All 5 scenarios and both final boss phases cleared."));
        return selections;
    }

    /**
     * Checks that only an available skill starts a timed challenge.
     */
    @Test
    void handleInput_skillChoice_startsOnlyValidChallenges() {
        startGame();
        machine.handleInput("unknown skill");
        assertEquals(0, clock.startCount);
        assertEquals(0, ui.enemyTurns);
        assertEquals(1, ui.invalidSkills);

        machine.handleInput(" pInG ");
        assertEquals(1, clock.startCount);
        assertTrue(clock.durationSeconds >= 15);
        assertEquals(clock.durationSeconds, ui.challenge.getTime());
        assertEquals(0, ui.enemyTurns);
        assertTrue(report().contains("Time remaining: " + clock.durationSeconds + "s"));
    }

    /**
     * Checks damage, persistence and continued combat after an inaccurate submission.
     */
    @Test
    void handleInput_inaccurateAnswer_appliesMetricsDamageAndKeepsFighting() throws IOException {
        startGame();
        machine.handleInput("Ping");
        String expected = ui.challenge.getText();
        String typed = expected.substring(0, expected.length() / 2);
        TypingResult result = TypingEvaluator.evaluateTyping(expected, typed, 10.0, clock.durationSeconds);
        int damage = (int) (result.getWpm() * Math.pow(result.getAccuracyPercent() / 100.0, 4));

        submitAfterTenSeconds(typed);

        assertEquals(java.util.List.of(result.getWpm() + "," + result.getAccuracyPercent()),
                Files.readAllLines(tempDir.resolve("history.txt")));

        assertEquals(30 - damage, ui.enemy.getHealth());
        assertTrue(ui.enemy.getHealth() > 0);
        assertTrue(damage > 0);
        assertEquals(90, ui.player.getHealth());
        assertEquals(1, ui.enemyTurns);
        assertEquals(1, ui.encounters);
        assertFalse(report().contains("You win!"));
        assertFalse(report().contains("Wrong words:"));
        assertFalse(result.getWordErrors().isEmpty());

        machine.handleInput("Ping");
        assertEquals(2, clock.startCount);
    }

    /**
     * Checks that an empty expired attempt deals no damage and triggers only one retaliation.
     */
    @Test
    void update_expiredEmptyAttempt_dealsZeroDamageAndRetaliatesOnce() {
        startGame();
        machine.handleInput("Ping");
        clock.elapsedSeconds = clock.durationSeconds + 1.0;
        clock.remainingSeconds = 0;
        machine.update();
        machine.update();

        assertEquals(30, ui.enemy.getHealth());
        assertEquals(90, ui.player.getHealth());
        assertEquals(1, ui.enemyTurns);
        assertFalse(report().contains("Damage penalty:"));
        assertEquals(1, report().split("Time's up!", -1).length - 1);

        machine.handleInput("Ping");
        assertEquals(2, clock.startCount);
    }

    /**
     * Checks that timeout scores unfinished input while preserving countdown display order.
     */
    @Test
    void update_liveCountdown_scoresUnsubmittedAnswerAndPreservesTimerOrder() throws Exception {
        ByteArrayOutputStream terminalOutput = new ByteArrayOutputStream();
        // Use stream-based output on every OS; a paused POSIX PTY also pauses its
        // output pump.
        try (Terminal terminal = TerminalBuilder.builder().system(false).provider("exec").type("xterm")
                .streams(new ByteArrayInputStream(new byte[0]), terminalOutput).paused(true).build()) {
            terminal.setSize(new Size(80, 24));
            CountdownReader reader = new CountdownReader(terminal);
            StateUi.attachTerminal(reader);
            startGame();
            machine.handleInput("Ping");
            String expected = ui.challenge.getText();
            String typed = expected.substring(0, expected.length() / 2);
            reader.getBuffer().write(typed);
            clock.remainingSeconds = 10;
            clock.elapsedSeconds = 5.0;
            machine.update();

            assertEquals(typed, reader.getBuffer().toString());
            String display = reader.getDisplayedBufferWithPrompts(new ArrayList<>()).toString();
            assertTrue(display.contains("Time remaining: 10s"));
            assertTrue(display.indexOf("Time remaining: 10s") < display.indexOf("Type the following"));
            assertTrue(display.indexOf("Type the following") < display.indexOf(expected));

            clock.remainingSeconds = 0;
            double elapsed = clock.durationSeconds + 1.0;
            clock.elapsedSeconds = elapsed;
            machine.update();
            assertEquals("", reader.getBuffer().toString());
            assertFalse(reader.getDisplayedBufferWithPrompts(new ArrayList<>())
                    .toString().contains("Time remaining:"));
            assertEquals(1, ui.enemyTurns);
            TypingResult timedOut = TypingEvaluator.evaluateTyping(expected, typed, elapsed, clock.durationSeconds);
            int damage = (int) (timedOut.getWpm() * Math.pow(timedOut.getAccuracyPercent() / 100.0, 4));
            assertTrue(damage > 0);
            assertEquals(30 - damage, ui.enemy.getHealth());
            assertEquals(90, ui.player.getHealth());
            String feedback = terminalOutput.toString(StandardCharsets.UTF_8);
            assertTrue(feedback.contains(String.format(Locale.ROOT, "WPM: %.2f", timedOut.getWpm())));
            assertTrue(feedback.contains(String.format(Locale.ROOT,
                    "Accuracy: %.2f%%", timedOut.getAccuracyPercent())));
            assertFalse(feedback.contains("Damage penalty:"));
            assertFalse(feedback.contains("Wrong words:"));

            machine.handleInput("Ping");
            assertTrue(reader.getDisplayedBufferWithPrompts(new ArrayList<>())
                    .toString().contains("Time remaining: " + clock.durationSeconds + "s"));
        } finally {
            StateUi.detachTerminal();
        }
    }

    /**
     * Checks that a late correct answer deals typing damage without an additional penalty.
     */
    @Test
    void handleInput_lateCorrectAnswer_usesMetricsWithoutAnExtraPenalty() {
        advanceToThirdScenario();
        machine.handleInput("Ping");
        String expected = ui.challenge.getText();
        double elapsed = clock.durationSeconds + 1.0;
        TypingResult result = TypingEvaluator.evaluateTyping(expected, expected, elapsed, clock.durationSeconds);
        int damage = (int) result.getWpm();
        clock.elapsedSeconds = elapsed;
        clock.remainingSeconds = 0;
        machine.handleInput(expected);

        assertTrue(damage > 0);
        assertEquals(50 - damage, ui.enemy.getHealth());
        assertEquals(90, ui.player.getHealth());
        assertEquals(1, ui.enemyTurns);
        assertTrue(report().contains("Accuracy: 100.00%"));
        assertFalse(report().contains("Damage penalty:"));
    }

    /**
     * Checks that a submission exactly at the deadline is accepted without timeout feedback.
     */
    @Test
    void handleInput_atExactLimit_appliesFullDamage() {
        advanceToThirdScenario();
        machine.handleInput("Ping");
        String expected = ui.challenge.getText();
        TypingResult result = TypingEvaluator.evaluateTyping(expected, expected,
                clock.durationSeconds, clock.durationSeconds);
        int damage = (int) result.getWpm();
        clock.elapsedSeconds = clock.durationSeconds;
        clock.remainingSeconds = 0;
        machine.update();
        assertEquals(0, ui.enemyTurns);
        machine.handleInput(expected);

        assertEquals(50 - damage, ui.enemy.getHealth());
        assertEquals(1, ui.enemyTurns);
        assertFalse(report().contains("Damage penalty:"));
        assertFalse(report().contains("Time's up!"));
    }

    /**
     * Checks progression through five scenarios and two boss phases before returning to the menu.
     */
    @Test
    void handleInput_fiveVictoriesAndBothBossPhases_returnsToMenuOnlyAfterFinalVictory() {
        startGame();
        // Take one hit first to verify that remaining health carries into later
        // scenarios.
        machine.handleInput("Ping");
        submitAfterTenSeconds("");
        Player originalPlayer = ui.player;

        for (int scenario = 1; scenario <= 5; scenario++) {
            assertEquals(scenario, ui.encounters);
            assertEquals(90, ui.player.getHealth());
            assertTrue(ui.player == originalPlayer);
            machine.handleInput("SIGKILL");
            submitAfterTenSeconds(ui.challenge.getText());
            if (scenario < 5) {
                assertEquals(scenario + 1, ui.encounters);
                assertFalse(report().contains("You win!"));
            }
        }

        assertEquals(6, ui.encounters);
        assertTrue(machine.nextState == null);
        assertTrue(report().contains("Final Boss - Phase 1/2"));
        assertFalse(report().contains("You win!"));
        for (int phase = 1; phase <= 2; phase++) {
            assertEquals(90, ui.player.getHealth());
            assertTrue(ui.player == originalPlayer);
            machine.handleInput("SIGKILL");
            submitAfterTenSeconds(ui.challenge.getText());
            if (phase == 1) {
                assertEquals(7, ui.encounters);
                assertEquals(70, ui.enemy.getHealth());
                assertFalse(report().contains("You win!"));
                assertTrue(report().contains("Final Boss - Phase 2/2"));
            }
        }
        assertEquals(7, ui.encounters);
        assertTrue(machine.nextState instanceof MainMenuState);
        assertTrue(machine.isRunning());
        assertTrue(report().contains("You win! All 5 scenarios and both final boss phases cleared."));
        assertFalse(report().contains("Scenario 6/5"));

        machine.handleInput("start");
        machine.handleInput("");
        assertTrue(machine.nextState instanceof GamePlayState);
        assertEquals(2, report().split("Scenario 1/5", -1).length - 1);
        assertTrue(report().substring(report().lastIndexOf("Scenario 1/5")).contains("HP: 100"));
        machine.handleInput("exit");
    }

    /**
     * Checks that player defeat ends the run before another scenario starts.
     */
    @Test
    void handleInput_playerDefeated_endsRunBeforeNextScenario() {
        startGame();
        for (int turn = 0; turn < 5; turn++) {
            machine.handleInput("Ping");
            submitAfterTenSeconds("");
        }

        assertTrue(ui.player.getIsDead());
        assertEquals(0, ui.player.getHealth());
        assertEquals(1, ui.encounters);
        assertTrue(machine.nextState instanceof MainMenuState);
        assertTrue(report().contains("Game over."));
        assertFalse(report().contains("Scenario 2/5"));
        assertFalse(report().contains("You win!"));
    }

    /**
     * Checks that the menu returned to after defeat reads the same history store.
     */
    @Test
    void handleInput_playerDefeated_recordsUsesSameHistoryStore() {
        RecordingSaveStore history = new RecordingSaveStore(tempDir.resolve("history.txt"));
        machine.start(new GamePlayState(machine, clock, ui, new FixedEnemyRandom(), history));
        for (int turn = 0; turn < 5; turn++) {
            machine.handleInput("Ping");
            submitAfterTenSeconds("");
        }

        machine.handleInput("Records");

        assertEquals(1, history.loadCount, "The returned menu must read the gameplay history store.");
        assertTrue(report().contains("Typing history (oldest to newest)"));
        assertTrue(report().contains("0.00%"));
        assertFalse(report().contains("No typing records saved yet"));
    }

    /**
     * Checks that exiting during a typing challenge stops without an enemy turn.
     */
    @Test
    void handleInput_exitDuringTyping_stopsWithoutTakingAnEnemyTurn() {
        startGame();
        machine.handleInput("Ping");
        machine.handleInput(" EXIT ");

        assertFalse(machine.isRunning());
        assertEquals(0, ui.enemyTurns);
        assertFalse(report().contains("WPM:"));
    }

    /**
     * Checks survival at normal typing performance for each fixed enemy selection.
     */
    @Test
    void normalPerformance_eachRepeatedEnemyType_survivesFiveScenariosAndBothBossPhases() {
        for (int enemyType = 0; enemyType < 8; enemyType++) {
            machine = new RecordingStateMachine();
            clock = new ControlledClock();
            ui = new RecordingCombatUi();
            machine.start(new GamePlayState(machine, clock, ui, new FixedEnemyRandom(enemyType),
                    new SaveStore(tempDir.resolve("history-" + enemyType + ".txt"))));
            int attempts = 0;
            while (!(machine.nextState instanceof MainMenuState) && attempts < 20) {
                submitAtNormalPerformance();
                attempts++;
            }

            assertEquals(12, attempts, "Normal performance should need eight regular and four boss attacks.");
            assertEquals(7, ui.encounters);
            assertFalse(ui.player.getIsDead());
            assertTrue(ui.player.getHealth() >= 10, "Every enemy selection must leave a normal player alive.");
            if (enemyType == 5 || enemyType == 7) {
                assertEquals(10, ui.player.getHealth(), "The strongest first attacks must leave ten health.");
            }
        }
    }

    /**
     * Checks that a boss transition retains player health, discards overkill and skips retaliation.
     */
    @Test
    void bossPhaseTransition_preservesPlayerHealthAndDiscardsOverkillWithoutRetaliation() {
        advanceToBoss();
        assertEquals(50, ui.enemy.getHealth());
        ui.player.setHealth(75);
        Player originalPlayer = ui.player;
        int enemyTurnsBefore = ui.enemyTurns;

        oneShotCurrentEncounter();

        assertEquals(7, ui.encounters);
        assertEquals(70, ui.enemy.getHealth(), "Overkill from phase one must not damage phase two.");
        assertEquals(75, ui.player.getHealth());
        assertTrue(ui.player == originalPlayer);
        assertEquals(enemyTurnsBefore, ui.enemyTurns);
        assertFalse(report().contains("You win!"));
        assertTrue(report().contains("Final Boss - Phase 2/2"));
    }

    /**
     * Checks that defeat in the first boss phase returns to the menu without starting the second.
     */
    @Test
    void bossPhaseOne_playerDefeat_returnsToMenuWithoutStartingPhaseTwo() {
        advanceToBoss();
        ui.player.setHealth(10);
        machine.handleInput("Ping");
        submitAfterTenSeconds("");

        assertTrue(machine.nextState instanceof MainMenuState);
        assertTrue(ui.player.getIsDead());
        assertEquals(6, ui.encounters);
        assertFalse(report().contains("Final Boss - Phase 2/2"));
        assertFalse(report().contains("You win!"));
    }

    /**
     * Checks that defeat in the second boss phase returns to the menu without announcing victory.
     */
    @Test
    void bossPhaseTwo_playerDefeat_returnsToMenuWithoutVictory() {
        advanceToBoss();
        oneShotCurrentEncounter();
        ui.player.setHealth(20);
        machine.handleInput("Ping");
        submitAfterTenSeconds("");

        assertTrue(machine.nextState instanceof MainMenuState);
        assertTrue(ui.player.getIsDead());
        assertEquals(7, ui.encounters);
        assertFalse(report().contains("You win!"));
    }

    /**
     * Checks that exiting in the second boss phase stops without another enemy turn.
     */
    @Test
    void bossPhaseTwo_exitDuringTyping_stopsWithoutAnEnemyTurn() {
        advanceToBoss();
        oneShotCurrentEncounter();
        int turnsBefore = ui.enemyTurns;
        machine.handleInput("Ping");
        machine.handleInput("exit");

        assertFalse(machine.isRunning());
        assertEquals(turnsBefore, ui.enemyTurns);
        assertFalse(report().contains("You win!"));
    }

    /**
     * Reaches a fifty-health enemy so ordinary on-time and late submissions do not
     * kill it.
     */
    private void advanceToThirdScenario() {
        startGame();
        oneShotCurrentEncounter();
        oneShotCurrentEncounter();
    }

    /**
     * Defeats the regular encounters while keeping the boss available for
     * phase-boundary tests.
     */
    private void advanceToBoss() {
        startGame();
        for (int scenario = 0; scenario < 5; scenario++) {
            oneShotCurrentEncounter();
        }
    }

    /**
     * Uses a deliberately large attack to exercise killing blows and discarded
     * overkill.
     */
    private void oneShotCurrentEncounter() {
        machine.handleInput("Ping");
        clock.elapsedSeconds = 1.0;
        clock.remainingSeconds = clock.durationSeconds - 1;
        machine.handleInput(ui.challenge.getText());
    }

    /**
     * Types real prompts at fifty WPM with at least ninety-five percent character
     * accuracy.
     */
    private void submitAtNormalPerformance() {
        machine.handleInput("Ping");
        String expected = ui.challenge.getText();
        char[] typed = expected.toCharArray();
        int errorsRemaining = (int) (typed.length * 0.05);
        for (int index = 0; errorsRemaining > 0; index++) {
            if (typed[index] != ' ') {
                typed[index] = '#';
                errorsRemaining--;
            }
        }
        String submission = new String(typed);
        double elapsed = submission.length() * 60.0 / (5.0 * 50.0);
        TypingResult result = TypingEvaluator.evaluateTyping(expected, submission, elapsed, clock.durationSeconds);
        assertEquals(50.0, result.getWpm(), 0.000001);
        assertTrue(result.getAccuracyPercent() >= 95.0 && result.getAccuracyPercent() < 100.0,
                "The fixture should approximate ninety-five percent accuracy: " + result.getAccuracyPercent());
        assertTrue(elapsed <= clock.durationSeconds, "The normal player must be able to finish the prompt.");
        clock.elapsedSeconds = elapsed;
        clock.remainingSeconds = (int) Math.ceil(clock.durationSeconds - elapsed);
        machine.handleInput(submission);
    }

    /**
     * Starts gameplay with controllable timing and observed combat output.
     */
    private void startGame() {
        machine.start(new GamePlayState(machine, clock, ui, new FixedEnemyRandom(),
                new SaveStore(tempDir.resolve("history.txt"))));
    }

    /**
     * Submits an answer at a reproducible duration within every skill's limit.
     *
     * @param typedText text captured or submitted for evaluation
     */
    private void submitAfterTenSeconds(String typedText) {
        clock.elapsedSeconds = 10.0;
        clock.remainingSeconds = clock.durationSeconds - 10;
        machine.handleInput(typedText);
    }

    /**
     * Returns the console output captured during the current test.
     *
     * @return captured console output as UTF-8 text
     */
    private String report() {
        return output.toString(StandardCharsets.UTF_8);
    }

    /**
     * Observes history reads without replacing real persistence through the
     * temporary file.
     */
    private static class RecordingSaveStore extends SaveStore {
        private int loadCount;

        /**
         * Creates a recording store backed by the supplied temporary history file.
         *
         * @param filePath location of the history file
         */
        RecordingSaveStore(Path filePath) {
            super(filePath);
        }

        /**
         * Counts history reads while loading records from the real temporary file.
         *
         * @return saved records in chronological order
         * @throws IOException if the history cannot be read or contains invalid metrics
         */
        @Override
        public List<SavedRecord> load() throws IOException {
            loadCount++;
            return super.load();
        }
    }

    /**
     * Fixes enemy selection while supplying deterministic, bounded prompt selections.
     * The current enemy bank requests a bound of eight; prompt banks request five or four.
     */
    private static class FixedEnemyRandom extends Random {
        private final int enemyIndex;

        /**
         * Creates a random fixture returning a fixed enemy index.
         */
        FixedEnemyRandom() {
            this(0);
        }

        /**
         * Creates a random fixture returning a fixed enemy index.
         *
         * @param enemyIndex fixed index returned for enemy selections
         */
        FixedEnemyRandom(int enemyIndex) {
            super(0);
            this.enemyIndex = enemyIndex;
        }

        /**
         * Returns the configured enemy index for the eight-enemy bank, otherwise a seeded random index.
         *
         * @param bound exclusive upper bound requested by the enemy or prompt bank
         * @return configured enemy index or a valid prompt index
         */
        @Override
        public int nextInt(int bound) {
            return bound == 8 ? enemyIndex : super.nextInt(bound);
        }
    }

    /**
     * Supplies deterministic time without waiting through real countdowns.
     */
    private static class ControlledClock extends ChallengeClock {
        private int durationSeconds;
        private int remainingSeconds;
        private double elapsedSeconds;
        private int startCount;

        /**
         * Records a timer start and resets the manually controlled elapsed and remaining times.
         *
         * @param durationSeconds duration recorded for this attempt
         */
        @Override
        public void start(int durationSeconds) {
            this.durationSeconds = durationSeconds;
            remainingSeconds = durationSeconds;
            elapsedSeconds = 0.0;
            startCount++;
        }

        /**
         * Returns the remaining time supplied by the test.
         *
         * @return manually controlled remaining seconds
         */
        @Override
        public int getRemainingSeconds() {
            return remainingSeconds;
        }

        /**
         * Returns the elapsed time supplied by the test.
         *
         * @return manually controlled elapsed seconds
         */
        @Override
        public double getElapsedSeconds() {
            return elapsedSeconds;
        }
    }

    /**
     * Observes real combat participants and guards against blocking console reads.
     */
    private static class RecordingCombatUi extends CombatUi {
        private Enemy enemy;
        private Player player;
        private TypingChallenge challenge;
        private int encounters;
        private int enemyTurns;
        private int invalidSkills;

        /**
         * Records the enemy and encounter count, then displays the normal combat announcement.
         *
         * @param enemy enemy entering the encounter
         */
        @Override
        public void showCombatStart(Enemy enemy) {
            this.enemy = enemy;
            encounters++;
            super.showCombatStart(enemy);
        }

        /**
         * Records the player, then displays the normal player-turn information.
         *
         * @param player player taking the turn
         * @param enemy opposing enemy
         */
        @Override
        public void showPlayerTurn(Player player, Enemy enemy) {
            this.player = player;
            super.showPlayerTurn(player, enemy);
        }

        /**
         * Counts enemy turns while preserving their normal display.
         *
         * @param enemy enemy taking the turn
         */
        @Override
        public void showEnemyTurn(Enemy enemy) {
            enemyTurns++;
            super.showEnemyTurn(enemy);
        }

        /**
         * Records the generated challenge while preserving its normal display.
         *
         * @param challenge generated typing challenge
         */
        @Override
        public void showTypingChallenge(TypingChallenge challenge) {
            this.challenge = challenge;
            super.showTypingChallenge(challenge);
        }

        /**
         * Counts invalid skill selections while displaying the normal feedback.
         */
        @Override
        public void showInvalidSkill() {
            invalidSkills++;
            super.showInvalidSkill();
        }

        /**
         * Rejects blocking skill reads because gameplay must use the state-machine input loop.
         *
         * @param activeSkills available skills; no selection is made
         * @return never returns normally
         * @throws AssertionError whenever called
         */
        @Override
        public Skill getSkillChoice(ArrayList<Skill> activeSkills) {
            throw new AssertionError("Gameplay must receive skills through the main input loop.");
        }

        /**
         * Rejects blocking submission reads because gameplay must use the state-machine input loop.
         *
         * @param typingChallenge challenge text; no input is read
         * @return never returns normally
         * @throws AssertionError whenever called
         */
        @Override
        public String getTypingSubmission(String typingChallenge) {
            throw new AssertionError("Gameplay must receive submissions through the main input loop.");
        }
    }

    /**
     * Records transitions while preserving normal state-machine behavior.
     */
    private static class RecordingStateMachine extends StateMachine {
        private State nextState;

        /**
         * Records the requested destination and performs the normal state transition.
         *
         * @param nextState state to activate
         */
        @Override
        public void transitState(State nextState) {
            this.nextState = nextState;
            super.transitState(nextState);
        }
    }
}
