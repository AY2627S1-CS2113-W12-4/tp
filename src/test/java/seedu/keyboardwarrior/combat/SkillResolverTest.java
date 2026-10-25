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
 * Tests typing-based player damage and fixed base damage for enemy skills.
 */
class SkillResolverTest {
    private static final Entity SOURCE = new Enemy("Source", 100);

    /**
     * Checks that an automated enemy attack uses its skill's base damage.
     */
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

    /**
     * Checks that perfect typing at one WPM deals one damage.
     */
    @Test
    void execute_eligibleTypingResult_appliesCalculatedDamage() {
        RecordingCombatUi ui = new RecordingCombatUi();
        SkillResolver resolver = new SkillResolver(ui);
        Enemy target = new Enemy("Target", 200);
        Skill skill = new Skill("Measured Strike", 100, Difficulty.EASY);
        TypingResult result = TypingEvaluator.evaluateTyping("abcde", "abcde", 60.0, 60.0);

        resolver.execute(SOURCE, target, skill, result);

        // Five correct characters in one minute give 1 WPM and therefore one damage.
        assertEquals(199, target.getHealth());
        assertEquals(1, ui.reportedDamage);
        assertFalse(ui.typingWasTooSlow);
    }

    /**
     * Checks that changing only the deadline does not change attack damage.
     */
    @Test
    void execute_sameTypingPerformance_hasNoTimeLimitPenalty() {
        RecordingCombatUi ui = new RecordingCombatUi();
        SkillResolver resolver = new SkillResolver(ui);
        Skill skill = SkillRegistry.getSkill("ping");
        String expected = "abcdefghijklmnopqrst";
        String typed = "abcdefghijklmnopqrsX";
        TypingResult onTime = TypingEvaluator.evaluateTyping(expected, typed, 4.8, 5.0);
        TypingResult late = TypingEvaluator.evaluateTyping(expected, typed, 4.8, 4.0);
        Enemy onTimeTarget = new Enemy("On Time", 100);
        Enemy lateTarget = new Enemy("Late", 100);

        resolver.execute(SOURCE, onTimeTarget, skill, onTime);
        resolver.execute(SOURCE, lateTarget, skill, late);

        assertFalse(onTime.isTimeLimitExceeded());
        assertTrue(late.isTimeLimitExceeded());
        assertEquals(60, onTimeTarget.getHealth());
        assertEquals(60, lateTarget.getHealth());
        assertEquals(40, ui.reportedDamage);
        assertFalse(ui.typingWasTooSlow);
    }

    /**
     * Checks that fifty WPM at ninety-five percent accuracy deals forty damage.
     */
    @Test
    void execute_targetPerformance_usesCharacterAccuracyAndRoundsDown() {
        RecordingCombatUi ui = new RecordingCombatUi();
        SkillResolver resolver = new SkillResolver(ui);
        Enemy target = new Enemy("Target", 100);
        Skill skill = SkillRegistry.getSkill("ping");
        TypingResult result = TypingEvaluator.evaluateTyping(
                "abcdefghijklmnopqrst", "abcdefghijklmnopqrsX", 4.8, 15.0);

        resolver.execute(SOURCE, target, skill, result);

        assertEquals(50.0, result.getWpm(), 0.000001);
        assertEquals(95.0, result.getAccuracyPercent(), 0.000001);
        // One wrong character gives 95% accuracy; 50 * 0.95^4 is 40.7253125, rounded down once.
        assertEquals(60, target.getHealth());
        assertEquals(40, ui.reportedDamage);
        assertFalse(ui.typingWasTooSlow);
    }

    /**
     * Checks that partial accuracy is raised to the fourth power before rounding damage down.
     */
    @Test
    void execute_partialAccuracy_raisesCorrectFractionToFourthPower() {
        RecordingCombatUi ui = new RecordingCombatUi();
        SkillResolver resolver = new SkillResolver(ui);
        Enemy target = new Enemy("Target", 200);
        Skill skill = new Skill("Measured Strike", 100, Difficulty.EASY);
        TypingResult result = TypingEvaluator.evaluateTyping("abcdefghij", "abcdeXXXXX", 2.4, 15.0);

        resolver.execute(SOURCE, target, skill, result);

        // 50 WPM at 50% accuracy gives floor(50 * 0.5^4), or three damage.
        assertEquals(50.0, result.getAccuracyPercent(), 0.000001);
        assertEquals(197, target.getHealth());
        assertEquals(3, ui.reportedDamage);
        assertFalse(ui.typingWasTooSlow);
    }

    /**
     * Checks that player damage depends on typing performance rather than skill base damage.
     */
    @Test
    void execute_playerDamage_isIndependentOfSelectedSkillBaseDamage() {
        RecordingCombatUi ui = new RecordingCombatUi();
        SkillResolver resolver = new SkillResolver(ui);
        TypingResult result = TypingEvaluator.evaluateTyping(
                "abcdefghijklmnopqrst", "abcdefghijklmnopqrsX", 4.8, 15.0);

        for (Skill skill : new Player("Player", 100).getActiveSkills()) {
            Enemy target = new Enemy("Target", 100);
            resolver.execute(SOURCE, target, skill, result);
            assertEquals(60, target.getHealth());
            assertEquals(40, ui.reportedDamage);
        }
        Enemy target = new Enemy("Target", 100);
        resolver.execute(SOURCE, target, new Skill("No Base Damage", 0, Difficulty.EASY), result);
        assertEquals(60, target.getHealth());
    }

    /**
     * Checks that completely incorrect text deals no damage regardless of typing speed.
     */
    @Test
    void execute_allCharactersIncorrect_dealsZeroDamageDespiteTypingSpeed() {
        RecordingCombatUi ui = new RecordingCombatUi();
        SkillResolver resolver = new SkillResolver(ui);
        Enemy target = new Enemy("Target", 80);
        Skill skill = new Skill("Missed Strike", 100, Difficulty.EASY);
        TypingResult result = TypingEvaluator.evaluateTyping("aaaaa", "bbbbb", 1.0, 15.0);

        resolver.execute(SOURCE, target, skill, result);

        assertEquals(80, target.getHealth());
        assertEquals(0, ui.reportedDamage);
        assertFalse(ui.typingWasTooSlow);
    }

    /**
     * Checks that an empty late submission deals no damage.
     */
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
        assertFalse(ui.typingWasTooSlow);
    }

    /**
     * Checks that extreme typing speed cannot overflow the target's health calculation.
     */
    @Test
    void execute_extremeTypingSpeed_capsDamageWithoutOverflowingHealth() {
        RecordingCombatUi ui = new RecordingCombatUi();
        SkillResolver resolver = new SkillResolver(ui);
        Enemy target = new Enemy("Target", 80);
        TypingResult result = TypingEvaluator.evaluateTyping("abcde", "abcde", 0.000000001, 15.0);

        resolver.execute(SOURCE, target, SkillRegistry.getSkill("ping"), result);

        assertEquals(0, target.getHealth());
        assertEquals(Integer.MAX_VALUE, ui.reportedDamage);
    }

    /**
     * Checks that damage greater than remaining health leaves the target at zero health.
     */
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

    /**
     * Checks that a resolver requires a combat interface.
     */
    @Test
    void constructor_nullUi_rejectsConstruction() {
        assertThrows(NullPointerException.class, () -> new SkillResolver(null));
    }

    /**
     * Checks that null attack dependencies are rejected without damaging the target.
     */
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

    /**
     * Checks that zero base damage leaves the target's health unchanged.
     */
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

    /**
     * Records resolver feedback without printing to the console.
     */
    private static class RecordingCombatUi extends CombatUi {
        private String damagedEntityName;
        private int reportedDamage;
        private boolean typingWasTooSlow;

        /**
         * Records that slow-typing feedback was requested without printing it.
         */
        @Override
        public void showTypingSlow() {
            typingWasTooSlow = true;
        }

        /**
         * Records the damaged participant and reported damage without printing feedback.
         *
         * @param name name of the damaged participant
         * @param damage reported damage amount
         */
        @Override
        public void showDamage(String name, int damage) {
            damagedEntityName = name;
            reportedDamage = damage;
        }
    }
}
