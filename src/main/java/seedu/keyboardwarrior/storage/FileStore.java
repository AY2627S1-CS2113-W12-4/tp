package seedu.keyboardwarrior.storage;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

/** Shares file locations and safe replacement while subclasses define their own data formats and rules. */
public abstract class FileStore {
    /** Absolute destination path, ensuring even a relative filename has a parent directory. */
    protected final Path filePath;

    /** Accepts a destination without creating files or directories until a save is requested. */
    protected FileStore(Path filePath) {
        this.filePath = Objects.requireNonNull(filePath).toAbsolutePath();
    }

    /** Returns the data directory so related stores can share its location. */
    public Path getDirectory() {
        return filePath.getParent();
    }

    /**
     * Writes a complete temporary file before replacing the destination, cleaning up on failure too.
     * The temporary file shares the destination directory to allow an atomic move when supported;
     * otherwise, replacement falls back to a regular move.
     *
     * @param prefix prefix identifying temporary files belonging to this store
     * @param writeContents writes the contents and closes any opened streams before returning
     * @throws IOException if directory creation, writing, replacement or cleanup fails
     */
    protected final void writeSafely(String prefix, FileContentWriter writeContents) throws IOException {
        Files.createDirectories(getDirectory());
        Path temporary = Files.createTempFile(getDirectory(), prefix, ".tmp");
        try {
            writeContents.write(temporary);
            try {
                Files.move(temporary, filePath, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException exception) {
                Files.move(temporary, filePath, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    /** Allows each store to write its own format while propagating checked I/O failures. */
    @FunctionalInterface
    protected interface FileContentWriter {
        /** Writes to the temporary path and closes any opened streams before returning. */
        void write(Path temporary) throws IOException;
    }
}
