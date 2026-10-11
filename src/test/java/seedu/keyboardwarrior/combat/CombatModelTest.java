package seedu.keyboardwarrior.combat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
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
        assertEquals(4, player.getActiveSkills().size());
        assertSame(SkillRegistry.getSkill("ping"), player.getActiveSkills().get(0));
        assertSame(SkillRegistry.getSkill("brute_force"), player.getActiveSkills().get(1));
        assertSame(SkillRegistry.getSkill("buffer_overflow"), player.getActiveSkills().get(2));
        assertSame(SkillRegistry.getSkill("sigkill"), player.getActiveSkills().get(3));
    }

    @Test
    void skill_new_exposesConfiguredValues() {
        Skill skill = new Skill("Test Strike", 12, Difficulty.HARD);

        assertEquals("Test Strike", skill.getName());
        assertEquals(12, skill.getBaseDamage());
        assertEquals(Difficulty.HARD, skill.getDifficulty());
    }
}
