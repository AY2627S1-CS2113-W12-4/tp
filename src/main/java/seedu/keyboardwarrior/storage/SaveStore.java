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

    /** Returns the data directory so run saves can share its location, including temporary test storage. */
    public Path getDirectory() {
        return filePath.getParent();
    }

    /** A saved typing speed and accuracy percentage, without combat-specific data. */
    public record SavedRecord(double wpm, double accuracyPercent) {
    }

    /**
     * Loads up to ten records, oldest first; a missing file means no history yet.
     *
     * @throws IOException if the file cannot be read or contains invalid metrics
     */
    public List<SavedRecord> load() throws IOException {
        List<String> lines;
        try {
            lines = Files.readAllLines(filePath);
        } catch (NoSuchFileException e) {
            return List.of();
        }
        List<SavedRecord> records = new ArrayList<>();
        for (String line : lines.subList(Math.max(0, lines.size() - MAX_RECORDS), lines.size())) {
            String[] values = line.split(",", -1);
            try {
                if (values.length != 2) {
                    throw new IllegalArgumentException("Expected two metrics");
                }
                double wpm = Double.parseDouble(values[0]);
                double accuracy = Double.parseDouble(values[1]);
                if (!Double.isFinite(wpm) || wpm < 0
                        || !Double.isFinite(accuracy) || accuracy < 0 || accuracy > 100) {
                    throw new IllegalArgumentException("Metrics outside their valid ranges");
                }
                records.add(new SavedRecord(wpm, accuracy));
            } catch (IllegalArgumentException e) {
                throw new IOException("Typing history contains an invalid record.", e);
            }
        }
        return List.copyOf(records);
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
