package seedu.keyboardwarrior.state;

import java.util.Objects;

import seedu.keyboardwarrior.GameDifficulty;
import seedu.keyboardwarrior.storage.SaveStore;
import seedu.keyboardwarrior.ui.StateUi;

/** Lets the player select a run difficulty before returning to the main menu. */
public class DifficultySelectionState extends State {
    private final SaveStore saveStore;

    /** Retains the menu's typing-history location while choosing a difficulty. */
    DifficultySelectionState(StateMachine stateMachine, SaveStore saveStore) {
        super("Choose Difficulty", stateMachine);
        this.saveStore = Objects.requireNonNull(saveStore);
    }

    @Override
    public void enter() {
        StateUi.showDifficultyMenu();
    }

    @Override
    public void exit() {
    }

    @Override
    public void handleInput(String input) {
        String choice = input.trim();
        if (choice.equalsIgnoreCase("exit")) {
            stateMachine.stop();
            return;
        }
        GameDifficulty[] difficulties = GameDifficulty.values();
        for (int index = 0; index < difficulties.length; index++) {
            GameDifficulty difficulty = difficulties[index];
            if (choice.equals(Integer.toString(index + 1))
                    || choice.equalsIgnoreCase(difficulty.getDisplayName())) {
                stateMachine.transitState(new MainMenuState(stateMachine, saveStore, difficulty));
                return;
            }
        }
        StateUi.printLine("Invalid difficulty. Enter 1-4, Easy, Normal, Hard or Coder; Exit to quit.");
    }

    @Override
    public void update() {
    }
}
