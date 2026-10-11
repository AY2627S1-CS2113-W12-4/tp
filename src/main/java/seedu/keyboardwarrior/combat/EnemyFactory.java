package seedu.keyboardwarrior.combat;

/**
 * EnemyFactory
 */
public class EnemyFactory {

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
