package seedu.keyboardwarrior.typing;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.keyboardwarrior.typing.TypingChallenge.Difficulty;

/**
 * Tests varied prompt selection and avoidance of consecutive repeated passages.
 */
class PromptBankTest {
    /**
     * Checks prompt variety without consecutive repeats at every difficulty.
     */
    @Test
    void getRandomPrompt_eachDifficulty_hasVarietyWithoutConsecutiveRepeats() {
        PromptBank bank = new PromptBank(new Random(42));

        for (Difficulty difficulty : Difficulty.values()) {
            Set<String> passages = new HashSet<>();
            String previous = null;
            for (int selection = 0; selection < 100; selection++) {
                String passage = bank.getRandomPrompt(difficulty);
                assertFalse(passage.isBlank());
                assertNotEquals(previous, passage);
                passages.add(passage);
                previous = passage;
            }
            assertTrue(passages.size() >= 5, "Each difficulty should offer at least five passages.");
        }
    }

    /**
     * Checks that switching difficulty retains the previous selection for each level.
     */
    @Test
    void getRandomPrompt_switchingDifficulty_preservesPreviousSelectionForEachLevel() {
        PromptBank bank = new PromptBank(new Random(17));
        String previousEasy = bank.getRandomPrompt(Difficulty.EASY);
        bank.getRandomPrompt(Difficulty.HARD);

        assertNotEquals(previousEasy, bank.getRandomPrompt(Difficulty.EASY));
    }
}
