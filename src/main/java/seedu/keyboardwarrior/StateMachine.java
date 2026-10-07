package seedu.keyboardwarrior;

/**
 * Keeps track of the active application state and delegates game-loop work to it.
 */
public class StateMachine {
    private State currentState;
    private boolean isRunning = true;

    /**
     * Activates the first state of the application.
     *
     * @param initialState the first state to enter
     */
    public void start(State initialState) {
        currentState = initialState;
        currentState.enter();
    }

    /**
     * Leaves the active state and enters the next one.
     *
     * @param nextState the state to activate
     */
    public void transitState(State nextState) {
        this.currentState.exit();
        this.currentState = nextState;
        this.currentState.enter();
    }

    /**
     * Separates the player's input from the response, then passes it to the active state.
     *
     * @param input the player's input
     */
    public void handleInput(String input) {
        Ui.showSeparator();
        this.currentState.handleInput(input);
    }

    /**
     * Lets the active state perform time-based work without player input.
     */
    public void update() {
        this.currentState.update();
    }

    /**
     * Leaves the active state and requests that the application stop.
     */
    public void stop() {
        if (isRunning) {
            currentState.exit();
            isRunning = false;
            Ui.showExit();
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
