package seedu.keyboardwarrior.combat;

/**
 * Represents the player and initializes the skills available to them.
 */
public class Player extends Entity {
    /**
     * Creates a player with all skills from the standard player loadout.
     *
     * @param name name displayed during combat
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
}
