package seedu.keyboardwarrior.encounter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Random;

import org.junit.jupiter.api.Test;

import seedu.keyboardwarrior.combat.Enemy;
import seedu.keyboardwarrior.combat.EnemyFactory;
import seedu.keyboardwarrior.combat.Player;
import seedu.keyboardwarrior.combat.Skill;
import seedu.keyboardwarrior.combat.SkillRegistry;
import seedu.keyboardwarrior.typing.PromptBank;
import seedu.keyboardwarrior.typing.TypingChallenge;
import seedu.keyboardwarrior.typing.TypingChallenge.Difficulty;
import seedu.keyboardwarrior.typing.TypingChallengeFactory;
import seedu.keyboardwarrior.typing.TypingEvaluator;
import seedu.keyboardwarrior.typing.TypingResult;
import seedu.keyboardwarrior.ui.CombatUi;

/** Tests support-skill turns and the lifetime of combat time adjustments. */
class CombatTimeBonusTest {
    /** Overclock preserves enemy HP, permits retaliation and extends only later turns of the current fight. */
    @Test
    void overclock_duringCombat_grantsTimeWithoutDamageAndResetsOnVictory() {
        Player player = new Player("Player", 100);
        player.addSkill(SkillRegistry.getSkill("overclock"));
        player.setBonusTime(5);
        Enemy enemy = EnemyFactory.createScenarioEnemy("rogue_ai", 1);
        RecordingCombatUi ui = new RecordingCombatUi(player);
        TypingChallengeFactory factory = new TypingChallengeFactory(new FixedPromptBank());
        CombatEncounter encounter = new CombatEncounter(player, enemy, ui, factory);
        encounter.begin();

        assertEquals(5, player.getBonusTime(), "Starting a fight must preserve incoming event adjustments.");
        TypingChallenge overclock = encounter.preparePlayerTurn(SkillRegistry.getSkill("overclock"));
        assertEquals(20, overclock.getTime());
        encounter.submitTypingResult(perfectResult());

        assertEquals(30, enemy.getHealth());
        assertEquals(90, player.getHealth(), "The enemy must still retaliate against a support skill.");
        assertEquals(1, ui.enemyTurns);
        assertEquals(25, player.getBonusTime());
        assertFalse(encounter.getIsCompleted());
        encounter.snapshot();
        assertEquals(25, player.getBonusTime(), "Saving an active fight must not clear its adjustment.");
        assertEquals(40, encounter.preparePlayerTurn(SkillRegistry.getSkill("ping")).getTime());
        encounter.submitTypingResult(perfectResult());

        assertTrue(encounter.getEncounterOutcome().getIsVictory());
        assertEquals(0, player.getBonusTime());
        assertEquals(0, ui.bonusAtCompletion);
        assertEquals(1, ui.enemyTurns, "The killing blow must not grant another enemy turn.");
        CombatEncounter next = new CombatEncounter(player, EnemyFactory.createScenarioEnemy("rogue_ai", 2),
                ui, factory);
        next.begin();
        assertEquals(15, next.preparePlayerTurn(SkillRegistry.getSkill("ping")).getTime());
    }

    /** An Overclock reward earned on a lethal enemy turn must be cleared by defeat. */
    @Test
    void overclock_lethalRetaliation_clearsNewlyEarnedTime() {
        Player player = new Player("Player", 10);
        Skill overclock = SkillRegistry.getSkill("overclock");
        player.addSkill(overclock);
        player.setBonusTime(5);
        Enemy enemy = EnemyFactory.createScenarioEnemy("rogue_ai", 1);
        RecordingCombatUi ui = new RecordingCombatUi(player);
        CombatEncounter encounter = new CombatEncounter(player, enemy, ui,
                new TypingChallengeFactory(new FixedPromptBank()));
        encounter.begin();
        encounter.preparePlayerTurn(overclock);

        encounter.submitTypingResult(perfectResult());

        assertEquals(30, enemy.getHealth());
        assertEquals(0, player.getHealth());
        assertTrue(player.getIsDead());
        assertTrue(encounter.getIsCompleted());
        assertFalse(encounter.getEncounterOutcome().getIsVictory());
        assertEquals(0, player.getBonusTime());
        assertEquals(0, ui.bonusAtCompletion);
    }

    /** Victory and defeat must clear both positive and negative adjustments, including integer extremes. */
    @Test
    void completion_signedAdjustments_resetsOnBothOutcomes() {
        for (int bonus : new int[] {20, -5, Integer.MAX_VALUE, Integer.MIN_VALUE}) {
            for (boolean victory : new boolean[] {true, false}) {
                Player player = new Player("Player", victory ? 100 : 10);
                player.setBonusTime(bonus);
                Enemy enemy = EnemyFactory.createScenarioEnemy("rogue_ai", 1);
                RecordingCombatUi ui = new RecordingCombatUi(player);
                CombatEncounter encounter = new CombatEncounter(player, enemy, ui,
                        new TypingChallengeFactory(new FixedPromptBank()));
                encounter.begin();
                encounter.preparePlayerTurn(SkillRegistry.getSkill("ping"));
                TypingResult result = victory ? perfectResult()
                        : TypingEvaluator.evaluateTyping("abcde", "", 1.0, 15.0);

                encounter.submitTypingResult(result);

                assertEquals(victory, encounter.getEncounterOutcome().getIsVictory());
                assertEquals(0, player.getBonusTime());
                assertEquals(0, ui.bonusAtCompletion);
            }
        }
    }

    /** Already-defeated participants use the same completion cleanup even without a typing turn. */
    @Test
    void begin_finishedParticipants_resetsTimeAdjustment() {
        for (boolean victory : new boolean[] {true, false}) {
            Player player = new Player("Player", victory ? 100 : 0);
            player.setBonusTime(-5);
            Enemy enemy = EnemyFactory.createScenarioEnemy("rogue_ai", 1);
            if (victory) {
                enemy.setHealth(0);
            }
            CombatEncounter encounter = new CombatEncounter(player, enemy, new RecordingCombatUi(player));

            encounter.begin();

            assertTrue(encounter.getIsCompleted());
            assertEquals(victory, encounter.getEncounterOutcome().getIsVictory());
            assertEquals(0, player.getBonusTime());
        }
    }

    /** Returns fifty WPM at full accuracy, yielding fifty attack damage or twenty bonus seconds on Hard. */
    private TypingResult perfectResult() {
        return TypingEvaluator.evaluateTyping("abcde", "abcde", 1.2, 15.0);
    }

    /** Keeps countdown expectations independent of randomly selected passage lengths. */
    private static class FixedPromptBank extends PromptBank {
        /** Uses a deterministic source even though passage selection is overridden. */
        FixedPromptBank() {
            super(new Random(42));
        }

        @Override
        public String getRandomPrompt(Difficulty difficulty) {
            return "abcde";
        }
    }

    /** Suppresses terminal output and records time adjustments at the completion callbacks. */
    private static class RecordingCombatUi extends CombatUi {
        private final Player player;
        private int enemyTurns;
        private int bonusAtCompletion = -1;

        /** Retains the player whose adjustment should be cleared before completion feedback. */
        RecordingCombatUi(Player player) {
            this.player = player;
        }

        @Override
        public void showCombatStart(Enemy enemy) {
        }

        @Override
        public void showPlayerTurn(Player player, Enemy enemy) {
        }

        @Override
        public void showTypingPrep() {
        }

        @Override
        public void showTimeBonus(int timeBonus) {
        }

        @Override
        public void showDamage(String name, int damage) {
        }

        @Override
        public void showEnemyTurn(Enemy enemy) {
            enemyTurns++;
        }

        @Override
        public void showVictory(Enemy enemy) {
            bonusAtCompletion = player.getBonusTime();
        }

        @Override
        public void showDefeat() {
            bonusAtCompletion = player.getBonusTime();
        }
    }
}
