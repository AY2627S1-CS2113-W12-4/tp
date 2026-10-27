package seedu.keyboardwarrior.encounter;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import seedu.keyboardwarrior.combat.Enemy;
import seedu.keyboardwarrior.combat.EnemyFactory;
import seedu.keyboardwarrior.combat.Player;
import seedu.keyboardwarrior.typing.TypingChallengeFactory;
import seedu.keyboardwarrior.ui.CombatUi;
import seedu.keyboardwarrior.ui.EventUi;

/**
 * EncounterFactory
 */
public class EncounterFactory {

    private final Random random;

    public EncounterFactory(Random random) {
        this.random = random;
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
        int eventType = random.nextInt(2);
        switch (eventType) {
        case 0:
        case 1:
        default:
            return null;
        }
    }
}
