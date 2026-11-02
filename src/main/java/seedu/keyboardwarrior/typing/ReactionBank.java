package seedu.keyboardwarrior.typing;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Stores reaction commands and selects them using the run's shared random source.
 */
public class ReactionBank {

    /** Commands that can be displayed during a reaction encounter. */
    private final List<String> reactions = new ArrayList<>();
    /** Master random instance supplied by the encounter factory. */
    private final Random random;

    /**
     * Registers reaction commands without consuming or replacing the supplied random source.
     *
     * @param random nonnull master random instance shared by the run
     */
    public ReactionBank(Random random) {
        this.random = random;

        reactions.add("dodge");
        reactions.add("parry");
        reactions.add("block");
        reactions.add("deflect");
        reactions.add("roll");
        reactions.add("duck");
        reactions.add("step back");
    }

    /**
     * Selects a command, allowing consecutive repeats and multiword commands.
     *
     * @return nonblank command to display and match against the player's response
     */
    public String getRandomReaction() {
        assert !reactions.isEmpty() : "The reaction bank must contain at least one command";
        assert reactions.stream().allMatch(reaction -> reaction != null && !reaction.isBlank())
                : "Registered reaction commands must be nonblank";
        return reactions.get(random.nextInt(reactions.size()));
    }
}
