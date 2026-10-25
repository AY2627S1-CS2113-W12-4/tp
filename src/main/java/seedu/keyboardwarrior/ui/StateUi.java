package seedu.keyboardwarrior.ui;

import seedu.keyboardwarrior.CountdownReader;
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

    private StateUi() {
        // Utility class: callers use the display methods without creating an instance.
    }

    /**
     * Connects terminal input and output to the UI.
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

    /** Clears the editing line before asking for the next combat skill. */
    public static void clearInput() {
        if (reader != null) {
            reader.clearInput();
        }
    }

    /**
     * Returns and clears the unfinished passage for automatic timeout evaluation.
     */
    public static String takeCurrentInput() {
        return reader == null ? "" : reader.takeCurrentInput();
    }

    /** Displays the timer above the typing instruction and target passage. */
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
     * Shows the target text and explains how to submit text before the deadline.
     */
    public static void showTypingChallenge(String text) {
        printLine("Type the following text. Press Enter to submit each line before time runs out:");
        printLine(text);
    }

    /**
     * Prints output above any input currently being edited, or directly in tests.
     * Combat output uses this same terminal so it does not overwrite the editable
     * line.
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
        showBanner();
        showSeparator();
        printLine("Type Start to begin five combat scenarios, or Exit to quit.");
    }

    /**
     * Explains which commands the main menu accepts.
     */
    public static void showInvalidOption() {
        printLine("Invalid option. Please enter Start or Exit.");
    }

    public static void showSeedPrompt() {
        printLine("Enter a seed (or press Enter for a random seed):");
    }

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

    /** Displays progress before the next scenario begins. */
    public static void showScenario(int number, int total) {
        showSeparator();
        printLine("Scenario " + number + "/" + total);
    }

    /** Announces victory after all five enemies have been defeated. */
    public static void showRunVictory() {
        printLine("You win! All 5 scenarios cleared.");
    }

    /** Announces that player defeat ended the run. */
    public static void showRunDefeat() {
        printLine("Game over. Start a new run from the main menu.");
    }

    /**
     * Displays evaluated metrics while retaining individual word errors in the
     * result.
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
}
