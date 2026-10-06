package seedu.duke;

/**
 * Stores the text and fixed time limit for a typing challenge.
 * Both values remain unchanged after construction.
 */
public final class Challenge {
    private final String challengeText;
    /** The challenge's time limit in seconds. */
    private final int countdownTime;

    /** Creates a challenge with the default text and a 15-second time limit. */
    public Challenge() {
        this("The quick brown fox jumps over the lazy dog.", 15);
    }

    /**
     * Creates a challenge with the supplied text and time limit.
     *
     * @param challengeText the text the player needs to type
     * @param countdownTime the fixed time limit in seconds
     */
    public Challenge(String challengeText, int countdownTime) {
        this.challengeText = challengeText;
        this.countdownTime = countdownTime;
    }

    public String getChallengeText() {
        return challengeText;
    }

    public int getCountdownTime() {
        return countdownTime;
    }
}
