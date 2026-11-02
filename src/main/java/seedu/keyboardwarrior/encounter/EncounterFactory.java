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
 * EncounterFactory
 */
public class EncounterFactory {

    private final Random random;
    private final PuzzleBank puzzleBank;
    private final ReactionBank reactionBank;

    private enum EventType {
        PuzzleEvent,
        ReactionEvent,
        DialogueEvent
    }

    public EncounterFactory(Random random) {
        this.random = random;
        this.puzzleBank = new PuzzleBank(random);
        this.reactionBank = new ReactionBank(random);
    }

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

        return choices;
    }

    private EventEncounter getRandomEvent(Player player, EventUi eventUi) {
        EventType[] allTypes = EventType.values();
        EventType eventType = allTypes[random.nextInt(allTypes.length)];
        switch (eventType) {
        case PuzzleEvent:
            return new PuzzleEncounter(player, eventUi, puzzleBank.getRandomPuzzle());
        case ReactionEvent:
            return new ReactionEncounter(player, eventUi, reactionBank.getRandomReaction(), random);
        case DialogueEvent:
            // TODO: Replace with DialogueEvent once implemented.
            return new PuzzleEncounter(player, eventUi, puzzleBank.getRandomPuzzle());
        default:
            return new PuzzleEncounter(player, eventUi, puzzleBank.getRandomPuzzle());
        }
    }
}
