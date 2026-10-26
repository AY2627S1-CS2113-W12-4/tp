package seedu.keyboardwarrior.combat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import seedu.keyboardwarrior.typing.TypingChallenge.Difficulty;

/**
 * Tests lookup of predefined combat skills.
 */
class SkillRegistryTest {
    /**
     * Checks that a known identifier returns the shared skill definition.
     */
    @Test
    void getSkill_knownId_returnsConfiguredSkill() {
        Skill skill = SkillRegistry.getSkill("buffer_overflow");

        assertEquals("Buffer Overflow", skill.getName());
        assertEquals(40, skill.getBaseDamage());
        assertEquals(Difficulty.HARD, skill.getDifficulty());
        assertSame(skill, SkillRegistry.getSkill("buffer_overflow"));
    }

    /**
     * Checks that an unknown skill identifier produces a useful exception.
     */
    @Test
    void getSkill_unknownId_throwsException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> SkillRegistry.getSkill("not_registered"));

        assertEquals("Skill not found: not_registered", exception.getMessage());
    }
}
