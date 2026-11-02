package seedu.keyboardwarrior.encounter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Random;

import org.junit.jupiter.api.Test;

import seedu.keyboardwarrior.GameDifficulty;
import seedu.keyboardwarrior.combat.Player;
import seedu.keyboardwarrior.combat.Skill;
import seedu.keyboardwarrior.combat.SkillRegistry;
import seedu.keyboardwarrior.typing.PromptBank;
import seedu.keyboardwarrior.typing.TypingChallenge;
import seedu.keyboardwarrior.typing.TypingChallengeFactory;
import seedu.keyboardwarrior.ui.EventUi;

/** Tests merchant branches, retryable preparation and exactly-once terminal effects. */
class DialogueEncounterTest {
    /** A full trade grants one shared-registry skill, reports victory and draws from the supplied RNG once. */
    @Test
    void trade_correctAnswers_grantsOneSkillAndRejectsInvalidLifecycleCalls() {
        Fixture fixture = new Fixture(100, false);
        Random expectedRandom = new Random(42);
        DialogueEncounter encounter = fixture.encounter;
        assertNull(encounter.updateEvent(fixture.factory));
        assertThrows(IllegalStateException.class, () -> encounter.submitAnswer("answer", fixture.factory));
        assertThrows(IllegalStateException.class, encounter::getEncounterOutcome);

        TypingChallenge opening = encounter.prepareEvent(fixture.factory);
        assertEquals("I will not be extorted", opening.getText());
        assertEquals(10, opening.getTime());
        assertThrows(IllegalStateException.class, () -> encounter.prepareEvent(fixture.factory));
        assertThrows(NullPointerException.class, () -> encounter.submitAnswer(null, fixture.factory));
        TypingChallenge trade = encounter.submitAnswer(" I WILL NOT BE EXTORTED ", fixture.factory);
        assertEquals("Show me your rare wares", trade.getText());
        assertEquals(10, trade.getTime());
        assertFalse(encounter.getIsCompleted());
        assertThrows(NullPointerException.class, () -> encounter.submitAnswer(null, fixture.factory));
        assertEquals(5, fixture.player.getActiveSkills().size());

        assertNull(encounter.submitAnswer(" SHOW ME YOUR RARE WARES ", fixture.factory));

        assertTrue(encounter.getIsCompleted());
        assertTrue(encounter.getEncounterOutcome().getIsVictory());
        assertSame(SkillRegistry.getSkill("overclock"), fixture.ui.reward);
        assertEquals(6, fixture.player.getActiveSkills().size());
        assertEquals(1, fixture.ui.skillRewards);
        assertEquals(100, fixture.player.getHealth());
        assertThrows(IllegalStateException.class, () -> encounter.submitAnswer(trade.getText(), fixture.factory));
        assertThrows(IllegalStateException.class, () -> encounter.prepareEvent(fixture.factory));
        assertNull(encounter.updateEvent(fixture.factory));
        assertNull(fixture.rewards.popRandomReward());
        expectedRandom.nextInt(1);
        assertEquals(expectedRandom.nextLong(), fixture.random.nextLong());
    }

    /** Empty reward pools heal once, saturate safely and report the actual amount gained. */
    @Test
    void trade_emptyPool_healsAndReportsActualIncrease() {
        for (int health : new int[] {100, Integer.MAX_VALUE - 5, Integer.MAX_VALUE}) {
            Fixture fixture = new Fixture(health, true);
            fixture.openTrade();
            fixture.encounter.submitAnswer("Show me your rare wares", fixture.factory);

            int expectedHealth = (int) Math.min(Integer.MAX_VALUE, (long) health + 20);
            assertEquals(expectedHealth, fixture.player.getHealth());
            assertEquals(expectedHealth - health, fixture.ui.healing);
            assertEquals(expectedHealth, fixture.ui.reportedHealth);
            assertEquals(1, fixture.ui.healthRewards);
            assertTrue(fixture.encounter.getEncounterOutcome().getIsVictory());
            assertThrows(IllegalStateException.class,
                    () -> fixture.encounter.submitAnswer("Show me your rare wares", fixture.factory));
            assertEquals(expectedHealth, fixture.player.getHealth());
            assertEquals(new Random(42).nextLong(), fixture.random.nextLong());
        }
    }

    /** An unsuccessful trade preserves survival and applies its signed penalty once, even at the lower limit. */
    @Test
    void trade_wrongAnswer_appliesOneSaturatingPenalty() {
        for (int bonus : new int[] {7, Integer.MIN_VALUE + 1}) {
            Fixture fixture = new Fixture(100, false);
            fixture.player.setBonusTime(bonus);
            fixture.openTrade();
            fixture.encounter.submitAnswer("wrong", fixture.factory);

            assertEquals((int) Math.max(Integer.MIN_VALUE, (long) bonus - 5), fixture.player.getBonusTime());
            assertEquals(1, fixture.ui.penalties);
            assertEquals(100, fixture.player.getHealth());
            assertEquals(5, fixture.player.getActiveSkills().size());
            assertTrue(fixture.encounter.getEncounterOutcome().getIsVictory());
            assertThrows(IllegalStateException.class, () -> fixture.encounter.submitAnswer("wrong", fixture.factory));
            assertEquals(new Random(42).nextLong(), fixture.random.nextLong());
        }
    }

    /** The escape branch accepts case-insensitive input and leaves the player unchanged on success. */
    @Test
    void escape_correctAnswer_completesWithoutEffects() {
        Fixture fixture = new Fixture(10, false);
        fixture.encounter.prepareEvent(fixture.factory);
        TypingChallenge escape = fixture.encounter.submitAnswer("wrong", fixture.factory);
        assertEquals("flee", escape.getText());
        assertEquals(5, escape.getTime());

        fixture.encounter.submitAnswer(" FLEE ", fixture.factory);

        assertEquals(10, fixture.player.getHealth());
        assertFalse(fixture.player.getIsDead());
        assertEquals(0, fixture.player.getBonusTime());
        assertEquals(1, fixture.ui.escapes);
        assertTrue(fixture.encounter.getEncounterOutcome().getIsVictory());
        assertEquals(new Random(42).nextLong(), fixture.random.nextLong());
    }

    /** Guard damage handles below-lethal, exactly-lethal and overkill health boundaries. */
    @Test
    void escape_wrongAnswer_clampsHealthAndMarksDeath() {
        for (int health : new int[] {1, 19, 20, 21, Integer.MAX_VALUE}) {
            Fixture fixture = new Fixture(health, false);
            fixture.encounter.prepareEvent(fixture.factory);
            fixture.encounter.submitAnswer("wrong", fixture.factory);

            fixture.encounter.submitAnswer("wrong", fixture.factory);

            int expectedHealth = Math.max(0, health - 20);
            assertEquals(expectedHealth, fixture.player.getHealth());
            assertEquals(expectedHealth == 0, fixture.player.getIsDead());
            assertEquals(expectedHealth, fixture.ui.reportedHealth);
            assertEquals(20, fixture.ui.damage);
            assertEquals(1, fixture.ui.guardAttacks);
            assertTrue(fixture.encounter.getIsCompleted());
            assertEquals(expectedHealth > 0, fixture.encounter.getEncounterOutcome().getIsVictory());
            assertThrows(IllegalStateException.class, () -> fixture.encounter.submitAnswer("wrong", fixture.factory));
            assertEquals(expectedHealth, fixture.player.getHealth());
        }
    }

    /** Failed initial generation must not announce a prompt or make an answer legal. */
    @Test
    void prepareEvent_factoryFailure_canRetryWithoutAdvancing() {
        for (boolean returnsNull : new boolean[] {false, true}) {
            Fixture fixture = new Fixture(100, false);
            fixture.factory.failOnRequest = 1;
            fixture.factory.returnNullOnFailure = returnsNull;
            if (returnsNull) {
                assertThrows(NullPointerException.class, () -> fixture.encounter.prepareEvent(fixture.factory));
            } else {
                assertThrows(IllegalStateException.class, () -> fixture.encounter.prepareEvent(fixture.factory));
            }
            assertEquals(0, fixture.ui.lines);
            assertFalse(fixture.encounter.getIsCompleted());
            assertThrows(IllegalStateException.class,
                    () -> fixture.encounter.submitAnswer("I will not be extorted", fixture.factory));

            assertNotNull(fixture.encounter.prepareEvent(fixture.factory));
            assertEquals(2, fixture.ui.lines);
            assertEquals(new Random(42).nextLong(), fixture.random.nextLong());
        }
    }

    /** Either branch can retry its opening response when follow-up generation fails. */
    @Test
    void submitAnswer_followUpFailure_preservesOpeningAndAllowsRetry() {
        for (boolean trade : new boolean[] {true, false}) {
            Fixture fixture = new Fixture(100, false);
            fixture.factory.failOnRequest = 2;
            fixture.encounter.prepareEvent(fixture.factory);
            String openingAnswer = trade ? "I will not be extorted" : "wrong";
            assertThrows(IllegalStateException.class,
                    () -> fixture.encounter.submitAnswer(openingAnswer, fixture.factory));
            assertEquals(2, fixture.ui.lines);
            assertEquals(5, fixture.player.getActiveSkills().size());
            assertEquals(100, fixture.player.getHealth());
            assertFalse(fixture.encounter.getIsCompleted());

            TypingChallenge followUp = fixture.encounter.submitAnswer(openingAnswer, fixture.factory);
            assertEquals(trade ? "Show me your rare wares" : "flee", followUp.getText());
            assertEquals(trade ? 10 : 5, followUp.getTime());
            assertEquals(4, fixture.ui.lines);
            fixture.encounter.submitAnswer(followUp.getText(), fixture.factory);
            assertTrue(fixture.encounter.getEncounterOutcome().getIsVictory());
            assertEquals(trade ? 6 : 5, fixture.player.getActiveSkills().size());
        }
    }

    /** Once effects begin, a failed UI callback must not permit another reward or damage application. */
    @Test
    void submitAnswer_feedbackFailure_preventsRepeatedEffects() {
        Fixture trade = new Fixture(100, false);
        trade.openTrade();
        trade.ui.failRewardFeedback = true;
        assertThrows(IllegalStateException.class,
                () -> trade.encounter.submitAnswer("Show me your rare wares", trade.factory));
        assertEquals(6, trade.player.getActiveSkills().size());
        assertThrows(IllegalStateException.class,
                () -> trade.encounter.submitAnswer("Show me your rare wares", trade.factory));

        Fixture escape = new Fixture(10, false);
        escape.encounter.prepareEvent(escape.factory);
        escape.encounter.submitAnswer("wrong", escape.factory);
        escape.ui.failDamageFeedback = true;
        assertThrows(IllegalStateException.class, () -> escape.encounter.submitAnswer("wrong", escape.factory));
        assertEquals(0, escape.player.getHealth());
        assertTrue(escape.player.getIsDead());
        assertThrows(IllegalStateException.class, () -> escape.encounter.submitAnswer("wrong", escape.factory));
    }

    /** Both response stages use the selected difficulty without introducing player bonus-time scaling. */
    @Test
    void challenges_eachDifficulty_scaleBothBranches() {
        for (GameDifficulty difficulty : GameDifficulty.values()) {
            for (boolean trade : new boolean[] {true, false}) {
                Fixture fixture = new Fixture(100, false);
                fixture.player.setBonusTime(100);
                TypingChallengeFactory factory = new TypingChallengeFactory(new PromptBank(fixture.random), difficulty);
                assertEquals(difficulty.scaleCountdown(10), fixture.encounter.prepareEvent(factory).getTime());
                assertEquals(difficulty.scaleCountdown(trade ? 10 : 5),
                        fixture.encounter.submitAnswer(trade ? "I will not be extorted" : "wrong", factory).getTime());
            }
        }
    }

    /** Invalid dependencies fail at construction rather than after an answer has been consumed. */
    @Test
    void constructor_nullDependencies_rejectsConstruction() {
        Fixture fixture = new Fixture(100, false);
        assertThrows(NullPointerException.class, () -> new DialogueEncounter(null, fixture.ui, fixture.rewards));
        assertThrows(NullPointerException.class, () -> new DialogueEncounter(fixture.player, null, fixture.rewards));
        assertThrows(NullPointerException.class, () -> new DialogueEncounter(fixture.player, fixture.ui, null));
    }

    /** Groups one player's dependencies with a single seeded random source. */
    private static class Fixture {
        private final Random random = new Random(42);
        private final Player player;
        private final RecordingEventUi ui = new RecordingEventUi();
        private final RewardPool rewards;
        private final FailingFactory factory = new FailingFactory(random);
        private final DialogueEncounter encounter;

        /** Creates either a standard player or a player who already owns every reward. */
        Fixture(int health, boolean ownsAllSkills) {
            player = new Player("Player", health);
            if (ownsAllSkills) {
                player.addSkill(SkillRegistry.getSkill("overclock"));
            }
            rewards = new RewardPool(random, player);
            encounter = new DialogueEncounter(player, ui, rewards);
        }

        /** Advances through the opening response to the trade branch. */
        void openTrade() {
            encounter.prepareEvent(factory);
            encounter.submitAnswer("I will not be extorted", factory);
        }
    }

    /** Injects a failure on a selected request without consuming the shared random sequence. */
    private static class FailingFactory extends TypingChallengeFactory {
        private int requests;
        private int failOnRequest = -1;
        private boolean returnNullOnFailure;

        /** Retains the player's shared random source for the prompt bank. */
        FailingFactory(Random random) {
            super(new PromptBank(random));
        }

        @Override
        public TypingChallenge generateCustom(String text, int seconds) {
            requests++;
            if (requests == failOnRequest) {
                if (returnNullOnFailure) {
                    return null;
                }
                throw new IllegalStateException("Simulated challenge creation failure");
            }
            return super.generateCustom(text, seconds);
        }
    }

    /** Records dialogue feedback and optionally fails after terminal effects have been applied. */
    private static class RecordingEventUi extends EventUi {
        private int lines;
        private int skillRewards;
        private int healthRewards;
        private int healing;
        private int penalties;
        private int escapes;
        private int guardAttacks;
        private int damage;
        private int reportedHealth;
        private Skill reward;
        private boolean failRewardFeedback;
        private boolean failDamageFeedback;

        @Override
        public void showDialogueText(String text) {
            lines++;
        }

        @Override
        public void showDialogueVictory(Skill newSkill) {
            skillRewards++;
            reward = newSkill;
            if (failRewardFeedback) {
                throw new IllegalStateException("Simulated reward feedback failure");
            }
        }

        @Override
        public void showAltDialogueVictory(int increaseAmount, int currentHP) {
            healthRewards++;
            healing = increaseAmount;
            reportedHealth = currentHP;
        }

        @Override
        public void showDialogueDefeat1() {
            penalties++;
        }

        @Override
        public void showDialogueNeutral() {
            escapes++;
        }

        @Override
        public void showDialogueDefeat2(int damage, int currentHP) {
            guardAttacks++;
            this.damage = damage;
            reportedHealth = currentHP;
            if (failDamageFeedback) {
                throw new IllegalStateException("Simulated damage feedback failure");
            }
        }
    }
}
