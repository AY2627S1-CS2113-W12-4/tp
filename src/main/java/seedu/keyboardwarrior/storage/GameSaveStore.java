package seedu.keyboardwarrior.storage;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;

import seedu.keyboardwarrior.GameDifficulty;
import seedu.keyboardwarrior.GameSave;
import seedu.keyboardwarrior.encounter.EncounterSnapshot;
import seedu.keyboardwarrior.seed.RandomSnapshot;
import seedu.keyboardwarrior.typing.TypingChallenge.Difficulty;

/** Stores one active run separately from typing history, replacing saves only after writing succeeds. */
public class GameSaveStore extends FileStore {
    /** Uses the default run save relative to the game's launch directory. */
    public GameSaveStore() {
        this(Path.of("data", "game-save.properties"));
    }

    /** Accepts a file location so tests and alternate data directories remain isolated. */
    public GameSaveStore(Path filePath) {
        super(filePath);
    }

    /** Loads and validates a run; a missing file means there is no game to continue. */
    public Optional<GameSave> load() throws IOException {
        Properties data = new Properties();
        try (Reader reader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8)) {
            data.load(reader);
            int version = integer(data, "version");
            if (version == 1) {
                return Optional.of(loadLegacySave(data));
            }
            if (version != GameSave.CURRENT_VERSION) {
                throw new IllegalArgumentException("Unsupported game save version: " + version);
            }
            return Optional.of(loadCurrentSave(data));
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
        data.setProperty("bonusTime", Integer.toString(save.bonusTime()));
        data.setProperty("skillIds", String.join(",", save.skillIds()));
        data.setProperty("choiceCount", Integer.toString(save.choices().size()));
        if (save.activeEncounter() != null) {
            saveEncounter(data, "activeEncounter.", save.activeEncounter());
        } else {
            for (int index = 0; index < save.choices().size(); index++) {
                saveEncounter(data, "choice." + index + ".", save.choices().get(index));
            }
        }
        if (save.randomSnapshot() != null) {
            data.setProperty("randomState", Long.toString(save.randomSnapshot().state()));
            save.randomSnapshot().previousPromptIndices().forEach((difficulty, index) ->
                    data.setProperty("previousPrompt." + difficulty.name(), Integer.toString(index)));
        }
        writeSafely("game-save-", temporary -> {
            try (Writer writer = Files.newBufferedWriter(temporary, StandardCharsets.UTF_8)) {
                data.store(writer, "Keyboard Warrior active run");
            }
        });
    }

    /** Removes a completed or lost run without affecting typing history. */
    public void delete() throws IOException {
        Files.deleteIfExists(filePath);
    }

    /** Loads combat-only version-one files into the current snapshot without replaying a turn. */
    private static GameSave loadLegacySave(Properties data) {
        GameDifficulty difficulty = GameDifficulty.valueOf(required(data, "difficulty"));
        int health = integer(data, "playerHealth");
        if (health > difficulty.getStartingHealth()) {
            throw new IllegalArgumentException("Legacy player health is outside the allowed range");
        }
        return new GameSave(1, difficulty, health, integer(data, "scenarioIndex"), integer(data, "bossPhase"),
                required(data, "enemyType"), integer(data, "enemyHealth"), integer(data, "nextEnemySkillIndex"),
                loadRandomSnapshot(data));
    }

    /** Requires either one active encounter or three pending choices, preserving their selected content. */
    private static GameSave loadCurrentSave(Properties data) {
        int count = integer(data, "choiceCount");
        if (count != 0 && count != 3) {
            throw new IllegalArgumentException("Saved choice count must be zero or three");
        }
        List<EncounterSnapshot> choices = new ArrayList<>();
        for (int index = 0; index < count; index++) {
            choices.add(loadEncounter(data, "choice." + index + "."));
        }
        EncounterSnapshot active = count == 0 ? loadEncounter(data, "activeEncounter.") : null;
        validateEncounterPrefixes(data, count);
        boolean combat = active != null && active.type().equals("combat");
        return new GameSave(GameSave.CURRENT_VERSION,
                GameDifficulty.valueOf(required(data, "difficulty")), integer(data, "playerHealth"),
                integer(data, "scenarioIndex"), integer(data, "bossPhase"),
                combat ? active.data().get("enemyType") : "",
                combat ? Integer.parseInt(active.data().get("enemyHealth")) : 0,
                combat ? Integer.parseInt(active.data().get("nextEnemySkillIndex")) : 0,
                loadRandomSnapshot(data), integer(data, "bonusTime"),
                List.of(required(data, "skillIds").split(",", -1)), choices, active);
    }

    /** Rejects ignored extra choices and mixed active/pending encounter representations. */
    private static void validateEncounterPrefixes(Properties data, int count) {
        for (String key : data.stringPropertyNames()) {
            if (count != 0 && key.startsWith("activeEncounter.")) {
                throw new IllegalArgumentException("Pending choices cannot include an active encounter");
            }
            if (key.startsWith("choice.")) {
                boolean valid = false;
                for (int index = 0; index < count; index++) {
                    valid |= key.startsWith("choice." + index + ".");
                }
                if (!valid) {
                    throw new IllegalArgumentException("Unexpected saved encounter choice");
                }
            }
        }
    }

    /** Reads one typed snapshot, letting its constructor reject unknown or missing content fields. */
    private static EncounterSnapshot loadEncounter(Properties data, String prefix) {
        String type = required(data, prefix + "type");
        Map<String, String> fields = new HashMap<>();
        for (String key : data.stringPropertyNames()) {
            if (key.startsWith(prefix) && !key.equals(prefix + "type")) {
                fields.put(key.substring(prefix.length()), data.getProperty(key));
            }
        }
        return new EncounterSnapshot(type, fields);
    }

    /** Writes snapshot data under an isolated prefix without storing implementation objects. */
    private static void saveEncounter(Properties data, String prefix, EncounterSnapshot snapshot) {
        data.setProperty(prefix + "type", snapshot.type());
        snapshot.data().forEach((key, value) -> data.setProperty(prefix + key, value));
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
