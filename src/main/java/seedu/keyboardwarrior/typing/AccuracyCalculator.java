package seedu.keyboardwarrior.typing;

/**
 * Calculates the percentage of matching word positions in a typing attempt.
 */
final class AccuracyCalculator {
    private AccuracyCalculator() {
    }

    /**
     * Returns word accuracy using the positive comparison count supplied by the evaluator.
     */
    static double calculate(int correctWordCount, int comparisonCount) {
        return correctWordCount * 100.0 / comparisonCount;
    }
}
