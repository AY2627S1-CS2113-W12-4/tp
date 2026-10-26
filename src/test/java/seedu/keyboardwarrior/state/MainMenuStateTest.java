package seedu.keyboardwarrior.state;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.keyboardwarrior.GameDifficulty;
import seedu.keyboardwarrior.GameSave;
import seedu.keyboardwarrior.storage.GameSaveStore;
import seedu.keyboardwarrior.storage.SaveStore;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests menu history display and persistence using real temporary files.
 */
class MainMenuStateTest {
    @TempDir
    Path tempDir;

    /**
     * Checks that gameplay started from a menu with custom history appends to that same file.
     */
    @Test
    void continue_savedRun_restoresSavedDifficultyAndSharesHistoryLocation() throws IOException {
        GameSaveStore runStore = new GameSaveStore(tempDir.resolve("game-save.properties"));
        GameSave save = new GameSave(1, GameDifficulty.CODER, 65, 2, 0, "cipher_serpent", 23, 1);
        runStore.save(save);
        PrintStream original = System.out;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (PrintStream capture = new PrintStream(output, true, StandardCharsets.UTF_8)) {
            System.setOut(capture);
            StateMachine machine = new StateMachine();
            machine.start(new MainMenuState(machine, new SaveStore(tempDir.resolve("history.txt")),
                    GameDifficulty.EASY));
            machine.handleInput("5");
            String report = output.toString(StandardCharsets.UTF_8);
            assertTrue(report.contains("Resuming saved run. Difficulty: Coder"));
            assertTrue(report.contains("Scenario 3/5"));
            assertTrue(report.contains("Cipher Serpent | HP: 23"));
            assertTrue(report.contains("HP: 65"));
            machine.handleInput("Encryption");
            assertTrue(output.toString(StandardCharsets.UTF_8).contains("Time limit: 48 seconds"));
            machine.handleInput("");
            assertEquals(1, Files.readAllLines(tempDir.resolve("history.txt")).size());
            machine.handleInput("exit");
        } finally {
            System.setOut(original);
        }
    }

    @Test
    void continue_missingOrInvalidSave_leavesMenuUsableAndDoesNotOverwriteFile() throws IOException {
        PrintStream original = System.out;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Path path = tempDir.resolve("game-save.properties");
        try (PrintStream capture = new PrintStream(output, true, StandardCharsets.UTF_8)) {
            System.setOut(capture);
            StateMachine machine = new StateMachine();
            machine.start(new MainMenuState(machine, new SaveStore(tempDir.resolve("history.txt"))));
            machine.handleInput("continue");
            assertTrue(output.toString(StandardCharsets.UTF_8).contains("No saved game to continue"));
            Files.writeString(path, "version=broken");
            machine.handleInput(" CoNtInUe ");
            assertTrue(output.toString(StandardCharsets.UTF_8).contains("Could not load game"));
            assertEquals("version=broken", Files.readString(path));
            machine.handleInput("records");
            assertTrue(output.toString(StandardCharsets.UTF_8).contains("No typing records saved yet"));
            assertTrue(machine.isRunning());
            machine.stop();
        } finally {
            System.setOut(original);
        }
    }

    @Test
    void chooseDifficulty_numbersAndNames_returnToMenuAndAffectNextRun() {
        String[] choices = {"1", " nOrMaL ", "3", " CoDeR "};
        String[] names = {"Easy", "Normal", "Hard", "Coder"};
        int[] health = {400, 200, 100, 80};
        for (int index = 0; index < choices.length; index++) {
            PrintStream original = System.out;
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            try (PrintStream capture = new PrintStream(output, true, StandardCharsets.UTF_8)) {
                System.setOut(capture);
                StateMachine machine = new StateMachine();
                machine.start(new MainMenuState(machine, new SaveStore(tempDir.resolve("history.txt"))));
                assertTrue(output.toString(StandardCharsets.UTF_8).contains("Current difficulty: Hard"));
                machine.handleInput(index % 2 == 0 ? "4" : " Choose Difficulty ");
                machine.handleInput("invalid");
                assertTrue(output.toString(StandardCharsets.UTF_8).contains("Invalid difficulty"));
                machine.handleInput(choices[index]);
                machine.handleInput("2");
                assertTrue(output.toString(StandardCharsets.UTF_8).contains("Current difficulty: " + names[index]));
                assertTrue(output.toString(StandardCharsets.UTF_8).contains("No typing records saved yet"));
                machine.handleInput("1");
                machine.handleInput("3");
                assertTrue(output.toString(StandardCharsets.UTF_8).contains("Starting run with seed: 3"));
                assertTrue(output.toString(StandardCharsets.UTF_8).contains("HP: " + health[index]));
                machine.handleInput("exit");
            } finally {
                System.setOut(original);
            }
        }
    }

    @Test
    void menu_exitNumberAndDifficultyExit_stopApplication() {
        StateMachine menuMachine = new StateMachine();
        menuMachine.start(new MainMenuState(menuMachine, new SaveStore(tempDir.resolve("history.txt"))));
        menuMachine.handleInput("3");
        assertFalse(menuMachine.isRunning());

        StateMachine difficultyMachine = new StateMachine();
        difficultyMachine.start(new MainMenuState(difficultyMachine, new SaveStore(tempDir.resolve("history.txt"))));
        difficultyMachine.handleInput("4");
        difficultyMachine.update();
        difficultyMachine.handleInput("exit");
        assertFalse(difficultyMachine.isRunning());
    }

    @Test
    void start_customHistory_savesAttemptsToTheSameFile() throws IOException {
        Path history = tempDir.resolve("history.txt");
        Files.writeString(history, "42.5,87.25\n");
        PrintStream original = System.out;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (PrintStream capture = new PrintStream(output, true, StandardCharsets.UTF_8)) {
            System.setOut(capture);
            StateMachine machine = new StateMachine();
            machine.start(new MainMenuState(machine, new SaveStore(history)));

            machine.handleInput("start");
            machine.handleInput("42");
            machine.handleInput("Ping");
            machine.handleInput("");

            assertEquals(List.of("42.5,87.25", "0.0,0.0"), Files.readAllLines(history));
            machine.handleInput("exit");
        } finally {
            System.setOut(original);
        }
    }

    /**
     * Checks that numeric and text seeds reproduce the first enemy and prompt through the menu.
     */
    @Test
    void start_sameSeed_repeatsEnemyAndPrompt() {
        assertEquals(startWithSeed("42"), startWithSeed(" 42 "));
        String textSeed = "keyboard-warrior";
        assertEquals(startWithSeed(textSeed), startWithSeed(Long.toString(textSeed.hashCode())));
        assertNotEquals(startWithSeed("42"), startWithSeed("43"));
    }

    /**
     * Captures the first encounter and challenge started through the real menu.
     *
     * @param seed numeric or text seed submitted at the seed prompt
     * @return combat and challenge output, excluding the seed announcement
     */
    private String startWithSeed(String seed) {
        PrintStream original = System.out;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (PrintStream capture = new PrintStream(output, true, StandardCharsets.UTF_8)) {
            System.setOut(capture);
            StateMachine machine = new StateMachine();
            machine.start(new MainMenuState(machine, new SaveStore(tempDir.resolve("history.txt"))));
            machine.handleInput("start");
            machine.handleInput(seed);
            machine.handleInput("Ping");
            machine.handleInput("exit");
        } finally {
            System.setOut(original);
        }
        String report = output.toString(StandardCharsets.UTF_8);
        assertTrue(report.contains("Scenario 1/5"));
        return report.substring(report.indexOf("Scenario 1/5"));
    }

    /**
     * Checks that viewing saved metrics preserves their order and leaves the menu usable.
     */
    @Test
    void records_savedHistory_displaysMetricsAndStaysInMenu() throws IOException {
        Files.writeString(tempDir.resolve("history.txt"), "42.5,87.25\n60.0,100.0\n");
        String output = viewRecords();
        assertTrue(output.contains("42.50"));
        assertTrue(output.contains("87.25%"));
        assertTrue(output.contains("60.00"));
        assertTrue(output.indexOf("42.50") < output.indexOf("60.00"));
        assertTrue(output.contains("oldest to newest"));
    }

    /**
     * Checks the empty-history message when the save file does not exist.
     */
    @Test
    void records_missingHistory_displaysEmptyMessage() {
        assertTrue(viewRecords().contains("No typing records saved yet"));
    }

    /**
     * Checks that malformed history produces a message without crashing the menu.
     */
    @Test
    void records_invalidHistory_displaysErrorWithoutCrashing() throws IOException {
        Files.writeString(tempDir.resolve("history.txt"), "invalid\n");
        assertTrue(viewRecords().contains("Could not load typing history"));
    }

    /**
     * Captures the menu output and verifies that viewing records leaves the menu usable.
     *
     * @return captured output from viewing records and checking menu usability
     */
    private String viewRecords() {
        PrintStream original = System.out;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (PrintStream capture = new PrintStream(output, true, StandardCharsets.UTF_8)) {
            System.setOut(capture);
            StateMachine machine = new StateMachine();
            machine.start(new MainMenuState(machine, new SaveStore(tempDir.resolve("history.txt"))));
            machine.handleInput(" ReCoRdS ");
            assertTrue(machine.isRunning());
            machine.handleInput("unknown");
            assertTrue(output.toString(StandardCharsets.UTF_8).contains("Please enter Start, Records, Exit"));
            machine.stop();
        } finally {
            System.setOut(original);
        }
        return output.toString(StandardCharsets.UTF_8);
    }

    /** Checks both commands display only the best attempt, breaking WPM ties by accuracy. */
    @Test
    void leaderboard_savedHistory_showsHighestScoreWithoutChangingHistory() throws IOException {
        Path history = tempDir.resolve("history.txt");
        String records = "42.5,100.0\n60.0,80.0\n60.0,95.0\n60.0,95.0\n50.0,100.0\n";
        Files.writeString(history, records);
        for (String command : List.of("6", " LeAdErBoArD ")) {
            String output = viewLeaderboard(command);
            List<String> rows = output.lines().filter(line -> line.contains("%")).toList();
            assertEquals(1, rows.size());
            assertEquals(List.of("60.00", "95.00%"), List.of(rows.getFirst().trim().split("\\s+")));
            assertEquals(records, Files.readString(history));
        }
        Files.writeString(history, "20.0,100.0\n".repeat(10));
        String retainedScore = viewLeaderboard("leaderboard");
        assertTrue(retainedScore.contains("All-time highest score"));
        List<String> rows = retainedScore.lines().filter(line -> line.contains("%")).toList();
        assertEquals(1, rows.size());
        assertEquals(List.of("60.00", "95.00%"), List.of(rows.getFirst().trim().split("\\s+")));
    }

    /** Checks that a first-time player can open the leaderboard without a history file. */
    @Test
    void leaderboard_missingHistory_displaysEmptyMessage() {
        assertTrue(viewLeaderboard("6").contains("No typing records saved yet"));
    }

    /** Checks that corrupt history produces a readable error and leaves the menu usable. */
    @Test
    void leaderboard_invalidHistory_displaysErrorWithoutCrashing() throws IOException {
        Files.writeString(tempDir.resolve("history.txt"), "invalid\n");
        assertTrue(viewLeaderboard("leaderboard").contains("Could not load leaderboard"));
    }

    /** Captures leaderboard output and checks that Start still works afterwards. */
    private String viewLeaderboard(String command) {
        PrintStream original = System.out;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (PrintStream capture = new PrintStream(output, true, StandardCharsets.UTF_8)) {
            System.setOut(capture);
            StateMachine machine = new StateMachine();
            machine.start(new MainMenuState(machine, new SaveStore(tempDir.resolve("history.txt"))));
            assertTrue(output.toString(StandardCharsets.UTF_8).contains("6. Leaderboard"));
            output.reset();
            machine.handleInput(command);
            String report = output.toString(StandardCharsets.UTF_8);
            assertTrue(machine.isRunning());
            machine.handleInput("start");
            assertTrue(output.toString(StandardCharsets.UTF_8).contains("Enter a seed"));
            machine.stop();
            return report;
        } finally {
            System.setOut(original);
        }
    }
}
