package seedu.keyboardwarrior.typing;

/**
 * Tracks a challenge countdown without blocking player input.
 */
public class ChallengeClock {
    private static final long NANOS_PER_SECOND = 1_000_000_000L;
    private long startTimeNanos;
    private long durationNanos;

    /**
     * Creates a clock with no active countdown.
     */
    public ChallengeClock() {
        startTimeNanos = 0L;
        durationNanos = 0L;
    }

    /**
     * Starts or restarts the timer with the given duration.
     *
     * @param durationSeconds countdown duration in seconds; zero expires immediately
     * @throws IllegalArgumentException if the duration is negative
     */
    public void start(int durationSeconds) {
        if (durationSeconds < 0) {
            throw new IllegalArgumentException("Timer duration cannot be negative.");
        }
        durationNanos = durationSeconds * NANOS_PER_SECOND;
        // nanoTime measures elapsed time independently of changes to the system clock.
        startTimeNanos = System.nanoTime();
    }

    /**
     * Returns remaining seconds, rounding up any partial second.
     *
     * @return zero before starting or after expiry, otherwise the seconds remaining
     */
    public int getRemainingSeconds() {
        if (durationNanos == 0) {
            return 0;
        }
        long elapsedNanos = System.nanoTime() - startTimeNanos;
        long remainingNanos = Math.max(0L, durationNanos - elapsedNanos);
        return (int) ((remainingNanos + NANOS_PER_SECOND - 1) / NANOS_PER_SECOND);
    }

    /**
     * Returns fractional seconds since a positive-duration countdown started.
     *
     * @return elapsed seconds, or zero when no positive-duration timer was started
     */
    public double getElapsedSeconds() {
        return durationNanos == 0 ? 0.0 : (System.nanoTime() - startTimeNanos) / (double) NANOS_PER_SECOND;
    }
}
