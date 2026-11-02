package seedu.keyboardwarrior.combat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

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

    /** Checks that reward candidates have an explicit order independent of hash map traversal. */
    @Test
    void getAllSkills_returnsSharedSkillsSortedByRegistryIdentifier() {
        assertEquals(List.of(
                SkillRegistry.getSkill("brute_force"),
                SkillRegistry.getSkill("buffer_overflow"),
                SkillRegistry.getSkill("encryption"),
                SkillRegistry.getSkill("overclock"),
                SkillRegistry.getSkill("ping"),
                SkillRegistry.getSkill("sigkill")), SkillRegistry.getAllSkills());
    }

    /** Checks that consuming a copied reward list does not remove registered skills. */
    @Test
    void getAllSkills_mutatingReturnedList_preservesRegistry() {
        List<Skill> skills = SkillRegistry.getAllSkills();
        Skill first = skills.removeFirst();

        assertEquals(6, SkillRegistry.getAllSkills().size());
        assertSame(first, SkillRegistry.getAllSkills().getFirst());
    }
}
