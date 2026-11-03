package seedu.keyboardwarrior.encounter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

import seedu.keyboardwarrior.GameDifficulty;
import seedu.keyboardwarrior.combat.Enemy;
import seedu.keyboardwarrior.combat.EnemyFactory;
import seedu.keyboardwarrior.combat.Player;
import seedu.keyboardwarrior.typing.PuzzleBank;
import seedu.keyboardwarrior.typing.ReactionBank;
import seedu.keyboardwarrior.typing.TypingChallengeFactory;
import seedu.keyboardwarrior.ui.CombatUi;
import seedu.keyboardwarrior.ui.EventUi;

/**
 * Creates encounter choices using one shared random sequence for opponents and
 * events.
 */
public class EncounterFactory {

    /**
     * Run's master random source, also passed by reference to banks and reaction
     * encounters.
     */
    private final Random random;
    /**
     * Puzzle content selected with the same random source as the encounter choices.
     */
    private final PuzzleBank puzzleBank;
    /**
     * Reaction commands selected with the same random source as the encounter
     * choices.
     */
    private final ReactionBank reactionBank;

    /**
     * Shared pool of unclaimed skill rewards, initialized for the constructor's
     * player.
     */
    private final RewardPool rewardPool;

    /**
     * Event categories sampled uniformly from the implemented encounters.
     */
    private enum EventType {
        /** Presents a question and checks the submitted answer. */
        PuzzleEvent,
        /** Delays a command before requesting a quick response. */
        ReactionEvent,
        /** Runs a branching merchant conversation. */
        DialogueEvent
    }

    /**
     * Creates the event banks and reward pool using the supplied random instance.
     * The pool excludes skills owned by the supplied player at construction.
     * Keep this factory for that player's run so offered puzzles share the
     * remaining rewards.
     *
     * @param random nonnull master random instance shared by the current run
     * @param player player whose existing skills are excluded from the reward pool
     */
    public EncounterFactory(Random random, Player player) {
        this.random = random;
        this.puzzleBank = new PuzzleBank(random);
        this.reactionBank = new ReactionBank(random);
        this.rewardPool = new RewardPool(random, player);
    }

    /**
     * Creates three choices: combat, an event, then a randomly chosen combat or
     * event.
     * Generating every offered choice consumes the shared random sequence, even if
     * not selected.
     * Combat currently uses default enemy health and the constructor's default
     * damage difficulty.
     *
     * @param player                 same player used to initialize this factory's
     *                               reward pool
     * @param combatUi               interface for combat encounters
     * @param eventUi                interface for event encounters
     * @param typingChallengeFactory run's shared prompt factory, backed by the same
     *                               master random
     * @return three fresh encounters in display order
     */
    public List<Encounter> generateChoices(Player player, CombatUi combatUi, EventUi eventUi,
            TypingChallengeFactory typingChallengeFactory) {
        return generateChoices(player, combatUi, eventUi, typingChallengeFactory, GameDifficulty.HARD, 0);
    }

    /**
     * Creates the same three choices using the current scenario's combat health and run difficulty.
     * A zero scenario retains the legacy default-enemy behavior for standalone callers.
     *
     * @param player run's shared player
     * @param combatUi combat display
     * @param eventUi event display
     * @param typingChallengeFactory shared prompt and countdown factory
     * @param difficulty selected run damage multiplier
     * @param scenarioNumber current regular scenario, one through five, or zero for default health
     * @return combat, event and one further random choice
     */
    public List<Encounter> generateChoices(Player player, CombatUi combatUi, EventUi eventUi,
            TypingChallengeFactory typingChallengeFactory, GameDifficulty difficulty, int scenarioNumber) {
        Objects.requireNonNull(difficulty, "Run difficulty must not be null");
        if (scenarioNumber < 0 || scenarioNumber > 5) {
            throw new IllegalArgumentException("Scenario number must be zero through five");
        }
        List<Encounter> choices = new ArrayList<>();

        Enemy enemy = createChoiceEnemy(scenarioNumber);
        choices.add(new CombatEncounter(player, enemy, combatUi, typingChallengeFactory, difficulty));
        choices.add(getRandomEvent(player, eventUi));

        if (random.nextBoolean()) {
            Enemy enemy2 = createChoiceEnemy(scenarioNumber);
            choices.add(new CombatEncounter(player, enemy2, combatUi, typingChallengeFactory, difficulty));
        } else {
            choices.add(getRandomEvent(player, eventUi));
        }

        assert choices.size() == 3 && !choices.contains(null) : "Every offer must contain three encounters";
        assert choices.get(0) instanceof CombatEncounter && choices.get(1) instanceof EventEncounter
                : "Every offer must include both combat and an event";
        return choices;
    }

    /** Selects an opponent with balanced scenario HP without drawing extra randomness. */
    private Enemy createChoiceEnemy(int scenarioNumber) {
        return scenarioNumber == 0 ? EnemyFactory.createRandomEnemy(random)
                : EnemyFactory.createRandomEnemy(random, scenarioNumber);
    }

    /** Saves already selected content for a combat offer, stable combat turn or unfinished event. */
    public EncounterSnapshot snapshot(Encounter encounter) {
        Objects.requireNonNull(encounter, "Encounter must not be null");
        if (encounter instanceof EventEncounter event) {
            return event.snapshot();
        }
        if (encounter instanceof CombatEncounter combat) {
            CombatEncounter.CombatSnapshot snapshot = combat.snapshotForSave();
            return new EncounterSnapshot("combat", Map.of("enemyType", snapshot.enemyType(),
                    "enemyHealth", Integer.toString(snapshot.enemyHealth()),
                    "nextEnemySkillIndex", Integer.toString(snapshot.nextEnemySkillIndex())));
        }
        throw new IllegalArgumentException("Unsupported encounter class");
    }

    /**
     * Recreates saved content without consuming randomness or applying player effects.
     * The caller starts a combat with its saved attack index, or resumes an active event.
     *
     * @param snapshot selected content and stable stage
     * @param player restored player shared by every offer
     * @param combatUi combat display
     * @param eventUi event display
     * @param typingChallengeFactory shared countdown factory retained by combat
     * @param difficulty saved run difficulty
     * @param scenarioNumber one-based regular scenario, or six during the boss
     * @param bossPhase zero for regular scenarios, otherwise one or two
     * @return unstarted combat or event at its saved lifecycle stage
     */
    public Encounter restore(EncounterSnapshot snapshot, Player player, CombatUi combatUi, EventUi eventUi,
            TypingChallengeFactory typingChallengeFactory, GameDifficulty difficulty,
            int scenarioNumber, int bossPhase) {
        Objects.requireNonNull(snapshot, "Encounter snapshot must not be null");
        Map<String, String> data = snapshot.data();
        switch (snapshot.type()) {
        case "combat":
            return restoreCombat(data, player, combatUi, typingChallengeFactory, difficulty, scenarioNumber, bossPhase);
        case "puzzle":
            return restorePuzzle(data, player, eventUi);
        case "reaction":
            return restoreReaction(data, player, eventUi);
        case "dialogue":
            return restoreDialogue(data, player, eventUi);
        default:
            throw new IllegalArgumentException("Unsupported saved encounter: " + snapshot.type());
        }
    }

    /** Rebuilds a combat opponent without selecting another enemy or starting its turn. */
    private CombatEncounter restoreCombat(Map<String, String> data, Player player, CombatUi combatUi,
            TypingChallengeFactory typingChallengeFactory, GameDifficulty difficulty,
            int scenarioNumber, int bossPhase) {
        Enemy enemy = restoreCombatEnemy(data, scenarioNumber, bossPhase);
        return new CombatEncounter(player, enemy, combatUi, typingChallengeFactory, difficulty);
    }

    /** Validates saved opponent bounds before restoring health, without consuming randomness. */
    private Enemy restoreCombatEnemy(Map<String, String> data, int scenarioNumber, int bossPhase) {
        Enemy enemy = bossPhase == 0
                ? EnemyFactory.createScenarioEnemy(data.get("enemyType"), scenarioNumber)
                : EnemyFactory.createBossPhase(bossPhase);
        if (bossPhase != 0 && !data.get("enemyType").equals("kernel_overlord")) {
            throw new IllegalArgumentException("A boss snapshot must contain the Kernel Overlord");
        }
        int health = Integer.parseInt(data.get("enemyHealth"));
        int attackIndex = Integer.parseInt(data.get("nextEnemySkillIndex"));
        if (health > enemy.getHealth() || attackIndex >= enemy.getActiveSkills().size()) {
            throw new IllegalArgumentException("Saved combat data exceeds the opponent's bounds");
        }
        enemy.setHealth(health);
        return enemy;
    }

    /** Restores the chosen puzzle and lifecycle stage without drawing content or rewards. */
    private PuzzleEncounter restorePuzzle(Map<String, String> data, Player player, EventUi eventUi) {
        PuzzleEncounter puzzle = new PuzzleEncounter(player, eventUi,
                new PuzzleBank.Puzzle(data.get("question"), data.get("answer")), random, rewardPool);
        puzzle.restorePhase(data.get("phase"));
        return puzzle;
    }

    /** Restores the selected command and remaining wait without drawing another delay. */
    private ReactionEncounter restoreReaction(Map<String, String> data, Player player, EventUi eventUi) {
        ReactionEncounter reaction = new ReactionEncounter(player, eventUi, data.get("targetWord"), random);
        reaction.restorePhase(data.get("phase"), Long.parseLong(data.get("delayMillis")));
        return reaction;
    }

    /** Restores the conversation branch without replaying its previous rewards or penalties. */
    private DialogueEncounter restoreDialogue(Map<String, String> data, Player player, EventUi eventUi) {
        DialogueEncounter dialogue = new DialogueEncounter(player, eventUi, rewardPool);
        dialogue.restorePhase(data.get("phase"), Integer.parseInt(data.get("node")));
        return dialogue;
    }

    /**
     * Samples an event category and its content from the run's shared random
     * sequence.
     * Reaction encounters retain that same instance for their later delay
     * selection. Puzzle and dialogue encounters share the factory's reward pool
     * and draw rewards only when their reward conditions are met.
     *
     * @param player  player receiving the event's effects
     * @param eventUi interface used to display the event
     * @return a fresh puzzle, reaction or dialogue event
     */
    private EventEncounter getRandomEvent(Player player, EventUi eventUi) {
        EventType[] allTypes = EventType.values();
        assert allTypes.length > 0 : "At least one event category must be available";
        EventType eventType = allTypes[random.nextInt(allTypes.length)];
        switch (eventType) {
        case PuzzleEvent:
            return new PuzzleEncounter(player, eventUi, puzzleBank.getRandomPuzzle(), random, rewardPool);
        case ReactionEvent:
            return new ReactionEncounter(player, eventUi, reactionBank.getRandomReaction(), random);
        case DialogueEvent:
            return new DialogueEncounter(player, eventUi, rewardPool);
        default:
            return new PuzzleEncounter(player, eventUi, puzzleBank.getRandomPuzzle(), random, rewardPool);
        }
    }
}
