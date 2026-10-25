package seedu.keyboardwarrior.typing;

import java.util.Objects;

import seedu.keyboardwarrior.typing.TypingChallenge.Difficulty;

/**
 * Creates typing challenges with prompts and time limits suited to a
 * difficulty.
 */
public class TypingChallengeFactory {
    private final PromptBank bank;

    /** Creates a factory backed by a new prompt bank. */
    public TypingChallengeFactory() {
        this(new PromptBank());
    }

    /**
     * Creates a factory using a shared prompt bank, retaining selections across
     * encounters.
     *
     * @param bank prompt bank used to generate challenges
     */
    public TypingChallengeFactory(PromptBank bank) {
        this.bank = Objects.requireNonNull(bank);
    }

    /**
     * Generates a challenge for the requested difficulty.
     *
     * @param difficulty desired challenge difficulty
     * @return challenge containing a matching prompt and time limit
     */
    public TypingChallenge generate(Difficulty difficulty) {
        String selectedText = bank.getRandomPrompt(difficulty);
        int timeLimit = getTimeLimit(difficulty);

        return new TypingChallenge(selectedText, timeLimit);
    }

    /** Returns the configured time limit for a difficulty. */
    private int getTimeLimit(Difficulty difficulty) {
        switch (difficulty) {
        case EASY:
            return 15;
        case MEDIUM:
            return 30;
        case HARD:
            return 30;
        case ULTIMATE:
            return 45;
        default:
            return 30;
        }
    }
}
