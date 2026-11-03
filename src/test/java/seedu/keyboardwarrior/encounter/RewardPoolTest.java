package seedu.keyboardwarrior.encounter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.Test;

import seedu.keyboardwarrior.combat.Player;
import seedu.keyboardwarrior.combat.SkillRegistry;

/** Tests seeded reward draws against an independently ordered list of registry identifiers. */
class RewardPoolTest {
    /** A seeded sequence must use registry-ID order and remove owned or already drawn skills. */
    @Test
    void popRandomReward_seededDraws_followSortedRegistryIdsWithoutReplacement() {
        Player player = new Player("Player", 100);
        player.getActiveSkills().clear();
        player.addSkill(SkillRegistry.getSkill("ping"));
        Random sharedRandom = new Random(42);
        Random expectedRandom = new Random(42);
        RewardPool pool = new RewardPool(sharedRandom, player);
        List<String> remainingIds = new ArrayList<>(List.of(
                "brute_force", "buffer_overflow", "encryption", "overclock", "sigkill"));

        while (!remainingIds.isEmpty()) {
            String expectedId = remainingIds.remove(expectedRandom.nextInt(remainingIds.size()));
            assertSame(SkillRegistry.getSkill(expectedId), pool.popRandomReward());
        }

        assertNull(pool.popRandomReward());
        assertEquals(expectedRandom.nextLong(), sharedRandom.nextLong(),
                "The pool must consume only the supplied generator, with no draw when empty.");
    }
}
