package seedu.keyboardwarrior;

/**
 * Displays the application's console interface, keeping presentation separate from state logic.
 * The methods are static because this UI has no per-instance data.
 */
public final class Ui {
    private static final String SEPARATOR = "----------------------------------------";

    private Ui() {
        // Utility class: callers use the display methods without creating an instance.
    }

    /**
     * Displays the Keyboard Warrior banner.
     */
    public static void showBanner() {
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
    }

    /**
     * Displays a divider between sections or between user input and a response.
     */
    public static void showSeparator() {
        System.out.println(SEPARATOR);
    }

    /**
     * Prompts for a command without a newline so the player types on the same line.
     */
    public static void showInputPrompt() {
        System.out.print("Your input: ");
        System.out.flush();
    }

    /**
     * Displays the banner and available main-menu choices.
     */
    public static void showMainMenu() {
        showBanner();
        showSeparator();
        System.out.println("Choose 1 option:");
        System.out.println("1. Start");
        System.out.println("2. Exit");
    }

    /**
     * Explains which commands the main menu accepts.
     */
    public static void showInvalidOption() {
        System.out.println("Invalid option. Please enter Start or Exit.");
    }

    /**
     * Announces entry into gameplay.
     */
    public static void showGameplayStart() {
        System.out.println("Begin gameplay");
    }

    /**
     * Displays feedback for a gameplay command.
     */
    public static void showPlaying() {
        System.out.println("Playing...");
    }

    /**
     * Announces that the application has stopped.
     */
    public static void showExit() {
        System.out.println("Exit.");
    }
}
