package seedu.keyboardwarrior.typing;

/**
 * Calculates gross typing speed using five submitted characters per word.
 */
final class WpmCalculator {
    private WpmCalculator() {
    }

    /**
     * Returns gross WPM for a normalized character count and validated typing duration.
     */
    static double calculate(int characterCount, double elapsedSeconds) {
        // Incorrect characters contribute to gross speed; accuracy measures correctness separately.
        return (characterCount / 5.0) / (elapsedSeconds / 60.0);
    }
}
