package seedu.keyboardwarrior.combat;

import java.util.Objects;

import seedu.keyboardwarrior.typing.TypingChallenge.Difficulty;

/**
 * Defines a combat action and the typing challenge required to use it.
 */
public class Skill {
    private String name;
    private int baseDamage;
    /** Base signed seconds, scaled by typing performance when the resolver applies this skill. */
    private int timeBonus;
    private Difficulty difficulty;

    /**
     * Creates a skill with damage, a countdown effect and an associated typing difficulty.
     *
     * @param name       name displayed in the combat interface
     * @param baseDamage damage used for automated attacks without a typing result
     * @param timeBonus base signed countdown adjustment earned at 50 WPM and full accuracy on Hard
     * @param difficulty difficulty of the associated typing challenge
     * @throws NullPointerException     if the name or difficulty is null
     * @throws IllegalArgumentException if the name is blank or base damage is
     *                                  negative
     */
    public Skill(String name, int baseDamage, int timeBonus, Difficulty difficulty) {
        this.name = Objects.requireNonNull(name, "Skill name must not be null");
        if (name.isBlank()) {
            throw new IllegalArgumentException("Skill name must not be blank");
        }
        if (baseDamage < 0) {
            throw new IllegalArgumentException("Base damage must not be negative");
        }
        this.baseDamage = baseDamage;
        this.timeBonus = timeBonus;
        this.difficulty = Objects.requireNonNull(difficulty, "Skill difficulty must not be null");
    }

    /**
     * Returns the display name of the skill.
     *
     * @return skill name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the skill's unmodified damage.
     *
     * @return base damage
     */
    public int getBaseDamage() {
        return baseDamage;
    }

    /**
     * Returns the base countdown effect before the resolver scales it by typing performance.
     * Zero means no effect; negative values reduce future countdowns. The resolver multiplies
     * this value by WPM divided by fifty, accuracy to the fourth power and the run multiplier,
     * then rounds down to whole seconds.
     *
     * @return base signed adjustment in seconds, before performance and challenge countdown scaling
     */
    public int getTimeBonus() {
        return timeBonus;
    }

    /**
     * Returns the difficulty used to generate the typing challenge.
     *
     * @return challenge difficulty
     */
    public Difficulty getDifficulty() {
        return difficulty;
    }
}
