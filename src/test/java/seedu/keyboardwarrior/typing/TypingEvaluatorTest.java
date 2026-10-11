package seedu.keyboardwarrior.typing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Tests typing metrics, positional word errors and the damage time limit.
 */
class TypingEvaluatorTest {
    @Test
    public void evaluateTyping_perfectSubmission_returnsFullAccuracy() {
        TypingResult result =
                TypingEvaluator.evaluateTyping("Strike the goblin", "Strike the goblin", 6.0, 10.0);

        assertEquals(34.0, result.getWpm(), 0.000001);
        assertEquals(100.0, result.getAccuracyPercent(), 0.000001);
        assertEquals(6.0, result.getElapsedSeconds(), 0.000001);
        assertEquals(10.0, result.getRequiredSeconds(), 0.000001);
        assertTrue(result.canDealDamage());
        assertTrue(result.getWordErrors().isEmpty());
    }

    @Test
    public void evaluateTyping_wrongWord_returnsMetricsAndError() {
        TypingResult result =
                TypingEvaluator.evaluateTyping("Strike the goblin", "Strike teh goblin", 6.0, 10.0);

        assertEquals(34.0, result.getWpm(), 0.000001);
        assertEquals(200.0 / 3.0, result.getAccuracyPercent(), 0.000001);
        assertEquals(1, result.getWordErrors().size());
        assertWordError(result.getWordErrors().get(0), 2, "the", "teh", ErrorType.WRONG);
    }

    @Test
    public void evaluateTyping_fractionalWpm_preservesPrecision() {
        TypingResult result =
                TypingEvaluator.evaluateTyping("Strike the goblin", "Strike the goblin", 60.0, 120.0);

        assertEquals(3.4, result.getWpm(), 0.000001);
    }

    @Test
    public void evaluateTyping_repeatedWhitespace_normalizesBothTexts() {
        TypingResult result = TypingEvaluator.evaluateTyping("\tStrike  the goblin\n",
                "  Strike \t the  goblin \r\n", 6.0, 10.0);

        assertEquals(34.0, result.getWpm(), 0.000001);
        assertEquals(100.0, result.getAccuracyPercent(), 0.000001);
        assertTrue(result.getWordErrors().isEmpty());
    }

    @Test
    public void evaluateTyping_missingLastWord_reportsMissingWord() {
        TypingResult result =
                TypingEvaluator.evaluateTyping("Strike the goblin", "Strike the", 6.0, 10.0);

        assertEquals(20.0, result.getWpm(), 0.000001);
        assertEquals(200.0 / 3.0, result.getAccuracyPercent(), 0.000001);
        assertEquals(1, result.getWordErrors().size());
        assertWordError(result.getWordErrors().get(0), 3, "goblin", "", ErrorType.MISSING);
    }

    @Test
    public void evaluateTyping_extraLastWord_penalizesAccuracy() {
        TypingResult result =
                TypingEvaluator.evaluateTyping("Strike the goblin", "Strike the goblin now", 6.0, 10.0);

        assertEquals(42.0, result.getWpm(), 0.000001);
        assertEquals(75.0, result.getAccuracyPercent(), 0.000001);
        assertEquals(1, result.getWordErrors().size());
        assertWordError(result.getWordErrors().get(0), 4, "", "now", ErrorType.EXTRA);
    }

    @Test
    public void evaluateTyping_missingMiddleWord_comparesByPosition() {
        TypingResult result =
                TypingEvaluator.evaluateTyping("Strike the goblin", "Strike goblin", 6.0, 10.0);

        assertEquals(100.0 / 3.0, result.getAccuracyPercent(), 0.000001);
        assertEquals(2, result.getWordErrors().size());
        assertWordError(result.getWordErrors().get(0), 2, "the", "goblin", ErrorType.WRONG);
        assertWordError(result.getWordErrors().get(1), 3, "goblin", "", ErrorType.MISSING);
    }

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

    @Test
    public void evaluateTyping_caseAndPunctuationDifferences_countAsErrors() {
        TypingResult result =
                TypingEvaluator.evaluateTyping("Strike the goblin!", "strike the goblin", 6.0, 10.0);

        assertEquals(100.0 / 3.0, result.getAccuracyPercent(), 0.000001);
        assertEquals(2, result.getWordErrors().size());
        assertWordError(result.getWordErrors().get(0), 1, "Strike", "strike", ErrorType.WRONG);
        assertWordError(result.getWordErrors().get(1), 3, "goblin!", "goblin", ErrorType.WRONG);
    }

    @Test
    public void evaluateTyping_repeatedWords_keepsErrorPosition() {
        TypingResult result =
                TypingEvaluator.evaluateTyping("go go home", "go no home", 6.0, 10.0);

        assertEquals(1, result.getWordErrors().size());
        assertWordError(result.getWordErrors().get(0), 2, "go", "no", ErrorType.WRONG);
    }

    @Test
    public void evaluateTyping_invalidDuration_throwsException() {
        double[] invalidDurations = {0.0, -1.0, Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY};
        for (double elapsedSeconds : invalidDurations) {
            assertThrows(IllegalArgumentException.class,
                    () -> TypingEvaluator.evaluateTyping("Strike", "Strike", elapsedSeconds, 10.0));
        }
    }

    @Test
    public void evaluateTyping_blankChallenge_throwsException() {
        for (String expectedText : List.of("", " \t\n")) {
            assertThrows(IllegalArgumentException.class,
                    () -> TypingEvaluator.evaluateTyping(expectedText, "Strike", 6.0, 10.0));
        }
    }

    @Test
    public void evaluateTyping_nullText_throwsException() {
        assertThrows(NullPointerException.class,
                () -> TypingEvaluator.evaluateTyping(null, "Strike", 6.0, 10.0));
        assertThrows(NullPointerException.class,
                () -> TypingEvaluator.evaluateTyping("Strike", null, 6.0, 10.0));
    }

    @Test
    public void evaluateTyping_returnedErrors_cannotBeModified() {
        TypingResult result =
                TypingEvaluator.evaluateTyping("Strike", "strike", 6.0, 10.0);

        assertThrows(UnsupportedOperationException.class, () -> result.getWordErrors().clear());
    }

    @Test
    public void evaluateTyping_overTimeLimit_blocksDamageDespitePerfectAccuracy() {
        TypingResult result =
                TypingEvaluator.evaluateTyping("Strike the goblin", "Strike the goblin", 6.0, 5.0);

        assertFalse(result.canDealDamage());
        assertEquals(34.0, result.getWpm(), 0.000001);
        assertEquals(100.0, result.getAccuracyPercent(), 0.000001);
        assertTrue(result.getWordErrors().isEmpty());
    }

    @Test
    public void evaluateTyping_exactTimeLimit_allowsDamage() {
        TypingResult result =
                TypingEvaluator.evaluateTyping("Strike the goblin", "Strike the goblin", 6.0, 6.0);

        assertTrue(result.canDealDamage());
    }

    @Test
    public void evaluateTyping_justOverTimeLimit_blocksDamage() {
        TypingResult result =
                TypingEvaluator.evaluateTyping("Strike the goblin", "Strike the goblin", Math.nextUp(6.0), 6.0);

        assertFalse(result.canDealDamage());
    }

    @Test
    public void evaluateTyping_overTimeLimit_preservesWordFeedback() {
        TypingResult result =
                TypingEvaluator.evaluateTyping("Strike the goblin", "Strike teh goblin", 6.0, 5.0);

        assertFalse(result.canDealDamage());
        assertEquals(200.0 / 3.0, result.getAccuracyPercent(), 0.000001);
        assertEquals(1, result.getWordErrors().size());
        assertWordError(result.getWordErrors().get(0), 2, "the", "teh", ErrorType.WRONG);
    }

    @Test
    public void evaluateTyping_invalidRequiredTime_throwsException() {
        double[] invalidTimes = {0.0, -1.0, Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY};
        for (double requiredSeconds : invalidTimes) {
            assertThrows(IllegalArgumentException.class,
                    () -> TypingEvaluator.evaluateTyping("Strike", "Strike", 6.0, requiredSeconds));
        }
    }

    /**
     * Checks the position, words and category of a reported mismatch.
     */
    private static void assertWordError(WordError error, int position, String expectedWord,
            String typedWord, ErrorType type) {
        assertEquals(position, error.getPosition());
        assertEquals(expectedWord, error.getExpectedWord());
        assertEquals(typedWord, error.getTypedWord());
        assertEquals(type, error.getType());
    }
}
