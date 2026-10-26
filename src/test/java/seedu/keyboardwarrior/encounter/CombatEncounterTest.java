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
    /**
     * Checks that an encounter rejects null participants, interfaces and prompt factories.
     */
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

    /**
     * Checks that invalid enemy skills can be corrected before combat begins.
     */
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

    /**
     * Checks that invalid turn order or skill choices leave a valid turn available.
     */
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

    /**
     * Checks that invalid submissions do not consume the pending player attack.
     */
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

    /**
     * Checks that a failed prompt generation can be retried without a pending attack.
     */
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

    /**
     * Checks that synchronous combat records victory when the player defeats the enemy.
     */
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

    /**
     * Checks that an empty player submission lets the enemy defeat the player.
     */
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

    /**
     * Checks that repeated partial attacks rotate enemy skills and eventually produce victory.
     */
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

    /**
     * Checks that a late attack still deals typing damage and allows a surviving enemy to respond.
     */
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

    /**
     * Checks that a late killing blow completes combat without enemy retaliation.
     */
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

    /**
     * Checks that an on-time killing blow completes combat without enemy retaliation.
     */
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

    /**
     * Simulates a failed generation followed by a successful retry.
     */
    private static class FailingOnceChallengeFactory extends TypingChallengeFactory {
        private boolean shouldFail = true;

        /**
         * Fails the first generation request and delegates subsequent requests to the real factory.
         *
         * @param difficulty requested challenge difficulty
         * @return generated challenge after the first failed request
         * @throws IllegalStateException on the first call
         */
        @Override
        public TypingChallenge generate(Difficulty difficulty) {
            if (shouldFail) {
                shouldFail = false;
                throw new IllegalStateException("Simulated challenge generation failure");
            }
            return super.generate(difficulty);
        }
    }

    /**
     * Supplies deterministic choices and records encounter UI events.
     */
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

        /**
         * Creates a combat fixture with a fixed skill and configurable submission behavior.
         *
         * @param selectedSkill skill returned for player selections
         */
        RecordingCombatUi(Skill selectedSkill) {
            this(selectedSkill, false);
        }

        /**
         * Creates a combat fixture with a fixed skill and configurable submission behavior.
         *
         * @param selectedSkill skill returned for player selections
         * @param submitEmptyText whether submissions are empty instead of matching the displayed prompt
         */
        RecordingCombatUi(Skill selectedSkill, boolean submitEmptyText) {
            this.selectedSkill = selectedSkill;
            this.submitEmptyText = submitEmptyText;
        }

        /**
         * Counts encounter-start notifications without printing them.
         *
         * @param enemy enemy announced by the encounter
         */
        @Override
        public void showCombatStart(Enemy enemy) {
            combatStartCount++;
        }

        /**
         * Counts player-turn notifications without printing them.
         *
         * @param player player taking the turn
         * @param enemy opposing enemy
         */
        @Override
        public void showPlayerTurn(Player player, Enemy enemy) {
            playerTurnCount++;
        }

        /**
         * Counts skill requests and returns the configured skill without reading input.
         *
         * @param activeSkills available skills; unused by this fixture
         * @return configured skill
         */
        @Override
        public Skill getSkillChoice(ArrayList<Skill> activeSkills) {
            skillChoiceCount++;
            return selectedSkill;
        }

        /**
         * Suppresses the preparation message during encounter tests.
         */
        @Override
        public void showTypingPrep() {
            // No console output is needed while testing the encounter lifecycle.
        }

        /**
         * Counts submissions and returns an empty answer or the exact supplied prompt.
         *
         * @param typingChallenge displayed challenge text
         * @return empty text or the exact prompt, according to the fixture configuration
         */
        @Override
        public String getTypingSubmission(String typingChallenge) {
            submissionCount++;
            return submitEmptyText ? "" : typingChallenge;
        }

        /**
         * Counts requests for slow-typing feedback without printing them.
         */
        @Override
        public void showTypingSlow() {
            penalizedAttackCount++;
        }

        /**
         * Counts enemy-turn notifications without printing them.
         *
         * @param enemy enemy taking the turn
         */
        @Override
        public void showEnemyTurn(Enemy enemy) {
            enemyTurnCount++;
        }

        /**
         * Suppresses damage output; assertions inspect entity health instead.
         *
         * @param name damaged participant's name; unused by this fixture
         * @param damage reported damage; unused by this fixture
         */
        @Override
        public void showDamage(String name, int damage) {
            // Damage is verified through entity health in these encounter-level tests.
        }

        /**
         * Records that victory was announced without printing it.
         *
         * @param enemy defeated enemy
         */
        @Override
        public void showVictory(Enemy enemy) {
            victoryShown = true;
        }

        /**
         * Records that defeat was announced without printing it.
         */
        @Override
        public void showDefeat() {
            defeatShown = true;
        }
    }
}
