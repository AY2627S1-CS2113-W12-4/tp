package seedu.keyboardwarrior.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.keyboardwarrior.typing.TypingEvaluator;
import seedu.keyboardwarrior.typing.TypingResult;

/**
 * Verifies file creation, persistence across stores, retention and storage failures.
 */
class SaveStoreTest {
    @TempDir
    Path tempDir;

    /**
     * Checks that saving creates missing directories and persists the evaluated metrics.
     */
    @Test
    void save_missingDirectory_createsFileWithMetrics() throws IOException {
        Path file = tempDir.resolve("data/history.txt");
        TypingResult result = TypingEvaluator.evaluateTyping("hello world", "hello there", 10, 15);

        new SaveStore(file).save(result);

        assertEquals(List.of(result.getWpm() + "," + result.getAccuracyPercent()), Files.readAllLines(file));
        assertEquals(List.of(new SaveStore.SavedRecord(result.getWpm(), result.getAccuracyPercent())),
                new SaveStore(file).load());
    }

    /**
     * Checks that reopening storage retains only the latest ten records in chronological order.
     */
    @Test
    void save_reopenedStore_keepsLatestTenInOrder() throws IOException {
        Path file = tempDir.resolve("history.txt");
        for (int seconds = 1; seconds <= 12; seconds++) {
            new SaveStore(file).save(TypingEvaluator.evaluateTyping("hello", "hello", seconds, 15));
        }

        List<String> lines = Files.readAllLines(file);
        List<SaveStore.SavedRecord> loaded = new SaveStore(file).load();
        assertEquals(10, lines.size());
        assertEquals(10, loaded.size());
        for (int index = 0; index < 10; index++) {
            TypingResult expected = TypingEvaluator.evaluateTyping("hello", "hello", index + 3, 15);
            assertEquals(expected.getWpm() + ",100.0", lines.get(index));
            assertEquals(new SaveStore.SavedRecord(expected.getWpm(), 100.0), loaded.get(index));
        }
    }

    /**
     * Checks that invalid stored metrics cause an I/O error without rewriting the file.
     */
    @Test
    void load_invalidMetrics_reportsIoFailureWithoutChangingHistory() throws IOException {
        Path file = tempDir.resolve("history.txt");
        for (String invalid : List.of("NaN,95", "Infinity,95", "-1,95", "50,NaN",
                "50,Infinity", "50,-1", "50,101", "50", "50,95,extra")) {
            String contents = "40,90\n" + invalid + "\n";
            Files.writeString(file, contents);

            assertThrows(IOException.class, () -> new SaveStore(file).load(), invalid);
            assertEquals(contents, Files.readString(file));
        }
    }

    /**
     * Checks that an unusable parent path produces an error without modifying the existing file.
     */
    @Test
    void save_parentIsFile_reportsFailureWithoutChangingIt() throws IOException {
        Path parent = tempDir.resolve("data");
        Files.writeString(parent, "existing content");
        SaveStore store = new SaveStore(parent.resolve("history.txt"));
        TypingResult result = TypingEvaluator.evaluateTyping("hello", "hello", 10, 15);

        assertThrows(IOException.class, () -> store.save(result));
        assertEquals("existing content", Files.readString(parent));
    }
}
