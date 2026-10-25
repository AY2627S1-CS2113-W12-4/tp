package seedu.keyboardwarrior.seed;

import java.util.Random;

/**
 * Converts optional numeric or text input into a seed for reproducible random choices.
 */
public class SeedGenerator {

    /**
     * Parses a trimmed signed long, or uses the trimmed text's hash code when parsing fails.
     * Null or blank input produces a newly generated random seed.
     *
     * @param input optional numeric or text seed
     * @return seed suitable for {@link Random}
     */
    public static long parseSeed(String input) {
        if (input == null || input.isBlank()) {
            return new Random().nextLong();
        }

        input = input.trim();

        try {
            return Long.parseLong(input);
        } catch (NumberFormatException e) {
            return (long) input.hashCode();
        }
    }
}
