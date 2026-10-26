package seedu.keyboardwarrior.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.keyboardwarrior.GameDifficulty;
import seedu.keyboardwarrior.GameSave;
import seedu.keyboardwarrior.seed.RandomSnapshot;
import seedu.keyboardwarrior.typing.TypingChallenge.Difficulty;

/** Tests persistent run snapshots, malformed files and replacement failures using temporary storage. */
class GameSaveStoreTest {
    @TempDir
    Path directory;

    @Test
    void save_randomStateAndHistory_roundTripAndRejectMalformedFields() throws IOException {
        GameSaveStore store = new GameSaveStore(directory.resolve("game-save.properties"));
        GameSave save = new GameSave(1, GameDifficulty.HARD, 90, 0, 0, "rogue_ai", 25, 1,
                new RandomSnapshot(123456789, Map.of(Difficulty.EASY, 2, Difficulty.ULTIMATE, 4)));
        store.save(save);
        assertEquals(save, store.load().orElseThrow());
        Path path = directory.resolve("game-save.properties");
        String valid = Files.readString(path);
        String[][] changes = {
            {"randomState=123456789", "randomState=-1"},
            {"randomState=123456789", "randomState=281474976710656"},
            {"randomState=123456789", "randomState=invalid"},
            {"randomState=123456789", "unused=123456789"},
            {"previousPrompt.EASY=2", "previousPrompt.EASY=5"},
            {"previousPrompt.EASY=2", "previousPrompt.EASY=-1"},
            {"previousPrompt.EASY=2", "previousPrompt.UNKNOWN=2"}
        };
        for (String[] change : changes) {
            Files.writeString(path, valid.replace(change[0], change[1]));
            assertThrows(IOException.class, store::load);
        }
        Files.writeString(path, valid.replaceAll("(?m)^(randomState|previousPrompt\\.[A-Z]+)=.*\\R?", ""));
        assertEquals(new GameSave(1, GameDifficulty.HARD, 90, 0, 0, "rogue_ai", 25, 1),
                store.load().orElseThrow(), "Legacy saves without randomness must remain loadable.");
    }

    @Test
    void save_reopenedStore_restoresEveryFieldAcrossDifficultiesAndBossPhases() throws IOException {
        Path path = directory.resolve("data/game-save.properties");
        GameSaveStore store = new GameSaveStore(path);
        assertTrue(store.load().isEmpty());
        for (GameDifficulty difficulty : GameDifficulty.values()) {
            GameSave regular = new GameSave(1, difficulty, difficulty.getStartingHealth() - 10,
                    3, 0, "cipher_serpent", 23, 1);
            store.save(regular);
            assertEquals(regular, new GameSaveStore(path).load().orElseThrow());
            assertEquals("Cipher Serpent", regular.restoreEnemy().getName());
            assertEquals(23, regular.restoreEnemy().getHealth());
            for (int phase = 1; phase <= 2; phase++) {
                GameSave boss = new GameSave(1, difficulty, 25, 5, phase, "kernel_overlord", 17, 0);
                store.save(boss);
                assertEquals(boss, new GameSaveStore(path).load().orElseThrow());
                assertEquals("Kernel Overlord (Phase " + phase + ")", boss.restoreEnemy().getName());
            }
        }
    }

    @Test
    void load_invalidFields_reportsIoFailureWithoutChangingTheSave() throws IOException {
        String valid = "version=1\ndifficulty=HARD\nplayerHealth=90\nscenarioIndex=0\nbossPhase=0\n"
                + "enemyType=rogue_ai\nenemyHealth=25\nnextEnemySkillIndex=1\n";
        String[][] changes = {
            {"version=1", "version=2"},
            {"version=1", "version=no"},
            {"difficulty=HARD", "difficulty=UNKNOWN"},
            {"playerHealth=90", "playerHealth=0"},
            {"playerHealth=90", "playerHealth=101"},
            {"playerHealth=90\n", ""},
            {"scenarioIndex=0", "scenarioIndex=-1"},
            {"scenarioIndex=0", "scenarioIndex=5"},
            {"bossPhase=0", "bossPhase=1"},
            {"bossPhase=0", "bossPhase=3"},
            {"enemyType=rogue_ai", "enemyType=unknown"},
            {"enemyType=rogue_ai", "enemyType=kernel_overlord"},
            {"enemyHealth=25", "enemyHealth=0"},
            {"enemyHealth=25", "enemyHealth=31"},
            {"nextEnemySkillIndex=1", "nextEnemySkillIndex=-1"},
            {"nextEnemySkillIndex=1", "nextEnemySkillIndex=3"}
        };
        Path path = directory.resolve("game-save.properties");
        for (String[] change : changes) {
            String invalid = valid.replace(change[0], change[1]);
            Files.writeString(path, invalid);
            assertThrows(IOException.class, () -> new GameSaveStore(path).load(), invalid);
            assertEquals(invalid, Files.readString(path));
        }
        Files.writeString(path, valid.replace("scenarioIndex=0", "scenarioIndex=5")
                .replace("bossPhase=0", "bossPhase=1"));
        assertThrows(IOException.class, () -> new GameSaveStore(path).load(),
                "A boss phase cannot contain a regular opponent.");
    }

    @Test
    void save_failedReplacement_keepsDestinationContentsAndRemovesTemporaryFile() throws IOException {
        Path target = Files.createDirectory(directory.resolve("game-save.properties"));
        Path existing = target.resolve("keep.txt");
        Files.writeString(existing, "preserved");
        GameSave save = new GameSave(1, GameDifficulty.HARD, 90, 0, 0, "rogue_ai", 30, 0);

        assertThrows(IOException.class, () -> new GameSaveStore(target).save(save));

        assertEquals("preserved", Files.readString(existing));
        try (var entries = Files.list(directory)) {
            assertEquals(1, entries.count(), "Failed writes should not leave temporary save files.");
        }
    }

    @Test
    void save_invalidParent_reportsFailureAndPreservesExistingFile() throws IOException {
        Path parent = directory.resolve("blocked");
        Files.writeString(parent, "preserved");
        GameSave save = new GameSave(1, GameDifficulty.HARD, 90, 0, 0, "rogue_ai", 30, 0);

        assertThrows(IOException.class, () -> new GameSaveStore(parent.resolve("game-save.properties")).save(save));
        assertEquals("preserved", Files.readString(parent));
    }

    @Test
    void delete_onlyRemovesRunSaveAndAllowsRepeatedDeletion() throws IOException {
        Path path = directory.resolve("game-save.properties");
        Path history = directory.resolve("keyboardwarrior.txt");
        Files.writeString(history, "42.5,95.0\n");
        GameSaveStore store = new GameSaveStore(path);
        store.save(new GameSave(1, GameDifficulty.HARD, 90, 0, 0, "rogue_ai", 30, 0));

        store.delete();
        store.delete();

        assertFalse(Files.exists(path));
        assertTrue(store.load().isEmpty());
        assertEquals("42.5,95.0\n", Files.readString(history));
    }
}
