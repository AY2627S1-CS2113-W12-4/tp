package seedu.keyboardwarrior.combat;

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
     */
    public Skill(String name, int baseDamage, Difficulty difficulty) {
        this.name = name;
        this.baseDamage = baseDamage;
        this.difficulty = difficulty;
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
