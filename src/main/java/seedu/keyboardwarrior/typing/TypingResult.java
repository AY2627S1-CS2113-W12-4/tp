package seedu.keyboardwarrior.typing;

import java.util.List;
import java.util.Objects;

/**
 * Stores immutable typing metrics, the time limit and word errors for the CLI and game components.
 */
public final class TypingResult {
    /** Gross words per minute, including incorrect submitted characters. */
    private final double wpm;
    /** Percentage of matching word positions, from zero to one hundred. */
    private final double accuracyPercent;
    private final double elapsedSeconds;
    /** Maximum typing duration before the twenty-percent damage penalty applies. */
    private final double requiredSeconds;
    private final List<WordError> wordErrors;

    /**
     * Stores evaluated metrics and a defensive copy of the word errors.
     *
     * @throws IllegalArgumentException if WPM is negative or nonfinite, accuracy is outside
     *     zero to one hundred, or either duration is not positive and finite
     * @throws NullPointerException if the error list or one of its entries is null
     */
    TypingResult(double wpm, double accuracyPercent, double elapsedSeconds,
            double requiredSeconds, List<WordError> wordErrors) {
        if (!Double.isFinite(wpm) || wpm < 0) {
            throw new IllegalArgumentException("WPM must be nonnegative and finite");
        }
        if (!Double.isFinite(accuracyPercent) || accuracyPercent < 0 || accuracyPercent > 100) {
            throw new IllegalArgumentException("Accuracy must be finite and between zero and one hundred");
        }
        if (!Double.isFinite(elapsedSeconds) || elapsedSeconds <= 0) {
            throw new IllegalArgumentException("Elapsed seconds must be positive and finite");
        }
        if (!Double.isFinite(requiredSeconds) || requiredSeconds <= 0) {
            throw new IllegalArgumentException("Required seconds must be positive and finite");
        }
        this.wpm = wpm;
        this.accuracyPercent = accuracyPercent;
        this.elapsedSeconds = elapsedSeconds;
        this.requiredSeconds = requiredSeconds;
        this.wordErrors = List.copyOf(Objects.requireNonNull(wordErrors, "Word errors must not be null"));
    }

    /**
     * Returns the gross typing speed.
     *
     * @return words per minute
     */
    public double getWpm() {
        return wpm;
    }

    /**
     * Returns the percentage of correctly matched word positions.
     *
     * @return accuracy from zero to one hundred
     */
    public double getAccuracyPercent() {
        return accuracyPercent;
    }

    /**
     * Returns the duration of the typing attempt.
     *
     * @return elapsed seconds
     */
    public double getElapsedSeconds() {
        return elapsedSeconds;
    }

    /**
     * Returns the maximum duration that avoids a combat damage penalty.
     *
     * @return time limit in seconds
     */
    public double getRequiredSeconds() {
        return requiredSeconds;
    }

    /**
     * Returns whether the attempt exceeded its time limit.
     * Finishing exactly at the limit does not incur a penalty.
     */
    public boolean isTimeLimitExceeded() {
        return elapsedSeconds > requiredSeconds;
    }

    /**
     * Returns the factor applied to WPM/accuracy damage for this attempt's timing.
     * Late attempts retain eighty percent of their calculated damage.
     *
     * @return 0.8 when the limit was exceeded, otherwise 1.0
     */
    public double getTimeDamageMultiplier() {
        return isTimeLimitExceeded() ? 0.8 : 1.0;
    }

    /**
     * Returns an immutable list of mismatched word positions.
     *
     * @return word errors in position order
     */
    public List<WordError> getWordErrors() {
        return wordErrors;
    }
}
