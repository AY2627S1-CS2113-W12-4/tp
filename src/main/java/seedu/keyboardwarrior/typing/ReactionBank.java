package seedu.keyboardwarrior.typing;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * ReactionBank
 */
public class ReactionBank {

    private final List<String> reactions = new ArrayList<>();
    private final Random random;

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

    public String getRandomReaction() {
        return reactions.get(random.nextInt(reactions.size()));
    }
}
