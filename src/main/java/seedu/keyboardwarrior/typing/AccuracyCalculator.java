package seedu.keyboardwarrior.typing;

/**
 * Calculates character accuracy without counting later characters as wrong after one insertion or deletion.
 */
final class AccuracyCalculator {
    /**
     * Prevents instantiation of this utility class.
     */
    private AccuracyCalculator() {
    }

    /**
     * Returns accuracy from the minimum number of character edits between normalized texts.
     * Unicode code points are compared so a supplementary character counts as one edit.
     * The evaluator guarantees a nonempty expected text.
     *
     * @param expectedText normalized, nonempty target text
     * @param typedText normalized submission, which may be empty
     * @return calculated accuracy percentage
     */
    static double calculate(String expectedText, String typedText) {
        int[] expectedCharacters = expectedText.codePoints().toArray();
        int[] typedCharacters = typedText.codePoints().toArray();
        int comparisonCount = Math.max(expectedCharacters.length, typedCharacters.length);
        assert comparisonCount > 0 : "A validated challenge must contain a character";
        int distance = editDistance(expectedCharacters, typedCharacters);
        assert distance >= 0 && distance <= comparisonCount : "Character edits must fit the compared text";
        return 100.0 * (1.0 - distance / (double) comparisonCount);
    }

    /**
     * Computes Levenshtein distance using two rows rather than a full character-by-character table.
     *
     * @param first first sequence of Unicode code points
     * @param second second sequence of Unicode code points
     * @return minimum number of insertions, deletions and substitutions
     */
    private static int editDistance(int[] first, int[] second) {
        // Keep the shorter passage in the columns to bound the working memory.
        if (first.length < second.length) {
            return editDistance(second, first);
        }
        int[] previous = new int[second.length + 1];
        int[] current = new int[second.length + 1];
        for (int column = 0; column <= second.length; column++) {
            previous[column] = column;
        }
        for (int row = 1; row <= first.length; row++) {
            current[0] = row;
            for (int column = 1; column <= second.length; column++) {
                int substitution = previous[column - 1] + (first[row - 1] == second[column - 1] ? 0 : 1);
                current[column] = Math.min(substitution, Math.min(previous[column] + 1, current[column - 1] + 1));
            }
            int[] reusable = previous;
            previous = current;
            current = reusable;
        }
        return previous[second.length];
    }
}
