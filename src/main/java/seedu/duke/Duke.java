package seedu.duke;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.Scanner;

public class Duke {
    /** Duration, in milliseconds, between consecutive game-loop updates. */
    private static final int TICK_MILLIS = 100;

    /**
     * Starts a background input reader and runs the game loop on the main thread.
     * The input reader may block while waiting for a line, but it only adds complete
     * commands to {@code inputQueue}. The main thread is the only thread that changes
     * the state machine, so timer updates and input handling cannot change a state concurrently.
     */
    public static void main(String[] args) {
        StateMachine stateMachine = new StateMachine();
        BlockingQueue<String> inputQueue = new LinkedBlockingQueue<>();

        stateMachine.start(new MainMenuState(stateMachine));

        Thread inputThread = new Thread(() -> {
            try (Scanner scanner = new Scanner(System.in)) {
                while (scanner.hasNextLine()) {
                    inputQueue.offer(scanner.nextLine());
                }
            }
        });
        inputThread.setDaemon(true);
        inputThread.start();

        while (stateMachine.isRunning()) {
            String input;
            while ((input = inputQueue.poll()) != null) {
                stateMachine.handleInput(input);
                if (!stateMachine.isRunning()) {
                    break;
                }
            }
            if (!stateMachine.isRunning()) {
                break;
            }
            stateMachine.update();
            try {
                Thread.sleep(TICK_MILLIS);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}
