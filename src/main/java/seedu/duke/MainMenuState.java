package seedu.duke;

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
        System.out.println("=== Keyboard Warrior ===");
        System.out.println("Start");
        System.out.println("Exit");
    }

    @Override
    public void exit() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    @Override
    public void handleInput(String input) {
        if (input.equalsIgnoreCase("start")) {
            stateMachine.transitState(new GamePlayState(stateMachine));
        } else if (input.equalsIgnoreCase("exit")) {
            stateMachine.stop();
        } else {
            System.out.println("Invalid option. Please enter Start or Exit.");
        }
    }
}
