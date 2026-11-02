package seedu.keyboardwarrior.ui;

import seedu.keyboardwarrior.combat.Skill;
import seedu.keyboardwarrior.typing.PuzzleBank.Puzzle;

/**
 * Displays puzzle and reaction prompts and their reported rewards or penalties.
 */
public class EventUi {

    /**
     * Displays a puzzle's question without exposing its answer.
     *
     * @param puzzle puzzle whose question is shown
     */
    public void showPuzzle(Puzzle puzzle) {
        StateUi.printLine("A puzzle appears in front of you...");
        StateUi.printLine("");
        StateUi.printLine(puzzle.question());
    }

    /**
     * Announces a skill reward after a puzzle is solved.
     *
     * @param newSkill skill awarded to the player
     */
    public void showPuzzleVictory(Skill newSkill) {
        StateUi.printLine("Leet is pleased... It rewards you...");
        StateUi.printLine("You have received: " + newSkill.getName() + "!");
    }

    /**
     * Announces the health reward given when a solved puzzle has no remaining skill
     * rewards.
     *
     * @param increaseAmount health awarded instead of a skill
     * @param currentHP      player's health after the reward
     */
    public void showAltPuzzleVictory(int increaseAmount, int currentHP) {
        StateUi.printLine("You have learned all that I can teach you. Come, rest with me and we shall learn together.");
        StateUi.printLine("HP increased by " + increaseAmount + ". Current HP: " + currentHP);
    }

    /** Announces that an incorrect puzzle answer reduced future countdowns. */
    public void showPuzzleDefeat() {
        StateUi.printLine("Leet is displeased. It reduced your reply time...");
    }

    /** Warns the player to wait for a reaction command. */
    public void showReaction() {
        StateUi.printLine("Something's afoot. Look out for danger...");
    }

    /**
     * Reveals the command that the player must enter immediately.
     *
     * @param targetWord reaction command, displayed in uppercase
     */
    public void showReactionWord(String targetWord) {
        StateUi.printLine("!!! TYPE '" + targetWord.toUpperCase() + "' NOW !!!");
    }

    /**
     * Reports the health reward after a successful reaction.
     *
     * @param increaseAmount health awarded
     * @param currentHP      player's health after the reward
     */
    public void showReactionVictory(int increaseAmount, int currentHP) {
        StateUi.printLine("You gain a little bit of courage.");
        StateUi.printLine("HP increased by " + increaseAmount + ". Current HP: " + currentHP);
    }

    /**
     * Reports a failed reaction, the missed command and the resulting health.
     *
     * @param damage     health removed
     * @param currentHP  player's health after damage
     * @param targetWord expected reaction command
     */
    public void showReactionDefeat(int damage, int currentHP, String targetWord) {
        StateUi.printLine("Too slow! You failed to " + targetWord + " in time.");
        StateUi.printLine("You took " + damage + " damage! You have " + currentHP + " HP remaining.");
    }

    public void showDialogueText(String text) {
        StateUi.printLine(text);
    }

    public void showDialogueVictory(Skill newSkill) {
        StateUi.printLine("Deal struck! You earned a new Skill.");
    }

    public void showAltDialogueVictory(int increaseAmount, int currentHP) {
        StateUi.printLine("He has no Skills left. Take a potion!");
        StateUi.printLine("HP increased by " + increaseAmount + ". Current HP: " + currentHP);

    }

    public void showDialogueDefeat1() {
        StateUi.printLine("You hesitated! He revokes the deal and kicks you out.");
        StateUi.printLine("The event left you a little dazed. Your reply time has been reduced.");
    }

    public void showDialogueNeutral() {
        StateUi.printLine("You escaped the guards without a scratch");
    }

    public void showDialogueDefeat2(int damage, int currentHP) {
        StateUi.printLine("The guards catch you!");
        StateUi.printLine("You took " + damage + " damage! You have " + currentHP + " HP remaining.");
    }
}
