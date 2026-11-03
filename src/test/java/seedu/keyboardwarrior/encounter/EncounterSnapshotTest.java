package seedu.keyboardwarrior.encounter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.Test;

import seedu.keyboardwarrior.GameDifficulty;
import seedu.keyboardwarrior.combat.Player;
import seedu.keyboardwarrior.combat.SkillRegistry;
import seedu.keyboardwarrior.seed.RunRandom;
import seedu.keyboardwarrior.typing.PromptBank;
import seedu.keyboardwarrior.typing.PuzzleBank.Puzzle;
import seedu.keyboardwarrior.typing.TypingChallenge;
import seedu.keyboardwarrior.typing.TypingChallengeFactory;
import seedu.keyboardwarrior.typing.TypingEvaluator;
import seedu.keyboardwarrior.ui.CombatUi;
import seedu.keyboardwarrior.ui.EventUi;

/** Checks stable encounter restoration, unchanged random sequences and preserved pending effects. */
class EncounterSnapshotTest {
    /** Save fields are immutable and malformed or transient lifecycle data is rejected early. */
    @Test
    void snapshot_validationRejectsTransientAndMalformedData() {
        Map<String, String> data = new HashMap<>(Map.of("phase", "NOT_STARTED", "node", "0"));
        EncounterSnapshot snapshot = new EncounterSnapshot("dialogue", data);
        data.put("node", "2");
        assertEquals("0", snapshot.data().get("node"));
        assertThrows(UnsupportedOperationException.class, () -> snapshot.data().put("node", "2"));
        assertThrows(IllegalArgumentException.class,
                () -> new EncounterSnapshot("dialogue", Map.of("phase", "RESOLVING", "node", "1")));
        assertThrows(IllegalArgumentException.class,
                () -> new EncounterSnapshot("dialogue", Map.of("phase", "NOT_STARTED", "node", "2")));
        assertThrows(IllegalArgumentException.class, () -> new EncounterSnapshot("puzzle",
                Map.of("phase", "ACCEPTING_ANSWER", "question", "Question", "answer", " ")));
        assertThrows(IllegalArgumentException.class, () -> new EncounterSnapshot("reaction",
                Map.of("phase", "WAITING", "targetWord", "dodge", "delayMillis", "-1")));
    }

    /** Saving and rebuilding offered choices must not consume another random draw or change their content. */
    @Test
    void factory_restoreOffers_preservesContentAndRandomPosition() {
        Player player = new Player("Player", 100);
        RunRandom random = new RunRandom(42);
        TypingChallengeFactory challenges = new TypingChallengeFactory(new PromptBank(random));
        EncounterFactory factory = new EncounterFactory(random, player);
        List<EncounterSnapshot> snapshots = factory.generateChoices(player, new CombatUi(), new EventUi(),
                challenges, GameDifficulty.HARD, 3).stream().map(factory::snapshot).toList();
        long randomPosition = random.snapshot();

        List<EncounterSnapshot> restored = snapshots.stream()
                .map(snapshot -> factory.restore(snapshot, player, new CombatUi(), new EventUi(),
                        challenges, GameDifficulty.HARD, 3, 0))
                .map(factory::snapshot).toList();

        assertEquals(snapshots, restored);
        assertEquals(randomPosition, random.snapshot());
    }

    /** The production overload retains both scenario HP and the selected damage multiplier. */
    @Test
    void factory_balancedChoices_applyScenarioHealthAndDifficulty() {
        Player player = new Player("Player", 200);
        Random random = new Random(42);
        TypingChallengeFactory challenges = new TypingChallengeFactory(new PromptBank(random), GameDifficulty.NORMAL);
        EncounterFactory factory = new EncounterFactory(random, player);
        CombatEncounter combat = (CombatEncounter) factory.generateChoices(player, new CombatUi(), new EventUi(),
                challenges, GameDifficulty.NORMAL, 5).get(0);
        assertEquals("70", factory.snapshot(combat).data().get("enemyHealth"));
        assertThrows(IllegalStateException.class, combat::snapshot);
        combat.begin();
        combat.preparePlayerTurn(SkillRegistry.getSkill("ping"));
        combat.submitTypingResult(TypingEvaluator.evaluateTyping("abcde", "abcde", 6, 10));
        assertEquals("50", factory.snapshot(combat).data().get("enemyHealth"));
    }

    /** A restored combat keeps its remaining HP, run multiplier and next retaliation instead of replaying it. */
    @Test
    void factory_restoreCombat_preservesNextAttackAndDamage() {
        Player player = new Player("Player", 200);
        Random random = new Random(42);
        TypingChallengeFactory challenges = new TypingChallengeFactory(new PromptBank(random), GameDifficulty.NORMAL);
        EncounterFactory factory = new EncounterFactory(random, player);
        CombatEncounter original = (CombatEncounter) factory.generateChoices(player, new CombatUi(), new EventUi(),
                challenges, GameDifficulty.NORMAL, 5).get(0);
        original.begin();
        original.preparePlayerTurn(SkillRegistry.getSkill("ping"));
        original.submitTypingResult(TypingEvaluator.evaluateTyping("abc", "abc", 6, 10));
        EncounterSnapshot snapshot = factory.snapshot(original);
        Player resumedPlayer = new Player("Player", player.getHealth());
        CombatEncounter restored = (CombatEncounter) factory.restore(snapshot, resumedPlayer, new CombatUi(),
                new EventUi(), challenges, GameDifficulty.NORMAL, 5, 0);
        restored.begin(Integer.parseInt(snapshot.data().get("nextEnemySkillIndex")));

        original.preparePlayerTurn(SkillRegistry.getSkill("ping"));
        original.submitTypingResult(TypingEvaluator.evaluateTyping("abc", "abc", 6, 10));
        restored.preparePlayerTurn(SkillRegistry.getSkill("ping"));
        restored.submitTypingResult(TypingEvaluator.evaluateTyping("abc", "abc", 6, 10));

        assertEquals(player.getHealth(), resumedPlayer.getHealth());
        assertEquals(factory.snapshot(original), factory.snapshot(restored));
    }

    /** A restored puzzle keeps its question without granting its pending reward twice. */
    @Test
    void puzzle_resume_retainsQuestionAndAwardsOnlyAfterAnswer() {
        Player player = new Player("Player", 100);
        Random random = new Random(42);
        TypingChallengeFactory challenges = new TypingChallengeFactory(new PromptBank(random));
        PuzzleEncounter original = new PuzzleEncounter(player, new EventUi(), new Puzzle("Selected question", "answer"),
                random, new RewardPool(random, player));
        original.prepareEvent(challenges);
        EncounterFactory factory = new EncounterFactory(random, player);
        EventEncounter restored = (EventEncounter) factory.restore(original.snapshot(), player, new CombatUi(),
                new EventUi(), challenges, GameDifficulty.HARD, 1, 0);

        assertEquals("answer", restored.resumeEvent(challenges).getText());
        assertEquals(5, player.getActiveSkills().size());
        restored.submitAnswer("answer", challenges);
        assertEquals(6, player.getActiveSkills().size());
        assertTrue(restored.getIsCompleted());
        assertThrows(IllegalStateException.class, restored::snapshot);
    }

    /** Dialogue saves keep the selected trade or escape branch, with its effects still pending. */
    @Test
    void dialogue_resumeBothBranches_doesNotReplayOpeningOrEffects() {
        for (boolean trade : new boolean[] {true, false}) {
            Player player = new Player("Player", 100);
            Random random = new Random(42);
            TypingChallengeFactory challenges = new TypingChallengeFactory(new PromptBank(random));
            EncounterFactory factory = new EncounterFactory(random, player);
            DialogueEncounter original = new DialogueEncounter(player, new EventUi(), new RewardPool(random, player));
            original.prepareEvent(challenges);
            TypingChallenge expected = original.submitAnswer(trade ? "I will not be extorted" : "wrong", challenges);
            EventEncounter restored = (EventEncounter) factory.restore(original.snapshot(), player, new CombatUi(),
                    new EventUi(), challenges, GameDifficulty.HARD, 2, 0);

            TypingChallenge resumed = restored.resumeEvent(challenges);
            assertEquals(expected.getText(), resumed.getText());
            assertEquals(expected.getTime(), resumed.getTime());
            assertEquals(100, player.getHealth());
            assertEquals(5, player.getActiveSkills().size());
            assertFalse(restored.getIsCompleted());
            restored.submitAnswer(resumed.getText(), challenges);
            assertTrue(restored.getIsCompleted());
            assertEquals(trade ? 6 : 5, player.getActiveSkills().size());
        }
    }

    /** Waiting ambushes resume their remaining delay without redrawing it or revealing the command early. */
    @Test
    void reaction_resumeWaiting_retainsRemainingDelayAndRandomPosition() {
        Player player = new Player("Player", 100);
        CountingRandom random = new CountingRandom();
        AtomicLong time = new AtomicLong(1000);
        TypingChallengeFactory challenges = new TypingChallengeFactory(new PromptBank(random));
        ReactionEncounter original = new ReactionEncounter(player, new EventUi(), "step back", random, time::get);
        original.prepareEvent(challenges);
        time.addAndGet(1000);
        EncounterSnapshot snapshot = original.snapshot();
        assertEquals("5234", snapshot.data().get("delayMillis"));
        ReactionEncounter restored = new ReactionEncounter(player, new EventUi(), "step back", random, time::get);
        restored.restorePhase(snapshot.data().get("phase"), Long.parseLong(snapshot.data().get("delayMillis")));

        assertNull(restored.resumeEvent(challenges));
        time.addAndGet(5234);
        assertNull(restored.updateEvent(challenges));
        time.incrementAndGet();
        assertEquals("step back", restored.updateEvent(challenges).getText());
        assertEquals(1, random.draws);
        EncounterSnapshot active = restored.snapshot();
        ReactionEncounter prompt = new ReactionEncounter(player, new EventUi(), "step back", random, time::get);
        prompt.restorePhase(active.data().get("phase"), 0);
        assertEquals("step back", prompt.resumeEvent(challenges).getText());
        assertEquals(100, player.getHealth());
        prompt.submitAnswer("step back", challenges);
        assertEquals(110, player.getHealth());
        assertEquals(1, random.draws);
    }

    /** Counts only the delay draw and makes its value deterministic. */
    private static class CountingRandom extends Random {
        private int draws;

        @Override
        public int nextInt(int bound) {
            draws++;
            return 1234;
        }
    }
}
