package seedu.keyboardwarrior;

import java.util.Scanner;

/**
 * Starts the Keyboard Warrior application with an introductory greeting.
 */
public class KeyboardWarrior {
    /**
     * Main entry point for the Keyboard Warrior application.
     */
    public static void main(String[] args) {
        String banner = " ____        _        \n"
                + "|  _ \\ _   _| | _____ \n"
                + "| | | | | | | |/ / _ \\\n"
                + "| |_| | |_| |   <  __/\n"
                + "|____/ \\__,_|_|\\_\\___|\n";
        System.out.println(banner);
        System.out.println("What is your name?");

        Scanner in = new Scanner(System.in);
        System.out.println("Hello " + in.nextLine());
    }
}
