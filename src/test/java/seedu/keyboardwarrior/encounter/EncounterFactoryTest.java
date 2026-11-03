package seedu.keyboardwarrior.encounter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.Test;

import seedu.keyboardwarrior.combat.Player;
import seedu.keyboardwarrior.typing.PromptBank;
import seedu.keyboardwarrior.typing.TypingChallengeFactory;
import seedu.keyboardwarrior.ui.CombatUi;
import seedu.keyboardwarrior.ui.EventUi;

/** Verifies dialogue choices and their shared reward pool and random source. */
class EncounterFactoryTest {
    /** Two offered dialogues must share rewards so the second trade heals instead of granting a duplicate skill. */
    @Test
    void generateChoices_dialogueSelection_usesSharedRewardPoolAndRandom() {
        DialogueRandom random = new DialogueRandom();
        Player player = new Player("Player", 100);
        TypingChallengeFactory challengeFactory = new TypingChallengeFactory(new PromptBank(random));
        List<Encounter> choices = new EncounterFactory(random, player)
                .generateChoices(player, new CombatUi(), new EventUi(), challengeFactory);

        assertEquals(3, choices.size());
        assertInstanceOf(CombatEncounter.class, choices.get(0));
        DialogueEncounter first = assertInstanceOf(DialogueEncounter.class, choices.get(1));
        DialogueEncounter second = assertInstanceOf(DialogueEncounter.class, choices.get(2));
        assertEquals(3, random.integerDraws);
        assertEquals(1, random.booleanDraws);

        completeTrade(first, challengeFactory);
        assertEquals(6, player.getActiveSkills().size());
        assertEquals(100, player.getHealth());
        assertEquals(4, random.integerDraws);

        completeTrade(second, challengeFactory);
        assertEquals(6, player.getActiveSkills().size());
        assertEquals(120, player.getHealth());
        assertEquals(4, random.integerDraws, "An empty shared reward pool must not draw again.");
    }

    /** Completes the two matching responses and checks the successful outcome. */
    private void completeTrade(DialogueEncounter encounter, TypingChallengeFactory factory) {
        encounter.prepareEvent(factory);
        encounter.submitAnswer("I will not be extorted", factory);
        encounter.submitAnswer("Show me your rare wares", factory);
        assertTrue(encounter.getEncounterOutcome().getIsVictory());
    }

    /** Chooses dialogue for event categories and counts use of this exact random instance. */
    private static class DialogueRandom extends Random {
        private int integerDraws;
        private int booleanDraws;

        @Override
        public int nextInt(int bound) {
            integerDraws++;
            return bound == 3 ? 2 : 0;
        }

        @Override
        public boolean nextBoolean() {
            booleanDraws++;
            return false;
        }
    }
}
