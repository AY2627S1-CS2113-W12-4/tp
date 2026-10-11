package seedu.keyboardwarrior.combat;

import java.util.ArrayList;

public abstract class Entity {
    protected String name;
    protected int health;
    protected Boolean isDead;
    protected ArrayList<Skill> activeSkills;

    public Entity(String name, int health) {
        this.name = name;
        this.health = health;
        this.isDead = false;
        this.activeSkills = new ArrayList<>();
    }

    public void setHealth(int health) {
        this.health = health;
    }

    public void setIsDead(Boolean isDead) {
        this.isDead = isDead;
    }

    public void addSkill(Skill skill) {
        activeSkills.add(skill);
    }

    public String getName() {
        return name;
    }

    public int getHealth() {
        return health;
    }

    public boolean getIsDead() {
        return isDead;
    }

    public ArrayList<Skill> getActiveSkills() {
        return activeSkills;
    }
}
