package seedu.keyboardwarrior.typing;

/**
 * Describes a mismatch at a word position in the final submission.
 */
public enum ErrorType {
    /** A submitted word differs from the expected word at the same position. */
    WRONG,
    /** An expected word has no submitted word at the same position. */
    MISSING,
    /** A submitted word has no expected word at the same position. */
    EXTRA
}
