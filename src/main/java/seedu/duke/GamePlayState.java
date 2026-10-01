package seedu.duke;

/**
 * Represents the state in which the player is playing the game.
 */
public class GamePlayState extends State {
    /**
     * Creates the gameplay state.
     *
     * @param stateMachine the machine that controls this state
     */
    public GamePlayState(StateMachine stateMachine) {
        super("Gameplay", stateMachine);
    }

    @Override
    public void enter() {
        System.out.println("Begin gameplay");
    }

    @Override
    public void exit() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    @Override
    public void handleInput(String input) {
        if (input.equalsIgnoreCase("exit")) {
            System.out.println("Exit");
            return;
        }

        System.out.println("Playing...");
    }
}
