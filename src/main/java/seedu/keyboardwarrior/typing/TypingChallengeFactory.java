package seedu.keyboardwarrior.typing;

import seedu.keyboardwarrior.typing.TypingChallenge.Difficulty;

/**
 * TypingChallengeFactory
 */
public class TypingChallengeFactory {
    private final PromptBank bank;

    public TypingChallengeFactory() {
        this.bank = new PromptBank();
    }

    public TypingChallenge generate(Difficulty difficulty) {
        String selectedText = bank.getRandomPrompt(difficulty);
        int timeLimit = getTimeLimit(difficulty);

        return new TypingChallenge(selectedText, timeLimit);
    }

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
