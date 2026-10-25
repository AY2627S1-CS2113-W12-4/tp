package seedu.keyboardwarrior.state;

import java.util.Objects;
import java.util.logging.Logger;

import seedu.keyboardwarrior.ui.StateUi;

/**
 * Keeps track of the active application state and delegates game-loop work to
 * it.
 */
public class StateMachine {
    /** Diagnostic state changes; FINE messages are hidden by default. */
    private static final Logger LOGGER = Logger.getLogger(StateMachine.class.getName());
    private State currentState;
    private boolean isRunning = true;

    /**
     * Activates the first state of the application.
     *
     * @param initialState the first state to enter
     * @throws NullPointerException  if the initial state is null
     * @throws IllegalStateException if this machine was already started or stopped
     */
    public void start(State initialState) {
        Objects.requireNonNull(initialState, "Initial state must not be null");
        if (currentState != null || !isRunning) {
            throw new IllegalStateException("State machine can only be started once.");
        }
        currentState = initialState;
        LOGGER.fine(() -> "Starting state: " + initialState);
        currentState.enter();
    }

    /**
     * Leaves the active state and enters the next one.
     *
     * @param nextState the state to activate
     * @throws NullPointerException  if the next state is null
     * @throws IllegalStateException if the machine has not started or has stopped
     */
    public void transitState(State nextState) {
        Objects.requireNonNull(nextState, "Next state must not be null");
        requireActiveState();
        LOGGER.fine(() -> "Transition from " + currentState + " to " + nextState);
        this.currentState.exit();
        this.currentState = nextState;
        this.currentState.enter();
    }

    /**
     * Separates the player's input from the response, then passes it to the active
     * state.
     *
     * @param input the player's input
     * @throws NullPointerException  if input is null
     * @throws IllegalStateException if the machine has not started or has stopped
     */
    public void handleInput(String input) {
        Objects.requireNonNull(input, "Input must not be null");
        requireActiveState();
        StateUi.showSeparator();
        this.currentState.handleInput(input);
    }

    /**
     * Lets the active state perform time-based work without player input.
     *
     * @throws IllegalStateException if the machine has not started or has stopped
     */
    public void update() {
        requireActiveState();
        this.currentState.update();
    }

    /**
     * Leaves the active state and requests that the application stop.
     * Repeated calls after stopping do nothing.
     *
     * @throws IllegalStateException if the machine has not started
     */
    public void stop() {
        if (isRunning) {
            requireActiveState();
            LOGGER.fine(() -> "Stopping state: " + currentState);
            currentState.exit();
            isRunning = false;
            StateUi.showExit();
        }
    }

    /** Rejects lifecycle operations before startup or after shutdown. */
    private void requireActiveState() {
        if (currentState == null || !isRunning) {
            throw new IllegalStateException("State machine must be started and running.");
        }
    }

    /**
     * Reports whether the application should continue accepting input.
     *
     * @return true while the application is running
     */
    public boolean isRunning() {
        return isRunning;
    }
}
