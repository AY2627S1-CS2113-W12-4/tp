package seedu.keyboardwarrior.state;

import java.io.IOException;
import java.util.Objects;

import seedu.keyboardwarrior.storage.SaveStore;
import seedu.keyboardwarrior.ui.StateUi;

/**
 * Displays the main menu and responds to the player's menu choices.
 */
public class MainMenuState extends State {
    private final SaveStore saveStore;

    /**
     * Creates the main-menu state.
     *
     * @param stateMachine the machine that controls this state
     */
    public MainMenuState(StateMachine stateMachine) {
        this(stateMachine, new SaveStore());
    }

    /** Accepts a history file so menu tests can use temporary records. */
    MainMenuState(StateMachine stateMachine, SaveStore saveStore) {
        super("Main Menu", stateMachine);
        this.saveStore = Objects.requireNonNull(saveStore);
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
        } else if (input.trim().equalsIgnoreCase("records")) {
            try {
                StateUi.showRecords(saveStore.load());
            } catch (IOException e) {
                StateUi.printLine("Could not load typing history: " + e.getMessage());
            }
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
