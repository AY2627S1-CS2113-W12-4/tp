package seedu.keyboardwarrior;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import seedu.keyboardwarrior.state.State;
import seedu.keyboardwarrior.state.StateMachine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Exercises safe plain-console ticks independently of native terminal support. */
@Timeout(5)
class KeyboardWarriorPlainLoopTest {
    @Test
    void runPlainLoop_waitingForEnter_advancesWaitingWithoutSubmittingUnfinishedText() throws IOException {
        StateMachine machine = new StateMachine();
        CountDownLatch promptRevealed = new CountDownLatch(1);
        RecordingState state = new RecordingState(machine, promptRevealed::countDown);
        machine.start(state);
        try (Reader input = new DelayedReader(promptRevealed, "complete answer\n")) {
            KeyboardWarrior.runPlainLoop(input, machine);
        }

        assertEquals(List.of("complete answer"), state.inputs);
        assertTrue(state.waitingUpdates > 0, "The prompt must become ready without requiring input first.");
        assertEquals(0, state.fullUpdates, "Plain mode must not submit an unavailable unfinished buffer.");
        assertFalse(machine.isRunning());
    }

    @Test
    void runPlainLoop_eofAfterUnterminatedLine_preservesQueuedInputOrder() throws IOException {
        StateMachine machine = new StateMachine();
        RecordingState state = new RecordingState(machine, () -> { });
        machine.start(state);

        KeyboardWarrior.runPlainLoop(new StringReader("first\nsecond\nlast"), machine);

        assertEquals(List.of("first", "second", "last"), state.inputs);
        assertEquals(1, state.exits);
        assertEquals(0, state.fullUpdates);
    }

    @Test
    void runPlainLoop_emptyInput_stopsWithoutInventingACommand() throws IOException {
        StateMachine machine = new StateMachine();
        RecordingState state = new RecordingState(machine, () -> { });
        machine.start(state);

        KeyboardWarrior.runPlainLoop(new StringReader(""), machine);

        assertTrue(state.inputs.isEmpty());
        assertEquals(1, state.exits);
        assertFalse(machine.isRunning());
    }

    @Test
    void runPlainLoop_readerFailure_propagatesAndStopsActiveState() {
        StateMachine machine = new StateMachine();
        RecordingState state = new RecordingState(machine, () -> { });
        machine.start(state);
        Reader input = new Reader() {
            @Override
            public int read(char[] buffer, int offset, int length) throws IOException {
                throw new IOException("simulated input failure");
            }

            @Override
            public void close() {
            }
        };

        IOException failure = assertThrows(IOException.class, () -> KeyboardWarrior.runPlainLoop(input, machine));

        assertEquals("simulated input failure", failure.getMessage());
        assertEquals(1, state.exits);
        assertFalse(machine.isRunning());
    }

    @Test
    void runPlainLoop_interruption_preservesFlagAndStopsActiveState() throws IOException {
        StateMachine machine = new StateMachine();
        RecordingState state = new RecordingState(machine, () -> { });
        machine.start(state);
        try {
            Thread.currentThread().interrupt();
            KeyboardWarrior.runPlainLoop(new StringReader("not dispatched"), machine);

            assertTrue(Thread.currentThread().isInterrupted());
            assertTrue(state.inputs.isEmpty());
            assertFalse(machine.isRunning());
            assertEquals(1, state.exits);
        } finally {
            // Do not leave the JUnit worker interrupted after checking the application's contract.
            Thread.interrupted();
        }
    }

    /** Records main-thread callbacks and optionally reveals the input source's delayed prompt. */
    private static class RecordingState extends State {
        private final List<String> inputs = new ArrayList<>();
        private final Runnable revealPrompt;
        private int waitingUpdates;
        private int fullUpdates;
        private int exits;

        RecordingState(StateMachine machine, Runnable revealPrompt) {
            super("Plain loop test", machine);
            this.revealPrompt = revealPrompt;
        }

        @Override
        public void enter() {
        }

        @Override
        public void exit() {
            exits++;
        }

        @Override
        public void handleInput(String input) {
            inputs.add(input);
        }

        @Override
        public void update() {
            fullUpdates++;
        }

        @Override
        public void updateWaiting() {
            waitingUpdates++;
            revealPrompt.run();
        }
    }

    /** Holds a complete input line until a main-thread waiting tick reveals its prompt. */
    private static class DelayedReader extends Reader {
        private final CountDownLatch promptRevealed;
        private final StringReader input;

        DelayedReader(CountDownLatch promptRevealed, String text) {
            this.promptRevealed = promptRevealed;
            this.input = new StringReader(text);
        }

        @Override
        public int read(char[] buffer, int offset, int length) throws IOException {
            try {
                promptRevealed.await();
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                throw new IOException("Input reader interrupted", exception);
            }
            return input.read(buffer, offset, length);
        }

        @Override
        public void close() {
            input.close();
        }
    }
}
