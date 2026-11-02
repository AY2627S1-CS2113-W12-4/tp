package seedu.keyboardwarrior.typing;

import java.util.Objects;

import seedu.keyboardwarrior.GameDifficulty;
import seedu.keyboardwarrior.combat.Skill;
import seedu.keyboardwarrior.typing.TypingChallenge.Difficulty;

/**
 * Creates typing challenges with prompts and time limits suited to a
 * difficulty.
 */
public class TypingChallengeFactory {
    /**
     * Baseline speed used to give each passage a realistic completion window.
     */
    private static final double TARGET_WPM = 50.0;
    /**
     * Allows twenty percent extra time beyond the baseline passage completion time.
     */
    private static final double TIME_ALLOWANCE = 1.2;
    /** Lower bound for adjusted skill countdowns, in seconds before difficulty scaling. */
    private static final int MIN_TYPING_TIME = 10;
    private final PromptBank bank;
    private final GameDifficulty gameDifficulty;

    /**
     * Creates a factory backed by a new prompt bank.
     */
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
        this(bank, GameDifficulty.HARD);
    }

    /**
     * Retains passage selection history and applies the selected run's countdown
     * multiplier.
     */
    public TypingChallengeFactory(PromptBank bank, GameDifficulty gameDifficulty) {
        this.bank = Objects.requireNonNull(bank);
        this.gameDifficulty = Objects.requireNonNull(gameDifficulty);
    }

    /**
     * Creates a skill's challenge, giving Encryption random symbols and a base time
     * of sixty seconds.
     * Other skills retain their difficulty-based passages and length-aware
     * deadlines. The player's signed adjustment is applied before the ten-second
     * minimum and difficulty scaling.
     *
     * @param skill selected player skill
     * @param bonusTime signed countdown adjustment in seconds; negative values reduce time
     * @return challenge for the selected skill
     * @throws NullPointerException if the skill is null
     */
    public TypingChallenge generateForSkill(Skill skill, int bonusTime) {
        Objects.requireNonNull(skill, "Skill must not be null");
        if (skill.getName().equalsIgnoreCase("Encryption")) {
            assert (long) 60 + bonusTime <= Integer.MAX_VALUE : "Adjusted Encryption time must not overflow";
            int finalTime = Math.max(60 + bonusTime, MIN_TYPING_TIME);
            return new TypingChallenge(bank.getEncryptionPrompt(), gameDifficulty.scaleCountdown(finalTime));
        }
        return generate(skill.getDifficulty(), bonusTime);
    }

    /**
     * Creates an event challenge without selecting a random prompt.
     * The supplied countdown is clamped to at least one second before difficulty scaling.
     * Callers include any desired player adjustment in {@code baseTimeLimit} themselves.
     *
     * @param customText nonblank expected answer or reaction command
     * @param baseTimeLimit countdown in seconds before clamping and difficulty scaling
     * @return challenge containing the supplied text and scaled countdown
     * @throws NullPointerException if the text is null
     * @throws IllegalArgumentException if the text is blank
     */
    public TypingChallenge generateCustom(String customText, int baseTimeLimit) {
        int finalTime = Math.max(baseTimeLimit, 1);
        return new TypingChallenge(customText, gameDifficulty.scaleCountdown(finalTime));
    }

    /**
     * Generates a challenge with a random prompt for the requested difficulty.
     * The deadline is the larger of the difficulty minimum and the rounded-up time
     * for fifty WPM plus twenty percent, using normalized UTF-16 text length.
     * The player's adjustment is then applied with a ten-second minimum before difficulty scaling.
     *
     * @param difficulty desired challenge difficulty
     * @param bonusTime signed countdown adjustment in seconds; negative values reduce time
     * @return challenge containing a matching prompt and time limit
     * @throws NullPointerException if the difficulty is null
     */
    public TypingChallenge generate(Difficulty difficulty, int bonusTime) {
        Objects.requireNonNull(difficulty, "Challenge difficulty must not be null");
        String selectedText = bank.getRandomPrompt(difficulty);
        int characterCount = selectedText.trim().replaceAll("\\s+", " ").length();
        int passageTime = (int) Math.ceil(characterCount * 60.0 / (5.0 * TARGET_WPM) * TIME_ALLOWANCE);
        int timeLimit = Math.max(getMinimumTimeLimit(difficulty), passageTime);

        assert (long) timeLimit + bonusTime <= Integer.MAX_VALUE : "Adjusted passage time must not overflow";
        int finalTime = Math.max(timeLimit + bonusTime, MIN_TYPING_TIME);

        return new TypingChallenge(selectedText, gameDifficulty.scaleCountdown(finalTime));
    }

    /**
     * Returns the difficulty's minimum; longer passages receive a length-aware
     * allowance.
     *
     * @param difficulty difficulty whose minimum deadline is requested
     * @return minimum allowed duration in seconds
     */
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
