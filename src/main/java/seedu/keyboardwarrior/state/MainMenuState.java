package seedu.keyboardwarrior.state;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

import seedu.keyboardwarrior.GameDifficulty;
import seedu.keyboardwarrior.GameSave;
import seedu.keyboardwarrior.storage.GameSaveStore;
import seedu.keyboardwarrior.storage.SaveStore;
import seedu.keyboardwarrior.ui.StateUi;

/**
 * Displays the main menu and responds to the player's menu choices.
 */
public class MainMenuState extends State {
    private final SaveStore saveStore;
    private final GameDifficulty difficulty;

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
        this(stateMachine, saveStore, GameDifficulty.HARD);
    }

    /** Retains the selected difficulty between runs without changing the history location. */
    MainMenuState(StateMachine stateMachine, SaveStore saveStore, GameDifficulty difficulty) {
        super("Main Menu", stateMachine);
        this.saveStore = Objects.requireNonNull(saveStore);
        this.difficulty = Objects.requireNonNull(difficulty);
    }

    @Override
    public void enter() {
        StateUi.showMainMenu(difficulty);
    }

    @Override
    public void exit() {
    }

    @Override
    public void handleInput(String input) {
        String choice = input.trim();
        if (choice.equalsIgnoreCase("start") || choice.equals("1")) {
            stateMachine.transitState(new GamePlayState(stateMachine, saveStore, difficulty));
        } else if (choice.equalsIgnoreCase("records") || choice.equals("2")) {
            try {
                StateUi.showRecords(saveStore.load());
            } catch (IOException e) {
                StateUi.printLine("Could not load typing history: " + e.getMessage());
            }
        } else if (choice.equalsIgnoreCase("exit") || choice.equals("3")) {
            stateMachine.stop();
        } else if (choice.equalsIgnoreCase("choose difficulty") || choice.equals("4")) {
            stateMachine.transitState(new DifficultySelectionState(stateMachine, saveStore));
        } else if (choice.equalsIgnoreCase("continue") || choice.equals("5")) {
            try {
                GameSaveStore gameSaveStore = new GameSaveStore(
                        saveStore.getDirectory().resolve("game-save.properties"));
                Optional<GameSave> save = gameSaveStore.load();
                if (save.isPresent()) {
                    stateMachine.transitState(new GamePlayState(stateMachine, saveStore, save.get()));
                } else {
                    StateUi.printLine("No saved game to continue. Choose Start to begin a new run.");
                }
            } catch (IOException exception) {
                StateUi.printLine("Could not load game: " + exception.getMessage());
            }
        } else {
            StateUi.showInvalidOption();
        }
    }

    @Override
    public void update() {
    }
}
