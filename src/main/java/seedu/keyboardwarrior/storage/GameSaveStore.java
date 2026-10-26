package seedu.keyboardwarrior.storage;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.EnumMap;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;

import seedu.keyboardwarrior.GameDifficulty;
import seedu.keyboardwarrior.GameSave;
import seedu.keyboardwarrior.seed.RandomSnapshot;
import seedu.keyboardwarrior.typing.TypingChallenge.Difficulty;

/** Stores one active run separately from typing history, replacing saves only after writing succeeds. */
public class GameSaveStore {
    private final Path filePath;

    /** Uses the default run save relative to the game's launch directory. */
    public GameSaveStore() {
        this(Path.of("data", "game-save.properties"));
    }

    /** Accepts a file location so tests and alternate data directories remain isolated. */
    public GameSaveStore(Path filePath) {
        this.filePath = Objects.requireNonNull(filePath).toAbsolutePath();
    }

    /** Loads and validates a run; a missing file means there is no game to continue. */
    public Optional<GameSave> load() throws IOException {
        Properties data = new Properties();
        try (Reader reader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8)) {
            data.load(reader);
            return Optional.of(new GameSave(integer(data, "version"),
                    GameDifficulty.valueOf(required(data, "difficulty")), integer(data, "playerHealth"),
                    integer(data, "scenarioIndex"), integer(data, "bossPhase"), required(data, "enemyType"),
                    integer(data, "enemyHealth"), integer(data, "nextEnemySkillIndex"), loadRandomSnapshot(data)));
        } catch (NoSuchFileException exception) {
            return Optional.empty();
        } catch (IllegalArgumentException exception) {
            throw new IOException("Invalid game save: " + exception.getMessage(), exception);
        }
    }

    /** Writes a complete temporary file, then atomically replaces the previous save when supported. */
    public void save(GameSave save) throws IOException {
        Objects.requireNonNull(save);
        Properties data = new Properties();
        data.setProperty("version", Integer.toString(save.version()));
        data.setProperty("difficulty", save.difficulty().name());
        data.setProperty("playerHealth", Integer.toString(save.playerHealth()));
        data.setProperty("scenarioIndex", Integer.toString(save.scenarioIndex()));
        data.setProperty("bossPhase", Integer.toString(save.bossPhase()));
        data.setProperty("enemyType", save.enemyType());
        data.setProperty("enemyHealth", Integer.toString(save.enemyHealth()));
        data.setProperty("nextEnemySkillIndex", Integer.toString(save.nextEnemySkillIndex()));
        if (save.randomSnapshot() != null) {
            data.setProperty("randomState", Long.toString(save.randomSnapshot().state()));
            save.randomSnapshot().previousPromptIndices().forEach((difficulty, index) ->
                    data.setProperty("previousPrompt." + difficulty.name(), Integer.toString(index)));
        }
        Files.createDirectories(filePath.getParent());
        Path temporary = Files.createTempFile(filePath.getParent(), "game-save-", ".tmp");
        try {
            try (Writer writer = Files.newBufferedWriter(temporary, StandardCharsets.UTF_8)) {
                data.store(writer, "Keyboard Warrior active run");
            }
            try {
                Files.move(temporary, filePath, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException exception) {
                Files.move(temporary, filePath, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    /** Removes a completed or lost run without affecting typing history. */
    public void delete() throws IOException {
        Files.deleteIfExists(filePath);
    }

    private static String required(Properties data, String key) {
        String value = data.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Missing field: " + key);
        }
        return value;
    }

    private static int integer(Properties data, String key) {
        return Integer.parseInt(required(data, key));
    }

    /** Missing random fields identify legacy saves; partial or malformed fields are rejected. */
    private static RandomSnapshot loadRandomSnapshot(Properties data) {
        EnumMap<Difficulty, Integer> history = new EnumMap<>(Difficulty.class);
        for (String key : data.stringPropertyNames()) {
            if (key.startsWith("previousPrompt.")) {
                Difficulty difficulty = Difficulty.valueOf(key.substring("previousPrompt.".length()));
                history.put(difficulty, integer(data, key));
            }
        }
        if (!data.containsKey("randomState") && history.isEmpty()) {
            return null;
        }
        return new RandomSnapshot(Long.parseLong(required(data, "randomState")), history);
    }
}
