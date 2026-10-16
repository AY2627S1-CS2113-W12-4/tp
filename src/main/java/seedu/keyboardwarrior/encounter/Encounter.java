package seedu.keyboardwarrior.encounter;

// import seedu.keyboardwarrior.typing.TypingResult;

/** Defines the lifecycle and result shared by all game encounters. */
public abstract class Encounter {
    /** Whether the encounter has reached a final outcome. */
    protected boolean isCompleted;
    /** Result populated when the encounter completes. */
    protected EncounterOutcome outcome;

    /** Creates an incomplete encounter with a default outcome container. */
    public Encounter() {
        this.isCompleted = false;
        this.outcome = new EncounterOutcome();
    }

    /** Runs the encounter until it reaches a final outcome. */
    public abstract void start();

    /**
     * Returns a short description suitable for menus and logs.
     *
     * @return encounter description
     */
    public abstract String getDisplayText();

    // public abstract void processTypingResult(TypingResult result);

    /**
     * Reports whether this encounter has finished.
     *
     * @return {@code true} after the encounter reaches an outcome
     */
    public boolean getIsCompleted() {
        return isCompleted;
    }

    /**
     * Returns the result of a completed encounter.
     *
     * @return final encounter outcome
     * @throws IllegalStateException if the encounter has not completed
     */
    public EncounterOutcome getEncounterOutcome() {
        if (!isCompleted) {
            throw new IllegalStateException("Cannot get outcome before encounter is complete!");
        }

        assert outcome != null : "A completed encounter must have an outcome";
        return outcome;
    }
}
