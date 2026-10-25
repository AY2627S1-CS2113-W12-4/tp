package seedu.keyboardwarrior.typing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import seedu.keyboardwarrior.ui.CombatUi;

/**
 * Verifies synchronous challenge evaluation using controlled input and timing.
 */
class TypingChallengeRunnerTest {
    private final ManualClock clock = new ManualClock();
    private final StubCombatUi ui = new StubCombatUi();
    private final TypingChallengeRunner runner = new TypingChallengeRunner(ui, clock);

    /**
     * Checks that initialization neither starts the timer nor reads input.
     */
    @Test
    void init_preparesChallengeWithoutStartingClockOrReadingInput() {
        runner.init(new TypingChallenge("hello world", 15));

        assertEquals(0, clock.startCount);
        assertEquals(0, ui.submissionCount);
    }

    /**
     * Checks that synchronous submission at the deadline has no timing penalty.
     */
    @Test
    void run_exactDeadline_acceptsSubmissionWithoutPenalty() {
        runner.init(new TypingChallenge("hello world", 15));
        clock.elapsedSeconds = 15.0;
        ui.submission = "hello world";

        TypingResult result = runner.run();

        assertEquals("hello world", ui.displayedPrompt);
        assertEquals(1, ui.submissionCount);
        assertEquals(1, clock.startCount);
        assertEquals(15, clock.durationSeconds);
        assertEquals(100.0, result.getAccuracyPercent());
        assertEquals(8.8, result.getWpm(), 0.000001);
        assertEquals(15.0, result.getElapsedSeconds());
        assertFalse(result.isTimeLimitExceeded());
        assertEquals(1.0, result.getTimeDamageMultiplier());
    }

    /**
     * Checks that synchronous submission after the deadline is still scored without a penalty.
     */
    @Test
    void run_afterDeadline_scoresLateTextWithoutPenalty() {
        runner.init(new TypingChallenge("hello world", 15));
        clock.elapsedSeconds = Math.nextUp(15.0);
        ui.submission = "hello world";

        TypingResult result = runner.run();

        assertEquals(100.0, result.getAccuracyPercent());
        assertTrue(result.getWpm() > 0.0);
        assertEquals(clock.elapsedSeconds, result.getElapsedSeconds());
        assertTrue(result.isTimeLimitExceeded());
        assertEquals(1.0, result.getTimeDamageMultiplier());
    }

    /**
     * Checks accuracy and word diagnostics for a partially correct answer.
     */
    @Test
    void run_partialAnswer_reportsAccuracyAndWordErrors() {
        runner.init(new TypingChallenge("hello world", 15));
        clock.elapsedSeconds = 5.0;
        ui.submission = "hello worle";

        TypingResult result = runner.run();

        assertEquals(1000.0 / 11.0, result.getAccuracyPercent(), 0.000001);
        assertEquals(26.4, result.getWpm(), 0.000001);
        assertEquals(1, result.getWordErrors().size());
        assertFalse(result.isTimeLimitExceeded());
    }

    /**
     * Checks that one incorrect submission finishes the synchronous challenge.
     */
    @Test
    void run_wrongAnswer_finishesAfterOneSubmission() {
        runner.init(new TypingChallenge("hello world", 15));
        clock.elapsedSeconds = 5.0;
        ui.submission = "wrong answer";

        TypingResult result = runner.run();

        assertTrue(result.getAccuracyPercent() < 100.0);
        assertEquals(2, result.getWordErrors().size());
        assertEquals(1, ui.submissionCount);
    }

    /**
     * Checks that an empty late answer reports missing words and zero typing metrics.
     */
    @Test
    void run_emptyLateAnswer_reportsMissingWordsAndZeroMetrics() {
        runner.init(new TypingChallenge("hello world", 15));
        clock.elapsedSeconds = 16.0;
        ui.submission = "";

        TypingResult result = runner.run();

        assertEquals(0.0, result.getAccuracyPercent());
        assertEquals(0.0, result.getWpm());
        assertEquals(2, result.getWordErrors().size());
        assertTrue(result.getWordErrors().stream().allMatch(error -> error.getType() == ErrorType.MISSING));
        assertEquals(1.0, result.getTimeDamageMultiplier());
    }

    /**
     * Checks that reinitializing the runner replaces the prompt and deadline.
     */
    @Test
    void init_nextChallenge_usesNewPromptAndTimeLimit() {
        runner.init(new TypingChallenge("old prompt", 15));
        clock.elapsedSeconds = 20.0;
        ui.submission = "old prompt";
        runner.run();
        runner.init(new TypingChallenge("fresh prompt", 30));
        clock.elapsedSeconds = 2.0;
        ui.submission = "fresh prompt";

        TypingResult result = runner.run();

        assertEquals("fresh prompt", ui.displayedPrompt);
        assertEquals(2, clock.startCount);
        assertEquals(30, clock.durationSeconds);
        assertEquals(100.0, result.getAccuracyPercent());
        assertEquals(30.0, result.getRequiredSeconds());
        assertEquals(2.0, result.getElapsedSeconds());
        assertFalse(result.isTimeLimitExceeded());
    }

    /**
     * Supplies a fixed elapsed duration and records when timing starts.
     */
    private static class ManualClock extends ChallengeClock {
        private double elapsedSeconds;
        private int durationSeconds;
        private int startCount;

        /**
         * Records the requested duration without changing the elapsed time supplied by the test.
         *
         * @param seconds duration recorded for this attempt
         */
        @Override
        public void start(int seconds) {
            durationSeconds = seconds;
            startCount++;
        }

        /**
         * Returns the elapsed duration supplied by the test.
         *
         * @return configured elapsed seconds
         */
        @Override
        public double getElapsedSeconds() {
            return elapsedSeconds;
        }
    }

    /**
     * Records prompts and returns a supplied answer without reading console input.
     */
    private class StubCombatUi extends CombatUi {
        private String submission;
        private String displayedPrompt;
        private int submissionCount;

        /**
         * Verifies that timing has started, records the prompt and returns the configured answer.
         *
         * @param typingChallenge challenge text presented by the runner
         * @return configured submission
         */
        @Override
        public String getTypingSubmission(String typingChallenge) {
            assertTrue(clock.startCount > 0, "The clock must start before input is read");
            displayedPrompt = typingChallenge;
            submissionCount++;
            return submission;
        }
    }
}
