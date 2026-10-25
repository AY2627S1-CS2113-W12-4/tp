package seedu.keyboardwarrior.typing;

import java.util.Objects;

import seedu.keyboardwarrior.GameDifficulty;
import seedu.keyboardwarrior.combat.Skill;
import seedu.keyboardwarrior.typing.TypingChallenge.Difficulty;

/** Creates typing challenges with prompts and time limits suited to a difficulty. */
public class TypingChallengeFactory {
    /** Baseline speed used to give each passage a realistic completion window. */
    private static final double TARGET_WPM = 50.0;
    /** Allows twenty percent extra time beyond the baseline passage completion time. */
    private static final double TIME_ALLOWANCE = 1.2;
    private final PromptBank bank;
    private final GameDifficulty gameDifficulty;

    /** Creates a factory backed by a new prompt bank. */
    public TypingChallengeFactory() {
        this(new PromptBank());
    }

    /**
     * Creates a factory using a shared prompt bank, retaining selections across encounters.
     *
     * @param bank prompt bank used to generate challenges
     */
    public TypingChallengeFactory(PromptBank bank) {
        this(bank, GameDifficulty.HARD);
    }

    /** Retains passage selection history and applies the selected run's countdown multiplier. */
    public TypingChallengeFactory(PromptBank bank, GameDifficulty gameDifficulty) {
        this.bank = Objects.requireNonNull(bank);
        this.gameDifficulty = Objects.requireNonNull(gameDifficulty);
    }

    /**
     * Creates a skill's challenge, giving Encryption random symbols and a base time of sixty seconds.
     * Other skills retain their difficulty-based passages and length-aware deadlines.
     *
     * @param skill selected player skill
     * @return challenge for the selected skill
     */
    public TypingChallenge generateForSkill(Skill skill) {
        Objects.requireNonNull(skill, "Skill must not be null");
        if (skill.getName().equalsIgnoreCase("Encryption")) {
            return new TypingChallenge(bank.getEncryptionPrompt(), gameDifficulty.scaleCountdown(60));
        }
        return generate(skill.getDifficulty());
    }

    /**
     * Generates a challenge for the requested difficulty.
     *
     * @param difficulty desired challenge difficulty
     * @return challenge containing a matching prompt and time limit
     */
    public TypingChallenge generate(Difficulty difficulty) {
        Objects.requireNonNull(difficulty, "Challenge difficulty must not be null");
        String selectedText = bank.getRandomPrompt(difficulty);
        int characterCount = selectedText.trim().replaceAll("\\s+", " ").length();
        int passageTime = (int) Math.ceil(characterCount * 60.0 / (5.0 * TARGET_WPM) * TIME_ALLOWANCE);
        int timeLimit = Math.max(getMinimumTimeLimit(difficulty), passageTime);

        return new TypingChallenge(selectedText, gameDifficulty.scaleCountdown(timeLimit));
    }

    /** Returns the difficulty's minimum; longer passages receive a length-aware allowance. */
    private int getMinimumTimeLimit(Difficulty difficulty) {
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
