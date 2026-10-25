package seedu.keyboardwarrior.state;

import java.io.IOException;
import java.util.Objects;
import java.util.Random;

import seedu.keyboardwarrior.seed.SeedGenerator;
import seedu.keyboardwarrior.storage.SaveStore;
import seedu.keyboardwarrior.ui.StateUi;

/**
 * Displays the main menu and responds to the player's menu choices.
 */
public class MainMenuState extends State {
    /**
     * Whether the next non-exit input is interpreted as a seed rather than a menu command.
     */
    private boolean isWaitingForSeed = false;
    private final SaveStore saveStore;

    /**
     * Creates the main-menu state.
     *
     * @param stateMachine the machine that controls this state
     */
    public MainMenuState(StateMachine stateMachine) {
        this(stateMachine, new SaveStore());
    }

    /**
     * Accepts a history file so menu tests can use temporary records.
     *
     * @param stateMachine machine that controls this state
     * @param saveStore history store retained when returning to the menu
     */
    MainMenuState(StateMachine stateMachine, SaveStore saveStore) {
        super("Main Menu", stateMachine);
        this.saveStore = Objects.requireNonNull(saveStore);
    }

    /**
     * Displays the main menu.
     */
    @Override
    public void enter() {
        StateUi.showMainMenu();
    }

    /**
     * Leaves the menu without additional cleanup.
     */
    @Override
    public void exit() {
    }

    /**
     * Handles menu commands or consumes the pending seed input.
     * Exit always stops the application. After Start, other input is parsed as a seed.
     *
     * @param input input interpreted according to the current state
     */
    @Override
    public void handleInput(String input) {
        if (input.trim().equalsIgnoreCase("exit")) {
            stateMachine.stop();
            return;
        }
        if (isWaitingForSeed) {
            long seed = SeedGenerator.parseSeed(input);
            StateUi.showSeed(seed);

            Random masterRandom = new Random(seed);

            stateMachine.transitState(new GamePlayState(stateMachine, masterRandom, saveStore));
            return;
        }

        if (input.trim().equalsIgnoreCase("start")) {
            isWaitingForSeed = true;
            StateUi.showSeedPrompt();
        } else if (input.trim().equalsIgnoreCase("records")) {
            try {
                StateUi.showRecords(saveStore.load());
            } catch (IOException e) {
                StateUi.printLine("Could not load typing history: " + e.getMessage());
            }
        } else {
            StateUi.showInvalidOption();
        }
    }

    /**
     * Performs no periodic work while the menu is active.
     */
    @Override
    public void update() {
    }
}
