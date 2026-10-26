package seedu.keyboardwarrior.typing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Tests character accuracy, typing speed, retained word diagnostics and deadline metadata.
 */
class TypingEvaluatorTest {
    /**
     * Checks that a perfect submission produces full accuracy and no word errors.
     */
    @Test
    public void evaluateTyping_perfectSubmission_returnsFullAccuracy() {
        TypingResult result =
                TypingEvaluator.evaluateTyping("Strike the goblin", "Strike the goblin", 6.0, 10.0);

        assertEquals(34.0, result.getWpm(), 0.000001);
        assertEquals(100.0, result.getAccuracyPercent(), 0.000001);
        assertEquals(6.0, result.getElapsedSeconds(), 0.000001);
        assertEquals(10.0, result.getRequiredSeconds(), 0.000001);
        assertFalse(result.isTimeLimitExceeded());
        assertEquals(1.0, result.getTimeDamageMultiplier());
        assertTrue(result.getWordErrors().isEmpty());
    }

    /**
     * Checks typing metrics and positional feedback for an incorrect word.
     */
    @Test
    public void evaluateTyping_wrongWord_returnsMetricsAndError() {
        TypingResult result =
                TypingEvaluator.evaluateTyping("Strike the goblin", "Strike teh goblin", 6.0, 10.0);

        assertEquals(34.0, result.getWpm(), 0.000001);
        assertEquals(1500.0 / 17.0, result.getAccuracyPercent(), 0.000001);
        assertEquals(1, result.getWordErrors().size());
        assertWordError(result.getWordErrors().get(0), 2, "the", "teh", ErrorType.WRONG);
    }

    /**
     * Checks that fractional typing speed retains its precision.
     */
    @Test
    public void evaluateTyping_fractionalWpm_preservesPrecision() {
        TypingResult result =
                TypingEvaluator.evaluateTyping("Strike the goblin", "Strike the goblin", 60.0, 120.0);

        assertEquals(3.4, result.getWpm(), 0.000001);
    }

    /**
     * Checks that whitespace normalization treats equivalent passages equally.
     */
    @Test
    public void evaluateTyping_repeatedWhitespace_normalizesBothTexts() {
        TypingResult result = TypingEvaluator.evaluateTyping("\tStrike  the goblin\n",
                "  Strike \t the  goblin \r\n", 6.0, 10.0);

        assertEquals(34.0, result.getWpm(), 0.000001);
        assertEquals(100.0, result.getAccuracyPercent(), 0.000001);
        assertTrue(result.getWordErrors().isEmpty());
    }

    /**
     * Checks that an omitted final word is reported as missing.
     */
    @Test
    public void evaluateTyping_missingLastWord_reportsMissingWord() {
        TypingResult result =
                TypingEvaluator.evaluateTyping("Strike the goblin", "Strike the", 6.0, 10.0);

        assertEquals(20.0, result.getWpm(), 0.000001);
        assertEquals(1000.0 / 17.0, result.getAccuracyPercent(), 0.000001);
        assertEquals(1, result.getWordErrors().size());
        assertWordError(result.getWordErrors().get(0), 3, "goblin", "", ErrorType.MISSING);
    }

    /**
     * Checks that an extra final word lowers accuracy and is reported as extra.
     */
    @Test
    public void evaluateTyping_extraLastWord_penalizesAccuracy() {
        TypingResult result =
                TypingEvaluator.evaluateTyping("Strike the goblin", "Strike the goblin now", 6.0, 10.0);

        assertEquals(42.0, result.getWpm(), 0.000001);
        assertEquals(1700.0 / 21.0, result.getAccuracyPercent(), 0.000001);
        assertEquals(1, result.getWordErrors().size());
        assertWordError(result.getWordErrors().get(0), 4, "", "now", ErrorType.EXTRA);
    }

    /**
     * Checks character accuracy independently of positional errors after a missing middle word.
     */
    @Test
    public void evaluateTyping_missingMiddleWord_doesNotShiftCharacterAccuracy() {
        TypingResult result =
                TypingEvaluator.evaluateTyping("Strike the goblin", "Strike goblin", 6.0, 10.0);

        assertEquals(1300.0 / 17.0, result.getAccuracyPercent(), 0.000001);
        assertEquals(2, result.getWordErrors().size());
        assertWordError(result.getWordErrors().get(0), 2, "the", "goblin", ErrorType.WRONG);
        assertWordError(result.getWordErrors().get(1), 3, "goblin", "", ErrorType.MISSING);
    }

    /**
     * Checks that an empty answer has zero metrics and reports every expected word as missing.
     */
    @Test
    public void evaluateTyping_emptySubmission_reportsAllWordsMissing() {
        for (String typedText : List.of("", " \t\n")) {
            TypingResult result =
                    TypingEvaluator.evaluateTyping("Strike the goblin", typedText, 6.0, 10.0);

            assertEquals(0.0, result.getWpm(), 0.000001);
            assertEquals(0.0, result.getAccuracyPercent(), 0.000001);
            assertEquals(3, result.getWordErrors().size());
            assertWordError(result.getWordErrors().get(0), 1, "Strike", "", ErrorType.MISSING);
            assertWordError(result.getWordErrors().get(1), 2, "the", "", ErrorType.MISSING);
            assertWordError(result.getWordErrors().get(2), 3, "goblin", "", ErrorType.MISSING);
        }
    }

    /**
     * Checks that capitalization and punctuation differences affect accuracy and word feedback.
     */
    @Test
    public void evaluateTyping_caseAndPunctuationDifferences_countAsErrors() {
        TypingResult result =
                TypingEvaluator.evaluateTyping("Strike the goblin!", "strike the goblin", 6.0, 10.0);

        assertEquals(1600.0 / 18.0, result.getAccuracyPercent(), 0.000001);
        assertEquals(2, result.getWordErrors().size());
        assertWordError(result.getWordErrors().get(0), 1, "Strike", "strike", ErrorType.WRONG);
        assertWordError(result.getWordErrors().get(1), 3, "goblin!", "goblin", ErrorType.WRONG);
    }

    /**
     * Checks that repeated words do not obscure the position of a mismatch.
     */
    @Test
    public void evaluateTyping_repeatedWords_keepsErrorPosition() {
        TypingResult result =
                TypingEvaluator.evaluateTyping("go go home", "go no home", 6.0, 10.0);

        assertEquals(1, result.getWordErrors().size());
        assertWordError(result.getWordErrors().get(0), 2, "go", "no", ErrorType.WRONG);
    }

    /**
     * Checks that elapsed typing time must be positive and finite.
     */
    @Test
    public void evaluateTyping_invalidDuration_throwsException() {
        double[] invalidDurations = {0.0, -1.0, Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY};
        for (double elapsedSeconds : invalidDurations) {
            assertThrows(IllegalArgumentException.class,
                    () -> TypingEvaluator.evaluateTyping("Strike", "Strike", elapsedSeconds, 10.0));
        }
    }

    /**
     * Checks that evaluation rejects an empty or whitespace-only challenge.
     */
    @Test
    public void evaluateTyping_blankChallenge_throwsException() {
        for (String expectedText : List.of("", " \t\n")) {
            assertThrows(IllegalArgumentException.class,
                    () -> TypingEvaluator.evaluateTyping(expectedText, "Strike", 6.0, 10.0));
        }
    }

    /**
     * Checks that evaluation rejects null expected or submitted text.
     */
    @Test
    public void evaluateTyping_nullText_throwsException() {
        assertThrows(NullPointerException.class,
                () -> TypingEvaluator.evaluateTyping(null, "Strike", 6.0, 10.0));
        assertThrows(NullPointerException.class,
                () -> TypingEvaluator.evaluateTyping("Strike", null, 6.0, 10.0));
    }

    /**
     * Checks that callers cannot modify the result's word-error list.
     */
    @Test
    public void evaluateTyping_returnedErrors_cannotBeModified() {
        TypingResult result =
                TypingEvaluator.evaluateTyping("Strike", "strike", 6.0, 10.0);

        assertThrows(UnsupportedOperationException.class, () -> result.getWordErrors().clear());
    }

    /**
     * Checks that exceeding the deadline preserves typing metrics without a damage penalty.
     */
    @Test
    public void evaluateTyping_overTimeLimit_preservesPerformanceWithoutPenalty() {
        TypingResult result =
                TypingEvaluator.evaluateTyping("Strike the goblin", "Strike the goblin", 6.0, 5.0);

        assertTrue(result.isTimeLimitExceeded());
        assertEquals(1.0, result.getTimeDamageMultiplier());
        assertEquals(34.0, result.getWpm(), 0.000001);
        assertEquals(100.0, result.getAccuracyPercent(), 0.000001);
        assertTrue(result.getWordErrors().isEmpty());
    }

    /**
     * Checks that a submission exactly at the deadline is not marked late.
     */
    @Test
    public void evaluateTyping_exactTimeLimit_hasNoDamagePenalty() {
        TypingResult result =
                TypingEvaluator.evaluateTyping("Strike the goblin", "Strike the goblin", 6.0, 6.0);

        assertFalse(result.isTimeLimitExceeded());
        assertEquals(1.0, result.getTimeDamageMultiplier());
    }

    /**
     * Checks that a submission just beyond the deadline is late without a damage penalty.
     */
    @Test
    public void evaluateTyping_justOverTimeLimit_hasNoDamagePenalty() {
        TypingResult result =
                TypingEvaluator.evaluateTyping("Strike the goblin", "Strike the goblin", Math.nextUp(6.0), 6.0);

        assertTrue(result.isTimeLimitExceeded());
        assertEquals(1.0, result.getTimeDamageMultiplier());
    }

    /**
     * Checks that late attempts retain positional word feedback.
     */
    @Test
    public void evaluateTyping_overTimeLimit_preservesWordFeedback() {
        TypingResult result =
                TypingEvaluator.evaluateTyping("Strike the goblin", "Strike teh goblin", 6.0, 5.0);

        assertTrue(result.isTimeLimitExceeded());
        assertEquals(1.0, result.getTimeDamageMultiplier());
        assertEquals(1500.0 / 17.0, result.getAccuracyPercent(), 0.000001);
        assertEquals(1, result.getWordErrors().size());
        assertWordError(result.getWordErrors().get(0), 2, "the", "teh", ErrorType.WRONG);
    }

    /**
     * Checks that the required challenge duration must be positive and finite.
     */
    @Test
    public void evaluateTyping_invalidRequiredTime_throwsException() {
        double[] invalidTimes = {0.0, -1.0, Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY};
        for (double requiredSeconds : invalidTimes) {
            assertThrows(IllegalArgumentException.class,
                    () -> TypingEvaluator.evaluateTyping("Strike", "Strike", 6.0, requiredSeconds));
        }
    }

    /**
     * Checks that one substituted character counts as one edit.
     */
    @Test
    public void evaluateTyping_singleCharacterSubstitution_countsOneEdit() {
        TypingResult result = TypingEvaluator.evaluateTyping("cat", "cut", 6.0, 10.0);

        assertEquals(200.0 / 3.0, result.getAccuracyPercent(), 0.000001);
    }

    /**
     * Checks that an inserted character uses the longer passage as the accuracy denominator.
     */
    @Test
    public void evaluateTyping_singleCharacterInsertion_usesLongerTextAsDenominator() {
        TypingResult result = TypingEvaluator.evaluateTyping("cat", "cart", 6.0, 10.0);

        assertEquals(75.0, result.getAccuracyPercent(), 0.000001);
    }

    /**
     * Checks that one deleted character counts as one edit.
     */
    @Test
    public void evaluateTyping_singleCharacterDeletion_countsOneEdit() {
        TypingResult result = TypingEvaluator.evaluateTyping("cart", "cat", 6.0, 10.0);

        assertEquals(75.0, result.getAccuracyPercent(), 0.000001);
    }

    /**
     * Checks that an unfinished suffix still gives credit for the characters already typed.
     */
    @Test
    public void evaluateTyping_missingSuffix_scoresCharactersAlreadyTyped() {
        TypingResult result = TypingEvaluator.evaluateTyping("hello world", "hello", 6.0, 10.0);

        assertEquals(500.0 / 11.0, result.getAccuracyPercent(), 0.000001);
    }

    /**
     * Checks that completely different characters produce zero accuracy.
     */
    @Test
    public void evaluateTyping_allCharactersDifferent_returnsZeroAccuracy() {
        TypingResult result = TypingEvaluator.evaluateTyping("cat", "dog", 6.0, 10.0);

        assertEquals(0.0, result.getAccuracyPercent(), 0.000001);
    }

    /**
     * Checks code-point accuracy and UTF-16-based speed for supplementary Unicode characters.
     */
    @Test
    public void evaluateTyping_supplementaryUnicodeCharacter_countsOneCharacterEdit() {
        TypingResult result = TypingEvaluator.evaluateTyping("a\uD83D\uDE00b", "a\uD83D\uDE80b", 6.0, 10.0);

        assertEquals(200.0 / 3.0, result.getAccuracyPercent(), 0.000001);
        assertEquals(8.0, result.getWpm(), 0.000001);
    }

    /**
     * Checks the position, words and category of a reported mismatch.
     *
     * @param error reported mismatch to verify
     * @param position one-based word position
     * @param expectedWord expected word, or an empty string when none exists
     * @param typedWord submitted word, or an empty string when none exists
     * @param type category of word mismatch
     */
    private static void assertWordError(WordError error, int position, String expectedWord,
            String typedWord, ErrorType type) {
        assertEquals(position, error.getPosition());
        assertEquals(expectedWord, error.getExpectedWord());
        assertEquals(typedWord, error.getTypedWord());
        assertEquals(type, error.getType());
    }
}
