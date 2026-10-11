package seedu.keyboardwarrior;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;


import org.jline.terminal.Size;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Exercises countdown rendering in a simulated terminal with cursor support.
 */
class UiTerminalTest {
    @Test
    void countdown_reusesStatusRowAndPreservesInput() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (Terminal terminal = TerminalBuilder.builder().system(false).type("xterm")
                .streams(new ByteArrayInputStream(new byte[0]), output).paused(true).build()) {
            terminal.setSize(new Size(80, 24));
            CountdownReader reader = new CountdownReader(terminal);
            Ui.attachTerminal(reader);
            reader.getBuffer().write("unfinished typing");
            Ui.showRemainingTime(60);
            Ui.updateCountdown(59);
            assertEquals("unfinished typing", reader.getBuffer().toString());
            assertTrue(reader.getDisplayedBufferWithPrompts(new java.util.ArrayList<>())
                    .toString().contains("Time remaining: 59s"));
            Ui.clearCountdown();
        } finally {
            Ui.detachTerminal();
        }
    }
}
