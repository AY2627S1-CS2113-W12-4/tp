package seedu.keyboardwarrior.combat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.junit.jupiter.api.Test;

/**
 * Tests creation of the supported enemy configurations.
 */
class EnemyFactoryTest {
    @Test
    void createRandomEnemy_seededSelection_offersVariedConfiguredOpponents() {
        Random random = new Random(42);
        Set<String> names = new HashSet<>();

        for (int selection = 0; selection < 100; selection++) {
            Enemy enemy = EnemyFactory.createRandomEnemy(random);
            names.add(enemy.getName());
            assertTrue(enemy.getHealth() >= 80 && enemy.getHealth() <= 120);
            assertTrue(enemy.getActiveSkills().size() >= 2);
            for (Skill skill : enemy.getActiveSkills()) {
                assertTrue(skill == SkillRegistry.getSkill("ping")
                        || skill == SkillRegistry.getSkill("brute_force")
                        || skill == SkillRegistry.getSkill("buffer_overflow"));
            }
        }

        assertEquals(8, names.size());
    }

    @Test
    void createRandomEnemy_sameSeed_createsIndependentEnemiesOfSameType() {
        Enemy first = EnemyFactory.createRandomEnemy(new Random(42));
        Enemy second = EnemyFactory.createRandomEnemy(new Random(42));
        int startingHealth = second.getHealth();

        assertEquals(first.getName(), second.getName());
        assertNotSame(first, second);
        first.setHealth(0);
        assertEquals(startingHealth, second.getHealth());
    }

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
