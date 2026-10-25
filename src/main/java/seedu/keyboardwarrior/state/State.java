package seedu.keyboardwarrior.state;

import java.util.Objects;

/**
 * Base class for every state managed by the application.
 */
public abstract class State {
    /**
     * Provides transitions and stopping behaviour to the state.
     */
    protected final StateMachine stateMachine;
    private final String stateName;

    /**
     * Creates a state with its display name and owning state machine.
     *
     * @param stateName name used when representing the state as text
     * @param stateMachine machine that controls this state
     * @throws NullPointerException if the name or state machine is null
     * @throws IllegalArgumentException if the name is blank
     */
    protected State(String stateName, StateMachine stateMachine) {
        Objects.requireNonNull(stateName, "State name must not be null");
        if (stateName.isBlank()) {
            throw new IllegalArgumentException("State name must not be blank.");
        }
        this.stateName = stateName;
        this.stateMachine = Objects.requireNonNull(stateMachine, "State machine must not be null");
    }

    /**
     * Returns the display name assigned to this state.
     *
     * @return state display name
     */
    @Override
    public String toString() {
        return stateName;
    }

    /**
     * Performs setup required when this state becomes active.
     */
    public abstract void enter();

    /**
     * Performs cleanup required before this state is replaced.
     */
    public abstract void exit();

    /**
     * Responds to one complete command entered by the player.
     *
     * @param input the player's command
     */
    public abstract void handleInput(String input);

    /**
     * Performs time-based work while this state is active.
     */
    public abstract void update();
}
