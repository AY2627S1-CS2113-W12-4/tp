package seedu.keyboardwarrior.combat;

import java.util.ArrayList;
import java.util.Objects;

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
     * @throws NullPointerException if the name is null
     * @throws IllegalArgumentException if the name is blank or health is negative
     */
    public Entity(String name, int health) {
        this.name = Objects.requireNonNull(name, "Entity name must not be null");
        if (name.isBlank()) {
            throw new IllegalArgumentException("Entity name must not be blank");
        }
        if (health < 0) {
            throw new IllegalArgumentException("Health must not be negative");
        }
        this.health = health;
        this.isDead = false;
        this.activeSkills = new ArrayList<>();
    }

    /**
     * Replaces the entity's current health value.
     *
     * @param health new health value
     * @throws IllegalArgumentException if health is negative
     */
    public void setHealth(int health) {
        if (health < 0) {
            throw new IllegalArgumentException("Health must not be negative");
        }
        this.health = health;
    }

    /**
     * Updates whether the entity has been defeated.
     *
     * @param isDead {@code true} if the entity is defeated
     * @throws NullPointerException if the defeated flag is null
     */
    public void setIsDead(Boolean isDead) {
        this.isDead = Objects.requireNonNull(isDead, "Defeated flag must not be null");
    }

    /**
     * Makes a skill available to this entity.
     *
     * @param skill skill to add
     * @throws NullPointerException if the skill is null
     */
    public void addSkill(Skill skill) {
        activeSkills.add(Objects.requireNonNull(skill, "Skill must not be null"));
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
        assert health >= 0 : "Entity health must remain nonnegative";
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
