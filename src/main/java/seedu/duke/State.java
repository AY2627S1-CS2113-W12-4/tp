package seedu.duke;

/**
 * Base class for every state managed by the application.
 */
public abstract class State {
    private String stateName;
    protected final StateMachine stateMachine;

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