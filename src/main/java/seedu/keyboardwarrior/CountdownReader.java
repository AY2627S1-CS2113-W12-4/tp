package seedu.keyboardwarrior;

import org.jline.reader.LineReader;
import org.jline.reader.impl.LineReaderImpl;
import org.jline.terminal.Terminal;

/**
 * Renders the countdown and editable input together under JLine's display lock.
 */
public class CountdownReader extends LineReaderImpl {
    private Integer remainingSeconds;

    /**
     * Creates a reader whose prompt is refreshed whenever a new input line starts.
     */
    public CountdownReader(Terminal terminal) throws java.io.IOException {
        super(terminal);
        getWidgets().put(LineReader.CALLBACK_INIT, () -> {
            setPrompt(countdownPrompt());
            return true;
        });
    }

    /**
     * Updates the prompt atomically with respect to typing and cursor movement.
     *
     * @param seconds seconds left, or null to remove the countdown
     */
    public void setCountdown(Integer seconds) {
        lock.lock();
        try {
            remainingSeconds = seconds;
            setPrompt(countdownPrompt());
            if (isReading()) {
                redisplay();
                getTerminal().flush();
            }
        } finally {
            lock.unlock();
        }
    }

    private String countdownPrompt() {
        return remainingSeconds == null ? "Your input: "
                : "Time remaining: " + remainingSeconds + "s\nYour input: ";
    }
}
