package seedu.keyboardwarrior.typing;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Checks countdown expiry, restarting, and duration validation.
 */
class ChallengeClockTest {
    /**
     * Checks that an unstarted clock reports no remaining time.
     */
    @Test
    void getRemainingSeconds_beforeStart_returnsZero() {
        assertEquals(0, new ChallengeClock().getRemainingSeconds());
    }

    /**
     * Checks that a zero-duration countdown expires immediately.
     */
    @Test
    void start_zeroDuration_returnsZero() {
        ChallengeClock clock = new ChallengeClock();
        clock.start(0);
        assertEquals(0, clock.getRemainingSeconds());
    }

    /**
     * Checks that a negative countdown duration is rejected.
     */
    @Test
    void start_negativeDuration_throwsException() {
        ChallengeClock clock = new ChallengeClock();
        assertThrows(IllegalArgumentException.class, () -> clock.start(-1));
    }

    /**
     * Checks that an expired countdown continues to report zero remaining seconds.
     */
    @Test
    void getRemainingSeconds_afterExpiry_returnsZero() throws InterruptedException {
        ChallengeClock clock = new ChallengeClock();
        clock.start(1);
        Thread.sleep(1100);
        assertEquals(0, clock.getRemainingSeconds());
        assertEquals(0, clock.getRemainingSeconds());
    }

    /**
     * Checks that restarting a clock replaces its previous duration.
     */
    @Test
    void start_calledAgain_replacesDuration() {
        ChallengeClock clock = new ChallengeClock();
        clock.start(0);
        clock.start(60);
        assertEquals(60, clock.getRemainingSeconds());
        clock.start(0);
        assertEquals(0, clock.getRemainingSeconds());
    }
}
