package seedu.keyboardwarrior;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.io.BufferedReader;
import java.io.Console;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.UserInterruptException;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import seedu.keyboardwarrior.state.StateMachine;
import seedu.keyboardwarrior.state.MainMenuState;
import seedu.keyboardwarrior.ui.StateUi;

/**
 * Runs the Keyboard Warrior application and its input-processing loop.
 */
public class KeyboardWarrior {
    /**
     * Logs terminal failures with their causes for troubleshooting.
     */
    private static final Logger LOGGER = Logger.getLogger(KeyboardWarrior.class.getName());
    /**
     * Delay between game-loop updates.
     */
    private static final int TICK_MILLIS = 100;

    /**
     * Uses plain line input for redirected consoles, or opens a live terminal
     * and runs the game loop on the main thread.
     * In live mode the background reader may block while waiting for a line,
     * but it only adds complete commands to the input queue. The main thread is the only thread
     * that changes the state machine, so timer updates and input handling cannot
     * change a state concurrently.
     *
     * @param args command-line arguments; currently unused
     */
    public static void main(String[] args) {
        try {
            Console console = System.console();
            if (console == null || !console.isTerminal()) {
                // Captured input/output is valid for a CLI, but cannot support native cursor control.
                // Choose this path before JLine attempts to load or initialize a system terminal.
                runPlainGame(new InputStreamReader(System.in));
                return;
            }
            Terminal systemTerminal = openSystemTerminal();
            if (systemTerminal == null) {
                runPlainGame(console.reader());
                return;
            }
            try (Terminal terminal = systemTerminal) {
                CountdownReader reader = new CountdownReader(terminal);
                if (!reader.supportsLiveDisplay()) {
                    LOGGER.fine(() -> "Using plain input for terminal: " + terminal.getType());
                    runPlainGame(terminal.reader());
                    return;
                }
                reader.setOpt(LineReader.Option.DISABLE_EVENT_EXPANSION);
                StateUi.attachTerminal(reader);
                runGame(reader);
            }
        } catch (IOException exception) {
            LOGGER.log(Level.WARNING, "Console input or terminal cleanup failed", exception);
            System.err.println("Unable to read console: " + exception.getMessage());
        } catch (RuntimeException | AssertionError exception) {
            LOGGER.log(Level.SEVERE, "Unexpected game failure", exception);
            throw exception;
        } finally {
            StateUi.detachTerminal();
        }
    }

    /**
     * Opens native terminal support when available without making it a startup requirement.
     *
     * @return system terminal, or null when terminal initialization is unavailable
     */
    private static Terminal openSystemTerminal() {
        TerminalBuilder builder = TerminalBuilder.builder().system(true).dumb(true);
        if (System.getProperty("os.name").startsWith("Windows")) {
            // Some launchers set TERM=dumb even for a real Windows console.
            builder.type("windows-vtp");
        }
        try {
            return builder.build();
        } catch (IOException | IllegalStateException exception) {
            LOGGER.log(Level.FINE, "Native terminal unavailable; using plain console input", exception);
            return null;
        }
    }

    /**
     * Runs the same states using complete lines when cursor control is unavailable.
     * A line-buffered console cannot deliver unfinished typing, so expiry is checked
     * when Enter submits the passage instead of scoring an empty buffer on a timer tick.
     * The supplied stream belongs to the JVM or terminal and is not closed here.
     *
     * @param input console character stream
     * @throws IOException if the console input fails
     */
    private static void runPlainGame(Reader input) throws IOException {
        StateUi.detachTerminal();
        StateUi.printLine("Live countdown is unavailable in this console. Press Enter to submit each passage.");
        BufferedReader reader = new BufferedReader(input);
        StateMachine stateMachine = new StateMachine();
        stateMachine.start(new MainMenuState(stateMachine));
        while (stateMachine.isRunning()) {
            StateUi.showInputPrompt();
            String line = reader.readLine();
            if (line == null) {
                stateMachine.stop();
                break;
            }
            stateMachine.handleInput(line);
        }
    }

    /**
     * Keeps state changes on the main thread while JLine reads and edits player
     * input.
     *
     * @param reader terminal reader supplying complete input lines
     */
    private static void runGame(LineReader reader) {
        StateMachine stateMachine = new StateMachine();
        BlockingQueue<String> inputQueue = new LinkedBlockingQueue<>();

        stateMachine.start(new MainMenuState(stateMachine));
        StateUi.showInputPrompt();

        Thread inputThread = new Thread(() -> {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    inputQueue.offer(reader.readLine("Your input: "));
                }
            } catch (EndOfFileException | UserInterruptException exception) {
                LOGGER.fine("Terminal input ended; requesting game exit");
                inputQueue.offer("exit");
            } catch (RuntimeException exception) {
                // A failed reader must stop the game instead of leaving the timer loop running
                // forever.
                LOGGER.log(Level.SEVERE, "Terminal input reader failed", exception);
                inputQueue.offer("exit");
            }
        }, "keyboard-warrior-input");
        inputThread.setDaemon(true);
        inputThread.start();

        try {
            while (stateMachine.isRunning()) {
                String input;
                while ((input = inputQueue.poll()) != null) {
                    stateMachine.handleInput(input);
                    if (!stateMachine.isRunning()) {
                        break;
                    }
                    StateUi.showInputPrompt();
                }
                if (!stateMachine.isRunning()) {
                    break;
                }
                stateMachine.update();
                try {
                    Thread.sleep(TICK_MILLIS);
                } catch (InterruptedException exception) {
                    LOGGER.fine("Game loop interrupted");
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        } finally {
            inputThread.interrupt();
        }
    }
}
