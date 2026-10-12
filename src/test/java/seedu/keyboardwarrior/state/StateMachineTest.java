package seedu.keyboardwarrior.state;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/** Tests lifecycle validation without hiding mistakes behind null-pointer failures. */
class StateMachineTest {
    @Test
    void lifecycle_beforeStart_rejectsOperations() {
        StateMachine machine = new StateMachine();
        RecordingState state = new RecordingState(machine);

        assertThrows(IllegalStateException.class, () -> machine.handleInput("start"));
        assertThrows(IllegalStateException.class, machine::update);
        assertThrows(IllegalStateException.class, () -> machine.transitState(state));
        assertThrows(IllegalStateException.class, machine::stop);
        assertEquals(0, state.enterCount);
    }

    @Test
    void start_nullOrRepeatedStart_preservesActiveState() {
        StateMachine machine = new StateMachine();
        RecordingState state = new RecordingState(machine);
        assertThrows(NullPointerException.class, () -> machine.start(null));
        machine.start(state);

        assertThrows(IllegalStateException.class, () -> machine.start(new RecordingState(machine)));
        machine.update();
        assertEquals(1, state.enterCount);
        assertEquals(1, state.updateCount);
        assertEquals(0, state.exitCount);
    }

    @Test
    void invalidInputOrTransition_doesNotChangeActiveState() {
        StateMachine machine = new StateMachine();
        RecordingState state = new RecordingState(machine);
        machine.start(state);

        assertThrows(NullPointerException.class, () -> machine.transitState(null));
        assertThrows(NullPointerException.class, () -> machine.handleInput(null));
        machine.handleInput("Ping");
        assertEquals("Ping", state.lastInput);
        assertEquals(0, state.exitCount);
    }

    @Test
    void transitState_validState_leavesOldStateAndDelegatesToNewState() {
        StateMachine machine = new StateMachine();
        RecordingState first = new RecordingState(machine);
        RecordingState second = new RecordingState(machine);
        machine.start(first);

        machine.transitState(second);
        machine.handleInput("start");
        machine.update();

        assertEquals(1, first.exitCount);
        assertEquals(0, first.updateCount);
        assertEquals(1, second.enterCount);
        assertEquals(1, second.updateCount);
        assertEquals("start", second.lastInput);
    }

    @Test
    void stop_repeatedCalls_exitsOnceAndRejectsFurtherWork() {
        StateMachine machine = new StateMachine();
        RecordingState state = new RecordingState(machine);
        machine.start(state);
        machine.stop();
        machine.stop();

        assertFalse(machine.isRunning());
        assertEquals(1, state.exitCount);
        assertThrows(IllegalStateException.class, () -> machine.handleInput("start"));
        assertThrows(IllegalStateException.class, machine::update);
        assertThrows(IllegalStateException.class, () -> machine.transitState(new RecordingState(machine)));
        assertThrows(IllegalStateException.class, () -> machine.start(new RecordingState(machine)));
    }

    @Test
    void testRuntime_enablesAssertions() {
        assertTrue(StateMachine.class.desiredAssertionStatus(), "Tests must run with Java assertions enabled.");
    }

    /** Records state callbacks so failed operations can be checked for side effects. */
    private static class RecordingState extends State {
        private int enterCount;
        private int exitCount;
        private int updateCount;
        private String lastInput;

        RecordingState(StateMachine machine) {
            super("Recording", machine);
        }

        @Override
        public void enter() {
            enterCount++;
        }

        @Override
        public void exit() {
            exitCount++;
        }

        @Override
        public void handleInput(String input) {
            lastInput = input;
        }

        @Override
        public void update() {
            updateCount++;
        }
    }
}
