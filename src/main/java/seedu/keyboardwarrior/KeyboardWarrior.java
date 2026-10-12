package seedu.keyboardwarrior;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.io.IOException;
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
    /** Logs terminal failures with their causes for troubleshooting. */
    private static final Logger LOGGER = Logger.getLogger(KeyboardWarrior.class.getName());
    /** Delay between game-loop updates. */
    private static final int TICK_MILLIS = 100;

    /**
     * Opens the terminal, starts a background input reader and runs the game loop
     * on the main thread.
     * The input reader may block while waiting for a line, but it only adds
     * complete
     * commands to {@code inputQueue}. The main thread is the only thread that
     * changes
     * the state machine, so timer updates and input handling cannot change a state
     * concurrently.
     *
     * @param args command-line arguments; currently unused
     */
    public static void main(String[] args) {
        TerminalBuilder terminalBuilder = TerminalBuilder.builder().system(true);
        if (System.console() != null && System.getProperty("os.name").startsWith("Windows")) {
            // Some launchers set TERM=dumb even for a console. Native mode delivers each
            // keystroke to JLine, which is required to score an answer before Enter.
            terminalBuilder.type("windows-vtp");
        }
        try (Terminal terminal = terminalBuilder.build()) {
            CountdownReader reader = new CountdownReader(terminal);
            if (!reader.supportsLiveDisplay()) {
                LOGGER.fine(() -> "Unsupported interactive console: " + terminal.getType());
                System.err.println("This console cannot display the live timer or capture typing before Enter.");
                String launcher = System.getProperty("os.name").startsWith("Windows")
                        ? ".\\run-game.bat" : "sh ./run-game.sh";
                System.err.println("Open IntelliJ's Terminal or a system terminal and run: " + launcher);
                return;
            }
            reader.setOpt(LineReader.Option.DISABLE_EVENT_EXPANSION);
            StateUi.attachTerminal(reader);
            runGame(reader);
        } catch (IOException exception) {
            LOGGER.log(Level.WARNING, "Terminal setup or cleanup failed", exception);
            System.err.println("Unable to open terminal: " + exception.getMessage());
        } catch (RuntimeException | AssertionError exception) {
            LOGGER.log(Level.SEVERE, "Unexpected game failure", exception);
            throw exception;
        } finally {
            StateUi.detachTerminal();
        }
    }

    /**
     * Keeps state changes on the main thread while JLine reads and edits player
     * input.
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
                // A failed reader must stop the game instead of leaving the timer loop running forever.
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
