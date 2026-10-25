package seedu.keyboardwarrior.typing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.keyboardwarrior.typing.TypingChallenge.Difficulty;

/**
 * Tests length-aware deadlines, difficulty minimums and shared passage history.
 */
class TypingChallengeFactoryTest {
    /**
     * Checks that every prompt allows fifty WPM with twenty percent extra time.
     */
    @Test
    void generate_eachDifficulty_allowsFiftyWpmWithTwentyPercentExtraTime() {
        TypingChallengeFactory factory = new TypingChallengeFactory(new PromptBank(new Random(42)));

        for (Difficulty difficulty : Difficulty.values()) {
            Set<String> checkedPrompts = new HashSet<>();
            for (int attempt = 0; attempt < 100; attempt++) {
                TypingChallenge challenge = factory.generate(difficulty);
                if (!checkedPrompts.add(challenge.getText())) {
                    continue;
                }
                String normalizedText = challenge.getText().trim().replaceAll("\\s+", " ");
                double completionSeconds = normalizedText.length() / 5.0 / 50.0 * 60.0;

                assertTrue(challenge.getTime() >= completionSeconds * 1.2,
                        "Every prompt should allow 50 WPM plus twenty percent extra time.");
                TypingResult result = TypingEvaluator.evaluateTyping(challenge.getText(), challenge.getText(),
                        completionSeconds, challenge.getTime());
                assertEquals(50.0, result.getWpm(), 0.000001);
                assertFalse(result.isTimeLimitExceeded());
            }
            assertEquals(5, checkedPrompts.size(), "The test should check every existing prompt for the level.");
        }
    }

    /**
     * Checks that short prompts retain the minimum deadline for their difficulty.
     */
    @Test
    void generate_shortPrompt_preservesDifficultyMinimums() {
        TypingChallengeFactory factory = new TypingChallengeFactory(new FixedPromptBank("short"));

        assertEquals(15, factory.generate(Difficulty.EASY).getTime());
        assertEquals(30, factory.generate(Difficulty.MEDIUM).getTime());
        assertEquals(30, factory.generate(Difficulty.HARD).getTime());
        assertEquals(45, factory.generate(Difficulty.ULTIMATE).getTime());
    }

    /**
     * Checks that deadline calculation normalizes whitespace and rounds the allowance up.
     */
    @Test
    void generate_longPrompt_normalizesWhitespaceAndRoundsAllowanceUp() {
        TypingChallengeFactory factory = new TypingChallengeFactory(new FixedPromptBank("  abcdefghij  ".repeat(10)));
        // Ten ten-character words and nine normalized spaces require ceil(109 * 0.288) seconds.
        assertEquals(32, factory.generate(Difficulty.EASY).getTime());
    }

    /**
     * Checks that generating a challenge requires a difficulty.
     */
    @Test
    void generate_nullDifficulty_throwsException() {
        TypingChallengeFactory factory = new TypingChallengeFactory();

        assertThrows(NullPointerException.class, () -> factory.generate(null));
    }

    /**
     * Checks that factories sharing a prompt bank avoid consecutive repeated passages.
     */
    @Test
    void generate_factoriesSharingBank_doNotRepeatPreviousPassage() {
        PromptBank bank = new PromptBank(new Random(42));
        TypingChallengeFactory firstFactory = new TypingChallengeFactory(bank);
        TypingChallengeFactory nextFactory = new TypingChallengeFactory(bank);

        assertNotEquals(firstFactory.generate(Difficulty.MEDIUM).getText(),
                nextFactory.generate(Difficulty.MEDIUM).getText());
    }

    /**
     * Supplies a known passage so deadline boundaries can be checked independently of random choice.
     */
    private static class FixedPromptBank extends PromptBank {
        private final String prompt;

        /**
         * Creates a bank returning the supplied passage for every difficulty.
         *
         * @param prompt passage returned for every difficulty
         */
        FixedPromptBank(String prompt) {
            this.prompt = prompt;
        }

        /**
         * Returns the fixture passage for every requested difficulty.
         *
         * @param difficulty requested difficulty; unused by this fixture
         * @return configured passage
         */
        @Override
        public String getRandomPrompt(Difficulty difficulty) {
            return prompt;
        }
    }
}
