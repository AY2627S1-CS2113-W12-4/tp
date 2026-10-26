package seedu.keyboardwarrior.state;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

import seedu.keyboardwarrior.GameDifficulty;
import seedu.keyboardwarrior.GameSave;
import seedu.keyboardwarrior.seed.RunRandom;
import seedu.keyboardwarrior.seed.SeedGenerator;
import seedu.keyboardwarrior.storage.GameSaveStore;
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
    private final GameDifficulty difficulty;

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
        this(stateMachine, saveStore, GameDifficulty.HARD);
    }

    /** Retains the selected difficulty between runs without changing the history location. */
    MainMenuState(StateMachine stateMachine, SaveStore saveStore, GameDifficulty difficulty) {
        super("Main Menu", stateMachine);
        this.saveStore = Objects.requireNonNull(saveStore);
        this.difficulty = Objects.requireNonNull(difficulty);
    }

    /**
     * Displays the main menu.
     */
    @Override
    public void enter() {
        StateUi.showMainMenu(difficulty);
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
        String choice = input.trim();
        if (choice.equalsIgnoreCase("exit") || (!isWaitingForSeed && choice.equals("3"))) {
            stateMachine.stop();
            return;
        }
        if (isWaitingForSeed) {
            long seed = SeedGenerator.parseSeed(input);
            StateUi.showSeed(seed);
            RunRandom masterRandom = new RunRandom(seed);
            stateMachine.transitState(new GamePlayState(stateMachine, masterRandom, saveStore, difficulty));
            return;
        }

        if (choice.equalsIgnoreCase("start") || choice.equals("1")) {
            isWaitingForSeed = true;
            StateUi.showSeedPrompt();
        } else if (choice.equalsIgnoreCase("records") || choice.equals("2")) {
            try {
                StateUi.showRecords(saveStore.load());
            } catch (IOException e) {
                StateUi.printLine("Could not load typing history: " + e.getMessage());
            }
        } else if (choice.equalsIgnoreCase("leaderboard") || choice.equals("6")) {
            try {
                StateUi.showLeaderboard(saveStore.loadHighScore());
            } catch (IOException exception) {
                StateUi.printLine("Could not load leaderboard: " + exception.getMessage());
            }
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

    /**
     * Performs no periodic work while the menu is active.
     */
    @Override
    public void update() {
    }
}
