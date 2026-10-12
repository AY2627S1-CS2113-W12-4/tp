package seedu.keyboardwarrior.typing;

/** Stores the target text and time limit for one typing challenge. */
public class TypingChallenge {

    /** Available challenge levels used by skills and prompt selection. */
    public enum Difficulty {
        /** Shortest and simplest challenge level. */
        EASY,
        /** Intermediate challenge level. */
        MEDIUM,
        /** Advanced challenge level. */
        HARD,
        /** Most demanding challenge level. */
        ULTIMATE
    }

    private String text;
    private int time;

    /**
     * Creates a typing challenge.
     *
     * @param text text the player must type
     * @param time allowed completion time in seconds
     */
    public TypingChallenge(String text, int time) {
        this.text = text;
        this.time = time;
    }

    /**
     * Returns the text the player must type.
     *
     * @return challenge text
     */
    public String getText() {
        return text;
    }

    /**
     * Returns the challenge time limit.
     *
     * @return allowed time in seconds
     */
    public int getTime() {
        return time;
    }
}
