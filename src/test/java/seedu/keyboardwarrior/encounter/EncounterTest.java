package seedu.keyboardwarrior.encounter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Tests the completion and outcome contract shared by encounters.
 */
class EncounterTest {
    /**
     * Checks that an incomplete encounter does not expose its outcome.
     */
    @Test
    void newEncounter_beforeCompletion_hasNoAccessibleOutcome() {
        TestEncounter encounter = new TestEncounter();

        assertFalse(encounter.getIsCompleted());
        assertEquals("Test Encounter", encounter.getDisplayText());
        assertThrows(IllegalStateException.class, encounter::getEncounterOutcome);
    }

    /**
     * Checks that a completed encounter returns its recorded outcome instance.
     */
    @Test
    void completedEncounter_returnsRecordedOutcome() {
        TestEncounter encounter = new TestEncounter();

        encounter.complete(true);

        assertTrue(encounter.getIsCompleted());
        EncounterOutcome outcome = encounter.getEncounterOutcome();
        assertTrue(outcome.getIsVictory());
        assertSame(outcome, encounter.getEncounterOutcome());
    }

    /**
     * Minimal encounter used to exercise the base-class lifecycle.
     */
    private static class TestEncounter extends Encounter {
        /**
         * Completes the fixture encounter with a defeat outcome.
         */
        @Override
        public void start() {
            complete(false);
        }

        /**
         * Returns the fixture's fixed display text.
         *
         * @return fixture encounter description
         */
        @Override
        public String getDisplayText() {
            return "Test Encounter";
        }

        /**
         * Marks the fixture encounter complete with the supplied outcome.
         *
         * @param isVictory whether the encounter should record victory
         */
        private void complete(boolean isVictory) {
            outcome.setVictory(isVictory);
            isCompleted = true;
        }
    }
}
