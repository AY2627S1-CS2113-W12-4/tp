package seedu.keyboardwarrior.combat;

/**
 * Represents the player and initializes the skills available to them.
 */
public class Player extends Entity {
    /**
     * Cumulative countdown adjustment in seconds; negative values represent event
     * penalties.
     */
    private int bonusTime = 0;

    /**
     * Creates a player with all skills from the standard player loadout.
     *
     * @param name   name displayed during combat
     * @param health starting health points
     */
    public Player(String name, int health) {
        super(name, health);

        addSkill(SkillRegistry.getSkill("ping"));
        addSkill(SkillRegistry.getSkill("brute_force"));
        addSkill(SkillRegistry.getSkill("buffer_overflow"));
        addSkill(SkillRegistry.getSkill("sigkill"));
        addSkill(SkillRegistry.getSkill("encryption"));

        assert activeSkills.size() == 5 && !activeSkills.contains(null)
                : "Players must receive the complete standard skill loadout";
    }

    /**
     * Replaces the countdown adjustment applied before difficulty scaling.
     * Negative adjustments are allowed; challenge factories enforce minimum
     * countdowns.
     *
     * @param time total adjustment in seconds, rather than an amount to add
     */
    public void setBonusTime(int time) {
        this.bonusTime = time;
    }

    /**
     * Returns the adjustment carried between encounters in this player instance.
     *
     * @return signed countdown adjustment in seconds
     */
    public int getBonusTime() {
        return bonusTime;
    }
}
