package seedu.keyboardwarrior.typing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Tests result validation and isolation from mutable word-error lists.
 */
class TypingResultTest {
    @Test
    public void constructor_invalidWpm_throwsException() {
        double[] invalidSpeeds = {-1.0, Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY};
        for (double wpm : invalidSpeeds) {
            assertThrows(IllegalArgumentException.class, () -> new TypingResult(wpm, 100.0, 6.0, 10.0, List.of()));
        }
    }

    @Test
    public void constructor_invalidAccuracy_throwsException() {
        double[] invalidAccuracies = {-1.0, 101.0, Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY};
        for (double accuracy : invalidAccuracies) {
            assertThrows(IllegalArgumentException.class, () -> new TypingResult(30.0, accuracy, 6.0, 10.0, List.of()));
        }
    }

    @Test
    public void constructor_invalidDuration_throwsException() {
        double[] invalidDurations = {0.0, -1.0, Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY};
        for (double duration : invalidDurations) {
            assertThrows(IllegalArgumentException.class,
                    () -> new TypingResult(30.0, 100.0, duration, 10.0, List.of()));
            assertThrows(IllegalArgumentException.class,
                    () -> new TypingResult(30.0, 100.0, 6.0, duration, List.of()));
        }
    }

    @Test
    public void constructor_zeroSpeedAndBoundaryAccuracies_areAllowed() {
        TypingResult emptyResult = new TypingResult(0.0, 0.0, 6.0, 10.0, List.of());
        TypingResult perfectResult = new TypingResult(30.0, 100.0, 6.0, 10.0, List.of());

        assertEquals(0.0, emptyResult.getWpm());
        assertEquals(0.0, emptyResult.getAccuracyPercent());
        assertEquals(100.0, perfectResult.getAccuracyPercent());
    }

    @Test
    public void constructor_nullErrorListOrEntry_throwsException() {
        assertThrows(NullPointerException.class, () -> new TypingResult(30.0, 100.0, 6.0, 10.0, null));
        List<WordError> errorsWithNull = Collections.singletonList(null);
        assertThrows(NullPointerException.class, () -> new TypingResult(30.0, 100.0, 6.0, 10.0, errorsWithNull));
    }

    @Test
    public void constructor_mutableErrorList_copiesErrors() {
        WordError error = new WordError(1, "Strike", "strike", ErrorType.WRONG);
        List<WordError> errors = new ArrayList<>(List.of(error));
        TypingResult result = new TypingResult(30.0, 0.0, 6.0, 10.0, errors);

        errors.clear();

        assertEquals(List.of(error), result.getWordErrors());
        assertThrows(UnsupportedOperationException.class, () -> result.getWordErrors().clear());
    }
}
