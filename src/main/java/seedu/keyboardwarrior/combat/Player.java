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
     * Adds a signed countdown adjustment, saturating at the integer limits instead of wrapping.
     *
     * @param seconds adjustment to add before challenge difficulty scaling
     * @return actual change, which may be smaller in magnitude at an integer limit
     */
    public int addBonusTime(int seconds) {
        int previousTime = bonusTime;
        bonusTime = (int) Math.max(Integer.MIN_VALUE,
                Math.min(Integer.MAX_VALUE, (long) bonusTime + seconds));
        return bonusTime - previousTime;
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
