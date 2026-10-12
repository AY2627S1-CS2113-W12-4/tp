package seedu.keyboardwarrior.ui;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

import org.jline.terminal.Size;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.junit.jupiter.api.Test;

import seedu.keyboardwarrior.CountdownReader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Exercises countdown rendering in a simulated terminal with cursor support.
 */
class StateUiTerminalTest {
    @Test
    void countdown_reusesStatusRowAndPreservesInput() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (Terminal terminal = TerminalBuilder.builder().system(false).type("xterm")
                .streams(new ByteArrayInputStream(new byte[0]), output).paused(true).build()) {
            terminal.setSize(new Size(80, 24));
            CountdownReader reader = new CountdownReader(terminal);
            StateUi.attachTerminal(reader);
            reader.getBuffer().write("unfinished typing");
            StateUi.showRemainingTime(60);
            StateUi.updateCountdown(59);
            assertEquals("unfinished typing", reader.getBuffer().toString());
            assertTrue(reader.getDisplayedBufferWithPrompts(new java.util.ArrayList<>())
                    .toString().contains("Time remaining: 59s"));
            StateUi.clearCountdown();
        } finally {
            StateUi.detachTerminal();
        }
    }

    @Test
    void takeCurrentInput_activeReader_returnsPassageAndClearsNextSubmission() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ExecutorService inputExecutor = Executors.newSingleThreadExecutor();
        try (PipedInputStream input = new PipedInputStream();
                PipedOutputStream keyboard = new PipedOutputStream(input);
                Terminal terminal = TerminalBuilder.builder().system(false).type("xterm")
                        .streams(input, output).build()) {
            terminal.setSize(new Size(80, 24));
            CountdownReader reader = new CountdownReader(terminal);
            StateUi.attachTerminal(reader);
            Future<String> submission = inputExecutor.submit(() -> reader.readLine("Your input: "));

            assertTrue(waitUntil(reader::isReading), "The terminal reader should start before typing.");
            keyboard.write("unfinished answer".getBytes(StandardCharsets.UTF_8));
            keyboard.flush();
            assertTrue(waitUntil(() -> reader.isReading()
                    && reader.getBuffer().toString().equals("unfinished answer")),
                    "The unfinished answer should reach the active editing buffer.");

            StateUi.showRemainingTime(0);
            assertEquals("unfinished answer", StateUi.takeCurrentInput());
            StateUi.clearCountdown();
            assertEquals("", reader.getBuffer().toString());
            keyboard.write("SIGKILL\r".getBytes(StandardCharsets.UTF_8));
            keyboard.flush();

            assertEquals("SIGKILL", submission.get(2, TimeUnit.SECONDS));
        } finally {
            StateUi.detachTerminal();
            inputExecutor.shutdownNow();
        }
    }

    /** Waits briefly for background terminal input without leaving a test blocked. */
    private boolean waitUntil(BooleanSupplier condition) throws InterruptedException {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(2);
        while (!condition.getAsBoolean() && System.nanoTime() < deadline) {
            Thread.sleep(10);
        }
        return condition.getAsBoolean();
    }
}
