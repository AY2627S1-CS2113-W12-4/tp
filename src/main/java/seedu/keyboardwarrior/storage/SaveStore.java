package seedu.keyboardwarrior.storage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import seedu.keyboardwarrior.typing.TypingResult;

/**
 * Persists the latest ten typing results as UTF-8 lines in {@code wpm,accuracyPercent} format.
 */
public class SaveStore {
    private static final int MAX_RECORDS = 10;
    private final Path filePath;
    private final LeaderboardStore leaderboardStore;

    /**
     * Uses a save file relative to the directory from which the game is launched.
     */
    public SaveStore() {
        this(Path.of("data", "keyboardwarrior.txt"));
    }

    /**
     * Accepts an alternative save location, including temporary files for tests.
     *
     * @param filePath location of the history file
     * @throws NullPointerException if the path is null
     */
    public SaveStore(Path filePath) {
        this.filePath = Objects.requireNonNull(filePath).toAbsolutePath();
        this.leaderboardStore = new LeaderboardStore(this.filePath.resolveSibling(
                this.filePath.getFileName() + ".high-score"));
    }

    /** Returns the data directory so run saves can share its location, including temporary test storage. */
    public Path getDirectory() {
        return filePath.getParent();
    }

    /**
     * A saved typing speed and accuracy percentage, without combat-specific data.
     *
     * @param wpm gross words per minute
     * @param accuracyPercent character accuracy from zero to one hundred
     */
    public record SavedRecord(double wpm, double accuracyPercent) {
    }

    /**
     * Loads up to ten records, oldest first; a missing file means no history yet.
     *
     * @return immutable list of up to ten saved records, oldest first
     * @throws IOException if the file cannot be read or one of its latest ten records has invalid metrics
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
     * Loads the all-time best attempt, initializing it from existing history on first use.
     * Previously discarded history cannot be recovered, but this score survives future history rotation.
     *
     * @return the best saved attempt, or empty if no attempts have been recorded
     * @throws IOException if the high score or history cannot be read, or initialization cannot be saved
     */
    public Optional<SavedRecord> loadHighScore() throws IOException {
        Optional<SavedRecord> best = leaderboardStore.load();
        if (best.isEmpty()) {
            best = load().stream().max(LeaderboardStore.SCORE_ORDER);
            if (best.isPresent()) {
                leaderboardStore.saveIfHigher(best.get());
            }
        }
        return best;
    }

    /**
     * Preserves the all-time high score, then appends a result and keeps the latest ten history entries.
     * Reads previous records on every save so history survives application restarts.
     *
     * @param result evaluated typing result
     * @throws NullPointerException if the result is null
     * @throws IOException if existing history cannot be read or the file cannot be written
     */
    public void save(TypingResult result) throws IOException {
        Objects.requireNonNull(result, "Typing result must not be null");
        // Migrate existing history before its best attempt can be discarded by the ten-record limit.
        loadHighScore();
        leaderboardStore.saveIfHigher(new SavedRecord(result.getWpm(), result.getAccuracyPercent()));
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
