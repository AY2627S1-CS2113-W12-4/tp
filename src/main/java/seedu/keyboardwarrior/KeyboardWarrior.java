package seedu.keyboardwarrior;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.io.IOException;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.UserInterruptException;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

/**
 * Runs the Keyboard Warrior application and its input-processing loop.
 */
public class KeyboardWarrior {
    private static final int TICK_MILLIS = 100;

    /**
     * Starts a background input reader and runs the game loop on the main thread.
     * The input reader may block while waiting for a line, but it only adds complete
     * commands to {@code inputQueue}. The main thread is the only thread that changes
     * the state machine, so timer updates and input handling cannot change a state concurrently.
     */
    public static void main(String[] args) {
        try (Terminal terminal = TerminalBuilder.builder().system(true).build()) {
            CountdownReader reader = new CountdownReader(terminal);
            reader.setOpt(LineReader.Option.DISABLE_EVENT_EXPANSION);
            Ui.attachTerminal(reader);
            runGame(reader);
        } catch (IOException exception) {
            System.err.println("Unable to open terminal: " + exception.getMessage());
        } finally {
            Ui.detachTerminal();
        }
    }

    /**
     * Keeps state changes on the main thread while JLine reads and edits player input.
     */
    private static void runGame(LineReader reader) {
        StateMachine stateMachine = new StateMachine();
        BlockingQueue<String> inputQueue = new LinkedBlockingQueue<>();

        stateMachine.start(new MainMenuState(stateMachine));
        Ui.showInputPrompt();

        Thread inputThread = new Thread(() -> {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    inputQueue.offer(reader.readLine("Your input: "));
                }
            } catch (EndOfFileException | UserInterruptException exception) {
                inputQueue.offer("exit");
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
                Ui.showInputPrompt();
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
        inputThread.interrupt();
    }
}
