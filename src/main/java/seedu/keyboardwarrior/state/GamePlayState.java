package seedu.keyboardwarrior.state;

import seedu.keyboardwarrior.ui.StateUi;

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
        StateUi.showGameplayStart();
    }

    @Override
    public void exit() {
    }

    @Override
    public void handleInput(String input) {
        if (input.equalsIgnoreCase("exit")) {
            stateMachine.stop();
            return;
        }
        StateUi.showPlaying();
    }

    @Override
    public void update() {
    }
}
