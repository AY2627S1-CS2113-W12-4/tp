package seedu.keyboardwarrior.encounter;

public class EncounterOutcome {
    private boolean isVictory;

    public EncounterOutcome() {
        this.isVictory = false;
    }

    public void setVictory(boolean isVictory) {
        this.isVictory = isVictory;
    }

    public boolean getIsVictory() {
        return isVictory;
    }
}
