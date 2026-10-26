package seedu.keyboardwarrior.seed;

import java.util.Random;

/** Java-compatible random sequence whose position can be saved between gameplay turns. */
public class RunRandom extends Random {
    private static final long MULTIPLIER = 0x5DEECE66DL;
    private static final long ADDEND = 0xBL;
    private static final long MASK = (1L << 48) - 1;
    /** Internal 48-bit position, rather than the original seed, so restoration needs no replay. */
    private long state;

    /** Starts the same sequence as {@link Random#Random(long)} for the supplied seed. */
    public RunRandom(long seed) {
        super(seed);
    }

    /** Resets the sequence using Java Random's seed scrambling rule. */
    @Override
    public synchronized void setSeed(long seed) {
        super.setSeed(seed);
        state = (seed ^ MULTIPLIER) & MASK;
    }

    /** Advances Java Random's linear congruential generator by one step. */
    @Override
    protected synchronized int next(int bits) {
        state = (state * MULTIPLIER + ADDEND) & MASK;
        return (int) (state >>> (48 - bits));
    }

    /** Returns the position used by gameplay's nextInt calls. */
    public synchronized long snapshot() {
        return state;
    }

    /** Restores a validated position without consuming any random choices. */
    public static RunRandom restore(long state) {
        if (state < 0 || state > MASK) {
            throw new IllegalArgumentException("Random state must be an unsigned 48-bit value");
        }
        RunRandom random = new RunRandom(0);
        random.state = state;
        return random;
    }
}
