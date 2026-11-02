package seedu.keyboardwarrior.encounter;

import java.util.List;
import java.util.Random;

import seedu.keyboardwarrior.combat.Player;
import seedu.keyboardwarrior.combat.Skill;
import seedu.keyboardwarrior.combat.SkillRegistry;

/**
 * Holds the remaining skill rewards for one player's run and draws them without replacement.
 * Skills already owned when the pool is created are excluded by object identity,
 * using the shared skill instances returned by the registry. Later changes to the
 * player's skills are not automatically reflected in this pool.
 */
public class RewardPool {
    /** Registry skills still eligible to be drawn; removing a reward consumes it for this pool. */
    private final List<Skill> availableSkills;
    /** Run's shared random source, retained without copying or reseeding. */
    private final Random random;

    /**
     * Takes a snapshot of registered skills that the player does not currently own.
     * Construction does not consume the supplied random sequence.
     *
     * @param random nonnull master random instance shared by the run
     * @param player player whose current skills are excluded from the initial pool
     */
    public RewardPool(Random random, Player player) {
        this.random = random;
        this.availableSkills = SkillRegistry.getAllSkills();
        this.availableSkills.removeAll(player.getActiveSkills());
    }

    /**
     * Randomly selects and removes one remaining skill; the caller grants it to the player.
     * An empty pool consumes no randomness, allowing the caller to provide an alternative reward.
     *
     * @return removed skill, or {@code null} if all rewards in this pool have been consumed
     */
    public Skill popRandomReward() {
        if (availableSkills.isEmpty()) {
            return null;
        }

        return availableSkills.remove(random.nextInt(availableSkills.size()));
    }
}
