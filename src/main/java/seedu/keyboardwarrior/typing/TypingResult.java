package seedu.keyboardwarrior.typing;

import java.util.List;

/**
 * Stores immutable typing metrics and word errors for the CLI and other game components.
 */
public final class TypingResult {
    /** Gross words per minute, including incorrect submitted characters. */
    private final double wpm;
    /** Percentage of matching word positions, from zero to one hundred. */
    private final double accuracyPercent;
    private final double elapsedSeconds;
    private final List<WordError> wordErrors;

    /**
     * Stores evaluated metrics and a defensive copy of the word errors.
     */
    TypingResult(double wpm, double accuracyPercent, double elapsedSeconds, List<WordError> wordErrors) {
        this.wpm = wpm;
        this.accuracyPercent = accuracyPercent;
        this.elapsedSeconds = elapsedSeconds;
        this.wordErrors = List.copyOf(wordErrors);
    }

    public double getWpm() {
        return wpm;
    }

    public double getAccuracyPercent() {
        return accuracyPercent;
    }

    public double getElapsedSeconds() {
        return elapsedSeconds;
    }

    public List<WordError> getWordErrors() {
        return wordErrors;
    }
}
