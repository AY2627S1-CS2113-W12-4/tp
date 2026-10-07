package seedu.keyboardwarrior.typing;

/**
 * Stores an immutable word mismatch, using an empty string for an absent word.
 */
public final class WordError {
    /** Word position starting at one, for display to the player. */
    private final int position;
    private final String expectedWord;
    private final String typedWord;
    private final ErrorType type;

    /**
     * Stores the expected and submitted word at a mismatch position.
     */
    WordError(int position, String expectedWord, String typedWord, ErrorType type) {
        this.position = position;
        this.expectedWord = expectedWord;
        this.typedWord = typedWord;
        this.type = type;
    }

    public int getPosition() {
        return position;
    }

    public String getExpectedWord() {
        return expectedWord;
    }

    public String getTypedWord() {
        return typedWord;
    }

    public ErrorType getType() {
        return type;
    }
}
