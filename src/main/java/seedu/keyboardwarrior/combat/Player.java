package seedu.keyboardwarrior.combat;

import java.util.ArrayList;

public class Player extends Entity {
    public Player(String name, int health) {
        super(name, health);

        addSkill(SkillRegistry.getSkill("ping"));
        addSkill(SkillRegistry.getSkill("brute_force"));
        addSkill(SkillRegistry.getSkill("buffer_overflow"));
        addSkill(SkillRegistry.getSkill("sigkill"));
    }
}
