package seedu.keyboardwarrior.encounter;

/**
 * Stores the final victory or defeat result of an encounter.
 */
public class EncounterOutcome {
    private boolean isVictory;

    /**
     * Creates an outcome defaulting to defeat until the encounter sets its result.
     */
    public EncounterOutcome() {
        this.isVictory = false;
    }

    /**
     * Records whether the player won the encounter.
     *
     * @param isVictory {@code true} for victory, or {@code false} for defeat
     */
    public void setVictory(boolean isVictory) {
        this.isVictory = isVictory;
    }

    /**
     * Reports whether the player won the encounter.
     *
     * @return {@code true} for victory
     */
    public boolean getIsVictory() {
        return isVictory;
    }
}
