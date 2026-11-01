package seedu.keyboardwarrior.encounter;

import seedu.keyboardwarrior.combat.Player;
import seedu.keyboardwarrior.typing.TypingChallenge;
import seedu.keyboardwarrior.typing.TypingChallengeFactory;
import seedu.keyboardwarrior.typing.PuzzleBank.Puzzle;
import seedu.keyboardwarrior.ui.EventUi;

/**
 * PuzzleEncounter
 */
public class PuzzleEncounter extends EventEncounter {

    private final Puzzle puzzle;

    private static final int PUZZLE_BASE_TIME_LIMIT = 30;
    private static final int PUZZLE_BASE_DAMAGE = 15;

    public PuzzleEncounter(Player player, EventUi ui, Puzzle puzzle) {
        super(player, ui);
        this.puzzle = puzzle;
    }

    @Override
    public TypingChallenge prepareEvent(TypingChallengeFactory factory) {
        ui.showPuzzle(puzzle);
        return factory.generateCustom(puzzle.answer(), PUZZLE_BASE_TIME_LIMIT);
    }

    @Override
    public TypingChallenge submitAnswer(String input, TypingChallengeFactory factory) {
        if (input.trim().equalsIgnoreCase(puzzle.answer())) {
            // TODO: generate Skill reward, then call ui.showVictory(skill)
            this.outcome.setVictory(true);
        } else {
            player.setHealth(player.getHealth() - PUZZLE_BASE_DAMAGE);
            ui.showDefeat(PUZZLE_BASE_DAMAGE, player.getHealth());
            this.outcome.setVictory(player.getHealth() > 0);
        }

        this.isCompleted = true;
        return null;
    }

    @Override
    public String getDisplayText() {
        return "Event: A Mysterious Puzzle";
    }
}
