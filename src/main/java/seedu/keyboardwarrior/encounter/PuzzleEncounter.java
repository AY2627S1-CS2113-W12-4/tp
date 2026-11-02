package seedu.keyboardwarrior.encounter;

import java.util.Objects;
import java.util.Random;

import seedu.keyboardwarrior.combat.Player;
import seedu.keyboardwarrior.combat.Skill;
import seedu.keyboardwarrior.typing.TypingChallenge;
import seedu.keyboardwarrior.typing.TypingChallengeFactory;
import seedu.keyboardwarrior.typing.PuzzleBank.Puzzle;
import seedu.keyboardwarrior.ui.EventUi;

/**
 * Presents a puzzle and applies a persistent countdown penalty for an incorrect
 * answer.
 * Correct answers draw a skill from the shared reward pool, or award health when it is empty.
 */
public class PuzzleEncounter extends EventEncounter {

    /**
     * Puzzle countdown before player adjustments and difficulty scaling, in
     * seconds.
     */
    private static final int BASE_TIME_LIMIT = 30;
    /**
     * Signed change to the player's accumulated countdown adjustment after a wrong
     * answer.
     */
    private static final int BONUS_TIME_REDUCTION = -2;

    /** Health awarded instead of a skill once the reward pool is exhausted. */
    private static final int BONUS_HEALTH = 20;

    /** Question shown to the player and expected answer used for comparison. */
    private final Puzzle puzzle;
    /** Retained run random reference; actual reward selection is delegated to the shared pool. */
    private final Random random;
    /** Remaining skill rewards shared across puzzle encounters in this run. */
    private final RewardPool rewardPool;

    /**
     * Creates a puzzle event using content already selected by the encounter
     * factory.
     *
     * @param player player receiving rewards or countdown penalties
     * @param ui     interface for puzzle prompts and results
     * @param puzzle selected question and expected answer
     * @param random run's shared random instance, retained without drawing from it directly
     * @param rewardPool shared pool initialized for this player with the same master random instance
     */
    public PuzzleEncounter(Player player, EventUi ui, Puzzle puzzle, Random random, RewardPool rewardPool) {
        super(player, ui);
        this.puzzle = puzzle;
        this.random = random;
        this.rewardPool = rewardPool;
    }

    /**
     * Displays the question and creates an answer challenge with the player's time
     * adjustment, saturating at the maximum integer countdown without overflow.
     *
     * @param factory run's shared challenge factory
     * @return challenge containing the expected answer and scaled countdown
     * @throws IllegalStateException if this event has already been prepared
     */
    @Override
    public TypingChallenge prepareEvent(TypingChallengeFactory factory) {
        requirePhase(EventPhase.NOT_STARTED);
        int adjustedTime = (int) Math.min(Integer.MAX_VALUE, (long) BASE_TIME_LIMIT + player.getBonusTime());
        TypingChallenge challenge = factory.generateCustom(puzzle.answer(), adjustedTime);
        phase = EventPhase.ACCEPTING_ANSWER;
        ui.showPuzzle(puzzle);
        return challenge;
    }

    /**
     * Compares the answer without case or surrounding whitespace and completes the
     * event.
     * A correct answer removes and grants one skill reward, or adds twenty health
     * if no skills remain in the pool, capped at the integer health limit.
     * An incorrect answer reduces future countdowns by two seconds before scaling,
     * saturating at the minimum integer adjustment.
     * The outcome is also marked as victory for an incorrect answer if the player
     * survives.
     *
     * @param input   submitted answer
     * @param factory shared factory; unused because this event has no follow-up
     *                challenge
     * @return {@code null}, as this event has finished
     * @throws IllegalStateException if the puzzle is not currently accepting an answer
     * @throws NullPointerException if the submitted input is null
     */
    @Override
    public TypingChallenge submitAnswer(String input, TypingChallengeFactory factory) {
        requirePhase(EventPhase.ACCEPTING_ANSWER);
        String answer = Objects.requireNonNull(input, "Puzzle answer must not be null").trim();
        // Consume the answer before applying effects so callbacks or failures cannot repeat rewards.
        phase = EventPhase.RESOLVING;
        if (answer.equalsIgnoreCase(puzzle.answer())) {
            Skill reward = rewardPool.popRandomReward();

            if (reward != null) {
                player.addSkill(reward);
                ui.showPuzzleVictory(reward);
            } else {
                int previousHealth = player.getHealth();
                player.setHealth((int) Math.min(Integer.MAX_VALUE, (long) previousHealth + BONUS_HEALTH));
                ui.showAltPuzzleVictory(player.getHealth() - previousHealth, player.getHealth());
            }
            this.outcome.setVictory(true);
        } else {
            player.addBonusTime(BONUS_TIME_REDUCTION);
            ui.showPuzzleDefeat();
            this.outcome.setVictory(player.getHealth() > 0);
        }

        completeEvent();
        return null;
    }

    /**
     * Returns the label shown when this event is offered.
     *
     * @return puzzle encounter label
     */
    @Override
    public String getDisplayText() {
        return "Event: A Mysterious Puzzle";
    }
}
