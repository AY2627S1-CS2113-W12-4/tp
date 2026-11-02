package seedu.keyboardwarrior;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import seedu.keyboardwarrior.combat.Enemy;
import seedu.keyboardwarrior.combat.EnemyFactory;
import seedu.keyboardwarrior.combat.Player;
import seedu.keyboardwarrior.combat.SkillRegistry;
import seedu.keyboardwarrior.encounter.EncounterSnapshot;
import seedu.keyboardwarrior.seed.RandomSnapshot;

/** Immutable run data for either three pending choices or one stable active encounter. */
public record GameSave(int version, GameDifficulty difficulty, int playerHealth, int scenarioIndex,
        int bossPhase, String enemyType, int enemyHealth, int nextEnemySkillIndex, RandomSnapshot randomSnapshot,
        int bonusTime, List<String> skillIds, List<EncounterSnapshot> choices, EncounterSnapshot activeEncounter) {
    /** Version two includes event choices, acquired skills and signed countdown adjustments. */
    public static final int CURRENT_VERSION = 2;

    /** Migrates older snapshots that predate random-state persistence. */
    public GameSave(int version, GameDifficulty difficulty, int playerHealth, int scenarioIndex,
            int bossPhase, String enemyType, int enemyHealth, int nextEnemySkillIndex) {
        this(version, difficulty, playerHealth, scenarioIndex, bossPhase, enemyType, enemyHealth,
                nextEnemySkillIndex, null);
    }

    /** Migrates combat-only snapshots, preserving their opponent and next attack exactly. */
    public GameSave(int version, GameDifficulty difficulty, int playerHealth, int scenarioIndex,
            int bossPhase, String enemyType, int enemyHealth, int nextEnemySkillIndex, RandomSnapshot randomSnapshot) {
        this(migratedVersion(version), difficulty, playerHealth, scenarioIndex, bossPhase, enemyType,
                enemyHealth, nextEnemySkillIndex, randomSnapshot, 0, SkillRegistry.getDefaultPlayerSkillIds(),
                List.of(), new EncounterSnapshot("combat", Map.of("enemyType", enemyType,
                        "enemyHealth", Integer.toString(enemyHealth),
                        "nextEnemySkillIndex", Integer.toString(nextEnemySkillIndex))));
    }

    /** Validates a complete snapshot before it can affect the active player or random sequence. */
    public GameSave {
        Objects.requireNonNull(difficulty, "Saved difficulty must not be null");
        Objects.requireNonNull(enemyType, "Saved enemy type must not be null");
        skillIds = List.copyOf(Objects.requireNonNull(skillIds, "Saved skills must not be null"));
        choices = List.copyOf(Objects.requireNonNull(choices, "Saved choices must not be null"));
        if (version != CURRENT_VERSION) {
            throw new IllegalArgumentException("Unsupported game save version: " + version);
        }
        if (playerHealth <= 0) {
            throw new IllegalArgumentException("Saved player health must be positive");
        }
        if (!(bossPhase == 0 && scenarioIndex >= 0 && scenarioIndex < 5)
                && !(bossPhase >= 1 && bossPhase <= 2 && scenarioIndex == 5)) {
            throw new IllegalArgumentException("Saved scenario and boss phase are inconsistent");
        }
        if (new HashSet<>(skillIds).size() != skillIds.size()
                || !skillIds.containsAll(SkillRegistry.getDefaultPlayerSkillIds())) {
            throw new IllegalArgumentException("Saved skills must be distinct and include the starting loadout");
        }
        skillIds.forEach(SkillRegistry::getSkill);
        if (activeEncounter == null) {
            if (bossPhase != 0 || choices.size() != 3
                    || !choices.get(0).type().equals("combat") || choices.get(1).type().equals("combat")) {
                throw new IllegalArgumentException("Saved selection must contain three valid encounter choices");
            }
            for (EncounterSnapshot choice : choices) {
                if (!choice.type().equals("combat") && !choice.data().get("phase").equals("NOT_STARTED")) {
                    throw new IllegalArgumentException("Offered events must not already be started");
                }
                validateEncounter(choice, scenarioIndex, bossPhase);
            }
        } else {
            if (!choices.isEmpty()) {
                throw new IllegalArgumentException("An active encounter cannot also contain pending choices");
            }
            validateEncounter(activeEncounter, scenarioIndex, bossPhase);
        }
        if (activeEncounter != null && activeEncounter.type().equals("combat")) {
            Map<String, String> data = activeEncounter.data();
            if (!enemyType.equals(data.get("enemyType"))
                    || enemyHealth != Integer.parseInt(data.get("enemyHealth"))
                    || nextEnemySkillIndex != Integer.parseInt(data.get("nextEnemySkillIndex"))) {
                throw new IllegalArgumentException("Saved combat fields must agree with the active encounter");
            }
        } else if (!enemyType.isEmpty() || enemyHealth != 0 || nextEnemySkillIndex != 0) {
            throw new IllegalArgumentException("A noncombat save must not contain opponent fields");
        }
    }

    /** Captures an offer without regenerating its already selected content on Continue. */
    public static GameSave forChoices(GameDifficulty difficulty, Player player, int scenarioIndex,
            RandomSnapshot randomSnapshot, List<EncounterSnapshot> choices) {
        return new GameSave(CURRENT_VERSION, difficulty, player.getHealth(), scenarioIndex, 0,
                "", 0, 0, randomSnapshot, player.getBonusTime(), ownedSkillIds(player), choices, null);
    }

    /** Captures a selected encounter and all persistent effects already earned by the player. */
    public static GameSave forEncounter(GameDifficulty difficulty, Player player, int scenarioIndex,
            int bossPhase, RandomSnapshot randomSnapshot, EncounterSnapshot active) {
        Objects.requireNonNull(active, "Active encounter must not be null");
        boolean combat = active.type().equals("combat");
        return new GameSave(CURRENT_VERSION, difficulty, player.getHealth(), scenarioIndex, bossPhase,
                combat ? active.data().get("enemyType") : "",
                combat ? Integer.parseInt(active.data().get("enemyHealth")) : 0,
                combat ? Integer.parseInt(active.data().get("nextEnemySkillIndex")) : 0,
                randomSnapshot, player.getBonusTime(), ownedSkillIds(player), List.of(), active);
    }

    /** Recreates the exact saved loadout and countdown adjustment, including health earned above starting HP. */
    public Player restorePlayer() {
        return new Player("Player", playerHealth, skillIds.stream().map(SkillRegistry::getSkill).toList(), bonusTime);
    }

    /** Recreates a saved combat opponent; event and choice snapshots have no opponent to restore. */
    public Enemy restoreEnemy() {
        if (activeEncounter == null || !activeEncounter.type().equals("combat")) {
            throw new IllegalStateException("The saved encounter is not combat");
        }
        Enemy enemy = createEnemy(enemyType, scenarioIndex, bossPhase);
        enemy.setHealth(enemyHealth);
        return enemy;
    }

    /** Keeps loadout order stable while translating shared skill definitions to persistent identifiers. */
    private static List<String> ownedSkillIds(Player player) {
        return player.getActiveSkills().stream().map(SkillRegistry::getId).toList();
    }

    /** Allows the old public combat constructors to load v1 data into the current representation. */
    private static int migratedVersion(int version) {
        if (version != 1 && version != CURRENT_VERSION) {
            throw new IllegalArgumentException("Unsupported game save version: " + version);
        }
        return CURRENT_VERSION;
    }

    /** Rejects combat data incompatible with progression and permits events only before the boss. */
    private static void validateEncounter(EncounterSnapshot snapshot, int scenarioIndex, int bossPhase) {
        if (!snapshot.type().equals("combat")) {
            if (bossPhase != 0) {
                throw new IllegalArgumentException("A boss phase must contain a combat encounter");
            }
            return;
        }
        Enemy enemy = createEnemy(snapshot.data().get("enemyType"), scenarioIndex, bossPhase);
        int health = Integer.parseInt(snapshot.data().get("enemyHealth"));
        int attackIndex = Integer.parseInt(snapshot.data().get("nextEnemySkillIndex"));
        if (health > enemy.getHealth() || attackIndex >= enemy.getActiveSkills().size()) {
            throw new IllegalArgumentException("Saved opponent health or attack index is outside the allowed range");
        }
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
