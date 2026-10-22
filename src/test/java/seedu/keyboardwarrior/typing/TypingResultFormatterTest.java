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

    @Test
    public void formatTypingSummary_nullResult_throwsException() {
        assertThrows(NullPointerException.class,
                () -> TypingResultFormatter.formatTypingSummary(null));
    }

    @Test
    public void formatTypingResult_wrongWord_formatsMetricsAndError() {
        TypingResult result =
                TypingEvaluator.evaluateTyping("Strike the goblin", "Strike teh goblin", 6.0, 10.0);
        String newline = System.lineSeparator();

        assertEquals(String.join(newline, "WPM: 34.00", "Accuracy: 88.24%",
                "Elapsed time: 6.00 seconds", "Required time: 10.00 seconds", "Wrong words:",
                "  Word 2: expected \"the\", typed \"teh\"", ""), TypingResultFormatter.formatTypingResult(result));
    }

    @Test
    public void formatTypingResult_missingAndExtraWords_formatsErrorTypes() {
        TypingResult missing =
                TypingEvaluator.evaluateTyping("Strike the goblin", "Strike the", 6.0, 10.0);
        TypingResult extra =
                TypingEvaluator.evaluateTyping("Strike the goblin", "Strike the goblin now", 6.0, 10.0);

        assertTrue(TypingResultFormatter.formatTypingResult(missing).contains("Word 3: missing \"goblin\""));
        assertTrue(TypingResultFormatter.formatTypingResult(extra).contains("Word 4: unexpected \"now\""));
    }

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

    @Test
    public void formatTypingResult_overTimeLimit_reportsMetricsAndRetainsDiagnostics() {
        TypingResult result =
                TypingEvaluator.evaluateTyping("Strike", "Strike", 6.0, 5.0);
        String newline = System.lineSeparator();

        assertEquals(String.join(newline, "WPM: 12.00", "Accuracy: 100.00%",
                "Elapsed time: 6.00 seconds", "Required time: 5.00 seconds",
                "Wrong words:", "  None", ""), TypingResultFormatter.formatTypingResult(result));
    }

    @Test
    public void formatTypingResult_exactTimeLimit_reportsNoDamagePenalty() {
        TypingResult result =
                TypingEvaluator.evaluateTyping("Strike", "Strike", 6.0, 6.0);

        assertFalse(TypingResultFormatter.formatTypingResult(result).contains("penalty"));
    }

    @Test
    public void formatTypingResult_nullResult_throwsException() {
        assertThrows(NullPointerException.class,
                () -> TypingResultFormatter.formatTypingResult(null));
    }
}
