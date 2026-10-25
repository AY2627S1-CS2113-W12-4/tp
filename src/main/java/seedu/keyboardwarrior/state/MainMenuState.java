package seedu.keyboardwarrior.state;

import java.util.Random;

import seedu.keyboardwarrior.seed.SeedGenerator;
import seedu.keyboardwarrior.ui.StateUi;

/**
 * Displays the main menu and responds to the player's menu choices.
 */
public class MainMenuState extends State {
    private boolean isWaitingForSeed = false;

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
        if (isWaitingForSeed) {
            long seed = SeedGenerator.parseSeed(input);
            StateUi.showSeed(seed);

            Random masterRandom = new Random(seed);

            stateMachine.transitState(new GamePlayState(stateMachine, masterRandom));
            return;
        }

        if (input.trim().equalsIgnoreCase("start")) {
            isWaitingForSeed = true;
            StateUi.showSeedPrompt();
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
