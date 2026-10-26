package seedu.keyboardwarrior;

/** Run settings that scale player damage, starting health and challenge time together. */
public enum GameDifficulty {
    EASY("Easy", 4.0),
    NORMAL("Normal", 2.0),
    HARD("Hard", 1.0),
    CODER("Coder", 0.8);

    private final String displayName;
    private final double multiplier;

    GameDifficulty(String displayName, double multiplier) {
        this.displayName = displayName;
        this.multiplier = multiplier;
    }

    /** Returns the name displayed in menus. */
    public String getDisplayName() {
        return displayName;
    }

    /** Returns the shared multiplier for damage, starting health and countdowns. */
    public double getMultiplier() {
        return multiplier;
    }

    /** Scales the base 100 HP once when a new run starts. */
    public int getStartingHealth() {
        return (int) (100 * multiplier);
    }

    /** Scales a base deadline, rounding up so the player receives the full allowance. */
    public int scaleCountdown(int baseSeconds) {
        if (baseSeconds <= 0) {
            throw new IllegalArgumentException("Base countdown must be positive");
        }
        return (int) Math.ceil(baseSeconds * multiplier);
    }
}
