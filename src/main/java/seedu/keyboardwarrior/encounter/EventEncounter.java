package seedu.keyboardwarrior.encounter;

import seedu.keyboardwarrior.combat.Player;
import seedu.keyboardwarrior.typing.TypingChallenge;
import seedu.keyboardwarrior.typing.TypingChallengeFactory;
import seedu.keyboardwarrior.ui.EventUi;

/**
 * EventEncounter
 */
public abstract class EventEncounter extends Encounter {
    protected final Player player;
    protected final EventUi ui;

    public EventEncounter(Player player, EventUi ui) {
        super();
        this.player = player;
        this.ui = ui;
    }

    public abstract TypingChallenge prepareEvent(TypingChallengeFactory factory);

    public abstract TypingChallenge submitAnswer(String input, TypingChallengeFactory factory);

    public TypingChallenge updateEvent(TypingChallengeFactory factory) {
        return null;
    }
}
