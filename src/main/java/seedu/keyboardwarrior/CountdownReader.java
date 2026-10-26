package seedu.keyboardwarrior;

import org.jline.reader.LineReader;
import org.jline.reader.impl.LineReaderImpl;
import org.jline.terminal.Terminal;
import org.jline.utils.InfoCmp.Capability;

/**
 * Renders the countdown and editable input together under JLine's display lock.
 */
public class CountdownReader extends LineReaderImpl {
    /**
     * Seconds shown in the prompt, or {@code null} when no countdown is active.
     */
    private Integer remainingSeconds;
    /**
     * Target passage displayed below the timer while a challenge is active.
     */
    private String challengeText;
    private int timeLimitSeconds;

    /**
     * Creates a reader whose prompt is refreshed whenever a new input line starts.
     *
     * @param terminal terminal used for input and output
     * @throws java.io.IOException if reader initialization encounters an I/O failure
     */
    public CountdownReader(Terminal terminal) throws java.io.IOException {
        super(terminal);
        getWidgets().put(LineReader.CALLBACK_INIT, () -> {
            setPrompt(countdownPrompt());
            return true;
        });
    }

    /**
     * Reports whether this console can redraw the timer above an editable answer.
     * Redirected Gradle/IDE consoles may supply basic input and output without cursor control.
     * Declaring an ANSI terminal type alone cannot restore a native terminal connection.
     *
     * @return true when terminal dimensions and cursor movement are available
     */
    public boolean supportsLiveDisplay() {
        Terminal terminal = getTerminal();
        return !Terminal.TYPE_DUMB.equals(terminal.getType())
                && !Terminal.TYPE_DUMB_COLOR.equals(terminal.getType())
                && terminal.getWidth() > 0 && terminal.getHeight() > 0
                && terminal.getStringCapability(Capability.cursor_up) != null
                && terminal.getStringCapability(Capability.cursor_left) != null;
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
            if (seconds == null) {
                challengeText = null;
            }
            refreshPrompt();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Displays the live timer above the instruction, passage and editable answer.
     *
     * @param text target passage displayed above the editable answer
     * @param seconds seconds to display
     */
    public void showTypingChallenge(String text, int seconds) {
        lock.lock();
        try {
            challengeText = text;
            timeLimitSeconds = seconds;
            remainingSeconds = seconds;
            refreshPrompt();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Clears the editing line without retaining its text.
     */
    public void clearInput() {
        takeCurrentInput();
    }

    /**
     * Captures and clears the current passage atomically when the timer expires.
     * The display lock prevents typing from changing the passage during capture.
     *
     * @return the text currently in the editable input line, without requiring Enter
     */
    public String takeCurrentInput() {
        lock.lock();
        try {
            String typedText = getBuffer().toString();
            getBuffer().clear();
            if (isReading() && supportsLiveDisplay()) {
                redisplay();
                getTerminal().flush();
            }
            return typedText;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Refreshes the complete challenge display while the display lock is held.
     */
    private void refreshPrompt() {
        if (!supportsLiveDisplay()) {
            return;
        }
        setPrompt(countdownPrompt());
        if (isReading()) {
            redisplay();
            getTerminal().flush();
        }
    }

    /**
     * Builds the prompt for the current countdown and terminal capabilities.
     *
     * @return ordinary input prompt or the complete countdown and challenge prompt
     */
    private String countdownPrompt() {
        if (remainingSeconds == null || !supportsLiveDisplay()) {
            return "Your input: ";
        }
        String challengeDisplay = challengeText == null ? ""
                : "Time limit: " + timeLimitSeconds + " seconds\n"
                // JLine expands percent markers in prompts, so passage symbols must be escaped.
                + "Type the following text and press Enter to attack:\n"
                + challengeText.replace("%", "%%") + "\n";
        return "Time remaining: " + remainingSeconds + "s\n" + challengeDisplay + "Your input: ";
    }
}
