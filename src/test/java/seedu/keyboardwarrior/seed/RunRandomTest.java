package seedu.keyboardwarrior.seed;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Random;

import org.junit.jupiter.api.Test;

/** Checks Java seed compatibility and exact continuation after restoring gameplay randomness. */
class RunRandomTest {
    @Test
    void nextInt_matchesJavaRandomIncludingRejectionSampling() {
        for (long seed : new long[] {0, 42, -1, Long.MIN_VALUE, Long.MAX_VALUE}) {
            Random expected = new Random(seed);
            RunRandom actual = new RunRandom(seed);
            int[] bounds = {1, 5, 8, 23, 1073741825};
            for (int index = 0; index < 1000; index++) {
                int bound = bounds[index % bounds.length];
                assertEquals(expected.nextInt(bound), actual.nextInt(bound));
            }
            actual.setSeed(seed);
            expected.setSeed(seed);
            assertEquals(expected.nextLong(), actual.nextLong());
        }
    }

    @Test
    void restore_continuesSequenceWithoutReplayingEarlierDraws() {
        RunRandom original = new RunRandom(42);
        for (int index = 0; index < 137; index++) {
            original.nextInt(23);
        }
        RunRandom resumed = RunRandom.restore(original.snapshot());
        for (int index = 0; index < 1000; index++) {
            assertEquals(original.nextInt(1073741825), resumed.nextInt(1073741825));
        }
        assertThrows(IllegalArgumentException.class, () -> RunRandom.restore(-1));
        assertThrows(IllegalArgumentException.class, () -> RunRandom.restore(1L << 48));
    }
}
