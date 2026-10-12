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

    /**
     * Returns the one-based position of the mismatch.
     *
     * @return word position
     */
    public int getPosition() {
        return position;
    }

    /**
     * Returns the expected word, or an empty string for an extra submitted word.
     *
     * @return expected word
     */
    public String getExpectedWord() {
        return expectedWord;
    }

    /**
     * Returns the submitted word, or an empty string for a missing word.
     *
     * @return submitted word
     */
    public String getTypedWord() {
        return typedWord;
    }

    /**
     * Returns the category of mismatch.
     *
     * @return error type
     */
    public ErrorType getType() {
        return type;
    }
}
