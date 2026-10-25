package seedu.keyboardwarrior.typing;

/**
 * Calculates gross typing speed using five normalized UTF-16 code units per word.
 */
final class WpmCalculator {
    /**
     * Prevents instantiation of this utility class.
     */
    private WpmCalculator() {
    }

    /**
     * Returns gross WPM for a normalized character count and validated typing duration.
     *
     * @param characterCount normalized UTF-16 code-unit count, including spaces
     * @param elapsedSeconds elapsed typing duration in seconds
     * @return gross words per minute
     */
    static double calculate(int characterCount, double elapsedSeconds) {
        // Incorrect characters contribute to gross speed; accuracy measures correctness separately.
        return (characterCount / 5.0) / (elapsedSeconds / 60.0);
    }
}
