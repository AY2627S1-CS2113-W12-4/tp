package seedu.keyboardwarrior.encounter;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
        List<Encounter> choices = new ArrayList<>();

        Enemy enemy = EnemyFactory.createRandomEnemy(random);
        choices.add(new CombatEncounter(player, enemy, combatUi, typingChallengeFactory));
        choices.add(getRandomEvent(player, eventUi));

        if (random.nextBoolean()) {
            Enemy enemy2 = EnemyFactory.createRandomEnemy(random);
            choices.add(new CombatEncounter(player, enemy2, combatUi, typingChallengeFactory));
        } else {
            choices.add(getRandomEvent(player, eventUi));
        }

        assert choices.size() == 3 && !choices.contains(null) : "Every offer must contain three encounters";
        assert choices.get(0) instanceof CombatEncounter && choices.get(1) instanceof EventEncounter
                : "Every offer must include both combat and an event";
        return choices;
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
