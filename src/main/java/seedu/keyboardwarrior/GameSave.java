package seedu.keyboardwarrior;

import java.util.Objects;

import seedu.keyboardwarrior.combat.Enemy;
import seedu.keyboardwarrior.combat.EnemyFactory;

/** Immutable, validated run data captured while the player is choosing a skill. */
public record GameSave(int version, GameDifficulty difficulty, int playerHealth, int scenarioIndex,
        int bossPhase, String enemyType, int enemyHealth, int nextEnemySkillIndex) {
    /** Version of the properties-file layout supported by this application. */
    public static final int CURRENT_VERSION = 1;

    /** Rejects snapshots that cannot represent a living run at a stable turn boundary. */
    public GameSave {
        Objects.requireNonNull(difficulty, "Saved difficulty must not be null");
        Objects.requireNonNull(enemyType, "Saved enemy type must not be null");
        if (version != CURRENT_VERSION) {
            throw new IllegalArgumentException("Unsupported game save version: " + version);
        }
        if (playerHealth <= 0 || playerHealth > difficulty.getStartingHealth()) {
            throw new IllegalArgumentException("Saved player health is outside the allowed range");
        }
        if (!(bossPhase == 0 && scenarioIndex >= 0 && scenarioIndex < 5)
                && !(bossPhase >= 1 && bossPhase <= 2 && scenarioIndex == 5)) {
            throw new IllegalArgumentException("Saved scenario and boss phase are inconsistent");
        }
        Enemy enemy = createEnemy(enemyType, scenarioIndex, bossPhase);
        if (enemyHealth <= 0 || enemyHealth > enemy.getHealth()) {
            throw new IllegalArgumentException("Saved enemy health is outside the allowed range");
        }
        if (nextEnemySkillIndex < 0 || nextEnemySkillIndex >= enemy.getActiveSkills().size()) {
            throw new IllegalArgumentException("Saved enemy attack index is outside the allowed range");
        }
    }

    /** Recreates the saved opponent with its remaining health and standard attack loadout. */
    public Enemy restoreEnemy() {
        Enemy enemy = createEnemy(enemyType, scenarioIndex, bossPhase);
        enemy.setHealth(enemyHealth);
        return enemy;
    }

    /** Checks that regular opponents and boss identifiers agree with the saved progression. */
    private static Enemy createEnemy(String enemyType, int scenarioIndex, int bossPhase) {
        if (bossPhase == 0) {
            return EnemyFactory.createScenarioEnemy(enemyType, scenarioIndex + 1);
        }
        if (!enemyType.equals("kernel_overlord")) {
            throw new IllegalArgumentException("A boss phase must contain the Kernel Overlord");
        }
        return EnemyFactory.createBossPhase(bossPhase);
    }
}
