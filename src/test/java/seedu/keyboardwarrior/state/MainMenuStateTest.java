package seedu.keyboardwarrior.state;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.keyboardwarrior.storage.SaveStore;

/**
 * Tests menu history display and persistence using real temporary files.
 */
class MainMenuStateTest {
    @TempDir
    Path tempDir;

    /**
     * Checks that gameplay started from a menu with custom history appends to that same file.
     */
    @Test
    void start_customHistory_savesAttemptsToTheSameFile() throws IOException {
        Path history = tempDir.resolve("history.txt");
        Files.writeString(history, "42.5,87.25\n");
        PrintStream original = System.out;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (PrintStream capture = new PrintStream(output, true, StandardCharsets.UTF_8)) {
            System.setOut(capture);
            StateMachine machine = new StateMachine();
            machine.start(new MainMenuState(machine, new SaveStore(history)));

            machine.handleInput("start");
            machine.handleInput("42");
            machine.handleInput("Ping");
            machine.handleInput("");

            assertEquals(List.of("42.5,87.25", "0.0,0.0"), Files.readAllLines(history));
            machine.handleInput("exit");
        } finally {
            System.setOut(original);
        }
    }

    /**
     * Checks that numeric and text seeds reproduce the first enemy and prompt through the menu.
     */
    @Test
    void start_sameSeed_repeatsEnemyAndPrompt() {
        assertEquals(startWithSeed("42"), startWithSeed(" 42 "));
        String textSeed = "keyboard-warrior";
        assertEquals(startWithSeed(textSeed), startWithSeed(Long.toString(textSeed.hashCode())));
        assertNotEquals(startWithSeed("42"), startWithSeed("43"));
    }

    /**
     * Captures the first encounter and challenge started through the real menu.
     *
     * @param seed numeric or text seed submitted at the seed prompt
     * @return combat and challenge output, excluding the seed announcement
     */
    private String startWithSeed(String seed) {
        PrintStream original = System.out;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (PrintStream capture = new PrintStream(output, true, StandardCharsets.UTF_8)) {
            System.setOut(capture);
            StateMachine machine = new StateMachine();
            machine.start(new MainMenuState(machine, new SaveStore(tempDir.resolve("history.txt"))));
            machine.handleInput("start");
            machine.handleInput(seed);
            machine.handleInput("Ping");
            machine.handleInput("exit");
        } finally {
            System.setOut(original);
        }
        String report = output.toString(StandardCharsets.UTF_8);
        assertTrue(report.contains("Scenario 1/5"));
        return report.substring(report.indexOf("Scenario 1/5"));
    }

    /**
     * Checks that viewing saved metrics preserves their order and leaves the menu usable.
     */
    @Test
    void records_savedHistory_displaysMetricsAndStaysInMenu() throws IOException {
        Files.writeString(tempDir.resolve("history.txt"), "42.5,87.25\n60.0,100.0\n");
        String output = viewRecords();
        assertTrue(output.contains("42.50"));
        assertTrue(output.contains("87.25%"));
        assertTrue(output.contains("60.00"));
        assertTrue(output.indexOf("42.50") < output.indexOf("60.00"));
        assertTrue(output.contains("oldest to newest"));
    }

    /**
     * Checks the empty-history message when the save file does not exist.
     */
    @Test
    void records_missingHistory_displaysEmptyMessage() {
        assertTrue(viewRecords().contains("No typing records saved yet"));
    }

    /**
     * Checks that malformed history produces a message without crashing the menu.
     */
    @Test
    void records_invalidHistory_displaysErrorWithoutCrashing() throws IOException {
        Files.writeString(tempDir.resolve("history.txt"), "invalid\n");
        assertTrue(viewRecords().contains("Could not load typing history"));
    }

    /**
     * Captures the menu output and verifies that viewing records leaves the menu usable.
     *
     * @return captured output from viewing records and checking menu usability
     */
    private String viewRecords() {
        PrintStream original = System.out;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (PrintStream capture = new PrintStream(output, true, StandardCharsets.UTF_8)) {
            System.setOut(capture);
            StateMachine machine = new StateMachine();
            machine.start(new MainMenuState(machine, new SaveStore(tempDir.resolve("history.txt"))));
            machine.handleInput(" ReCoRdS ");
            assertTrue(machine.isRunning());
            machine.handleInput("unknown");
            assertTrue(output.toString(StandardCharsets.UTF_8).contains("Please enter Start, Records or Exit"));
            machine.stop();
        } finally {
            System.setOut(original);
        }
        return output.toString(StandardCharsets.UTF_8);
    }
}
