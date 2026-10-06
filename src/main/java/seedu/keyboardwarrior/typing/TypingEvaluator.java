package seedu.keyboardwarrior.typing;

import java.util.List;
import java.util.Objects;

/**
 * Coordinates speed, accuracy and word-error evaluation for one typing attempt.
 */
public final class TypingEvaluator {
    private TypingEvaluator() {
    }

    /**
     * Evaluates the final submitted text using gross WPM and word accuracy.
     * Leading and trailing whitespace is removed, and repeated spaces, tabs and line breaks
     * are collapsed into a single space. Words are compared at corresponding positions,
     * with capitalization and punctuation preserved.
     *
     * @param expectedText Nonempty challenge text.
     * @param typedText Player's submitted text; an empty submission is allowed.
     * @param elapsedSeconds Positive, finite typing duration supplied by the timer component.
     * @return Typing metrics and incorrect, missing or extra words in position order.
     * @throws NullPointerException If either text is null.
     * @throws IllegalArgumentException If the challenge is blank or the duration is invalid.
     */
    public static TypingResult evaluateTyping(String expectedText, String typedText, double elapsedSeconds) {
        Objects.requireNonNull(expectedText, "Expected text must not be null");
        Objects.requireNonNull(typedText, "Typed text must not be null");
        if (!Double.isFinite(elapsedSeconds) || elapsedSeconds <= 0) {
            throw new IllegalArgumentException("Elapsed seconds must be positive and finite");
        }

        String normalizedExpected = normalizeText(expectedText);
        String normalizedTyped = normalizeText(typedText);
        if (normalizedExpected.isEmpty()) {
            throw new IllegalArgumentException("Expected text must contain at least one word");
        }

        String[] expectedWords = normalizedExpected.split(" ");
        String[] typedWords = normalizedTyped.isEmpty() ? new String[0] : normalizedTyped.split(" ");
        int comparisonCount = Math.max(expectedWords.length, typedWords.length);
        List<WordError> wordErrors = WordErrorFinder.find(expectedWords, typedWords);
        int correctWords = comparisonCount - wordErrors.size();
        double wpm = WpmCalculator.calculate(normalizedTyped.length(), elapsedSeconds);
        double accuracyPercent = AccuracyCalculator.calculate(correctWords, comparisonCount);
        return new TypingResult(wpm, accuracyPercent, elapsedSeconds, wordErrors);
    }

    /**
     * Returns text with leading/trailing whitespace removed and internal whitespace collapsed.
     */
    private static String normalizeText(String text) {
        return text.trim().replaceAll("\\s+", " ");
    }
}
