package seedu.keyboardwarrior.encounter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;

import org.junit.jupiter.api.Test;

import seedu.keyboardwarrior.combat.Enemy;
import seedu.keyboardwarrior.combat.Player;
import seedu.keyboardwarrior.combat.Skill;
import seedu.keyboardwarrior.combat.SkillRegistry;
import seedu.keyboardwarrior.typing.TypingChallenge.Difficulty;
import seedu.keyboardwarrior.ui.CombatUi;

/**
 * Tests complete victory and defeat paths through a combat encounter.
 */
class CombatEncounterTest {
    @Test
    void start_playerDefeatsEnemy_recordsVictory() {
        Player player = new Player("Player", 100);
        Enemy enemy = new Enemy("Training Bot", 1);
        enemy.addSkill(SkillRegistry.getSkill("ping"));
        RecordingCombatUi ui = new RecordingCombatUi(player.getActiveSkills().get(0));
        CombatEncounter encounter = new CombatEncounter(player, enemy, ui);

        assertEquals("Combat Encounter: Training Bot", encounter.getDisplayText());
        encounter.start();

        assertTrue(encounter.getIsCompleted());
        assertTrue(encounter.getEncounterOutcome().getIsVictory());
        assertEquals(0, enemy.getHealth());
        assertTrue(enemy.getIsDead());
        assertFalse(player.getIsDead());
        assertEquals(1, ui.combatStartCount);
        assertEquals(1, ui.playerTurnCount);
        assertEquals(0, ui.enemyTurnCount);
        assertTrue(ui.victoryShown);
        assertFalse(ui.defeatShown);
    }

    @Test
    void start_enemyDefeatsPlayer_recordsDefeat() {
        Player player = new Player("Player", 10);
        Skill zeroDamageSkill = new Skill("Wait", 0, Difficulty.EASY);
        player.getActiveSkills().clear();
        player.addSkill(zeroDamageSkill);
        Enemy enemy = new Enemy("Training Bot", 100);
        enemy.addSkill(SkillRegistry.getSkill("ping"));
        RecordingCombatUi ui = new RecordingCombatUi(zeroDamageSkill);
        CombatEncounter encounter = new CombatEncounter(player, enemy, ui);

        encounter.start();

        assertTrue(encounter.getIsCompleted());
        assertFalse(encounter.getEncounterOutcome().getIsVictory());
        assertEquals(0, player.getHealth());
        assertTrue(player.getIsDead());
        assertEquals(100, enemy.getHealth());
        assertFalse(enemy.getIsDead());
        assertEquals(1, ui.playerTurnCount);
        assertEquals(1, ui.enemyTurnCount);
        assertFalse(ui.victoryShown);
        assertTrue(ui.defeatShown);
    }

    /** Supplies deterministic choices and records encounter UI events. */
    private static class RecordingCombatUi extends CombatUi {
        private final Skill selectedSkill;
        private int combatStartCount;
        private int playerTurnCount;
        private int enemyTurnCount;
        private boolean victoryShown;
        private boolean defeatShown;

        RecordingCombatUi(Skill selectedSkill) {
            this.selectedSkill = selectedSkill;
        }

        @Override
        public void showCombatStart(Enemy enemy) {
            combatStartCount++;
        }

        @Override
        public void showPlayerTurn(Player player, Enemy enemy) {
            playerTurnCount++;
        }

        @Override
        public Skill getSkillChoice(ArrayList<Skill> activeSkills) {
            return selectedSkill;
        }

        @Override
        public void showTypingPrep() {
            // No console output is needed while testing the encounter lifecycle.
        }

        @Override
        public String getTypingSubmission(String typingChallenge) {
            return typingChallenge;
        }

        @Override
        public void showEnemyTurn(Enemy enemy) {
            enemyTurnCount++;
        }

        @Override
        public void showDamage(String name, int damage) {
            // Damage is verified through entity health in these encounter-level tests.
        }

        @Override
        public void showVictory(Enemy enemy) {
            victoryShown = true;
        }

        @Override
        public void showDefeat() {
            defeatShown = true;
        }
    }
}
