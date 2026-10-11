package seedu.keyboardwarrior.combat;

/** Creates predefined enemies with their health and skill loadouts configured. */
public class EnemyFactory {

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
        default:
            throw new IllegalArgumentException("Unknown enemy type: " + type);
        }

        return enemy;
    }
}
