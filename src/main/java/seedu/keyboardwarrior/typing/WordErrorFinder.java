package seedu.keyboardwarrior.typing;

import java.util.ArrayList;
import java.util.List;

/**
 * Identifies wrong, missing and extra words through positional comparison.
 */
final class WordErrorFinder {
    private WordErrorFinder() {
    }

    /**
     * Returns mismatches in position order for the evaluator's normalized word lists.
     */
    static List<WordError> find(String[] expectedWords, String[] typedWords) {
        int comparisonCount = Math.max(expectedWords.length, typedWords.length);
        List<WordError> wordErrors = new ArrayList<>();
        for (int index = 0; index < comparisonCount; index++) {
            if (index >= expectedWords.length) {
                wordErrors.add(new WordError(index + 1, "", typedWords[index], ErrorType.EXTRA));
            } else if (index >= typedWords.length) {
                wordErrors.add(new WordError(index + 1, expectedWords[index], "", ErrorType.MISSING));
            } else if (!expectedWords[index].equals(typedWords[index])) {
                wordErrors.add(new WordError(index + 1, expectedWords[index], typedWords[index], ErrorType.WRONG));
            }
        }
        return wordErrors;
    }
}
