package seedu.keyboardwarrior.typing;

import java.util.Locale;
import java.util.Objects;

/**
 * Formats typing metrics, damage eligibility and word errors for console display.
 */
public final class TypingResultFormatter {
    private TypingResultFormatter() {
    }

    /**
     * Returns a console-ready report with metrics rounded to two decimal places.
     * Formatting does not change the full-precision values stored in the result.
     *
     * @param result Evaluated typing result.
     * @return Metrics, the time limit and damage eligibility followed by word errors.
     * @throws NullPointerException If the result is null.
     */
    public static String formatTypingResult(TypingResult result) {
        Objects.requireNonNull(result, "Typing result must not be null");
        StringBuilder report = new StringBuilder(String.format(Locale.ROOT,
                "WPM: %.2f%nAccuracy: %.2f%%%nElapsed time: %.2f seconds%nRequired time: %.2f seconds%n",
                result.getWpm(), result.getAccuracyPercent(), result.getElapsedSeconds(), result.getRequiredSeconds()));
        String newline = System.lineSeparator();
        report.append(result.canDealDamage() ? "Damage allowed: Yes" : "Damage: 0 (time limit exceeded)")
                .append(newline);
        report.append("Wrong words:").append(newline);
        if (result.getWordErrors().isEmpty()) {
            report.append("  None").append(newline);
        } else {
            for (WordError error : result.getWordErrors()) {
                report.append("  Word ").append(error.getPosition()).append(": ");
                switch (error.getType()) {
                case WRONG:
                    report.append("expected \"").append(error.getExpectedWord())
                            .append("\", typed \"").append(error.getTypedWord()).append("\"");
                    break;
                case MISSING:
                    report.append("missing \"").append(error.getExpectedWord()).append("\"");
                    break;
                case EXTRA:
                    report.append("unexpected \"").append(error.getTypedWord()).append("\"");
                    break;
                default:
                    throw new IllegalStateException("Unknown word error type: " + error.getType());
                }
                report.append(newline);
            }
        }
        return report.toString();
    }
}
