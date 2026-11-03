package seedu.keyboardwarrior.encounter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.Test;

import seedu.keyboardwarrior.combat.Player;
import seedu.keyboardwarrior.combat.SkillRegistry;
import seedu.keyboardwarrior.typing.PromptBank;
import seedu.keyboardwarrior.typing.PuzzleBank.Puzzle;
import seedu.keyboardwarrior.typing.TypingChallengeFactory;
import seedu.keyboardwarrior.ui.EventUi;

/** Checks that event rewards, penalties and countdowns remain valid at integer boundaries. */
class RewardBoundaryTest {
    /** Puzzle healing saturates and reports only the health actually gained. */
    @Test
    void puzzle_healthNearLimit_saturatesAndReportsActualReward() {
        Player player = new Player("Player", Integer.MAX_VALUE - 5);
        player.addSkill(SkillRegistry.getSkill("overclock"));
        Random random = new Random(42);
        RecordingEventUi ui = new RecordingEventUi();
        TypingChallengeFactory factory = new TypingChallengeFactory(new PromptBank(random));
        PuzzleEncounter encounter = new PuzzleEncounter(player, ui, new Puzzle("Question", "answer"),
                random, new RewardPool(random, player));
        encounter.prepareEvent(factory);

        encounter.submitAnswer("answer", factory);

        assertEquals(Integer.MAX_VALUE, player.getHealth());
        assertEquals(5, ui.healthGained);
        assertTrue(encounter.getIsCompleted());
    }

    /** Reaction healing uses the same saturation rule as puzzle healing. */
    @Test
    void reaction_healthNearLimit_saturatesAndReportsActualReward() {
        Player player = new Player("Player", Integer.MAX_VALUE - 5);
        Random random = new Random(42);
        RecordingEventUi ui = new RecordingEventUi();
        AtomicLong time = new AtomicLong(1000);
        TypingChallengeFactory factory = new TypingChallengeFactory(new PromptBank(random));
        ReactionEncounter encounter = new ReactionEncounter(player, ui, "dodge", random, time::get);
        encounter.prepareEvent(factory);
        time.addAndGet(30_000);
        assertNotNull(encounter.updateEvent(factory));

        encounter.submitAnswer("dodge", factory);

        assertEquals(Integer.MAX_VALUE, player.getHealth());
        assertEquals(5, ui.healthGained);
        assertTrue(encounter.getIsCompleted());
    }

    /** Maximum bonuses must not wrap a puzzle's countdown to its minimum. */
    @Test
    void puzzle_maximumBonus_preservesLongCountdown() {
        Player player = new Player("Player", 100);
        player.setBonusTime(Integer.MAX_VALUE);
        Random random = new Random(42);
        TypingChallengeFactory factory = new TypingChallengeFactory(new PromptBank(random));
        PuzzleEncounter encounter = new PuzzleEncounter(player, new RecordingEventUi(),
                new Puzzle("Question", "answer"), random, new RewardPool(random, player));

        assertEquals(Integer.MAX_VALUE, encounter.prepareEvent(factory).getTime());
    }

    /** Penalties at the lower limit must not turn into large positive bonuses. */
    @Test
    void puzzle_minimumBonus_saturatesPenalty() {
        Player player = new Player("Player", 100);
        player.setBonusTime(Integer.MIN_VALUE + 1);
        Random random = new Random(42);
        TypingChallengeFactory factory = new TypingChallengeFactory(new PromptBank(random));
        PuzzleEncounter encounter = new PuzzleEncounter(player, new RecordingEventUi(),
                new Puzzle("Question", "answer"), random, new RewardPool(random, player));
        assertEquals(1, encounter.prepareEvent(factory).getTime());

        encounter.submitAnswer("wrong", factory);

        assertEquals(Integer.MIN_VALUE, player.getBonusTime());
        assertTrue(encounter.getIsCompleted());
    }

    /** Records the actual healing amount without printing event feedback. */
    private static class RecordingEventUi extends EventUi {
        private int healthGained;

        @Override
        public void showPuzzle(Puzzle puzzle) {
        }

        @Override
        public void showAltPuzzleVictory(int increaseAmount, int currentHP) {
            healthGained = increaseAmount;
        }

        @Override
        public void showPuzzleDefeat() {
        }

        @Override
        public void showReaction() {
        }

        @Override
        public void showReactionWord(String targetWord) {
        }

        @Override
        public void showReactionVictory(int increaseAmount, int currentHP) {
            healthGained = increaseAmount;
        }
    }
}
