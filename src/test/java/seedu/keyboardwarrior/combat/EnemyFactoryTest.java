package seedu.keyboardwarrior.combat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/**
 * Tests creation of the supported enemy configurations.
 */
class EnemyFactoryTest {
    @Test
    void createEnemy_rogueAi_returnsConfiguredEnemy() {
        Enemy enemy = EnemyFactory.createEnemy("rogue_ai");

        assertEquals("Script Kiddie", enemy.getName());
        assertEquals(80, enemy.getHealth());
        assertEquals(3, enemy.getActiveSkills().size());
        assertSame(SkillRegistry.getSkill("ping"), enemy.getActiveSkills().get(0));
        assertSame(SkillRegistry.getSkill("brute_force"), enemy.getActiveSkills().get(1));
        assertSame(SkillRegistry.getSkill("buffer_overflow"), enemy.getActiveSkills().get(2));
    }

    @Test
    void createEnemy_malwareBot_returnsConfiguredEnemy() {
        Enemy enemy = EnemyFactory.createEnemy("MALWARE_BOT");

        assertEquals("Malware Bot", enemy.getName());
        assertEquals(120, enemy.getHealth());
        assertEquals(2, enemy.getActiveSkills().size());
        assertSame(SkillRegistry.getSkill("ping"), enemy.getActiveSkills().get(0));
        assertSame(SkillRegistry.getSkill("brute_force"), enemy.getActiveSkills().get(1));
    }

    @Test
    void createEnemy_unknownType_throwsException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> EnemyFactory.createEnemy("unknown"));

        assertEquals("Unknown enemy type: unknown", exception.getMessage());
    }
}
