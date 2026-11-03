package seedu.keyboardwarrior.encounter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Random;

import org.junit.jupiter.api.Test;

import seedu.keyboardwarrior.combat.Player;
import seedu.keyboardwarrior.combat.Skill;
import seedu.keyboardwarrior.combat.SkillRegistry;
import seedu.keyboardwarrior.typing.PromptBank;
import seedu.keyboardwarrior.typing.PuzzleBank.Puzzle;
import seedu.keyboardwarrior.typing.TypingChallenge;
import seedu.keyboardwarrior.typing.TypingChallengeFactory;
import seedu.keyboardwarrior.ui.EventUi;

/** Tests that puzzle effects are applied only once after successful preparation. */
class PuzzleEncounterTest {
    /** Early and repeated submissions must not consume rewards or grant additional health. */
    @Test
    void lifecycle_invalidCalls_preserveSingleSkillReward() {
        Player player = new Player("Player", 100);
        Random random = new Random(42);
        RecordingEventUi ui = new RecordingEventUi();
        RewardPool rewards = new RewardPool(random, player);
        TypingChallengeFactory factory = new TypingChallengeFactory(new PromptBank(random));
        PuzzleEncounter encounter = new PuzzleEncounter(player, ui, new Puzzle("Question", "answer"), random, rewards);

        assertNull(encounter.updateEvent(factory));
        assertThrows(IllegalStateException.class, () -> encounter.submitAnswer("answer", factory));
        assertEquals(5, player.getActiveSkills().size());
        assertEquals(0, ui.rewardCount);
        assertFalse(encounter.getIsCompleted());
        assertEquals("answer", encounter.prepareEvent(factory).getText());
        assertThrows(IllegalStateException.class, () -> encounter.prepareEvent(factory));
        assertThrows(NullPointerException.class, () -> encounter.submitAnswer(null, factory));
        assertFalse(encounter.getIsCompleted());

        encounter.submitAnswer(" ANSWER ", factory);

        assertTrue(encounter.getIsCompleted());
        assertTrue(encounter.getEncounterOutcome().getIsVictory());
        assertEquals(6, player.getActiveSkills().size());
        assertSame(SkillRegistry.getSkill("overclock"), ui.reward);
        assertThrows(IllegalStateException.class, () -> encounter.submitAnswer("answer", factory));
        assertThrows(IllegalStateException.class, () -> encounter.prepareEvent(factory));
        assertNull(encounter.updateEvent(factory));
        assertEquals(1, ui.questionCount);
        assertEquals(1, ui.rewardCount);
        assertEquals(0, ui.healthRewardCount);
        assertEquals(100, player.getHealth());
        assertNull(rewards.popRandomReward());
    }

    /** The alternate reward must also be granted only once when all skills are already owned. */
    @Test
    void submitAnswer_emptyRewardPool_grantsHealthOnce() {
        Player player = new Player("Player", 100);
        player.addSkill(SkillRegistry.getSkill("overclock"));
        Random random = new Random(42);
        RecordingEventUi ui = new RecordingEventUi();
        TypingChallengeFactory factory = new TypingChallengeFactory(new PromptBank(random));
        PuzzleEncounter encounter = new PuzzleEncounter(player, ui, new Puzzle("Question", "answer"),
                random, new RewardPool(random, player));
        encounter.prepareEvent(factory);

        encounter.submitAnswer("answer", factory);

        assertThrows(IllegalStateException.class, () -> encounter.submitAnswer("answer", factory));
        assertEquals(120, player.getHealth());
        assertEquals(1, ui.healthRewardCount);
        assertTrue(encounter.getIsCompleted());
    }

    /** A wrong answer must not keep reducing the countdown after the event completes. */
    @Test
    void submitAnswer_incorrectAnswer_appliesPenaltyOnce() {
        Player player = new Player("Player", 100);
        Random random = new Random(42);
        RecordingEventUi ui = new RecordingEventUi();
        TypingChallengeFactory factory = new TypingChallengeFactory(new PromptBank(random));
        PuzzleEncounter encounter = new PuzzleEncounter(player, ui, new Puzzle("Question", "answer"),
                random, new RewardPool(random, player));
        encounter.prepareEvent(factory);

        encounter.submitAnswer("wrong", factory);

        assertThrows(IllegalStateException.class, () -> encounter.submitAnswer("wrong", factory));
        assertThrows(IllegalStateException.class, () -> encounter.submitAnswer("answer", factory));
        assertEquals(-2, player.getBonusTime());
        assertEquals(1, ui.penaltyCount);
        assertEquals(5, player.getActiveSkills().size());
        assertTrue(encounter.getIsCompleted());
        assertTrue(encounter.getEncounterOutcome().getIsVictory());
    }

    /** Failed challenge generation must not leave a puzzle ready for an answer. */
    @Test
    void prepareEvent_factoryFailure_allowsPreparationRetry() {
        Player player = new Player("Player", 100);
        Random random = new Random(42);
        RecordingEventUi ui = new RecordingEventUi();
        TypingChallengeFactory factory = new FailingOnceFactory(random);
        PuzzleEncounter encounter = new PuzzleEncounter(player, ui, new Puzzle("Question", "answer"),
                random, new RewardPool(random, player));

        assertThrows(IllegalStateException.class, () -> encounter.prepareEvent(factory));
        assertThrows(IllegalStateException.class, () -> encounter.submitAnswer("answer", factory));
        assertEquals(0, ui.questionCount);
        assertNotNull(encounter.prepareEvent(factory));
        encounter.submitAnswer("answer", factory);
        assertEquals(1, ui.questionCount);
        assertEquals(1, ui.rewardCount);
    }

    /** Feedback failures after a reward must not allow the same answer to apply another effect. */
    @Test
    void submitAnswer_feedbackFailure_doesNotAllowRewardRetry() {
        Player player = new Player("Player", 100);
        Random random = new Random(42);
        RecordingEventUi ui = new RecordingEventUi();
        ui.failRewardFeedback = true;
        TypingChallengeFactory factory = new TypingChallengeFactory(new PromptBank(random));
        PuzzleEncounter encounter = new PuzzleEncounter(player, ui, new Puzzle("Question", "answer"),
                random, new RewardPool(random, player));
        encounter.prepareEvent(factory);

        assertThrows(IllegalStateException.class, () -> encounter.submitAnswer("answer", factory));
        assertThrows(IllegalStateException.class, () -> encounter.submitAnswer("answer", factory));
        assertEquals(6, player.getActiveSkills().size());
        assertEquals(100, player.getHealth());
        assertEquals(1, ui.rewardCount);
        assertFalse(encounter.getIsCompleted());
    }

    /** Fails the first custom challenge request to exercise retryable preparation. */
    private static class FailingOnceFactory extends TypingChallengeFactory {
        private boolean shouldFail = true;

        /** Retains the run's shared random source. */
        FailingOnceFactory(Random random) {
            super(new PromptBank(random));
        }

        @Override
        public TypingChallenge generateCustom(String text, int seconds) {
            if (shouldFail) {
                shouldFail = false;
                throw new IllegalStateException("Simulated factory failure");
            }
            return super.generateCustom(text, seconds);
        }
    }

    /** Records puzzle feedback and can simulate a failure after the skill has been granted. */
    private static class RecordingEventUi extends EventUi {
        private int questionCount;
        private int rewardCount;
        private int healthRewardCount;
        private int penaltyCount;
        private Skill reward;
        private boolean failRewardFeedback;

        @Override
        public void showPuzzle(Puzzle puzzle) {
            questionCount++;
        }

        @Override
        public void showPuzzleVictory(Skill newSkill) {
            rewardCount++;
            reward = newSkill;
            if (failRewardFeedback) {
                throw new IllegalStateException("Simulated feedback failure");
            }
        }

        @Override
        public void showAltPuzzleVictory(int increaseAmount, int currentHP) {
            healthRewardCount++;
        }

        @Override
        public void showPuzzleDefeat() {
            penaltyCount++;
        }
    }
}
