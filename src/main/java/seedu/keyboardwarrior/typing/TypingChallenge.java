package seedu.keyboardwarrior.typing;

import java.util.Objects;

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

    private final String text;
    private final int time;

    /**
     * Creates a typing challenge.
     *
     * @param text text the player must type
     * @param time allowed completion time in seconds
     * @throws NullPointerException if text is null
     * @throws IllegalArgumentException if text is blank or time is not positive
     */
    public TypingChallenge(String text, int time) {
        Objects.requireNonNull(text, "Challenge text must not be null");
        if (text.isBlank()) {
            throw new IllegalArgumentException("Challenge text must contain at least one word");
        }
        if (time <= 0) {
            throw new IllegalArgumentException("Challenge time must be positive");
        }
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
