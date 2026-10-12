package seedu.keyboardwarrior.typing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Tests validation of challenge text and its time limit.
 */
class TypingChallengeTest {
    @Test
    public void constructor_validChallenge_preservesTextAndTime() {
        TypingChallenge challenge = new TypingChallenge("  Strike the goblin  ", 15);

        assertEquals("  Strike the goblin  ", challenge.getText());
        assertEquals(15, challenge.getTime());
    }

    @Test
    public void constructor_nullText_throwsException() {
        assertThrows(NullPointerException.class, () -> new TypingChallenge(null, 15));
    }

    @Test
    public void constructor_blankText_throwsException() {
        for (String text : List.of("", " \t\n")) {
            assertThrows(IllegalArgumentException.class, () -> new TypingChallenge(text, 15));
        }
    }

    @Test
    public void constructor_nonpositiveTime_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> new TypingChallenge("Strike", 0));
        assertThrows(IllegalArgumentException.class, () -> new TypingChallenge("Strike", -1));
    }
}
