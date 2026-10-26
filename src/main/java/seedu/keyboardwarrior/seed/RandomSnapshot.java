package seedu.keyboardwarrior.seed;

import java.util.Map;
import java.util.Objects;

import seedu.keyboardwarrior.typing.PromptBank;
import seedu.keyboardwarrior.typing.TypingChallenge.Difficulty;

/** Random position and prompt history needed to reproduce the next gameplay choices. */
public record RandomSnapshot(long state, Map<Difficulty, Integer> previousPromptIndices) {
    /** Copies prompt history and rejects invalid random positions or passage indices. */
    public RandomSnapshot {
        previousPromptIndices = Map.copyOf(Objects.requireNonNull(previousPromptIndices));
        new PromptBank(RunRandom.restore(state)).restoreHistory(previousPromptIndices);
    }
}
