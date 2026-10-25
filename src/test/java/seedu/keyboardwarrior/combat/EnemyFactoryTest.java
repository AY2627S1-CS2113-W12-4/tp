package seedu.keyboardwarrior.combat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Locale;
import java.util.Random;
import java.util.Set;

import org.junit.jupiter.api.Test;

/**
 * Tests creation of the supported enemy configurations.
 */
class EnemyFactoryTest {
    /**
     * Checks that seeded enemy selection covers all eight configured enemy types.
     */
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

    /**
     * Checks that equal seeds select the same type while creating independent enemies.
     */
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

    /**
     * Checks scenario health progression without changing each enemy type's skill loadout.
     */
    @Test
    void createRandomEnemy_regularScenarios_useProgressiveHealthAndOriginalLoadouts() {
        int[] expectedHealth = {30, 35, 50, 60, 70};

        for (int scenario = 1; scenario <= expectedHealth.length; scenario++) {
            for (int seed = 0; seed < 25; seed++) {
                Enemy original = EnemyFactory.createRandomEnemy(new Random(seed));
                Enemy scenarioEnemy = EnemyFactory.createRandomEnemy(new Random(seed), scenario);

                assertEquals(expectedHealth[scenario - 1], scenarioEnemy.getHealth());
                assertEquals(original.getName(), scenarioEnemy.getName());
                assertEquals(original.getActiveSkills(), scenarioEnemy.getActiveSkills());
                assertNotSame(original, scenarioEnemy);
            }
        }
    }

    /**
     * Checks that invalid scenario numbers and null random sources are rejected.
     */
    @Test
    void createRandomEnemy_invalidScenarioOrRandom_rejectsConfiguration() {
        for (int scenario : new int[] {Integer.MIN_VALUE, -1, 0, 6, Integer.MAX_VALUE}) {
            assertThrows(IllegalArgumentException.class,
                    () -> EnemyFactory.createRandomEnemy(new Random(42), scenario));
        }
        assertThrows(NullPointerException.class, () -> EnemyFactory.createRandomEnemy(null, 1));
    }

    /**
     * Checks that boss phases have separate health and the expected fixed attacks.
     */
    @Test
    void createBossPhase_configuredPhases_haveIndependentHealthAndFixedAttacks() {
        Enemy first = EnemyFactory.createBossPhase(1);
        Enemy second = EnemyFactory.createBossPhase(2);

        assertEquals("Kernel Overlord (Phase 1)", first.getName());
        assertEquals("Kernel Overlord (Phase 2)", second.getName());
        assertEquals(50, first.getHealth());
        assertEquals(70, second.getHealth());
        assertEquals(1, first.getActiveSkills().size());
        assertEquals(1, second.getActiveSkills().size());
        assertSame(SkillRegistry.getSkill("ping"), first.getActiveSkills().get(0));
        assertSame(SkillRegistry.getSkill("brute_force"), second.getActiveSkills().get(0));
        assertEquals(10, first.getActiveSkills().get(0).getBaseDamage());
        assertEquals(20, second.getActiveSkills().get(0).getBaseDamage());
        first.setHealth(0);
        assertEquals(70, second.getHealth());
        assertEquals(50, EnemyFactory.createBossPhase(1).getHealth());
    }

    /**
     * Checks that unsupported boss phase numbers are rejected.
     */
    @Test
    void createBossPhase_invalidPhase_rejectsConfiguration() {
        for (int phase : new int[] {Integer.MIN_VALUE, -1, 0, 3, Integer.MAX_VALUE}) {
            assertThrows(IllegalArgumentException.class, () -> EnemyFactory.createBossPhase(phase));
        }
    }

    /**
     * Checks the Script Kiddie's health and ordered skills.
     */
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

    /**
     * Checks the Malware Bot's health and ordered skills.
     */
    @Test
    void createEnemy_malwareBot_returnsConfiguredEnemy() {
        Enemy enemy = EnemyFactory.createEnemy("MALWARE_BOT");

        assertEquals("Malware Bot", enemy.getName());
        assertEquals(120, enemy.getHealth());
        assertEquals(2, enemy.getActiveSkills().size());
        assertSame(SkillRegistry.getSkill("ping"), enemy.getActiveSkills().get(0));
        assertSame(SkillRegistry.getSkill("brute_force"), enemy.getActiveSkills().get(1));
    }

    /**
     * Checks that enemy identifiers are interpreted independently of the default locale.
     */
    @Test
    void createEnemy_uppercaseId_doesNotDependOnDefaultLocale() {
        Locale previousLocale = Locale.getDefault();
        try {
            Locale.setDefault(Locale.forLanguageTag("tr-TR"));

            Enemy enemy = EnemyFactory.createEnemy("FIREWALL_GOLEM");

            assertEquals("Firewall Golem", enemy.getName());
            assertEquals(90, enemy.getHealth());
            assertSame(SkillRegistry.getSkill("ping"), enemy.getActiveSkills().get(0));
            assertSame(SkillRegistry.getSkill("buffer_overflow"), enemy.getActiveSkills().get(1));
        } finally {
            Locale.setDefault(previousLocale);
        }
    }

    /**
     * Checks that an unknown enemy identifier produces a useful exception.
     */
    @Test
    void createEnemy_unknownType_throwsException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> EnemyFactory.createEnemy("unknown"));

        assertEquals("Unknown enemy type: unknown", exception.getMessage());
    }
}
