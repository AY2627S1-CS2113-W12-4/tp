package seedu.keyboardwarrior.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Verifies that interrupted content writing cannot damage an existing save or leave temporary files. */
class FileStoreTest {
    @TempDir
    Path directory;

    @Test
    void writeSafely_partialWriteFailure_preservesSaveAndRemovesTemporaryFile() throws IOException {
        Path destination = directory.resolve("save.txt");
        Files.writeString(destination, "previous save");
        FileStore store = new FileStore(destination) { };

        assertThrows(IOException.class, () -> store.writeSafely("test-save-", temporary -> {
            Files.writeString(temporary, "incomplete save");
            throw new IOException("Simulated write failure");
        }));

        assertEquals("previous save", Files.readString(destination));
        try (var entries = Files.list(directory)) {
            assertEquals(List.of(destination), entries.toList());
        }
    }
}
