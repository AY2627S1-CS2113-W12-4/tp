package seedu.keyboardwarrior.typing;

import java.util.List;

/**
 * Stores immutable typing metrics, the time limit and word errors for the CLI and game components.
 */
public final class TypingResult {
    /** Gross words per minute, including incorrect submitted characters. */
    private final double wpm;
    /** Percentage of matching word positions, from zero to one hundred. */
    private final double accuracyPercent;
    private final double elapsedSeconds;
    /** Maximum typing duration that permits damage, including the exact limit. */
    private final double requiredSeconds;
    private final List<WordError> wordErrors;

    /**
     * Stores evaluated metrics and a defensive copy of the word errors.
     */
    TypingResult(double wpm, double accuracyPercent, double elapsedSeconds,
            double requiredSeconds, List<WordError> wordErrors) {
        this.wpm = wpm;
        this.accuracyPercent = accuracyPercent;
        this.elapsedSeconds = elapsedSeconds;
        this.requiredSeconds = requiredSeconds;
        this.wordErrors = List.copyOf(wordErrors);
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
     * Returns the maximum duration that permits combat damage.
     *
     * @return time limit in seconds
     */
    public double getRequiredSeconds() {
        return requiredSeconds;
    }

    /**
     * Returns whether the attempt meets the time limit for dealing damage.
     * Attempts that exceed the limit deal no damage, even if every word is correct.
     * Combat code determines the damage amount for attempts within the limit.
     */
    public boolean canDealDamage() {
        return elapsedSeconds <= requiredSeconds;
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
