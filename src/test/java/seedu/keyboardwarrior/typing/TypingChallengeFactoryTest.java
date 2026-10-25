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

import seedu.keyboardwarrior.GameDifficulty;
import seedu.keyboardwarrior.combat.SkillRegistry;
import seedu.keyboardwarrior.typing.TypingChallenge.Difficulty;

/** Tests length-aware deadlines, difficulty minimums and shared passage history. */
class TypingChallengeFactoryTest {
    @Test
    void generateForSkill_eachGameDifficulty_scalesAllSkillDeadlines() {
        String[] skillIds = {"ping", "brute_force", "buffer_overflow", "sigkill", "encryption"};
        int[][] expectedSeconds = {
            {60, 120, 120, 180, 240},
            {30, 60, 60, 90, 120},
            {15, 30, 30, 45, 60},
            {12, 24, 24, 36, 48}
        };
        GameDifficulty[] difficulties = GameDifficulty.values();
        for (int mode = 0; mode < difficulties.length; mode++) {
            TypingChallengeFactory factory = new TypingChallengeFactory(new FixedPromptBank("short"),
                    difficulties[mode]);
            for (int skill = 0; skill < skillIds.length; skill++) {
                assertEquals(expectedSeconds[mode][skill],
                        factory.generateForSkill(SkillRegistry.getSkill(skillIds[skill])).getTime());
            }
        }
        TypingChallengeFactory coderFactory = new TypingChallengeFactory(
                new FixedPromptBank("a".repeat(420)), GameDifficulty.CODER);
        assertEquals(97, coderFactory.generateForSkill(SkillRegistry.getSkill("sigkill")).getTime(),
                "Coder scales the full base deadline, then rounds 96.8 seconds up.");
    }

    @Test
    void generateForSkill_encryption_usesRandomSymbolsAndFixedDeadline() {
        TypingChallengeFactory factory = new TypingChallengeFactory(new FixedPromptBank("a".repeat(420)));
        Set<String> passages = new HashSet<>();
        String allowedSymbols = "!@#$%^&*()\\_+/*{}|:\"<>?";

        assertEquals(Difficulty.ULTIMATE, SkillRegistry.getSkill("encryption").getDifficulty());
        for (int attempt = 0; attempt < 100; attempt++) {
            TypingChallenge challenge = factory.generateForSkill(SkillRegistry.getSkill("encryption"));
            assertEquals(60, challenge.getTime());
            assertTrue(challenge.getText().length() >= 420 && challenge.getText().length() <= 426);
            for (String word : challenge.getText().split(" ", -1)) {
                assertTrue(word.length() >= 1 && word.length() <= 6);
                assertTrue(word.chars().allMatch(character -> allowedSymbols.indexOf(character) >= 0));
            }
            passages.add(challenge.getText());
        }
        assertEquals(100, passages.size(), "Encryption should generate fresh random passages.");
    }

    @Test
    void generateForSkill_sigkill_keepsUltimatePassageAndLengthAwareDeadline() {
        TypingChallengeFactory factory = new TypingChallengeFactory(new FixedPromptBank("a".repeat(420)));

        TypingChallenge challenge = factory.generateForSkill(SkillRegistry.getSkill("sigkill"));

        assertEquals("a".repeat(420), challenge.getText());
        assertEquals(121, challenge.getTime());
        assertThrows(NullPointerException.class, () -> factory.generateForSkill(null));
    }

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

    @Test
    void generate_shortPrompt_preservesDifficultyMinimums() {
        TypingChallengeFactory factory = new TypingChallengeFactory(new FixedPromptBank("short"));

        assertEquals(15, factory.generate(Difficulty.EASY).getTime());
        assertEquals(30, factory.generate(Difficulty.MEDIUM).getTime());
        assertEquals(30, factory.generate(Difficulty.HARD).getTime());
        assertEquals(45, factory.generate(Difficulty.ULTIMATE).getTime());
    }

    @Test
    void generate_longPrompt_normalizesWhitespaceAndRoundsAllowanceUp() {
        TypingChallengeFactory factory = new TypingChallengeFactory(new FixedPromptBank("  abcdefghij  ".repeat(10)));
        // Ten ten-character words and nine normalized spaces require ceil(109 * 0.288) seconds.
        assertEquals(32, factory.generate(Difficulty.EASY).getTime());
    }

    @Test
    void generate_nullDifficulty_throwsException() {
        TypingChallengeFactory factory = new TypingChallengeFactory();

        assertThrows(NullPointerException.class, () -> factory.generate(null));
    }

    @Test
    void generate_factoriesSharingBank_doNotRepeatPreviousPassage() {
        PromptBank bank = new PromptBank(new Random(42));
        TypingChallengeFactory firstFactory = new TypingChallengeFactory(bank);
        TypingChallengeFactory nextFactory = new TypingChallengeFactory(bank);

        assertNotEquals(firstFactory.generate(Difficulty.MEDIUM).getText(),
                nextFactory.generate(Difficulty.MEDIUM).getText());
    }

    /** Supplies a known passage so deadline boundaries can be checked independently of random choice. */
    private static class FixedPromptBank extends PromptBank {
        private final String prompt;

        FixedPromptBank(String prompt) {
            this.prompt = prompt;
        }

        @Override
        public String getRandomPrompt(Difficulty difficulty) {
            return prompt;
        }
    }
}
