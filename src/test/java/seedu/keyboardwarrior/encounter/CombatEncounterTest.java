package seedu.keyboardwarrior.encounter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;

import org.junit.jupiter.api.Test;

import seedu.keyboardwarrior.combat.Enemy;
import seedu.keyboardwarrior.combat.EnemyFactory;
import seedu.keyboardwarrior.combat.Player;
import seedu.keyboardwarrior.combat.Skill;
import seedu.keyboardwarrior.combat.SkillRegistry;
import seedu.keyboardwarrior.typing.TypingChallenge;
import seedu.keyboardwarrior.typing.TypingChallenge.Difficulty;
import seedu.keyboardwarrior.typing.TypingChallengeFactory;
import seedu.keyboardwarrior.typing.TypingEvaluator;
import seedu.keyboardwarrior.typing.TypingResult;
import seedu.keyboardwarrior.ui.CombatUi;

/**
 * Tests complete victory and defeat paths through a combat encounter.
 */
class CombatEncounterTest {
    @Test
    void snapshot_onlyAllowsStableActiveTurnsAndBeginValidatesSavedRotation() {
        Player player = new Player("Player", 100);
        Enemy enemy = EnemyFactory.createScenarioEnemy("rogue_ai", 1);
        Skill skill = SkillRegistry.getSkill("ping");
        CombatEncounter encounter = new CombatEncounter(player, enemy, new RecordingCombatUi(skill));
        assertThrows(IllegalStateException.class, encounter::snapshot);
        assertThrows(IllegalArgumentException.class, () -> encounter.begin(-1));
        assertThrows(IllegalArgumentException.class, () -> encounter.begin(3));
        encounter.begin(1);
        assertEquals(new CombatEncounter.CombatSnapshot("rogue_ai", 30, 1), encounter.snapshot());
        encounter.preparePlayerTurn(skill);
        assertThrows(IllegalStateException.class, encounter::snapshot);
        encounter.submitTypingResult(TypingEvaluator.evaluateTyping("abcde", "abcde", 0.1, 15));
        assertTrue(encounter.getIsCompleted());
        assertThrows(IllegalStateException.class, encounter::snapshot);
    }

    @Test
    void preparePlayerTurn_encryption_usesSymbolPassageAndSixtySeconds() {
        Player player = new Player("Player", 100);
        Enemy enemy = new Enemy("Training Bot", 100);
        enemy.addSkill(SkillRegistry.getSkill("ping"));
        Skill encryption = SkillRegistry.getSkill("encryption");
        CombatEncounter encounter = new CombatEncounter(player, enemy, new RecordingCombatUi(encryption));
        encounter.begin();

        TypingChallenge challenge = encounter.preparePlayerTurn(encryption);

        assertEquals(60, challenge.getTime());
        assertFalse(challenge.getText().chars().anyMatch(Character::isLetterOrDigit));
    }

    @Test
    void constructor_nullDependencies_rejectsInvalidEncounter() {
        Player player = new Player("Player", 100);
        Enemy enemy = new Enemy("Training Bot", 100);
        RecordingCombatUi ui = new RecordingCombatUi(SkillRegistry.getSkill("ping"));

        assertThrows(NullPointerException.class, () -> new CombatEncounter(null, enemy, ui));
        assertThrows(NullPointerException.class, () -> new CombatEncounter(player, null, ui));
        assertThrows(NullPointerException.class, () -> new CombatEncounter(player, enemy, null));
        assertThrows(NullPointerException.class, () -> new CombatEncounter(player, enemy, ui, null));
    }

    @Test
    void begin_invalidEnemySkills_rejectsBeforeStartingAndAllowsCorrection() {
        Player player = new Player("Player", 100);
        Enemy enemy = new Enemy("Training Bot", 100);
        RecordingCombatUi ui = new RecordingCombatUi(SkillRegistry.getSkill("ping"));
        CombatEncounter encounter = new CombatEncounter(player, enemy, ui);

        assertThrows(IllegalStateException.class, encounter::begin);
        enemy.getActiveSkills().add(null);
        assertThrows(IllegalStateException.class, encounter::begin);
        assertEquals(0, ui.combatStartCount);
        enemy.getActiveSkills().clear();
        enemy.addSkill(SkillRegistry.getSkill("ping"));
        encounter.begin();

        assertEquals(1, ui.combatStartCount);
        assertThrows(IllegalStateException.class, encounter::begin);
    }

    @Test
    void preparePlayerTurn_invalidOrderOrChoice_keepsValidTurnAvailable() {
        Player player = new Player("Player", 100);
        Skill skill = SkillRegistry.getSkill("ping");
        Enemy enemy = new Enemy("Training Bot", 100);
        enemy.addSkill(skill);
        RecordingCombatUi ui = new RecordingCombatUi(skill);
        CombatEncounter encounter = new CombatEncounter(player, enemy, ui);

        assertThrows(IllegalStateException.class, () -> encounter.preparePlayerTurn(skill));
        encounter.begin();
        assertThrows(NullPointerException.class, () -> encounter.preparePlayerTurn(null));
        Skill unavailable = new Skill("Unavailable", 10, Difficulty.EASY);
        assertThrows(IllegalArgumentException.class, () -> encounter.preparePlayerTurn(unavailable));
        assertTrue(encounter.preparePlayerTurn(skill).getTime() >= 15);
        assertThrows(IllegalStateException.class, () -> encounter.preparePlayerTurn(skill));
    }

    @Test
    void submitTypingResult_invalidOrderOrNull_doesNotConsumePendingAttack() {
        Player player = new Player("Player", 100);
        Skill skill = SkillRegistry.getSkill("ping");
        Enemy enemy = new Enemy("Training Bot", 1);
        enemy.addSkill(skill);
        RecordingCombatUi ui = new RecordingCombatUi(skill);
        CombatEncounter encounter = new CombatEncounter(player, enemy, ui);
        TypingResult result = TypingEvaluator.evaluateTyping("alpha", "alpha", 12.0, 15.0);

        encounter.begin();
        assertThrows(IllegalStateException.class, () -> encounter.submitTypingResult(result));
        encounter.preparePlayerTurn(skill);
        assertThrows(NullPointerException.class, () -> encounter.submitTypingResult(null));
        assertEquals(1, enemy.getHealth());
        assertEquals(100, player.getHealth());
        encounter.submitTypingResult(result);

        assertTrue(encounter.getIsCompleted());
        assertThrows(IllegalStateException.class, () -> encounter.submitTypingResult(result));
        assertThrows(IllegalStateException.class, () -> encounter.preparePlayerTurn(skill));
    }

    @Test
    void preparePlayerTurn_factoryFailure_doesNotLeaveAttackPending() {
        Player player = new Player("Player", 100);
        Skill skill = SkillRegistry.getSkill("ping");
        Enemy enemy = new Enemy("Training Bot", 100);
        enemy.addSkill(skill);
        RecordingCombatUi ui = new RecordingCombatUi(skill);
        CombatEncounter encounter = new CombatEncounter(player, enemy, ui, new FailingOnceChallengeFactory());
        encounter.begin();

        assertThrows(IllegalStateException.class, () -> encounter.preparePlayerTurn(skill));
        assertTrue(encounter.preparePlayerTurn(skill).getTime() >= 15);
    }

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
        RecordingCombatUi ui = new RecordingCombatUi(zeroDamageSkill, true);
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

    @Test
    void submitTypingResult_repeatedPartialAttempts_rotatesEnemySkillsUntilVictory() {
        Player player = new Player("Player", 100);
        Skill skill = SkillRegistry.getSkill("ping");
        Enemy enemy = new Enemy("Training Bot", 3);
        enemy.addSkill(SkillRegistry.getSkill("ping"));
        enemy.addSkill(SkillRegistry.getSkill("brute_force"));
        RecordingCombatUi ui = new RecordingCombatUi(skill);
        CombatEncounter encounter = new CombatEncounter(player, enemy, ui);
        TypingResult partialResult = TypingEvaluator.evaluateTyping("alpha beta", "alpha nope", 12.0, 15.0);

        encounter.begin();
        TypingChallenge challenge = encounter.preparePlayerTurn(skill);
        assertTrue(challenge.getTime() >= 15);
        encounter.submitTypingResult(partialResult);
        // 10 WPM at 60% character accuracy deals one damage after the fourth-power weighting.
        assertEquals(2, enemy.getHealth());
        assertEquals(90, player.getHealth());
        assertFalse(encounter.getIsCompleted());

        encounter.preparePlayerTurn(skill);
        encounter.submitTypingResult(partialResult);
        assertEquals(1, enemy.getHealth());
        assertEquals(70, player.getHealth());
        assertFalse(encounter.getIsCompleted());

        encounter.preparePlayerTurn(skill);
        encounter.submitTypingResult(partialResult);
        assertEquals(0, enemy.getHealth());
        assertEquals(70, player.getHealth());
        assertTrue(encounter.getIsCompleted());
        assertTrue(encounter.getEncounterOutcome().getIsVictory());
        assertEquals(3, ui.playerTurnCount);
        assertEquals(2, ui.enemyTurnCount);
        assertEquals(0, ui.skillChoiceCount);
        assertEquals(0, ui.submissionCount);
    }

    @Test
    void submitTypingResult_lateAttempt_usesTypingDamageAndAllowsEnemyResponse() {
        Player player = new Player("Player", 100);
        Skill skill = SkillRegistry.getSkill("ping");
        Enemy enemy = new Enemy("Training Bot", 100);
        enemy.addSkill(SkillRegistry.getSkill("ping"));
        RecordingCombatUi ui = new RecordingCombatUi(skill);
        CombatEncounter encounter = new CombatEncounter(player, enemy, ui);

        encounter.begin();
        encounter.preparePlayerTurn(skill);
        encounter.submitTypingResult(TypingEvaluator.evaluateTyping("alpha beta", "alpha beta", 15.01, 15.0));

        assertEquals(93, enemy.getHealth());
        assertEquals(90, player.getHealth());
        assertFalse(encounter.getIsCompleted());
        assertEquals(0, ui.penalizedAttackCount);
        assertEquals(1, ui.enemyTurnCount);
        assertEquals(2, ui.playerTurnCount);
    }

    @Test
    void submitTypingResult_lateKillingBlow_recordsVictoryWithoutRetaliation() {
        Player player = new Player("Player", 100);
        Skill skill = SkillRegistry.getSkill("ping");
        Enemy enemy = new Enemy("Training Bot", 1);
        enemy.addSkill(SkillRegistry.getSkill("ping"));
        RecordingCombatUi ui = new RecordingCombatUi(skill);
        CombatEncounter encounter = new CombatEncounter(player, enemy, ui);

        encounter.begin();
        encounter.preparePlayerTurn(skill);
        encounter.submitTypingResult(TypingEvaluator.evaluateTyping("alpha beta", "alpha beta", 15.01, 15.0));

        assertTrue(encounter.getIsCompleted());
        assertTrue(encounter.getEncounterOutcome().getIsVictory());
        assertEquals(0, enemy.getHealth());
        assertTrue(enemy.getIsDead());
        assertEquals(100, player.getHealth());
        assertEquals(0, ui.penalizedAttackCount);
        assertEquals(0, ui.enemyTurnCount);
        assertEquals(1, ui.playerTurnCount);
        assertTrue(ui.victoryShown);
    }

    @Test
    void submitTypingResult_killingBlow_recordsVictoryWithoutRetaliation() {
        Player player = new Player("Player", 100);
        Skill skill = SkillRegistry.getSkill("ping");
        Enemy enemy = new Enemy("Training Bot", 1);
        enemy.addSkill(SkillRegistry.getSkill("ping"));
        RecordingCombatUi ui = new RecordingCombatUi(skill);
        CombatEncounter encounter = new CombatEncounter(player, enemy, ui);

        encounter.begin();
        encounter.preparePlayerTurn(skill);
        encounter.submitTypingResult(TypingEvaluator.evaluateTyping("alpha beta", "alpha beta", 12.0, 15.0));

        assertTrue(encounter.getIsCompleted());
        assertTrue(encounter.getEncounterOutcome().getIsVictory());
        assertEquals(0, enemy.getHealth());
        assertTrue(enemy.getIsDead());
        assertEquals(100, player.getHealth());
        assertEquals(0, ui.enemyTurnCount);
        assertEquals(1, ui.playerTurnCount);
        assertTrue(ui.victoryShown);
    }

    /** Simulates a failed generation followed by a successful retry. */
    private static class FailingOnceChallengeFactory extends TypingChallengeFactory {
        private boolean shouldFail = true;

        @Override
        public TypingChallenge generate(Difficulty difficulty) {
            if (shouldFail) {
                shouldFail = false;
                throw new IllegalStateException("Simulated challenge generation failure");
            }
            return super.generate(difficulty);
        }
    }

    /** Supplies deterministic choices and records encounter UI events. */
    private static class RecordingCombatUi extends CombatUi {
        private final Skill selectedSkill;
        private final boolean submitEmptyText;
        private int combatStartCount;
        private int playerTurnCount;
        private int enemyTurnCount;
        private int skillChoiceCount;
        private int submissionCount;
        private int penalizedAttackCount;
        private boolean victoryShown;
        private boolean defeatShown;

        RecordingCombatUi(Skill selectedSkill) {
            this(selectedSkill, false);
        }

        RecordingCombatUi(Skill selectedSkill, boolean submitEmptyText) {
            this.selectedSkill = selectedSkill;
            this.submitEmptyText = submitEmptyText;
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
            skillChoiceCount++;
            return selectedSkill;
        }

        @Override
        public void showTypingPrep() {
            // No console output is needed while testing the encounter lifecycle.
        }

        @Override
        public String getTypingSubmission(String typingChallenge) {
            submissionCount++;
            return submitEmptyText ? "" : typingChallenge;
        }

        @Override
        public void showTypingSlow() {
            penalizedAttackCount++;
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
