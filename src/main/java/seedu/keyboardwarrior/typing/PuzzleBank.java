package seedu.keyboardwarrior.typing;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Stores puzzle questions and answers and selects them using the run's shared
 * random source.
 * Includes riddles, word scrambles, sequences and decoding questions with fixed expected answers.
 */
public class PuzzleBank {

    /**
     * Immutable question and expected answer for a puzzle encounter.
     *
     * @param question text displayed to the player
     * @param answer   expected response, used as the typing challenge's target text
     */
    public record Puzzle(String question, String answer) {
    }

    /** Registered questions and their expected answers. */
    private final List<Puzzle> puzzles = new ArrayList<>();
    /** Master random instance supplied by the encounter factory. */
    private final Random random;

    /**
     * Populates the bank without drawing from or replacing the supplied random
     * source.
     *
     * @param random nonnull master random instance shared by the run
     */
    public PuzzleBank(Random random) {
        this.random = random;

        // Classic Riddles
        puzzles.add(new Puzzle("What belongs to you, but other people use it more than you do?", "name"));
        puzzles.add(new Puzzle("I am an odd number. Take away a letter and I become even. What number am I?", "seven"));
        puzzles.add(new Puzzle("The more of this there is, the less you see. What is it?", "darkness"));

        // Word Scrambles
        puzzles.add(new Puzzle("Unscramble this word (Hint: A machine): R E T U P M O C", "computer"));
        puzzles.add(new Puzzle("Unscramble this word (Hint: Secret code): H I P C E R", "cipher"));
        puzzles.add(new Puzzle("Unscramble this word (Hint: Your weapon): K O Y B E R A D", "keyboard"));
        puzzles.add(new Puzzle("Unscramble this word (Hint: To fix code): G U B E D", "debug"));
        puzzles.add(new Puzzle("Unscramble this word (Hint: Mythical beast): G O R A D N", "dragon"));

        // Logic and Sequences
        puzzles.add(new Puzzle(
                "If you multiply this number by any other number, the answer will always be the same. "
                        + "What is the number?",
                "0"));
        puzzles.add(new Puzzle("What is the next letter in this sequence? O, T, T, F, F, S, S, E, __", "n"));
        puzzles.add(new Puzzle("Some months have 31 days. How many have 28?", "12"));

        // Ciphers and Tech Puzzles
        puzzles.add(new Puzzle("Decode this binary number into a standard decimal number: 1010", "10"));
        puzzles.add(new Puzzle("Shift each letter back by one in the alphabet (Caesar -1) to decode: J C N", "ibm"));
        puzzles.add(new Puzzle("Shift each letter forward by one in the alphabet (Caesar +1) to decode: G Z B J D Q",
                "hacker"));
        puzzles.add(new Puzzle("Which of these does not belong? Byte, Nibble, Apple, Bit", "apple"));
    }

    /**
     * Selects a registered puzzle; consecutive repeats are allowed.
     *
     * @return selected question and expected answer
     */
    public Puzzle getRandomPuzzle() {
        assert !puzzles.isEmpty() && !puzzles.contains(null) : "The puzzle bank must contain selectable records";
        return puzzles.get(random.nextInt(puzzles.size()));
    }
}
