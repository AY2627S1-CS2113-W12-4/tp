package seedu.keyboardwarrior.combat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import seedu.keyboardwarrior.typing.TypingChallenge.Difficulty;

/**
 * Tests the state and skill data exposed by combat entities.
 */
class CombatModelTest {
    @Test
    void enemy_newAndUpdated_preservesCombatState() {
        Enemy enemy = new Enemy("Training Bot", 50);
        Skill skill = new Skill("Test Strike", 12, Difficulty.EASY);

        assertEquals("Training Bot", enemy.getName());
        assertEquals(50, enemy.getHealth());
        assertFalse(enemy.getIsDead());
        assertTrue(enemy.getActiveSkills().isEmpty());

        enemy.setHealth(25);
        enemy.setIsDead(true);
        enemy.addSkill(skill);

        assertEquals(25, enemy.getHealth());
        assertTrue(enemy.getIsDead());
        assertEquals(1, enemy.getActiveSkills().size());
        assertSame(skill, enemy.getActiveSkills().get(0));
    }

    @Test
    void player_new_hasDefaultSkillsInOrder() {
        Player player = new Player("Player", 100);

        assertEquals("Player", player.getName());
        assertEquals(100, player.getHealth());
        assertFalse(player.getIsDead());
        assertEquals(5, player.getActiveSkills().size());
        assertSame(SkillRegistry.getSkill("ping"), player.getActiveSkills().get(0));
        assertSame(SkillRegistry.getSkill("brute_force"), player.getActiveSkills().get(1));
        assertSame(SkillRegistry.getSkill("buffer_overflow"), player.getActiveSkills().get(2));
        assertSame(SkillRegistry.getSkill("sigkill"), player.getActiveSkills().get(3));
        assertSame(SkillRegistry.getSkill("encryption"), player.getActiveSkills().get(4));
    }

    @Test
    void skill_new_exposesConfiguredValues() {
        Skill skill = new Skill("Test Strike", 12, Difficulty.HARD);

        assertEquals("Test Strike", skill.getName());
        assertEquals(12, skill.getBaseDamage());
        assertEquals(Difficulty.HARD, skill.getDifficulty());
    }

    @Test
    void entity_invalidNameOrHealth_rejectsConstruction() {
        assertThrows(NullPointerException.class, () -> new Enemy(null, 50));
        assertThrows(IllegalArgumentException.class, () -> new Enemy("", 50));
        assertThrows(IllegalArgumentException.class, () -> new Player(" \t", 50));
        assertThrows(IllegalArgumentException.class, () -> new Enemy("Target", -1));
    }

    @Test
    void entity_invalidUpdates_preservePreviousState() {
        Enemy enemy = new Enemy("Target", 50);

        assertThrows(IllegalArgumentException.class, () -> enemy.setHealth(-1));
        assertThrows(NullPointerException.class, () -> enemy.setIsDead(null));
        assertThrows(NullPointerException.class, () -> enemy.addSkill(null));

        assertEquals(50, enemy.getHealth());
        assertFalse(enemy.getIsDead());
        assertTrue(enemy.getActiveSkills().isEmpty());
    }

    @Test
    void entity_zeroHealth_isAllowedWithoutChangingDefeatedFlag() {
        Enemy enemy = new Enemy("Target", 0);

        assertEquals(0, enemy.getHealth());
        assertFalse(enemy.getIsDead());
        enemy.setHealth(1);
        enemy.setHealth(0);
        assertEquals(0, enemy.getHealth());
        assertFalse(enemy.getIsDead());
    }

    @Test
    void skill_invalidArguments_rejectsConstruction() {
        assertThrows(NullPointerException.class, () -> new Skill(null, 12, Difficulty.EASY));
        assertThrows(IllegalArgumentException.class, () -> new Skill("", 12, Difficulty.EASY));
        assertThrows(IllegalArgumentException.class, () -> new Skill(" \t", 12, Difficulty.EASY));
        assertThrows(IllegalArgumentException.class, () -> new Skill("Strike", -1, Difficulty.EASY));
        assertThrows(NullPointerException.class, () -> new Skill("Strike", 12, null));
    }

    @Test
    void skill_zeroDamage_isAllowed() {
        Skill skill = new Skill("Wait", 0, Difficulty.EASY);

        assertEquals(0, skill.getBaseDamage());
    }
}
