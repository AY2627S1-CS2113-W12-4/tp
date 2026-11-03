package seedu.keyboardwarrior.combat;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import seedu.keyboardwarrior.GameDifficulty;
import seedu.keyboardwarrior.typing.TypingChallenge.Difficulty;
import seedu.keyboardwarrior.typing.TypingEvaluator;
import seedu.keyboardwarrior.typing.TypingResult;
import seedu.keyboardwarrior.ui.CombatUi;

/** Tests performance-scaled time rewards independently of the existing damage calculations. */
class SkillTimeBonusTest {
    /** Perfect typing earns the base reward at fifty WPM and scales linearly with speed. */
    @Test
    void execute_perfectAccuracy_scalesWithSpeed() {
        assertReward("abcde", 2.4, GameDifficulty.HARD, 10);
        assertReward("abcde", 1.2, GameDifficulty.HARD, 20);
        assertReward("abcde", 0.6, GameDifficulty.HARD, 40);
    }

    /** Partial accuracy uses the same fourth-power curve as damage and rounds down. */
    @Test
    void execute_partialAccuracy_scalesWithoutRequiringPerfectTyping() {
        assertReward("abcdX", 1.2, GameDifficulty.HARD, 8);
        assertReward("abcXX", 1.2, GameDifficulty.HARD, 2);
    }

    /** The selected difficulty multiplies the reward before rounding. */
    @Test
    void execute_eachDifficulty_scalesTimeReward() {
        assertReward("abcde", 1.2, GameDifficulty.EASY, 80);
        assertReward("abcde", 1.2, GameDifficulty.NORMAL, 40);
        assertReward("abcde", 1.2, GameDifficulty.CODER, 16);
        assertReward("abcdX", 1.2, GameDifficulty.NORMAL, 16);
    }

    /** Empty or completely incorrect submissions must not earn time or announce an increase. */
    @Test
    void execute_noUsefulTyping_grantsNoBonus() {
        assertReward("", 1.2, GameDifficulty.HARD, 0);
        assertReward("XXXXX", 1.2, GameDifficulty.HARD, 0);
        assertReward("abcde", 60.0, GameDifficulty.HARD, 0);
    }

    /** Rounding the damage first would incorrectly turn this one-second reward into zero. */
    @Test
    void execute_fractionalPerformance_roundsOnlyFinalBonus() {
        assertReward("abcde", 60.0 / 2.6, GameDifficulty.HARD, 1);
    }

    /** Matching damage semantics, the deadline does not apply another performance penalty. */
    @Test
    void execute_lateAttempt_usesSamePerformanceFormula() {
        Player player = new Player("Player", 100);
        RecordingCombatUi ui = new RecordingCombatUi();
        TypingResult result = TypingEvaluator.evaluateTyping("abcde", "abcdX", 1.2, 1.0);

        new SkillResolver(ui).execute(player, new Enemy("Target", 100),
                SkillRegistry.getSkill("overclock"), result);

        assertEquals(8, player.getBonusTime());
        assertEquals(8, ui.reportedBonus);
    }

    /** Different skill base bonuses use the same curve, including signed adjustments. */
    @Test
    void execute_customBaseBonus_scalesConfiguredValue() {
        Player player = new Player("Player", 100);
        RecordingCombatUi ui = new RecordingCombatUi();
        SkillResolver resolver = new SkillResolver(ui);
        TypingResult result = TypingEvaluator.evaluateTyping("abcde", "abcdX", 1.2, 15.0);

        resolver.execute(player, new Enemy("Target", 100),
                new Skill("Small boost", 0, 10, Difficulty.EASY), result);
        assertEquals(4, player.getBonusTime());
        assertEquals(4, ui.reportedBonus);

        resolver.execute(player, new Enemy("Target", 100),
                new Skill("Penalty", 0, -20, Difficulty.EASY), result);
        assertEquals(-5, player.getBonusTime());
        assertEquals(-9, ui.reportedBonus);
    }

    /** Both a huge single reward and repeated additions saturate instead of wrapping. */
    @Test
    void execute_extremeReward_saturatesAndReportsActualChange() {
        Player player = new Player("Player", 100);
        player.setBonusTime(Integer.MAX_VALUE - 5);
        RecordingCombatUi ui = new RecordingCombatUi();
        SkillResolver resolver = new SkillResolver(ui);
        TypingResult result = TypingEvaluator.evaluateTyping("abcde", "abcde", 0.000000001, 15.0);
        Skill skill = SkillRegistry.getSkill("overclock");

        resolver.execute(player, new Enemy("Target", 100), skill, result);
        assertEquals(Integer.MAX_VALUE, player.getBonusTime());
        assertEquals(5, ui.reportedBonus);
        assertEquals(1, ui.bonusAnnouncements);

        resolver.execute(player, new Enemy("Target", 100), skill, result);
        assertEquals(Integer.MAX_VALUE, player.getBonusTime());
        assertEquals(1, ui.bonusAnnouncements);

        player.setBonusTime(0);
        resolver.execute(player, new Enemy("Target", 100), skill, result);
        assertEquals(Integer.MAX_VALUE, player.getBonusTime());
        assertEquals(Integer.MAX_VALUE, ui.reportedBonus);

        player.setBonusTime(Integer.MIN_VALUE + 5);
        resolver.execute(player, new Enemy("Target", 100),
                new Skill("Penalty", 0, -20, Difficulty.EASY), result);
        assertEquals(Integer.MIN_VALUE, player.getBonusTime());
        assertEquals(-5, ui.reportedBonus);
    }

    /** Automated attacks and skills without a time effect do not change the player's bonus. */
    @Test
    void execute_nonBonusPaths_preserveAdjustment() {
        Player player = new Player("Player", 100);
        player.setBonusTime(7);
        RecordingCombatUi ui = new RecordingCombatUi();
        SkillResolver resolver = new SkillResolver(ui);
        Skill overclock = SkillRegistry.getSkill("overclock");
        TypingResult result = TypingEvaluator.evaluateTyping("abcde", "abcde", 1.2, 15.0);

        resolver.execute(player, new Enemy("Target", 100), overclock, null);
        resolver.execute(player, new Enemy("Target", 100), SkillRegistry.getSkill("ping"), result);
        resolver.execute(new Enemy("Source", 100), player, overclock, result);

        assertEquals(7, player.getBonusTime());
        assertEquals(0, ui.bonusAnnouncements);
        assertEquals(100, player.getHealth(), "Overclock must not deal damage for a nonplayer source either.");
    }

    /**
     * Resolves Overclock and checks both the accumulated reward and the displayed amount.
     *
     * @param typedText submitted text compared against abcde
     * @param elapsedSeconds time spent typing
     * @param difficulty selected run difficulty
     * @param expectedBonus expected whole-second reward
     */
    private void assertReward(String typedText, double elapsedSeconds, GameDifficulty difficulty, int expectedBonus) {
        Player player = new Player("Player", 100);
        Enemy target = new Enemy("Target", 100);
        RecordingCombatUi ui = new RecordingCombatUi();
        TypingResult result = TypingEvaluator.evaluateTyping("abcde", typedText, elapsedSeconds, 120.0);

        new SkillResolver(ui, difficulty).execute(player, target,
                SkillRegistry.getSkill("overclock"), result);

        assertEquals(expectedBonus, player.getBonusTime());
        assertEquals(100, target.getHealth(), "Overclock must never convert typing performance into damage.");
        assertEquals(0, ui.reportedDamage);
        assertEquals(expectedBonus, ui.reportedBonus);
        assertEquals(expectedBonus == 0 ? 0 : 1, ui.bonusAnnouncements);
    }

    /** Captures time feedback and suppresses terminal damage output. */
    private static class RecordingCombatUi extends CombatUi {
        private int reportedBonus;
        private int bonusAnnouncements;
        private int reportedDamage;

        @Override
        public void showTimeBonus(int timeBonus) {
            reportedBonus = timeBonus;
            bonusAnnouncements++;
        }

        @Override
        public void showDamage(String name, int damage) {
            reportedDamage = damage;
        }
    }
}
