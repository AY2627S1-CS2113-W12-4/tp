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
import org.jline.terminal.impl.DumbTerminal;
import org.junit.jupiter.api.Test;

import seedu.keyboardwarrior.CountdownReader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Exercises countdown rendering with both interactive terminals and basic consoles.
 * Simulated terminals use stream-based output consistently across operating systems.
 */
class StateUiTerminalTest {
    /**
     * Checks that countdown redraws preserve the editable input and update the displayed seconds.
     */
    @Test
    void countdown_reusesStatusRowAndPreservesInput() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (Terminal terminal = TerminalBuilder.builder().system(false).provider("exec").type("xterm")
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

    /**
     * Checks that timeout capture clears the current answer before the next line is submitted.
     */
    @Test
    void takeCurrentInput_activeReader_returnsPassageAndClearsNextSubmission() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ExecutorService inputExecutor = Executors.newSingleThreadExecutor();
        try (PipedInputStream input = new PipedInputStream();
                PipedOutputStream keyboard = new PipedOutputStream(input);
                Terminal terminal = TerminalBuilder.builder().system(false).provider("exec").type("xterm")
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

    /**
     * Checks that basic terminals do not append countdown digits to the answer.
     */
    @Test
    void countdown_basicConsoleDoesNotAppendTimerDigits() throws Exception {
        for (String terminalType : new String[] {Terminal.TYPE_DUMB, Terminal.TYPE_DUMB_COLOR}) {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            try (PipedInputStream input = new PipedInputStream();
                    PipedOutputStream keyboard = new PipedOutputStream(input);
                    Terminal terminal = new DumbTerminal("basic-console", terminalType,
                            input, output, StandardCharsets.UTF_8)) {
                // A known size cannot compensate for a console's missing cursor controls.
                terminal.setSize(new Size(80, 24));
                assertCountdownLeavesTypingUntouched(terminal, keyboard, output);
            }
        }
    }

    /**
     * Checks that terminals with unknown dimensions do not append countdown digits to the answer.
     */
    @Test
    void countdown_unknownTerminalSizeDoesNotAppendTimerDigits() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (PipedInputStream input = new PipedInputStream();
                PipedOutputStream keyboard = new PipedOutputStream(input);
                Terminal terminal = TerminalBuilder.builder().system(false).provider("exec").type("xterm")
                        .streams(input, output).build()) {
            terminal.setSize(new Size(0, 0));
            assertCountdownLeavesTypingUntouched(terminal, keyboard, output);
        }
    }

    /**
     * Verifies that unsupported countdown redraws leave an active answer undisturbed.
     *
     * @param terminal terminal used for input and output
     * @param keyboard pipe used to simulate terminal keystrokes
     * @param output buffer receiving terminal output
     */
    private void assertCountdownLeavesTypingUntouched(Terminal terminal, PipedOutputStream keyboard,
            ByteArrayOutputStream output) throws Exception {
        ExecutorService inputExecutor = Executors.newSingleThreadExecutor();
        try {
            CountdownReader reader = new CountdownReader(terminal);
            StateUi.attachTerminal(reader);
            Future<String> submission = inputExecutor.submit(() -> reader.readLine("Your input: "));
            assertTrue(waitUntil(reader::isReading), "The reader should start before typing.");
            keyboard.write("unfinished answer".getBytes(StandardCharsets.UTF_8));
            keyboard.flush();
            assertTrue(waitUntil(() -> reader.getBuffer().toString().equals("unfinished answer")),
                    "The unfinished answer should reach the active editing buffer.");

            reader.showTypingChallenge("Test passage", 15);
            String outputBeforeCountdown = output.toString(StandardCharsets.UTF_8);
            for (int seconds = 4; seconds >= 0; seconds--) {
                StateUi.updateCountdown(seconds);
            }

            assertEquals(outputBeforeCountdown, output.toString(StandardCharsets.UTF_8),
                    "Countdown digits must not appear in a console without redraw support.");
            assertEquals("unfinished answer", reader.getBuffer().toString());
            keyboard.write('\r');
            keyboard.flush();
            assertEquals("unfinished answer", submission.get(2, TimeUnit.SECONDS));
        } finally {
            StateUi.detachTerminal();
            inputExecutor.shutdownNow();
        }
    }

    /**
     * Waits briefly for background terminal input without leaving a test blocked.
     *
     * @param condition condition to poll until satisfied or timed out
     * @return whether the condition became true before the polling finished
     */
    private boolean waitUntil(BooleanSupplier condition) throws InterruptedException {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(2);
        while (!condition.getAsBoolean() && System.nanoTime() < deadline) {
            Thread.sleep(10);
        }
        return condition.getAsBoolean();
    }
}
