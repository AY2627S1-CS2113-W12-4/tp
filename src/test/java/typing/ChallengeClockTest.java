package typing;

import org.junit.jupiter.api.Test;

import seedu.keyboardwarrior.typing.ChallengeClock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Checks countdown expiry, restarting, and duration validation.
 */
class ChallengeClockTest {
    @Test
    void getRemainingSeconds_beforeStart_returnsZero() {
        assertEquals(0, new ChallengeClock().getRemainingSeconds());
    }

    @Test
    void start_zeroDuration_returnsZero() {
        ChallengeClock clock = new ChallengeClock();
        clock.start(0);
        assertEquals(0, clock.getRemainingSeconds());
    }

    @Test
    void start_negativeDuration_throwsException() {
        ChallengeClock clock = new ChallengeClock();
        assertThrows(IllegalArgumentException.class, () -> clock.start(-1));
    }

    @Test
    void getRemainingSeconds_afterExpiry_returnsZero() throws InterruptedException {
        ChallengeClock clock = new ChallengeClock();
        clock.start(1);
        Thread.sleep(1100);
        assertEquals(0, clock.getRemainingSeconds());
        assertEquals(0, clock.getRemainingSeconds());
    }

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
