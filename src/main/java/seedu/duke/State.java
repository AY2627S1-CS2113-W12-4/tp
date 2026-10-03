package seedu.duke;

/**
 * Base class for every state managed by the application.
 */
public abstract class State {
    /** Provides transitions and stopping behaviour to the state. */
    protected final StateMachine stateMachine;
    private final String stateName;

    protected State(String stateName, StateMachine stateMachine) {
        this.stateName = stateName;
        this.stateMachine = stateMachine;
    }

    @Override
    public String toString() {
        return stateName;
    }

    /** Performs setup required when this state becomes active. */
    public abstract void enter();

    /** Performs cleanup required before this state is replaced. */
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
