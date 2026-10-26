package seedu.keyboardwarrior;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import java.util.jar.JarFile;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Launches the actual release JAR with redirected input/output, as automated OS smoke testers do.
 * Every child process uses a temporary working directory so gameplay cannot alter real saved records.
 */
class KeyboardWarriorSmokeTest {
    private static final int PROCESS_TIMEOUT_SECONDS = 10;

    @TempDir
    private Path tempDir;

    @Test
    void packagedJar_exitCommand_displaysMenuAndExitsCleanly() throws Exception {
        ProcessResult result = runJar("exit\n");

        assertCleanExit(result);
        assertTrue(result.output().contains("1. Start"), result.output());
        assertTrue(result.output().contains("2. Records"), result.output());
        assertTrue(result.output().contains("Exit."), result.output());
    }

    @Test
    void packagedJar_endOfInput_displaysMenuAndExitsCleanly() throws Exception {
        ProcessResult result = runJar("");

        assertCleanExit(result);
        assertTrue(result.output().contains("1. Start"), result.output());
        assertTrue(result.output().contains("Exit."), result.output());
    }

    @Test
    void packagedJar_endOfInputDuringChallenge_exitsWithoutScoringAnEmptyAttempt() throws Exception {
        ProcessResult result = runJar("Start\n2026\nPing\n");

        assertCleanExit(result);
        assertTrue(result.output().contains("Type the following text and press Enter to attack:"), result.output());
        assertTrue(result.output().contains("Exit."), result.output());
        assertFalse(result.output().contains("WPM:"), result.output());
        assertFalse(Files.exists(tempDir.resolve("working directory/data/keyboardwarrior.txt")));
    }

    @Test
    void packagedJar_unterminatedExitCommand_processesFinalLineBeforeEndOfInput() throws Exception {
        ProcessResult result = runJar("exit");

        assertCleanExit(result);
        assertTrue(result.output().contains("1. Start"), result.output());
        assertTrue(result.output().contains("Exit."), result.output());
    }

    @Test
    void packagedJar_unterminatedRecordsCommand_doesNotDiscardFinalLine() throws Exception {
        ProcessResult result = runJar("records");

        assertCleanExit(result);
        assertTrue(result.output().contains("Typing history (oldest to newest)"), result.output());
        assertTrue(result.output().contains("Exit."), result.output());
    }

    @Test
    void packagedJar_invalidCommand_reportsErrorAndAcceptsNextCommand() throws Exception {
        ProcessResult result = runJar("invalid menu command\nexit\n");

        assertCleanExit(result);
        assertTrue(result.output().contains("Invalid option."), result.output());
        assertTrue(result.output().contains("Exit."), result.output());
    }

    @Test
    void packagedJar_typingAttempt_displaysPassageAndEvaluatesSubmittedText() throws Exception {
        ProcessResult result = runJar("Start\n2026\nPing\nsmoke test answer\nExit\n");

        assertCleanExit(result);
        assertTrue(result.output().contains("Starting run with seed: 2026"), result.output());
        assertTrue(result.output().contains("Scenario 1/5"), result.output());
        assertTrue(result.output().contains("Type the following text and press Enter to attack:"), result.output());
        assertTrue(result.output().contains("WPM:"), result.output());
        assertTrue(result.output().contains("Accuracy:"), result.output());
        assertTrue(Files.exists(tempDir.resolve("working directory/data/keyboardwarrior.txt")));
    }

    @Test
    void packagedJar_manifest_enablesNativeAccessWithoutLauncherFlags() throws IOException {
        try (JarFile jar = new JarFile(releaseJar().toFile())) {
            var attributes = jar.getManifest().getMainAttributes();
            assertEquals("seedu.keyboardwarrior.KeyboardWarrior", attributes.getValue("Main-Class"));
            assertEquals("ALL-UNNAMED", attributes.getValue("Enable-Native-Access"));
        }
    }

    /**
     * Runs plain java -jar from a directory and JAR path containing spaces, without an inherited terminal.
     * Output goes to temporary files to prevent a full output pipe from deadlocking the child process.
     */
    private ProcessResult runJar(String input) throws Exception {
        Path workingDirectory = Files.createDirectory(tempDir.resolve("working directory"));
        Path jar = Files.copy(releaseJar(), workingDirectory.resolve("Keyboard Warrior demo.jar"));
        Path outputFile = tempDir.resolve("stdout.txt");
        Path errorFile = tempDir.resolve("stderr.txt");
        String executable = System.getProperty("os.name").startsWith("Windows") ? "java.exe" : "java";
        Path java = Path.of(System.getProperty("java.home"), "bin", executable);
        ProcessBuilder builder = new ProcessBuilder(java.toString(), "-ea", "-jar", jar.toString())
                .directory(workingDirectory.toFile())
                .redirectOutput(outputFile.toFile())
                .redirectError(errorFile.toFile());
        // The executable JAR must be self-contained rather than relying on the developer's VM options.
        builder.environment().remove("JAVA_TOOL_OPTIONS");
        builder.environment().remove("JDK_JAVA_OPTIONS");
        builder.environment().remove("_JAVA_OPTIONS");
        builder.environment().put("TERM", "dumb");
        Process process = builder.start();
        try {
            try (OutputStream keyboard = process.getOutputStream()) {
                keyboard.write(input.getBytes(StandardCharsets.UTF_8));
            }
            boolean completed = process.waitFor(PROCESS_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            assertTrue(completed, "The packaged game must exit instead of hanging after input ends.");
            return new ProcessResult(process.exitValue(), Files.readString(outputFile), Files.readString(errorFile));
        } finally {
            if (process.isAlive()) {
                process.destroyForcibly();
                process.waitFor(PROCESS_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            }
        }
    }

    /** Returns the freshly built distributable selected by Gradle's test task. */
    private static Path releaseJar() {
        String path = System.getProperty("keyboardwarrior.smoke.jar");
        assertTrue(path != null && !path.isBlank(), "Run these packaged-JAR checks through the Gradle test task.");
        return Path.of(path);
    }

    /** Requires clean startup and plain text output, not merely an exit code of zero. */
    private static void assertCleanExit(ProcessResult result) {
        assertEquals(0, result.exitCode(), result.error());
        assertEquals("", result.error(), "Normal redirected launches must not emit startup warnings or errors.");
        assertFalse(result.output().contains("This console cannot display"), result.output());
        assertFalse(result.output().contains("\u001b"),
                "Plain consoles must not receive ANSI cursor-control sequences.");
    }

    /** Captures the child process's outcome after its streams have been closed. */
    private record ProcessResult(int exitCode, String output, String error) {
    }
}
