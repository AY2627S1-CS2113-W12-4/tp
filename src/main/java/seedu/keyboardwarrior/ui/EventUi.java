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

    public void showPuzzleVictory(Skill newSkill) {
        StateUi.printLine("Leet is pleased... It rewards you...");
        StateUi.printLine("You have received: " + newSkill.getName() + "!");
    }

    public void showPuzzleDefeat() {
        StateUi.printLine("Leet is displeased. It reduced your reply time...");
    }

    public void showReaction() {
        StateUi.printLine("Something's afoot. Look out for danger...");
    }

    public void showReactionWord(String targetWord) {
        StateUi.printLine("!!! TYPE '" + targetWord.toUpperCase() + "' NOW !!!");
    }

    public void showReactionVictory(int increaseAmount, int currentHP) {
        StateUi.printLine("You gain a little bit of courage.");
        StateUi.printLine("HP increased by " + increaseAmount + ". Current HP: " + currentHP);
    }

    public void showReactionDefeat(int damage, int currentHP, String targetWord) {
        StateUi.printLine("Too slow! You failed to " + targetWord + " in time.");
        StateUi.printLine("You took " + damage + " damage! You have " + currentHP + " HP remaining.");
    }
}
