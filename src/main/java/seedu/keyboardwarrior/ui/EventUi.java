package seedu.keyboardwarrior.ui;

import seedu.keyboardwarrior.combat.Skill;
import seedu.keyboardwarrior.typing.PuzzleBank.Puzzle;

/**
 * EventUi
 */
public class EventUi {

    public void showPuzzle(Puzzle puzzle) {
        StateUi.printLine("A puzzle appears in front of you...");
        StateUi.printLine("");
        StateUi.printLine(puzzle.question());
    }

    public void showVictory(Skill newSkill) {
        StateUi.printLine("Leet is pleased... It rewards you...");
        StateUi.printLine("You have received: " + newSkill.getName() + "!");
    }

    public void showDefeat(int damage, int remainingHP) {
        StateUi.printLine("Leet is displeased. It forcibly kicks you to the next stage.");
        StateUi.printLine("You took " + damage + " damage! You have " + remainingHP + " HP remaining.");
    }
}
