package seedu.keyboardwarrior.typing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Random;

import org.junit.jupiter.api.Test;

import seedu.keyboardwarrior.typing.TypingChallenge.Difficulty;

/** Tests that varied challenges retain skill time limits and can share passage history. */
class TypingChallengeFactoryTest {
    @Test
    void generate_eachDifficulty_keepsExistingTimeLimit() {
        TypingChallengeFactory factory = new TypingChallengeFactory(new PromptBank(new Random(42)));

        assertEquals(15, factory.generate(Difficulty.EASY).getTime());
        assertEquals(30, factory.generate(Difficulty.MEDIUM).getTime());
        assertEquals(30, factory.generate(Difficulty.HARD).getTime());
        assertEquals(45, factory.generate(Difficulty.ULTIMATE).getTime());
        assertTrue(factory.generate(Difficulty.EASY).getText().length()
                < factory.generate(Difficulty.ULTIMATE).getText().length());
    }

    @Test
    void generate_factoriesSharingBank_doNotRepeatPreviousPassage() {
        PromptBank bank = new PromptBank(new Random(42));
        TypingChallengeFactory firstFactory = new TypingChallengeFactory(bank);
        TypingChallengeFactory nextFactory = new TypingChallengeFactory(bank);

        assertNotEquals(firstFactory.generate(Difficulty.MEDIUM).getText(),
                nextFactory.generate(Difficulty.MEDIUM).getText());
    }
}
