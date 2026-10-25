package seedu.keyboardwarrior.typing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Tests the console formatting of evaluated typing results.
 */
class TypingResultFormatterTest {
    /**
     * Checks that the gameplay summary omits word details without discarding them from the result.
     */
    @Test
    public void formatTypingSummary_wrongWord_omitsErrorDetailsAndPreservesResult() {
        TypingResult result =
                TypingEvaluator.evaluateTyping("Strike the goblin", "Strike teh goblin", 6.0, 10.0);
        String newline = System.lineSeparator();

        String summary = TypingResultFormatter.formatTypingSummary(result);

        assertEquals(String.join(newline, "WPM: 34.00", "Accuracy: 88.24%",
                "Elapsed time: 6.00 seconds", "Required time: 10.00 seconds", ""), summary);
        assertFalse(summary.contains("Wrong words:"));
        assertEquals(1, result.getWordErrors().size());
        assertEquals("teh", result.getWordErrors().get(0).getTypedWord());
        assertTrue(TypingResultFormatter.formatTypingResult(result).contains("Word 2:"));
    }

    /**
     * Checks that a late summary retains metrics without listing penalties or word errors.
     */
    @Test
    public void formatTypingSummary_overTimeLimit_keepsMetricsWithoutPenaltyOrErrors() {
        TypingResult result =
                TypingEvaluator.evaluateTyping("Strike the goblin", "Strike", 6.0, 5.0);

        String summary = TypingResultFormatter.formatTypingSummary(result);

        assertTrue(summary.contains("Elapsed time: 6.00 seconds"));
        assertTrue(summary.contains("Required time: 5.00 seconds"));
        assertFalse(summary.contains("penalty"));
        assertFalse(summary.contains("Wrong words:"));
        assertEquals(2, result.getWordErrors().size());
    }

    /**
     * Checks that summary formatting rejects a null result.
     */
    @Test
    public void formatTypingSummary_nullResult_throwsException() {
        assertThrows(NullPointerException.class,
                () -> TypingResultFormatter.formatTypingSummary(null));
    }

    /**
     * Checks the detailed report's rounded metrics and incorrect-word description.
     */
    @Test
    public void formatTypingResult_wrongWord_formatsMetricsAndError() {
        TypingResult result =
                TypingEvaluator.evaluateTyping("Strike the goblin", "Strike teh goblin", 6.0, 10.0);
        String newline = System.lineSeparator();

        assertEquals(String.join(newline, "WPM: 34.00", "Accuracy: 88.24%",
                "Elapsed time: 6.00 seconds", "Required time: 10.00 seconds", "Wrong words:",
                "  Word 2: expected \"the\", typed \"teh\"", ""), TypingResultFormatter.formatTypingResult(result));
    }

    /**
     * Checks distinct detailed messages for missing and extra words.
     */
    @Test
    public void formatTypingResult_missingAndExtraWords_formatsErrorTypes() {
        TypingResult missing =
                TypingEvaluator.evaluateTyping("Strike the goblin", "Strike the", 6.0, 10.0);
        TypingResult extra =
                TypingEvaluator.evaluateTyping("Strike the goblin", "Strike the goblin now", 6.0, 10.0);

        assertTrue(TypingResultFormatter.formatTypingResult(missing).contains("Word 3: missing \"goblin\""));
        assertTrue(TypingResultFormatter.formatTypingResult(extra).contains("Word 4: unexpected \"now\""));
    }

    /**
     * Checks that the detailed report explicitly states when there are no word errors.
     */
    @Test
    public void formatTypingResult_perfectSubmission_reportsNoErrors() {
        TypingResult result =
                TypingEvaluator.evaluateTyping("Strike", "Strike", 6.0, 10.0);
        String newline = System.lineSeparator();

        assertEquals(String.join(newline, "WPM: 12.00", "Accuracy: 100.00%",
                "Elapsed time: 6.00 seconds", "Required time: 10.00 seconds",
                "Wrong words:", "  None", ""),
                TypingResultFormatter.formatTypingResult(result));
    }

    /**
     * Checks that the detailed report for a late attempt still includes metrics and diagnostics.
     */
    @Test
    public void formatTypingResult_overTimeLimit_reportsMetricsAndRetainsDiagnostics() {
        TypingResult result =
                TypingEvaluator.evaluateTyping("Strike", "Strike", 6.0, 5.0);
        String newline = System.lineSeparator();

        assertEquals(String.join(newline, "WPM: 12.00", "Accuracy: 100.00%",
                "Elapsed time: 6.00 seconds", "Required time: 5.00 seconds",
                "Wrong words:", "  None", ""), TypingResultFormatter.formatTypingResult(result));
    }

    /**
     * Checks that a report at the exact deadline does not mention a damage penalty.
     */
    @Test
    public void formatTypingResult_exactTimeLimit_reportsNoDamagePenalty() {
        TypingResult result =
                TypingEvaluator.evaluateTyping("Strike", "Strike", 6.0, 6.0);

        assertFalse(TypingResultFormatter.formatTypingResult(result).contains("penalty"));
    }

    /**
     * Checks that detailed formatting rejects a null result.
     */
    @Test
    public void formatTypingResult_nullResult_throwsException() {
        assertThrows(NullPointerException.class,
                () -> TypingResultFormatter.formatTypingResult(null));
    }
}
