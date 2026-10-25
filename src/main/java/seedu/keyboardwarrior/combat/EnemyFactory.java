package seedu.keyboardwarrior.combat;

import java.util.Locale;
import java.util.Objects;
import java.util.Random;

/** Creates predefined enemies with their health and skill loadouts configured. */
public class EnemyFactory {
    /** Enemy types available when a scenario randomly chooses its opponent. */
    private static final String[] ENEMY_TYPES = {
        "rogue_ai", "malware_bot", "firewall_golem", "cache_goblin",
        "phantom_packet", "cipher_serpent", "trojan_knight", "null_dragon"
    };
    /** Health progression for the five regular scenarios, independent of enemy type. */
    private static final int[] SCENARIO_HEALTH = {30, 35, 50, 60, 70};

    /**
     * Creates a randomly selected enemy for a new scenario.
     *
     * @return a fresh enemy with its starting health and skills
     */
    public static Enemy createRandomEnemy() {
        return createRandomEnemy(new Random());
    }

    /**
     * Creates a randomly selected enemy using the supplied random source.
     *
     * @param random source used to select the enemy type
     * @return a fresh enemy with its starting health and skills
     */
    public static Enemy createRandomEnemy(Random random) {
        Objects.requireNonNull(random);
        assert ENEMY_TYPES.length > 0 : "At least one enemy type must be registered";
        return createEnemy(ENEMY_TYPES[random.nextInt(ENEMY_TYPES.length)]);
    }

    /**
     * Creates a random regular opponent with health determined by scenario progression.
     * The selected enemy keeps its usual name and skill rotation.
     *
     * @param random source used to select the enemy type
     * @param scenarioNumber one-based regular scenario number, from one to five
     * @return a fresh opponent with the scenario's health and the selected type's skills
     * @throws NullPointerException if the random source is null
     * @throws IllegalArgumentException if the scenario number is outside one to five
     */
    public static Enemy createRandomEnemy(Random random, int scenarioNumber) {
        Objects.requireNonNull(random, "Enemy random source must not be null");
        if (scenarioNumber < 1 || scenarioNumber > SCENARIO_HEALTH.length) {
            throw new IllegalArgumentException("Regular scenario number must be from 1 to 5");
        }
        Enemy enemy = createScenarioEnemy(ENEMY_TYPES[random.nextInt(ENEMY_TYPES.length)], scenarioNumber);
        assert enemy.getHealth() > 0 : "A regular scenario opponent must start with health";
        return enemy;
    }

    /** Recreates a particular regular opponent with the health allowed by its scenario. */
    public static Enemy createScenarioEnemy(String type, int scenarioNumber) {
        if (scenarioNumber < 1 || scenarioNumber > SCENARIO_HEALTH.length) {
            throw new IllegalArgumentException("Regular scenario number must be from 1 to 5");
        }
        Enemy enemy = createEnemy(type);
        enemy.setHealth(SCENARIO_HEALTH[scenarioNumber - 1]);
        return enemy;
    }

    /** Returns a stable save-file identifier for a configured opponent. */
    public static String getTypeId(Enemy enemy) {
        Objects.requireNonNull(enemy);
        for (String type : ENEMY_TYPES) {
            if (createEnemy(type).getName().equals(enemy.getName())) {
                return type;
            }
        }
        if (enemy.getName().equals("Kernel Overlord (Phase 1)")
                || enemy.getName().equals("Kernel Overlord (Phase 2)")) {
            return "kernel_overlord";
        }
        throw new IllegalArgumentException("Unknown enemy: " + enemy.getName());
    }

    /**
     * Creates one phase of the final Kernel Overlord boss encounter.
     * Each phase is a fresh opponent; the game controls the transition between phases.
     *
     * @param phase one for the 50-health phase, or two for the 70-health phase
     * @return a fresh boss phase with its fixed attack skill
     * @throws IllegalArgumentException if the phase is neither one nor two
     */
    public static Enemy createBossPhase(int phase) {
        if (phase < 1 || phase > 2) {
            throw new IllegalArgumentException("Boss phase must be 1 or 2");
        }
        Enemy boss = new Enemy("Kernel Overlord (Phase " + phase + ")", phase == 1 ? 50 : 70);
        boss.addSkill(SkillRegistry.getSkill(phase == 1 ? "ping" : "brute_force"));
        assert boss.getActiveSkills().size() == 1 : "A boss phase must have one fixed attack";
        return boss;
    }

    /**
     * Creates an enemy for a registered type identifier.
     *
     * @param type enemy identifier, such as {@code rogue_ai} or {@code malware_bot}
     * @return configured enemy
     * @throws IllegalArgumentException if the enemy type is unknown
     */
    public static Enemy createEnemy(String type) {
        Enemy enemy;

        switch (type.toLowerCase(Locale.ROOT)) {
        case "rogue_ai":
            enemy = new Enemy("Script Kiddie", 80);
            enemy.addSkill(SkillRegistry.getSkill("ping"));
            enemy.addSkill(SkillRegistry.getSkill("brute_force"));
            enemy.addSkill(SkillRegistry.getSkill("buffer_overflow"));
            break;
        case "malware_bot":
            enemy = new Enemy("Malware Bot", 120);
            enemy.addSkill(SkillRegistry.getSkill("ping"));
            enemy.addSkill(SkillRegistry.getSkill("brute_force"));
            break;
        case "firewall_golem":
            enemy = new Enemy("Firewall Golem", 90);
            enemy.addSkill(SkillRegistry.getSkill("ping"));
            enemy.addSkill(SkillRegistry.getSkill("buffer_overflow"));
            break;
        case "cache_goblin":
            enemy = new Enemy("Cache Goblin", 80);
            enemy.addSkill(SkillRegistry.getSkill("ping"));
            enemy.addSkill(SkillRegistry.getSkill("brute_force"));
            break;
        case "phantom_packet":
            enemy = new Enemy("Phantom Packet", 90);
            enemy.addSkill(SkillRegistry.getSkill("ping"));
            enemy.addSkill(SkillRegistry.getSkill("buffer_overflow"));
            break;
        case "cipher_serpent":
            enemy = new Enemy("Cipher Serpent", 100);
            enemy.addSkill(SkillRegistry.getSkill("brute_force"));
            enemy.addSkill(SkillRegistry.getSkill("buffer_overflow"));
            break;
        case "trojan_knight":
            enemy = new Enemy("Trojan Knight", 110);
            enemy.addSkill(SkillRegistry.getSkill("ping"));
            enemy.addSkill(SkillRegistry.getSkill("brute_force"));
            break;
        case "null_dragon":
            enemy = new Enemy("Null Dragon", 120);
            enemy.addSkill(SkillRegistry.getSkill("brute_force"));
            enemy.addSkill(SkillRegistry.getSkill("buffer_overflow"));
            break;
        default:
            throw new IllegalArgumentException("Unknown enemy type: " + type);
        }

        assert enemy.getHealth() > 0 : "Factory enemies must start with health";
        assert !enemy.getActiveSkills().isEmpty() && !enemy.getActiveSkills().contains(null)
                : "Factory enemies must have a valid skill loadout";
        return enemy;
    }
}
