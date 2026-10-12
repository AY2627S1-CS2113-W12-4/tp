package seedu.keyboardwarrior.state;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;

import org.jline.terminal.Size;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import seedu.keyboardwarrior.CountdownReader;
import seedu.keyboardwarrior.combat.Enemy;
import seedu.keyboardwarrior.combat.Player;
import seedu.keyboardwarrior.combat.Skill;
import seedu.keyboardwarrior.typing.ChallengeClock;
import seedu.keyboardwarrior.typing.TypingChallenge;
import seedu.keyboardwarrior.typing.TypingEvaluator;
import seedu.keyboardwarrior.typing.TypingResult;
import seedu.keyboardwarrior.ui.CombatUi;
import seedu.keyboardwarrior.ui.StateUi;

/** Tests timed attacks, five-scenario progression, defeat and quitting. */
class GamePlayStateTest {
    private ByteArrayOutputStream output;
    private PrintStream originalOutput;
    private PrintStream capturedOutput;
    private RecordingStateMachine machine;
    private ControlledClock clock;
    private RecordingCombatUi ui;

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

    @AfterEach
    void tearDown() {
        StateUi.detachTerminal();
        System.setOut(originalOutput);
        capturedOutput.close();
    }

    @Test
    void mainMenu_start_entersFirstScenario() {
        machine.start(new MainMenuState(machine));
        machine.handleInput(" Start ");

        assertTrue(machine.nextState instanceof GamePlayState);
        assertTrue(report().contains("Scenario 1/5"));
        assertTrue(report().contains("==== COMBAT STARTED ===="));
        machine.handleInput("exit");
        assertFalse(machine.isRunning());
    }

    @Test
    void handleInput_skillChoice_startsOnlyValidChallenges() {
        startGame();
        machine.handleInput("unknown skill");
        assertEquals(0, clock.startCount);
        assertEquals(0, ui.enemyTurns);
        assertEquals(1, ui.invalidSkills);

        machine.handleInput(" pInG ");
        assertEquals(1, clock.startCount);
        assertEquals(15, clock.durationSeconds);
        assertEquals(15, ui.challenge.getTime());
        assertEquals(0, ui.enemyTurns);
        assertTrue(report().contains("Time remaining: 15s"));
    }

    @Test
    void handleInput_inaccurateAnswer_appliesMetricsDamageAndKeepsFighting() {
        startGame();
        machine.handleInput("Ping");
        String expected = ui.challenge.getText();
        String typed = expected.substring(0, expected.lastIndexOf(' '));
        TypingResult result = TypingEvaluator.evaluateTyping(expected, typed, 10.0, 15.0);
        int damage = (int) (10 * (result.getAccuracyPercent() + result.getWpm()) / 100.0);

        submitAfterTenSeconds(typed);

        assertEquals(80 - damage, ui.enemy.getHealth());
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

    @Test
    void update_expiredEmptyAttempt_dealsZeroDamageAndRetaliatesOnce() {
        startGame();
        machine.handleInput("Ping");
        clock.elapsedSeconds = 16.0;
        clock.remainingSeconds = 0;
        machine.update();
        machine.update();

        assertEquals(80, ui.enemy.getHealth());
        assertEquals(90, ui.player.getHealth());
        assertEquals(1, ui.enemyTurns);
        assertTrue(report().contains("Damage penalty: 20% (time limit exceeded)"));
        assertEquals(1, report().split("Time's up!", -1).length - 1);

        machine.handleInput("Ping");
        assertEquals(2, clock.startCount);
    }

    @Test
    void update_liveCountdown_scoresUnsubmittedAnswerAndPreservesTimerOrder() throws Exception {
        ByteArrayOutputStream terminalOutput = new ByteArrayOutputStream();
        try (Terminal terminal = TerminalBuilder.builder().system(false).type("xterm")
                .streams(new ByteArrayInputStream(new byte[0]), terminalOutput).paused(true).build()) {
            terminal.setSize(new Size(80, 24));
            CountdownReader reader = new CountdownReader(terminal);
            StateUi.attachTerminal(reader);
            startGame();
            machine.handleInput("Ping");
            String expected = ui.challenge.getText();
            String typed = expected.substring(0, expected.lastIndexOf(' '));
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
            clock.elapsedSeconds = 16.0;
            machine.update();
            assertEquals("", reader.getBuffer().toString());
            assertFalse(reader.getDisplayedBufferWithPrompts(new ArrayList<>())
                    .toString().contains("Time remaining:"));
            assertEquals(1, ui.enemyTurns);
            TypingResult timedOut = TypingEvaluator.evaluateTyping(expected, typed, 16.0, 15.0);
            int damage = (int) (10 * (timedOut.getAccuracyPercent() + timedOut.getWpm()) / 100.0 * 0.8);
            assertTrue(damage > 0);
            assertEquals(80 - damage, ui.enemy.getHealth());
            assertEquals(90, ui.player.getHealth());
            String feedback = terminalOutput.toString(StandardCharsets.UTF_8);
            assertTrue(feedback.contains(String.format(Locale.ROOT, "WPM: %.2f", timedOut.getWpm())));
            assertTrue(feedback.contains(String.format(Locale.ROOT,
                    "Accuracy: %.2f%%", timedOut.getAccuracyPercent())));
            assertTrue(feedback.contains("Damage penalty: 20% (time limit exceeded)"));
            assertFalse(feedback.contains("Wrong words:"));

            machine.handleInput("Ping");
            assertTrue(reader.getDisplayedBufferWithPrompts(new ArrayList<>())
                    .toString().contains("Time remaining: 15s"));
        } finally {
            StateUi.detachTerminal();
        }
    }

    @Test
    void handleInput_lateCorrectAnswer_dealsReducedDamage() {
        startGame();
        machine.handleInput("Ping");
        String expected = ui.challenge.getText();
        TypingResult result = TypingEvaluator.evaluateTyping(expected, expected, 16.0, 15.0);
        int damage = (int) (10 * (result.getAccuracyPercent() + result.getWpm()) / 100.0 * 0.8);
        clock.elapsedSeconds = 16.0;
        clock.remainingSeconds = 0;
        machine.handleInput(expected);

        assertTrue(damage > 0);
        assertEquals(80 - damage, ui.enemy.getHealth());
        assertEquals(90, ui.player.getHealth());
        assertEquals(1, ui.enemyTurns);
        assertTrue(report().contains("Accuracy: 100.00%"));
        assertTrue(report().contains("Damage penalty: 20% (time limit exceeded)"));
    }

    @Test
    void handleInput_atExactLimit_appliesFullDamage() {
        startGame();
        machine.handleInput("Ping");
        String expected = ui.challenge.getText();
        TypingResult result = TypingEvaluator.evaluateTyping(expected, expected, 15.0, 15.0);
        int damage = (int) (10 * (result.getAccuracyPercent() + result.getWpm()) / 100.0);
        clock.elapsedSeconds = 15.0;
        clock.remainingSeconds = 0;
        machine.update();
        assertEquals(0, ui.enemyTurns);
        machine.handleInput(expected);

        assertEquals(80 - damage, ui.enemy.getHealth());
        assertEquals(1, ui.enemyTurns);
        assertTrue(report().contains("Damage penalty: 0%"));
        assertFalse(report().contains("Damage penalty: 20%"));
    }

    @Test
    void handleInput_fiveVictories_advancesAutomaticallyThenReturnsToMenu() {
        startGame();
        // Take one hit first to verify that remaining health carries into later scenarios.
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

        assertEquals(5, ui.encounters);
        assertTrue(machine.nextState instanceof MainMenuState);
        assertTrue(machine.isRunning());
        assertTrue(report().contains("You win! All 5 scenarios cleared."));
        assertFalse(report().contains("Scenario 6/5"));

        machine.handleInput("start");
        assertTrue(machine.nextState instanceof GamePlayState);
        assertEquals(2, report().split("Scenario 1/5", -1).length - 1);
        assertTrue(report().substring(report().lastIndexOf("Scenario 1/5")).contains("HP: 100"));
        machine.handleInput("exit");
    }

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

    @Test
    void handleInput_exitDuringTyping_stopsWithoutTakingAnEnemyTurn() {
        startGame();
        machine.handleInput("Ping");
        machine.handleInput(" EXIT ");

        assertFalse(machine.isRunning());
        assertEquals(0, ui.enemyTurns);
        assertFalse(report().contains("WPM:"));
    }

    /** Starts gameplay with controllable timing and observed combat output. */
    private void startGame() {
        machine.start(new GamePlayState(machine, clock, ui, new FixedEnemyRandom()));
    }

    /** Submits an answer at a reproducible duration within every skill's limit. */
    private void submitAfterTenSeconds(String typedText) {
        clock.elapsedSeconds = 10.0;
        clock.remainingSeconds = clock.durationSeconds - 10;
        machine.handleInput(typedText);
    }

    private String report() {
        return output.toString(StandardCharsets.UTF_8);
    }

    /** Keeps the test enemy's health and skill sequence independent of random content. */
    private static class FixedEnemyRandom extends Random {
        @Override
        public int nextInt(int bound) {
            return 0;
        }
    }

    /** Supplies deterministic time without waiting through real countdowns. */
    private static class ControlledClock extends ChallengeClock {
        private int durationSeconds;
        private int remainingSeconds;
        private double elapsedSeconds;
        private int startCount;

        @Override
        public void start(int durationSeconds) {
            this.durationSeconds = durationSeconds;
            remainingSeconds = durationSeconds;
            elapsedSeconds = 0.0;
            startCount++;
        }

        @Override
        public int getRemainingSeconds() {
            return remainingSeconds;
        }

        @Override
        public double getElapsedSeconds() {
            return elapsedSeconds;
        }
    }

    /** Observes real combat participants and guards against blocking console reads. */
    private static class RecordingCombatUi extends CombatUi {
        private Enemy enemy;
        private Player player;
        private TypingChallenge challenge;
        private int encounters;
        private int enemyTurns;
        private int invalidSkills;

        @Override
        public void showCombatStart(Enemy enemy) {
            this.enemy = enemy;
            encounters++;
            super.showCombatStart(enemy);
        }

        @Override
        public void showPlayerTurn(Player player, Enemy enemy) {
            this.player = player;
            super.showPlayerTurn(player, enemy);
        }

        @Override
        public void showEnemyTurn(Enemy enemy) {
            enemyTurns++;
            super.showEnemyTurn(enemy);
        }

        @Override
        public void showTypingChallenge(TypingChallenge challenge) {
            this.challenge = challenge;
            super.showTypingChallenge(challenge);
        }

        @Override
        public void showInvalidSkill() {
            invalidSkills++;
            super.showInvalidSkill();
        }

        @Override
        public Skill getSkillChoice(ArrayList<Skill> activeSkills) {
            throw new AssertionError("Gameplay must receive skills through the main input loop.");
        }

        @Override
        public String getTypingSubmission(String typingChallenge) {
            throw new AssertionError("Gameplay must receive submissions through the main input loop.");
        }
    }

    /** Records transitions while preserving normal state-machine behavior. */
    private static class RecordingStateMachine extends StateMachine {
        private State nextState;

        @Override
        public void transitState(State nextState) {
            this.nextState = nextState;
            super.transitState(nextState);
        }
    }
}
