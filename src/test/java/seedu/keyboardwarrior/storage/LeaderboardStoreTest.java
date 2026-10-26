package seedu.keyboardwarrior.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.keyboardwarrior.storage.SaveStore.SavedRecord;

/** Checks durable high-score comparisons and protection of invalid saved data. */
class LeaderboardStoreTest {
    @TempDir
    Path tempDir;

    /** Lower scores and exact ties leave the file untouched; better accuracy breaks WPM ties. */
    @Test
    void saveIfHigher_tiesAndLowerScores_keepsFileUntilImprovement() throws IOException {
        Path file = tempDir.resolve("best.txt");
        LeaderboardStore store = new LeaderboardStore(file);
        assertTrue(store.load().isEmpty());
        store.saveIfHigher(new SavedRecord(60, 90));
        Files.setLastModifiedTime(file, FileTime.fromMillis(1000000));
        FileTime savedTime = Files.getLastModifiedTime(file);
        for (SavedRecord candidate : List.of(new SavedRecord(50, 100), new SavedRecord(60, 80),
                new SavedRecord(60, 90))) {
            new LeaderboardStore(file).saveIfHigher(candidate);
            assertEquals(savedTime, Files.getLastModifiedTime(file));
            assertEquals(new SavedRecord(60, 90), store.load().orElseThrow());
        }
        store.saveIfHigher(new SavedRecord(60, 95));
        assertEquals(new SavedRecord(60, 95), new LeaderboardStore(file).load().orElseThrow());
        store.saveIfHigher(new SavedRecord(70, 80));
        assertEquals(new SavedRecord(70, 80), new LeaderboardStore(file).load().orElseThrow());
    }

    /** Corrupt data must report an error instead of silently resetting the all-time best. */
    @Test
    void saveIfHigher_invalidFile_reportsErrorAndPreservesContents() throws IOException {
        Path file = tempDir.resolve("best.txt");
        LeaderboardStore store = new LeaderboardStore(file);
        for (String contents : List.of("", "60,90\n70,100", "bad", "NaN,100", "60,Infinity",
                "-1,100", "60,101", "60,-1", "60,90,extra")) {
            Files.writeString(file, contents);
            assertThrows(IOException.class, store::load);
            assertThrows(IOException.class, () -> store.saveIfHigher(new SavedRecord(80, 100)));
            assertEquals(contents, Files.readString(file));
        }
    }

    /** A real zero-score attempt still counts as the first recorded score. */
    @Test
    void saveIfHigher_firstZeroScore_persistsAttempt() throws IOException {
        Path file = tempDir.resolve("nested/best.txt");
        new LeaderboardStore(file).saveIfHigher(new SavedRecord(0, 0));
        assertEquals(new SavedRecord(0, 0), new LeaderboardStore(file).load().orElseThrow());
    }
}
