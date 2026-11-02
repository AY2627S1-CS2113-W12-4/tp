package seedu.keyboardwarrior.encounter;

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
 * Skill rewards for correct answers are not yet implemented.
 */
public class PuzzleEncounter extends EventEncounter {

    /** Question shown to the player and expected answer used for comparison. */
    private final Puzzle puzzle;
    private final Random random;
    private final RewardPool rewardPool;

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

    private static final int BONUS_HEALTH = 20;

    /**
     * Creates a puzzle event using content already selected by the encounter
     * factory.
     *
     * @param player player receiving any countdown penalty
     * @param ui     interface for puzzle prompts and results
     * @param puzzle selected question and expected answer
     */
    public PuzzleEncounter(Player player, EventUi ui, Puzzle puzzle, Random random, RewardPool rewardPool) {
        super(player, ui);
        this.puzzle = puzzle;
        this.random = random;
        this.rewardPool = rewardPool;
    }

    /**
     * Displays the question and creates an answer challenge with the player's time
     * adjustment.
     *
     * @param factory run's shared challenge factory
     * @return challenge containing the expected answer and scaled countdown
     */
    @Override
    public TypingChallenge prepareEvent(TypingChallengeFactory factory) {
        ui.showPuzzle(puzzle);
        assert (long) BASE_TIME_LIMIT + player.getBonusTime() <= Integer.MAX_VALUE
                : "Adjusted puzzle time must not overflow";
        return factory.generateCustom(puzzle.answer(), BASE_TIME_LIMIT + player.getBonusTime());
    }

    /**
     * Compares the answer without case or surrounding whitespace and completes the
     * event.
     * An incorrect answer reduces future countdowns by two seconds before scaling.
     * The outcome is also marked as victory for an incorrect answer if the player
     * survives.
     *
     * @param input   submitted answer
     * @param factory shared factory; unused because this event has no follow-up
     *                challenge
     * @return {@code null}, as this event has finished
     */
    @Override
    public TypingChallenge submitAnswer(String input, TypingChallengeFactory factory) {
        if (input.trim().equalsIgnoreCase(puzzle.answer())) {
            Skill reward = rewardPool.popRandomReward();

            if (reward != null) {
                player.addSkill(reward);
                ui.showPuzzleVictory(reward);
            } else {
                player.setHealth(player.getHealth() + BONUS_HEALTH);
                ui.showAltPuzzleVictory(BONUS_HEALTH, player.getHealth());
            }
            this.outcome.setVictory(true);
        } else {
            assert (long) player.getBonusTime() + BONUS_TIME_REDUCTION >= Integer.MIN_VALUE
                    : "Puzzle penalties must not overflow the countdown adjustment";
            player.setBonusTime(player.getBonusTime() + BONUS_TIME_REDUCTION);
            ui.showPuzzleDefeat();
            this.outcome.setVictory(player.getHealth() > 0);
        }

        this.isCompleted = true;
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
