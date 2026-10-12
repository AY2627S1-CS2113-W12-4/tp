package seedu.keyboardwarrior.combat;

import java.util.ArrayList;

/** Represents a combat participant with health and a collection of usable skills. */
public abstract class Entity {
    /** Name displayed by the combat interface. */
    protected String name;
    /** Current health points; combat damage does not reduce this below zero. */
    protected int health;
    /** Whether this entity has been defeated. */
    protected Boolean isDead;
    /** Skills currently available to this entity. */
    protected ArrayList<Skill> activeSkills;

    /**
     * Creates a living combat participant without any active skills.
     *
     * @param name name displayed during combat
     * @param health starting health points
     */
    public Entity(String name, int health) {
        this.name = name;
        this.health = health;
        this.isDead = false;
        this.activeSkills = new ArrayList<>();
    }

    /**
     * Replaces the entity's current health value.
     *
     * @param health new health value
     */
    public void setHealth(int health) {
        this.health = health;
    }

    /**
     * Updates whether the entity has been defeated.
     *
     * @param isDead {@code true} if the entity is defeated
     */
    public void setIsDead(Boolean isDead) {
        this.isDead = isDead;
    }

    /**
     * Makes a skill available to this entity.
     *
     * @param skill skill to add
     */
    public void addSkill(Skill skill) {
        activeSkills.add(skill);
    }

    /**
     * Returns the name displayed during combat.
     *
     * @return entity name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the entity's current health.
     *
     * @return current health points
     */
    public int getHealth() {
        return health;
    }

    /**
     * Reports whether the entity has been defeated.
     *
     * @return {@code true} if the entity is dead
     */
    public boolean getIsDead() {
        return isDead;
    }

    /**
     * Returns the mutable list of skills available to this entity.
     *
     * @return active skills
     */
    public ArrayList<Skill> getActiveSkills() {
        return activeSkills;
    }
}
