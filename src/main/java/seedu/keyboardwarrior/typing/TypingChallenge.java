package seedu.keyboardwarrior.typing;

/**
 * TypingChallenge
 */
public class TypingChallenge {

    public enum Difficulty {
        EASY,
        MEDIUM,
        HARD,
        ULTIMATE
    }

    private String text;
    private int time;

    public TypingChallenge(String text, int time) {
        this.text = text;
        this.time = time;
    }

    public String getText() {
        return text;
    }

    public int getTime() {
        return time;
    }
}
