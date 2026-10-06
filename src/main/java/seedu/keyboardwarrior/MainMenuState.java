package seedu.keyboardwarrior;

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
        System.out.println("  _  __          _                         _");
        System.out.println(" | |/ /___ _   _| |__   ___   __ _ _ __ __| |");
        System.out.println(" | ' // _ \\ | | | '_ \\ / _ \\ / _` | '__/ _` |");
        System.out.println(" | . \\  __/ |_| | |_) | (_) | (_| | | | (_| |");
        System.out.println(" |_|\\_\\___|\\__, |_.__/ \\___/ \\__,_|_|  \\__,_|");
        System.out.println("           |___/");
        System.out.println(" __        __             _");
        System.out.println(" \\ \\      / /_ _ _ __ _ __(_) ___  _ __");
        System.out.println("  \\ \\ /\\ / / _` | '__| '__| |/ _ \\| '__|");
        System.out.println("   \\ V  V / (_| | |  | |  | | (_) | |");
        System.out.println("    \\_/\\_/ \\__,_|_|  |_|  |_|\\___/|_|");
        System.out.println("----------------------------------------");
        System.out.println("Choose 1 option:");
        System.out.println("1. Start");
        System.out.println("2. Exit");
    }

    @Override
    public void exit() {
        System.out.println("----------------------------------------");
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
    
    @Override
    public void update() {
    }
}
