package seedu.keyboardwarrior.typing;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * PuzzleBank
 */
public class PuzzleBank {

    public record Puzzle(String question, String answer) {
    }

    private final List<Puzzle> puzzles = new ArrayList<>();
    private final Random random;

    public PuzzleBank(Random random) {
        this.random = random;

        puzzles.add(new Puzzle("", ""));
    }

    public Puzzle getRandomPuzzle() {
        return puzzles.get(random.nextInt(puzzles.size()));
    }
}
