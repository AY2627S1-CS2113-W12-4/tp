package seedu.keyboardwarrior.seed;

import java.util.Random;

/**
 * SeedGenerator
 */
public class SeedGenerator {

    /**
     * Converts a user's input string into a usable numeric seed.
     * 
     * @param input The seed string (can be null, empty, numeric, or text)
     * @return A 64-bit long to be used for java.util.Random
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
