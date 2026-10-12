package seedu.keyboardwarrior.combat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import seedu.keyboardwarrior.typing.TypingChallenge.Difficulty;
import seedu.keyboardwarrior.typing.TypingEvaluator;
import seedu.keyboardwarrior.typing.TypingResult;
import seedu.keyboardwarrior.ui.CombatUi;

/**
 * Tests damage calculation and time-limit handling for combat skills.
 */
class SkillResolverTest {
    private static final Entity SOURCE = new Enemy("Source", 100);

    @Test
    void execute_withoutTypingResult_appliesBaseDamage() {
        RecordingCombatUi ui = new RecordingCombatUi();
        SkillResolver resolver = new SkillResolver(ui);
        Enemy target = new Enemy("Target", 80);
        Skill skill = new Skill("Enemy Strike", 15, Difficulty.EASY);

        resolver.execute(SOURCE, target, skill, null);

        assertEquals(65, target.getHealth());
        assertEquals("Target", ui.damagedEntityName);
        assertEquals(15, ui.reportedDamage);
        assertFalse(ui.typingWasTooSlow);
    }

    @Test
    void execute_eligibleTypingResult_appliesCalculatedDamage() {
        RecordingCombatUi ui = new RecordingCombatUi();
        SkillResolver resolver = new SkillResolver(ui);
        Enemy target = new Enemy("Target", 200);
        Skill skill = new Skill("Measured Strike", 100, Difficulty.EASY);
        TypingResult result = TypingEvaluator.evaluateTyping("abcde", "abcde", 60.0, 60.0);

        resolver.execute(SOURCE, target, skill, result);

        // Five correct characters in one minute give 1 WPM and a 1.01 multiplier.
        assertEquals(99, target.getHealth());
        assertEquals(101, ui.reportedDamage);
        assertFalse(ui.typingWasTooSlow);
    }

    @Test
    void execute_lateTypingResult_appliesTwentyPercentPenalty() {
        RecordingCombatUi ui = new RecordingCombatUi();
        SkillResolver resolver = new SkillResolver(ui);
        Enemy target = new Enemy("Target", 80);
        Skill skill = new Skill("Late Strike", 20, Difficulty.EASY);
        TypingResult result = TypingEvaluator.evaluateTyping("abcde", "abcde", 61.0, 60.0);

        resolver.execute(SOURCE, target, skill, result);

        assertEquals(64, target.getHealth());
        assertTrue(ui.typingWasTooSlow);
        assertEquals(16, ui.reportedDamage);
    }

    @Test
    void execute_lateFractionalDamage_roundsOnlyAfterPenalty() {
        RecordingCombatUi ui = new RecordingCombatUi();
        SkillResolver resolver = new SkillResolver(ui);
        Enemy target = new Enemy("Target", 80);
        Skill skill = new Skill("Fractional Strike", 12, Difficulty.EASY);
        TypingResult result = TypingEvaluator.evaluateTyping("abcde", "abcde", 10.0, 5.0);

        resolver.execute(SOURCE, target, skill, result);

        // 100% accuracy and 6 WPM produce 12.72 damage; after the penalty, 10.176 rounds down to 10.
        assertEquals(70, target.getHealth());
        assertEquals(10, ui.reportedDamage);
        assertTrue(ui.typingWasTooSlow);
    }

    @Test
    void execute_emptyLateAttempt_stillDealsZeroDamage() {
        RecordingCombatUi ui = new RecordingCombatUi();
        SkillResolver resolver = new SkillResolver(ui);
        Enemy target = new Enemy("Target", 80);
        Skill skill = new Skill("Empty Strike", 20, Difficulty.EASY);
        TypingResult result = TypingEvaluator.evaluateTyping("abcde", "", 61.0, 60.0);

        resolver.execute(SOURCE, target, skill, result);

        assertEquals(80, target.getHealth());
        assertEquals(0, ui.reportedDamage);
        assertTrue(ui.typingWasTooSlow);
    }

    @Test
    void execute_damageAboveHealth_clampsHealthAtZero() {
        RecordingCombatUi ui = new RecordingCombatUi();
        SkillResolver resolver = new SkillResolver(ui);
        Enemy target = new Enemy("Target", 10);
        Skill skill = new Skill("Heavy Strike", 25, Difficulty.EASY);

        resolver.execute(SOURCE, target, skill, null);

        assertEquals(0, target.getHealth());
        assertEquals(25, ui.reportedDamage);
    }

    @Test
    void constructor_nullUi_rejectsConstruction() {
        assertThrows(NullPointerException.class, () -> new SkillResolver(null));
    }

    @Test
    void execute_nullParticipantOrSkill_rejectsAttackWithoutDamage() {
        RecordingCombatUi ui = new RecordingCombatUi();
        SkillResolver resolver = new SkillResolver(ui);
        Enemy target = new Enemy("Target", 80);
        Skill skill = new Skill("Strike", 15, Difficulty.EASY);

        assertThrows(NullPointerException.class, () -> resolver.execute(null, target, skill, null));
        assertThrows(NullPointerException.class, () -> resolver.execute(SOURCE, null, skill, null));
        assertThrows(NullPointerException.class, () -> resolver.execute(SOURCE, target, null, null));

        assertEquals(80, target.getHealth());
        assertEquals(0, ui.reportedDamage);
        assertFalse(ui.typingWasTooSlow);
    }

    @Test
    void execute_zeroDamage_preservesHealth() {
        RecordingCombatUi ui = new RecordingCombatUi();
        SkillResolver resolver = new SkillResolver(ui);
        Enemy target = new Enemy("Target", 80);
        Skill skill = new Skill("Wait", 0, Difficulty.EASY);

        resolver.execute(SOURCE, target, skill, null);

        assertEquals(80, target.getHealth());
        assertEquals(0, ui.reportedDamage);
    }

    /** Records resolver feedback without printing to the console. */
    private static class RecordingCombatUi extends CombatUi {
        private String damagedEntityName;
        private int reportedDamage;
        private boolean typingWasTooSlow;

        @Override
        public void showTypingSlow() {
            typingWasTooSlow = true;
        }

        @Override
        public void showDamage(String name, int damage) {
            damagedEntityName = name;
            reportedDamage = damage;
        }
    }
}
