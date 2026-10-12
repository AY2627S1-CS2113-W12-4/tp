package seedu.keyboardwarrior.encounter;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Tests storage of an encounter's victory status.
 */
class EncounterOutcomeTest {
    @Test
    void setVictory_updatesOutcome() {
        EncounterOutcome outcome = new EncounterOutcome();

        assertFalse(outcome.getIsVictory());
        outcome.setVictory(true);
        assertTrue(outcome.getIsVictory());
        outcome.setVictory(false);
        assertFalse(outcome.getIsVictory());
    }
}
