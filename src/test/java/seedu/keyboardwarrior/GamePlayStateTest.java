package seedu.keyboardwarrior;

import org.junit.jupiter.api.Test;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import seedu.keyboardwarrior.typing.ChallengeClock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Verifies that gameplay starts its clock and ends when the countdown expires.
 */
class GamePlayStateTest {
    @Test
    void update_expired_displaysSubmittedTextResultsOnlyOnce() {
        RecordingStateMachine machine = new RecordingStateMachine();
        ControlledClock clock = new ControlledClock();
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream original = System.out;
        try (PrintStream capture = new PrintStream(output, true, StandardCharsets.UTF_8)) {
            System.setOut(capture);
            machine.start(new GamePlayState(machine, clock));
            machine.handleInput("The quick brown fox");
            machine.handleInput("jumps over the lazy dog.");
            String beforeTick = output.toString(StandardCharsets.UTF_8);
            clock.remainingSeconds = 59;
            machine.update();
            clock.remainingSeconds = 1;
            machine.update();
            assertEquals(beforeTick, output.toString(StandardCharsets.UTF_8));
            assertFalse(output.toString(StandardCharsets.UTF_8).contains("WPM:"));
            clock.remainingSeconds = 0;
            machine.update();
            machine.update();
            String report = output.toString(StandardCharsets.UTF_8);
            assertTrue(report.contains("Accuracy: 100.00%"));
            assertTrue(report.contains("Elapsed time: 60.00 seconds"));
            assertEquals(1, report.split("WPM:", -1).length - 1);
        } finally {
            System.setOut(original);
        }
    }

    @Test
    void handleInput_afterDeadline_evaluatesEmptySubmission() {
        RecordingStateMachine machine = new RecordingStateMachine();
        ControlledClock clock = new ControlledClock();
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream original = System.out;
        try (PrintStream capture = new PrintStream(output, true, StandardCharsets.UTF_8)) {
            System.setOut(capture);
            machine.start(new GamePlayState(machine, clock));
            clock.remainingSeconds = 0;
            machine.handleInput("The quick brown fox jumps over the lazy dog.");
            String report = output.toString(StandardCharsets.UTF_8);
            assertTrue(report.contains("WPM: 0.00"));
            assertTrue(report.contains("Accuracy: 0.00%"));
            assertTrue(report.contains("missing \"The\""));
        } finally {
            System.setOut(original);
        }
    }

    @Test
    void enter_startsCountdown() {
        RecordingStateMachine machine = new RecordingStateMachine();
        ControlledClock clock = new ControlledClock();
        machine.start(new GamePlayState(machine, clock));
        assertEquals(60, clock.remainingSeconds);
    }

    @Test
    void update_expiredWithoutInput_returnsToMenu() {
        RecordingStateMachine machine = new RecordingStateMachine();
        ControlledClock clock = new ControlledClock();
        machine.start(new GamePlayState(machine, clock));
        clock.remainingSeconds = 0;
        machine.update();
        assertTrue(machine.nextState instanceof MainMenuState);
        assertTrue(machine.isRunning());
    }

    @Test
    void handleInput_afterDeadline_returnsToMenu() {
        RecordingStateMachine machine = new RecordingStateMachine();
        ControlledClock clock = new ControlledClock();
        machine.start(new GamePlayState(machine, clock));
        clock.remainingSeconds = 0;
        machine.handleInput("typed too late");
        assertTrue(machine.nextState instanceof MainMenuState);
    }

    /**
     * Supplies a controllable countdown for testing deadline handling.
     */
    private static class ControlledClock extends ChallengeClock {
        private int remainingSeconds;

        @Override
        public void start(int durationSeconds) {
            remainingSeconds = durationSeconds;
        }

        @Override
        public int getRemainingSeconds() {
            return remainingSeconds;
        }
    }

    /**
     * Records transitions while preserving normal state-machine behavior.
     */
    private static class RecordingStateMachine extends StateMachine {
        private State nextState;

        @Override
        public void transitState(State nextState) {
            this.nextState = nextState;
            super.transitState(nextState);
        }
    }
}
