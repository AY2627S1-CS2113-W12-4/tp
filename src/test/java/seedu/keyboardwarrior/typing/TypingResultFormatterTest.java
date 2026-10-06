package seedu.keyboardwarrior.typing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Tests the console formatting of evaluated typing results.
 */
class TypingResultFormatterTest {
    @Test
    public void formatTypingResult_wrongWord_formatsMetricsAndError() {
        TypingResult result =
                TypingEvaluator.evaluateTyping("Strike the goblin", "Strike teh goblin", 6.0);
        String newline = System.lineSeparator();

        assertEquals(String.join(newline, "WPM: 34.00", "Accuracy: 66.67%", "Wrong words:",
                "  Word 2: expected \"the\", typed \"teh\"", ""), TypingResultFormatter.formatTypingResult(result));
    }

    @Test
    public void formatTypingResult_missingAndExtraWords_formatsErrorTypes() {
        TypingResult missing =
                TypingEvaluator.evaluateTyping("Strike the goblin", "Strike the", 6.0);
        TypingResult extra =
                TypingEvaluator.evaluateTyping("Strike the goblin", "Strike the goblin now", 6.0);

        assertTrue(TypingResultFormatter.formatTypingResult(missing).contains("Word 3: missing \"goblin\""));
        assertTrue(TypingResultFormatter.formatTypingResult(extra).contains("Word 4: unexpected \"now\""));
    }

    @Test
    public void formatTypingResult_perfectSubmission_reportsNoErrors() {
        TypingResult result =
                TypingEvaluator.evaluateTyping("Strike", "Strike", 6.0);
        String newline = System.lineSeparator();

        assertEquals(String.join(newline, "WPM: 12.00", "Accuracy: 100.00%", "Wrong words:", "  None", ""),
                TypingResultFormatter.formatTypingResult(result));
    }

    @Test
    public void formatTypingResult_nullResult_throwsException() {
        assertThrows(NullPointerException.class,
                () -> TypingResultFormatter.formatTypingResult(null));
    }
}
