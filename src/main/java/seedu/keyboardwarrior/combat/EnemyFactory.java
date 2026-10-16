package seedu.keyboardwarrior.combat;

import java.util.Objects;
import java.util.Random;

/** Creates predefined enemies with their health and skill loadouts configured. */
public class EnemyFactory {
    /** Enemy types available when a scenario randomly chooses its opponent. */
    private static final String[] ENEMY_TYPES = {
        "rogue_ai", "malware_bot", "firewall_golem", "cache_goblin",
        "phantom_packet", "cipher_serpent", "trojan_knight", "null_dragon"
    };

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
     * Creates an enemy for a registered type identifier.
     *
     * @param type enemy identifier, such as {@code rogue_ai} or {@code malware_bot}
     * @return configured enemy
     * @throws IllegalArgumentException if the enemy type is unknown
     */
    public static Enemy createEnemy(String type) {
        Enemy enemy;

        switch (type.toLowerCase()) {
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
