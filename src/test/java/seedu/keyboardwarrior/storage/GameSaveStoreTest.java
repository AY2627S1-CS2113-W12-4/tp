package seedu.keyboardwarrior.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.keyboardwarrior.GameDifficulty;
import seedu.keyboardwarrior.GameSave;
import seedu.keyboardwarrior.combat.Player;
import seedu.keyboardwarrior.combat.SkillRegistry;
import seedu.keyboardwarrior.encounter.EncounterSnapshot;
import seedu.keyboardwarrior.seed.RandomSnapshot;
import seedu.keyboardwarrior.typing.TypingChallenge.Difficulty;

/** Tests persistent run snapshots, malformed files and replacement failures using temporary storage. */
class GameSaveStoreTest {
    @TempDir
    Path directory;

    /** Event rewards and penalties survive a restart together with the selected event's stage. */
    @Test
    void save_eventStages_restoresEarnedSkillsBonusTimeAndHealthAboveStartingValue() throws IOException {
        Player player = new Player("Player", 120);
        player.addSkill(SkillRegistry.getSkill("overclock"));
        player.setBonusTime(-7);
        GameSaveStore store = new GameSaveStore(directory.resolve("game-save.properties"));
        List<EncounterSnapshot> stages = List.of(
                new EncounterSnapshot("puzzle", Map.of("phase", "NOT_STARTED", "question", "Two plus two?",
                        "answer", "4")),
                new EncounterSnapshot("puzzle", Map.of("phase", "ACCEPTING_ANSWER", "question", "Two plus two?",
                        "answer", "4")),
                new EncounterSnapshot("reaction", Map.of("phase", "WAITING", "targetWord", "step back",
                        "delayMillis", "1234")),
                new EncounterSnapshot("reaction", Map.of("phase", "ACCEPTING_ANSWER", "targetWord", "step back",
                        "delayMillis", "0")),
                new EncounterSnapshot("dialogue", Map.of("phase", "ACCEPTING_ANSWER", "node", "1")),
                new EncounterSnapshot("dialogue", Map.of("phase", "ACCEPTING_ANSWER", "node", "2")));
        RandomSnapshot random = new RandomSnapshot(1234, Map.of(Difficulty.EASY, 2));
        for (EncounterSnapshot stage : stages) {
            GameSave save = GameSave.forEncounter(GameDifficulty.HARD, player, 2, 0, random, stage);
            store.save(save);
            GameSave reopened = new GameSaveStore(directory.resolve("game-save.properties")).load().orElseThrow();
            assertEquals(save, reopened);
            Player restored = reopened.restorePlayer();
            assertEquals(120, restored.getHealth());
            assertEquals(-7, restored.getBonusTime());
            assertEquals(player.getActiveSkills(), restored.getActiveSkills());
            assertThrows(IllegalStateException.class, reopened::restoreEnemy);
        }
    }

    /** Offered content and random position are retained instead of rerolling choices after Continue. */
    @Test
    void save_threeChoices_roundTripsWithoutLosingOfferOrderOrPlayerEffects() throws IOException {
        Player player = new Player("Player", 105);
        player.setBonusTime(12);
        List<EncounterSnapshot> choices = List.of(
                combatSnapshot(),
                new EncounterSnapshot("dialogue", Map.of("phase", "NOT_STARTED", "node", "0")),
                new EncounterSnapshot("puzzle", Map.of("phase", "NOT_STARTED", "question", "Decode 1010",
                        "answer", "10")));
        GameSave save = GameSave.forChoices(GameDifficulty.HARD, player, 0,
                new RandomSnapshot(5678, Map.of()), choices);
        GameSaveStore store = new GameSaveStore(directory.resolve("game-save.properties"));
        store.save(save);
        GameSave loaded = store.load().orElseThrow();
        assertEquals(save, loaded);
        assertEquals(choices, loaded.choices());
        assertEquals(12, loaded.restorePlayer().getBonusTime());
        assertThrows(UnsupportedOperationException.class, () -> loaded.choices().clear());
        assertThrows(UnsupportedOperationException.class, () -> loaded.skillIds().clear());
    }

    /** Corrupt new-format content must be rejected without replacing the player's saved file. */
    @Test
    void load_invalidCurrentFormat_rejectsMalformedSkillsStagesAndMixedModes() throws IOException {
        Player player = new Player("Player", 110);
        Path path = directory.resolve("game-save.properties");
        GameSaveStore store = new GameSaveStore(path);
        store.save(GameSave.forEncounter(GameDifficulty.HARD, player, 0, 0, null, combatSnapshot()));
        String valid = Files.readString(path);
        String[][] changes = {
            {"choiceCount=0", "choiceCount=1"},
            {"bonusTime=0", "bonusTime=not-a-number"},
            {"skillIds=ping,brute_force,buffer_overflow,sigkill,encryption", "skillIds=ping,ping"},
            {"skillIds=ping,brute_force,buffer_overflow,sigkill,encryption",
                "skillIds=ping,brute_force,buffer_overflow,sigkill,encryption,unknown"},
            {"activeEncounter.type=combat", "activeEncounter.type=unknown"},
            {"activeEncounter.enemyHealth=30", "activeEncounter.enemyHealth=0"},
            {"playerHealth=110", "playerHealth=0"},
            {"activeEncounter.type=combat", "unusedType=combat"}
        };
        for (String[] change : changes) {
            assertTrue(valid.contains(change[0]), change[0]);
            String malformed = valid.replace(change[0], change[1]);
            Files.writeString(path, malformed);
            assertThrows(IOException.class, store::load);
            assertEquals(malformed, Files.readString(path));
        }
        Files.writeString(path, valid + "choice.0.type=dialogue\nchoice.0.phase=NOT_STARTED\nchoice.0.node=0\n");
        assertThrows(IOException.class, store::load);
    }

    /** Version-one files acquire the original loadout and migrate on the next save. */
    @Test
    void load_legacyCombatFile_migratesToCurrentFormatWithoutChangingStoredTurn() throws IOException {
        Path path = directory.resolve("game-save.properties");
        Files.writeString(path, "version=1\ndifficulty=HARD\nplayerHealth=90\nscenarioIndex=0\nbossPhase=0\n"
                + "enemyType=rogue_ai\nenemyHealth=25\nnextEnemySkillIndex=1\n");
        GameSaveStore store = new GameSaveStore(path);
        GameSave migrated = store.load().orElseThrow();
        assertEquals(GameSave.CURRENT_VERSION, migrated.version());
        assertEquals(90, migrated.restorePlayer().getHealth());
        assertEquals(0, migrated.restorePlayer().getBonusTime());
        assertEquals(SkillRegistry.getDefaultPlayerSkillIds(), migrated.skillIds());
        assertEquals(25, migrated.restoreEnemy().getHealth());
        assertEquals(1, migrated.nextEnemySkillIndex());
        store.save(migrated);
        assertTrue(Files.readString(path).contains("version=2"));
        assertEquals(migrated, store.load().orElseThrow());
    }

    /** Creates a living first-scenario opponent used by pending-choice and active-combat saves. */
    private static EncounterSnapshot combatSnapshot() {
        return new EncounterSnapshot("combat", Map.of("enemyType", "rogue_ai", "enemyHealth", "30",
                "nextEnemySkillIndex", "0"));
    }

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
            {"version=1", "version=3"},
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
