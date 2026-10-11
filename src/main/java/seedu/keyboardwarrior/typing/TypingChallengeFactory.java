package seedu.keyboardwarrior.typing;

import seedu.keyboardwarrior.typing.TypingChallenge.Difficulty;

/**
 * TypingChallengeFactory
 */
public class TypingChallengeFactory {
    private final PromptBank BANK;

    public TypingChallengeFactory() {
        this.BANK = new PromptBank();
    }

    public TypingChallenge generate(Difficulty difficulty) {
        String selectedText = BANK.getRandomPrompt(difficulty);
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
