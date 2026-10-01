package seedu.duke;

import java.util.Scanner;

public class Duke {
    /**
     * Starts the application and continuously sends player input to its active state.
     */
    public static void main(String[] args) {
        StateMachine stateMachine = new StateMachine();
        stateMachine.start(new MainMenuState(stateMachine));
        try (Scanner scanner = new Scanner(System.in)) {
            while (stateMachine.isRunning() && scanner.hasNextLine()) {
                String input = scanner.nextLine();
                stateMachine.handleInput(input);
            }
        }
    }
}
