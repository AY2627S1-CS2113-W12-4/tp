package seedu.keyboardwarrior.storage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import seedu.keyboardwarrior.storage.SaveStore.SavedRecord;

/** Persists one all-time best attempt independently of the rolling typing history. */
public final class LeaderboardStore extends FileStore {
    /** Higher WPM wins; accuracy breaks ties in WPM. */
    public static final Comparator<SavedRecord> SCORE_ORDER = Comparator.comparingDouble(SavedRecord::wpm)
            .thenComparingDouble(SavedRecord::accuracyPercent);

    /** Creates storage at the supplied high-score file location. */
    public LeaderboardStore(Path filePath) {
        super(filePath);
    }

    /** Loads the best attempt; missing files mean no score, while invalid files report an error. */
    public Optional<SavedRecord> load() throws IOException {
        List<String> lines;
        try {
            lines = Files.readAllLines(filePath);
        } catch (NoSuchFileException exception) {
            return Optional.empty();
        }
        try {
            if (lines.size() != 1) {
                throw new IllegalArgumentException("Expected one high score");
            }
            String[] values = lines.getFirst().split(",", -1);
            if (values.length != 2) {
                throw new IllegalArgumentException("Expected WPM and accuracy");
            }
            SavedRecord record = new SavedRecord(Double.parseDouble(values[0]), Double.parseDouble(values[1]));
            validate(record);
            return Optional.of(record);
        } catch (IllegalArgumentException exception) {
            throw new IOException("Leaderboard contains an invalid high score.", exception);
        }
    }

    /**
     * Saves a candidate only if it beats the stored score, or no score exists yet.
     * Writes a temporary file before replacing the previous score to protect it from incomplete writes.
     * Invalid existing scores are reported rather than overwritten.
     */
    public void saveIfHigher(SavedRecord candidate) throws IOException {
        validate(Objects.requireNonNull(candidate));
        Optional<SavedRecord> current = load();
        if (current.isPresent() && SCORE_ORDER.compare(candidate, current.get()) <= 0) {
            return;
        }
        writeSafely("high-score-", temporary ->
                Files.writeString(temporary, candidate.wpm() + "," + candidate.accuracyPercent() + "\n"));
    }

    /** Rejects nonfinite or out-of-range metrics before reading or writing a score. */
    private static void validate(SavedRecord record) {
        if (!Double.isFinite(record.wpm()) || record.wpm() < 0
                || !Double.isFinite(record.accuracyPercent())
                || record.accuracyPercent() < 0 || record.accuracyPercent() > 100) {
            throw new IllegalArgumentException("High-score metrics are outside their valid ranges");
        }
    }
}
