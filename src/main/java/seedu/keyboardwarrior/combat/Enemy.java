package seedu.keyboardwarrior.combat;

/** Represents an opponent that can participate in a combat encounter. */
public class Enemy extends Entity {
    /**
     * Creates an enemy without any active skills.
     *
     * @param name name displayed during combat
     * @param health starting health points
     */
    public Enemy(String name, int health) {
        super(name, health);
    }
}
