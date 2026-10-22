package seedu.keyboardwarrior.state;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.keyboardwarrior.storage.SaveStore;

/** Tests the records command against real temporary history files. */
class MainMenuStateTest {
    @TempDir
    Path tempDir;

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

    @Test
    void records_missingHistory_displaysEmptyMessage() {
        assertTrue(viewRecords().contains("No typing records saved yet"));
    }

    @Test
    void records_invalidHistory_displaysErrorWithoutCrashing() throws IOException {
        Files.writeString(tempDir.resolve("history.txt"), "invalid\n");
        assertTrue(viewRecords().contains("Could not load typing history"));
    }

    /** Captures the menu output and verifies that viewing records leaves the menu usable. */
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
