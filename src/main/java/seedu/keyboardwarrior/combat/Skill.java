package seedu.keyboardwarrior.combat;

import seedu.keyboardwarrior.typing.TypingChallenge.Difficulty;

public class Skill {
    private String name;
    private int baseDamage;
    private Difficulty difficulty;

    public Skill(String name, int baseDamage, Difficulty difficulty) {
        this.name = name;
        this.baseDamage = baseDamage;
        this.difficulty = difficulty;
    }

    public String getName() {
        return name;
    }

    public int getBaseDamage() {
        return baseDamage;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }
}
