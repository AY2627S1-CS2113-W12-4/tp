package seedu.keyboardwarrior.state;

import seedu.keyboardwarrior.ui.StateUi;

/**
 * Displays the main menu and responds to the player's menu choices.
 */
public class MainMenuState extends State {
    /**
     * Creates the main-menu state.
     *
     * @param stateMachine the machine that controls this state
     */
    public MainMenuState(StateMachine stateMachine) {
        super("Main Menu", stateMachine);
    }

    @Override
    public void enter() {
        StateUi.showMainMenu();
    }

    @Override
    public void exit() {
    }

    @Override
    public void handleInput(String input) {
        if (input.trim().equalsIgnoreCase("start")) {
            stateMachine.transitState(new GamePlayState(stateMachine));
        } else if (input.trim().equalsIgnoreCase("exit")) {
            stateMachine.stop();
        } else {
            StateUi.showInvalidOption();
        }
    }

    @Override
    public void update() {
    }
}
