package seedu.keyboardwarrior.storage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import seedu.keyboardwarrior.typing.TypingResult;

/** Persists the latest ten typing results as UTF-8 lines containing WPM,accuracy percentage. */
public class SaveStore {
    private static final int MAX_RECORDS = 10;
    private final Path filePath;

    /** Uses a save file relative to the directory from which the game is launched. */
    public SaveStore() {
        this(Path.of("data", "keyboardwarrior.txt"));
    }

    /** Accepts an alternative save location, including temporary files for tests. */
    public SaveStore(Path filePath) {
        this.filePath = Objects.requireNonNull(filePath).toAbsolutePath();
    }

    /**
     * Appends a result, discarding the oldest entries when more than ten exist.
     * Reads previous records on every save so history survives application restarts.
     *
     * @throws IOException if existing history cannot be read or the file cannot be written
     */
    public void save(TypingResult result) throws IOException {
        Objects.requireNonNull(result, "Typing result must not be null");
        List<String> records;
        try {
            records = new ArrayList<>(Files.readAllLines(filePath));
        } catch (NoSuchFileException e) {
            records = new ArrayList<>();
        }
        records.add(result.getWpm() + "," + result.getAccuracyPercent());
        int firstRecord = Math.max(0, records.size() - MAX_RECORDS);
        Files.createDirectories(filePath.getParent());
        Files.write(filePath, records.subList(firstRecord, records.size()));
    }
}
