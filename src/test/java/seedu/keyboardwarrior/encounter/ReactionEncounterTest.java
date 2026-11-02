package seedu.keyboardwarrior.encounter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.Test;

import seedu.keyboardwarrior.combat.Player;
import seedu.keyboardwarrior.typing.PromptBank;
import seedu.keyboardwarrior.typing.TypingChallenge;
import seedu.keyboardwarrior.typing.TypingChallengeFactory;
import seedu.keyboardwarrior.ui.EventUi;

/** Tests reaction lifecycle guards, prompt activation and lethal-health boundaries. */
class ReactionEncounterTest {
    /** Overkill must finish the encounter instead of attempting to store negative health. */
    @Test
    void submitAnswer_damageExceedsHealth_completesDefeatAtZeroHealth() {
        assertFailedReaction(20, 0, true);
    }

    /** Exactly lethal damage must update both health and the defeated flag. */
    @Test
    void submitAnswer_damageEqualsHealth_marksPlayerDead() {
        assertFailedReaction(30, 0, true);
    }

    /** Surviving a failed reaction preserves the existing survival-based outcome. */
    @Test
    void submitAnswer_damageBelowHealth_keepsPlayerAlive() {
        assertFailedReaction(31, 1, false);
    }

    /**
     * Resolves one failed reaction and checks the player, outcome and displayed health.
     *
     * @param startingHealth health before the reaction
     * @param expectedHealth health after thirty damage, clamped at zero
     * @param expectedDead whether the damage should defeat the player
     */
    private void assertFailedReaction(int startingHealth, int expectedHealth, boolean expectedDead) {
        Random random = new Random(42);
        Player player = new Player("Player", startingHealth);
        RecordingEventUi ui = new RecordingEventUi();
        TypingChallengeFactory factory = new TypingChallengeFactory(new PromptBank(random));
        AtomicLong time = new AtomicLong(1000);
        ReactionEncounter encounter = new ReactionEncounter(player, ui, "dodge", random, time::get);
        encounter.prepareEvent(factory);
        time.addAndGet(30_000);
        assertNotNull(encounter.updateEvent(factory));

        encounter.submitAnswer("wrong", factory);

        assertEquals(expectedHealth, player.getHealth());
        assertEquals(expectedDead, player.getIsDead());
        assertTrue(encounter.getIsCompleted());
        assertEquals(!expectedDead, encounter.getEncounterOutcome().getIsVictory());
        assertTrue(ui.failureShown);
        assertFalse(ui.successShown);
        assertEquals(30, ui.reportedDamage);
        assertEquals(expectedHealth, ui.reportedHealth);
        assertEquals("dodge", ui.expectedCommand);
        assertThrows(IllegalStateException.class, () -> encounter.submitAnswer("wrong", factory));
        assertNull(encounter.updateEvent(factory));
        assertEquals(expectedHealth, player.getHealth());
    }


    /** Invalid calls must not bypass the prompt, redraw the delay or repeat the reward. */
    @Test
    void lifecycle_invalidCalls_preserveSinglePromptAndReward() {
        Random random = new Random(42);
        Random expectedRandom = new Random(42);
        Player player = new Player("Player", 100);
        RecordingEventUi ui = new RecordingEventUi();
        AtomicLong time = new AtomicLong(1000);
        TypingChallengeFactory factory = new TypingChallengeFactory(new PromptBank(random));
        ReactionEncounter encounter = new ReactionEncounter(player, ui, "dodge", random, time::get);

        assertNull(encounter.updateEvent(factory));
        assertThrows(IllegalStateException.class, () -> encounter.submitAnswer("dodge", factory));
        assertEquals(0, ui.promptCount);
        assertEquals(100, player.getHealth());
        assertFalse(encounter.getIsCompleted());

        assertNull(encounter.prepareEvent(factory));
        long delay = 5000 + expectedRandom.nextInt(25000);
        assertThrows(IllegalStateException.class, () -> encounter.prepareEvent(factory));
        assertThrows(IllegalStateException.class, () -> encounter.submitAnswer("dodge", factory));
        assertNull(encounter.updateEvent(factory));
        time.addAndGet(delay);
        assertNull(encounter.updateEvent(factory));
        time.incrementAndGet();

        TypingChallenge challenge = encounter.updateEvent(factory);
        assertNotNull(challenge);
        assertEquals("dodge", challenge.getText());
        assertEquals(2, challenge.getTime());
        assertNull(encounter.updateEvent(factory));
        assertThrows(IllegalStateException.class, () -> encounter.prepareEvent(factory));

        encounter.submitAnswer(" DODGE ", factory);
        assertTrue(encounter.getIsCompleted());
        assertTrue(encounter.getEncounterOutcome().getIsVictory());
        assertThrows(IllegalStateException.class, () -> encounter.submitAnswer("dodge", factory));
        assertThrows(IllegalStateException.class, () -> encounter.prepareEvent(factory));
        time.addAndGet(30_000);
        assertNull(encounter.updateEvent(factory));
        assertEquals(110, player.getHealth());
        assertEquals(1, ui.preparationCount);
        assertEquals(1, ui.promptCount);
        assertEquals(1, ui.successCount);
        assertEquals(expectedRandom.nextLong(), random.nextLong(),
                "Rejected lifecycle calls must not consume additional random draws.");
    }

    /** Null input must not consume the active response or grant health. */
    @Test
    void submitAnswer_nullInput_preservesActivePrompt() {
        Random random = new Random(42);
        Player player = new Player("Player", 100);
        AtomicLong time = new AtomicLong(1000);
        RecordingEventUi ui = new RecordingEventUi();
        TypingChallengeFactory factory = new TypingChallengeFactory(new PromptBank(random));
        ReactionEncounter encounter = new ReactionEncounter(player, ui, "dodge", random, time::get);
        encounter.prepareEvent(factory);
        time.addAndGet(30_000);
        assertNotNull(encounter.updateEvent(factory));

        assertThrows(NullPointerException.class, () -> encounter.submitAnswer(null, factory));
        assertEquals(100, player.getHealth());
        assertFalse(encounter.getIsCompleted());
        encounter.submitAnswer("dodge", factory);
        assertEquals(110, player.getHealth());
        assertTrue(encounter.getIsCompleted());
    }

    /** A failed challenge generation must leave the event waiting so activation can be retried. */
    @Test
    void updateEvent_factoryFailure_allowsRetryWithoutRevealingPromptEarly() {
        Random random = new Random(42);
        Player player = new Player("Player", 100);
        AtomicLong time = new AtomicLong(1000);
        RecordingEventUi ui = new RecordingEventUi();
        TypingChallengeFactory factory = new FailingOnceFactory(random);
        ReactionEncounter encounter = new ReactionEncounter(player, ui, "dodge", random, time::get);
        encounter.prepareEvent(factory);
        time.addAndGet(30_000);

        assertThrows(IllegalStateException.class, () -> encounter.updateEvent(factory));
        assertEquals(0, ui.promptCount);
        assertThrows(IllegalStateException.class, () -> encounter.submitAnswer("dodge", factory));
        assertNotNull(encounter.updateEvent(factory));
        assertEquals(1, ui.promptCount);
        encounter.submitAnswer("dodge", factory);
        assertEquals(110, player.getHealth());
    }

    /** Fails one challenge request to exercise safe prompt activation after a retry. */
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

    /** Records feedback without writing to the terminal. */
    private static class RecordingEventUi extends EventUi {
        private int preparationCount;
        private int promptCount;
        private int successCount;
        private boolean failureShown;
        private boolean successShown;
        private int reportedDamage;
        private int reportedHealth;
        private String expectedCommand;

        @Override
        public void showReaction() {
            preparationCount++;
        }

        @Override
        public void showReactionWord(String targetWord) {
            promptCount++;
        }

        @Override
        public void showReactionVictory(int increaseAmount, int currentHP) {
            successShown = true;
            successCount++;
        }

        @Override
        public void showReactionDefeat(int damage, int currentHP, String targetWord) {
            failureShown = true;
            reportedDamage = damage;
            reportedHealth = currentHP;
            expectedCommand = targetWord;
        }
    }
}
