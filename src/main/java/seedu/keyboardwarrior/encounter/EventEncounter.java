package seedu.keyboardwarrior.encounter;

import seedu.keyboardwarrior.combat.Player;
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

    public abstract void submitAnswer(String input);

}
