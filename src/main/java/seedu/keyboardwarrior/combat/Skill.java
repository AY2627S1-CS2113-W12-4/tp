package seedu.keyboardwarrior.combat;

import java.util.Objects;

import seedu.keyboardwarrior.typing.TypingChallenge.Difficulty;

/** Defines a combat action and the typing challenge required to use it. */
public class Skill {
    private String name;
    private int baseDamage;
    private Difficulty difficulty;

    /**
     * Creates a skill.
     *
     * @param name name displayed in the combat interface
     * @param baseDamage damage before typing performance is applied
     * @param difficulty difficulty of the associated typing challenge
     * @throws NullPointerException if the name or difficulty is null
     * @throws IllegalArgumentException if the name is blank or base damage is negative
     */
    public Skill(String name, int baseDamage, Difficulty difficulty) {
        this.name = Objects.requireNonNull(name, "Skill name must not be null");
        if (name.isBlank()) {
            throw new IllegalArgumentException("Skill name must not be blank");
        }
        if (baseDamage < 0) {
            throw new IllegalArgumentException("Base damage must not be negative");
        }
        this.baseDamage = baseDamage;
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
     * Returns the difficulty used to generate the typing challenge.
     *
     * @return challenge difficulty
     */
    public Difficulty getDifficulty() {
        return difficulty;
    }
}
