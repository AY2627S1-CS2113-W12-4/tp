package seedu.keyboardwarrior.typing;

import java.util.List;
import java.util.Objects;

/**
 * Coordinates speed, accuracy, word errors and the time limit for one typing attempt.
 */
public final class TypingEvaluator {
    private TypingEvaluator() {
    }

    /**
     * Evaluates the final submitted text using gross WPM, character accuracy and a time limit.
     * Leading and trailing whitespace is removed, and repeated spaces, tabs and line breaks
     * are collapsed into a single space. Accuracy is one minus the Levenshtein character-edit
     * distance divided by the longer normalized text's character count, as a percentage.
     * Capitalization and punctuation are preserved. Detailed word errors remain positional
     * diagnostics and do not determine accuracy. Exceeding the required time does not reduce damage.
     *
     * @param expectedText Nonempty challenge text.
     * @param typedText Player's submitted text; an empty submission is allowed.
     * @param elapsedSeconds Positive, finite typing duration supplied by the timer component.
     * @param requiredSeconds Positive, finite time limit for the challenge, in seconds.
     * @return Typing metrics, word errors in position order and the time limit.
     * @throws NullPointerException If either text is null.
     * @throws IllegalArgumentException If the challenge is blank or either time is invalid.
     */
    public static TypingResult evaluateTyping(String expectedText, String typedText,
            double elapsedSeconds, double requiredSeconds) {
        Objects.requireNonNull(expectedText, "Expected text must not be null");
        Objects.requireNonNull(typedText, "Typed text must not be null");
        if (!Double.isFinite(elapsedSeconds) || elapsedSeconds <= 0) {
            throw new IllegalArgumentException("Elapsed seconds must be positive and finite");
        }
        if (!Double.isFinite(requiredSeconds) || requiredSeconds <= 0) {
            throw new IllegalArgumentException("Required seconds must be positive and finite");
        }

        String normalizedExpected = normalizeText(expectedText);
        String normalizedTyped = normalizeText(typedText);
        if (normalizedExpected.isEmpty()) {
            throw new IllegalArgumentException("Expected text must contain at least one word");
        }

        String[] expectedWords = normalizedExpected.split(" ");
        String[] typedWords = normalizedTyped.isEmpty() ? new String[0] : normalizedTyped.split(" ");
        List<WordError> wordErrors = WordErrorFinder.find(expectedWords, typedWords);
        double wpm = WpmCalculator.calculate(normalizedTyped.length(), elapsedSeconds);
        double accuracyPercent = AccuracyCalculator.calculate(normalizedExpected, normalizedTyped);
        return new TypingResult(wpm, accuracyPercent, elapsedSeconds, requiredSeconds, wordErrors);
    }

    /**
     * Returns text with leading/trailing whitespace removed and internal whitespace collapsed.
     */
    private static String normalizeText(String text) {
        return text.trim().replaceAll("\\s+", " ");
    }
}
