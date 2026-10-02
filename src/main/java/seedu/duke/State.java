package seedu.duke;

/**
 * Base class for every state managed by the application.
 */
public abstract class State {
    protected final StateMachine stateMachine;
    private String stateName;

    protected State(String stateName, StateMachine stateMachine) {
        this.stateName = stateName;
        this.stateMachine = stateMachine;
    }

    @Override
    public String toString() {
        return stateName;
    }

    public abstract void enter();

    public abstract void exit();

    public abstract void handleInput(String input);
}
