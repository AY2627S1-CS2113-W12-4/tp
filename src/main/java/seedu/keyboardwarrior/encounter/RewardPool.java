package seedu.keyboardwarrior.encounter;

import java.util.List;
import java.util.Random;

import seedu.keyboardwarrior.combat.Player;
import seedu.keyboardwarrior.combat.Skill;
import seedu.keyboardwarrior.combat.SkillRegistry;

/**
 * RewardPool
 */
public class RewardPool {
    private final List<Skill> availableSkills;
    private final Random random;

    public RewardPool(Random random, Player player) {
        this.random = random;
        this.availableSkills = SkillRegistry.getAllSkills();
        this.availableSkills.removeAll(player.getActiveSkills());
    }

    public Skill popRandomReward() {
        if (availableSkills.isEmpty()) {
            return null;
        }

        return availableSkills.remove(random.nextInt(availableSkills.size()));
    }
}
