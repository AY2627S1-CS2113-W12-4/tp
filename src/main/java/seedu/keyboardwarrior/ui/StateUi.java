package seedu.keyboardwarrior.ui;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

import seedu.keyboardwarrior.CountdownReader;
import seedu.keyboardwarrior.GameDifficulty;
import seedu.keyboardwarrior.storage.SaveStore.SavedRecord;
import seedu.keyboardwarrior.typing.TypingChallenge;
import seedu.keyboardwarrior.typing.TypingResult;
import seedu.keyboardwarrior.typing.TypingResultFormatter;

/**
 * Displays the application's console interface, keeping presentation separate
 * from state logic.
 * The methods are static because this UI has no per-instance data.
 */
public final class StateUi {
    private static final String SEPARATOR = "----------------------------------------";
    private static CountdownReader reader;

    /**
     * Prevents instantiation of this utility class.
     */
    private StateUi() {
        // Utility class: callers use the display methods without creating an instance.
    }

    /**
     * Connects terminal input and output to the UI.
     *
     * @param lineReader reader used for terminal input and display
     */
    public static void attachTerminal(CountdownReader lineReader) {
        reader = lineReader;
    }

    /**
     * Releases the reader after terminal shutdown.
     */
    public static void detachTerminal() {
        reader = null;
    }

    /**
     * Updates the countdown within the reader's prompt, preserving input.
     *
     * @param seconds seconds to display
     */
    public static void updateCountdown(int seconds) {
        if (reader != null) {
            reader.setCountdown(seconds);
        }
    }

    /**
     * Restores the ordinary prompt after gameplay.
     */
    public static void clearCountdown() {
        if (reader != null) {
            reader.setCountdown(null);
        }
    }

    /**
     * Clears the editing line before asking for the next combat skill.
     */
    public static void clearInput() {
        if (reader != null) {
            reader.clearInput();
        }
    }

    /**
     * Returns and clears the unfinished passage for automatic timeout evaluation.
     *
     * @return unfinished input, or an empty string if no terminal is attached
     */
    public static String takeCurrentInput() {
        return reader == null ? "" : reader.takeCurrentInput();
    }

    /**
     * Displays the timer above the typing instruction and target passage.
     *
     * @param challenge typing challenge to display
     */
    public static void showTypingChallenge(TypingChallenge challenge) {
        if (reader != null) {
            reader.showTypingChallenge(challenge.getText(), challenge.getTime());
        } else {
            showRemainingTime(challenge.getTime());
            printLine("Time limit: " + challenge.getTime() + " seconds");
            printLine("Type the following text and press Enter to attack:");
            printLine(challenge.getText());
        }
    }

    /**
     * Prints the supplied target text with the line-submission instruction; no timer is started.
     *
     * @param text target passage to print
     */
    public static void showTypingChallenge(String text) {
        printLine("Type the following text. Press Enter to submit each line before time runs out:");
        printLine(text);
    }

    /**
     * Prints output above any input currently being edited, or directly in tests.
     * Combat output uses this same terminal so it does not overwrite the editable
     * line.
     *
     * @param text output to print above any editable input
     */
    public static void printLine(String text) {
        if (reader == null) {
            System.out.println(text);
        } else {
            reader.printAbove(text);
        }
    }

    /**
     * Displays the Keyboard Warrior banner.
     */
    public static void showBanner() {
        printLine("  _  __          _                         _");
        printLine(" | |/ /___ _   _| |__   ___   __ _ _ __ __| |");
        printLine(" | ' // _ \\ | | | '_ \\ / _ \\ / _` | '__/ _` |");
        printLine(" | . \\  __/ |_| | |_) | (_) | (_| | | | (_| |");
        printLine(" |_|\\_\\___|\\__, |_.__/ \\___/ \\__,_|_|  \\__,_|");
        printLine("           |___/");
        printLine(" __        __             _");
        printLine(" \\ \\      / /_ _ _ __ _ __(_) ___  _ __");
        printLine("  \\ \\ /\\ / / _` | '__| '__| |/ _ \\| '__|");
        printLine("   \\ V  V / (_| | |  | |  | | (_) | |");
        printLine("    \\_/\\_/ \\__,_|_|  |_|  |_|\\___/|_|");
    }

    /**
     * Displays a divider between sections or between user input and a response.
     */
    public static void showSeparator() {
        printLine(SEPARATOR);
    }

    /**
     * Prompts for a command without a newline so the player types on the same line.
     */
    public static void showInputPrompt() {
        if (reader != null) {
            return;
        }
        System.out.print("Your input: ");
        System.out.flush();
    }

    /**
     * Displays the banner and available main-menu choices.
     */
    public static void showMainMenu() {
        showMainMenu(GameDifficulty.HARD);
    }

    /** Displays the six menu choices and the difficulty that the next new run will use. */
    public static void showMainMenu(GameDifficulty difficulty) {
        showBanner();
        showSeparator();
        printLine("Current difficulty: " + difficulty.getDisplayName());
        printLine("1. Start");
        printLine("2. Records");
        printLine("3. Exit");
        printLine("4. Choose Difficulty");
        printLine("5. Continue");
        printLine("6. Leaderboard");
        printLine("Enter a number or option name.");
    }

    /** Shows each run difficulty and its effects before the player chooses. */
    public static void showDifficultyMenu() {
        printLine("Choose Difficulty");
        GameDifficulty[] difficulties = GameDifficulty.values();
        for (int index = 0; index < difficulties.length; index++) {
            GameDifficulty difficulty = difficulties[index];
            printLine((index + 1) + ". " + difficulty.getDisplayName() + " | Damage and countdown x"
                    + difficulty.getMultiplier() + " | Starting HP: " + difficulty.getStartingHealth());
        }
        printLine("Enter a number or difficulty name; Exit to quit.");
    }

    /**
     * Explains which commands the main menu accepts.
     */
    public static void showInvalidOption() {
        printLine("Invalid option. Please enter Start, Records, Exit, Choose Difficulty, "
                + "Continue or Leaderboard (1-6).");
    }

    /** Reminds the player of the available commands after displaying saved results. */
    public static void showMenuOptionsPrompt() {
        printLine("Enter Start, Records, Exit, Choose Difficulty, Continue or Leaderboard (1-6).");
    }

    /**
     * Displays saved typing results in chronological order while remaining in the main menu.
     *
     * @param records saved records in display order, oldest first
     */
    public static void showRecords(List<SavedRecord> records) {
        printLine("Typing history (oldest to newest)");
        if (records.isEmpty()) {
            printLine("No typing records saved yet. Play a game to record your results.");
        } else {
            printLine(String.format(Locale.ROOT, "%3s  %10s  %10s", "#", "WPM", "Accuracy"));
            for (int index = 0; index < records.size(); index++) {
                SavedRecord record = records.get(index);
                printLine(String.format(Locale.ROOT, "%3d  %10.2f  %9.2f%%",
                        index + 1, record.wpm(), record.accuracyPercent()));
            }
        }
        showMenuOptionsPrompt();
    }

    /**
     * Prompts for an optional seed before starting a run.
     */
    public static void showSeedPrompt() {
        printLine("Enter a seed (or press Enter for a random seed):");
    }

    /**
     * Displays the numeric seed used for the new run.
     *
     * @param seed numeric seed used for the new run
     */
    public static void showSeed(long seed) {
        printLine("Starting run with seed: " + seed);
    }

    /**
     * Announces entry into gameplay.
     */
    public static void showGameplayStart() {
        printLine("Begin gameplay. Choose a skill, then type its prompt to attack.");
        printLine("Your remaining health carries into the next scenario. Type Exit at any time to quit.");
    }

    /**
     * Displays progress before the next scenario begins.
     *
     * @param number one-based scenario number
     * @param total total number of scenarios or phases
     */
    public static void showScenario(int number, int total) {
        showSeparator();
        printLine("Scenario " + number + "/" + total);
    }

    /**
     * Announces the current final-boss phase after the five regular scenarios.
     *
     * @param phase one-based boss phase number
     * @param total total number of scenarios or phases
     */
    public static void showBossPhase(int phase, int total) {
        showSeparator();
        printLine("Final Boss - Phase " + phase + "/" + total);
        if (phase == 2) {
            printLine("The Kernel Overlord becomes enraged! Its attacks now deal 20 damage.");
        }
    }

    /**
     * Announces victory after all five scenarios and both final-boss phases.
     */
    public static void showRunVictory() {
        printLine("You win! All 5 scenarios and both final boss phases cleared.");
    }

    /**
     * Announces that player defeat ended the run.
     */
    public static void showRunDefeat() {
        printLine("Game over. Start a new run from the main menu.");
    }

    /**
     * Displays evaluated metrics while retaining individual word errors in the
     * result.
     *
     * @param result evaluated typing result
     */
    public static void showTypingResult(TypingResult result) {
        printLine(TypingResultFormatter.formatTypingSummary(result));
    }

    /**
     * Displays feedback for a gameplay command.
     */
    public static void showPlaying() {
        printLine("Playing...");
    }

    /**
     * Displays the seconds left in the current challenge.
     *
     * @param seconds seconds to display
     */
    public static void showRemainingTime(int seconds) {
        if (reader != null) {
            updateCountdown(seconds);
        } else {
            printLine("Time remaining: " + seconds + "s");
        }
    }

    /**
     * Announces that the challenge deadline has been reached.
     */
    public static void showTimeUp() {
        printLine("");
        printLine("Time's up!");
    }

    /**
     * Announces that the player submitted the correct challenge text in time.
     */
    public static void showChallengeCompleted() {
        printLine("Challenge completed!");
    }

    /**
     * Announces that the application has stopped.
     */
    public static void showExit() {
        printLine("Exit.");
    }

    /**
     * Displays the persisted all-time best attempt, or an empty message before the first attempt.
     *
     * @param highScore the stored best attempt, if one exists
     */
    public static void showLeaderboard(Optional<SavedRecord> highScore) {
        printLine("Leaderboard - All-time highest score");
        printLine("Highest WPM, with accuracy breaking ties.");
        if (highScore.isEmpty()) {
            printLine("No typing records saved yet. Play a game to appear on the leaderboard.");
        } else {
            SavedRecord bestRecord = highScore.get();
            printLine(String.format(Locale.ROOT, "%10s  %10s", "WPM", "Accuracy"));
            printLine(String.format(Locale.ROOT, "%10.2f  %9.2f%%",
                    bestRecord.wpm(), bestRecord.accuracyPercent()));
        }
        showMenuOptionsPrompt();
    }
}
