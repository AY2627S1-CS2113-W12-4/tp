package seedu.keyboardwarrior.typing;

import java.util.List;
import java.util.Objects;

/**
 * Stores immutable typing metrics, the time limit and word errors for the CLI and game components.
 */
public final class TypingResult {
    /**
     * Gross words per minute, including incorrect submitted characters.
     */
    private final double wpm;
    /**
     * Character accuracy based on normalized edit distance, from zero to one hundred.
     */
    private final double accuracyPercent;
    private final double elapsedSeconds;
    /**
     * Duration allowed before the game automatically submits the current passage.
     */
    private final double requiredSeconds;
    private final List<WordError> wordErrors;

    /**
     * Stores evaluated metrics and a defensive copy of the word errors.
     *
     * @param wpm nonnegative, finite gross words per minute
     * @param accuracyPercent finite character accuracy from zero to one hundred
     * @param elapsedSeconds positive, finite typing duration in seconds
     * @param requiredSeconds positive, finite challenge time limit in seconds
     * @param wordErrors word mismatches in position order
     * @throws IllegalArgumentException if WPM is negative or nonfinite, accuracy is nonfinite or outside
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
     * Returns character accuracy after accounting for substitutions, insertions and deletions.
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
     * Returns the duration allowed before automatic submission.
     *
     * @return time limit in seconds
     */
    public double getRequiredSeconds() {
        return requiredSeconds;
    }

    /**
     * Returns whether the attempt exceeded its time limit.
     * Timing is retained for feedback; exceeding the limit does not reduce damage.
     *
     * @return {@code true} when elapsed time is strictly greater than the required duration
     */
    public boolean isTimeLimitExceeded() {
        return elapsedSeconds > requiredSeconds;
    }

    /**
     * Returns the neutral timing factor retained for compatibility with existing combat callers.
     * Damage depends on typing performance without an additional deadline penalty.
     *
     * @return 1.0 for every attempt
     */
    public double getTimeDamageMultiplier() {
        return 1.0;
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
