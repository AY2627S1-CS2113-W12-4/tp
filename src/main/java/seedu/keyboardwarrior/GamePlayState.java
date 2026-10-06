package seedu.keyboardwarrior;

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
        System.out.println("----------------------------------------");
    }

    @Override
    public void handleInput(String input) {
        if (input.equalsIgnoreCase("exit")) {
            stateMachine.stop();
            return;
        }
        System.out.println("Playing...");
    }
    
    @Override
    public void update() {
    }
}
