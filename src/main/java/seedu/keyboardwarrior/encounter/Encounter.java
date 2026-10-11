package seedu.keyboardwarrior.encounter;

// import seedu.keyboardwarrior.typing.TypingResult;

public abstract class Encounter {
    protected boolean isCompleted;
    protected EncounterOutcome outcome;

    public Encounter() {
        this.isCompleted = false;
        this.outcome = new EncounterOutcome();
    }

    public abstract void start();

    public abstract String getDisplayText();

    // public abstract void processTypingResult(TypingResult result);

    public boolean getIsCompleted() {
        return isCompleted;
    }

    public EncounterOutcome getEncounterOutcome() {
        if (!isCompleted) {
            throw new IllegalStateException("Cannot get outcome before encounter is complete!");
        }

        return outcome;
    }
}
