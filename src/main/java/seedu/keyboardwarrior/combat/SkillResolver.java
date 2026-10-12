package seedu.keyboardwarrior.combat;

import seedu.keyboardwarrior.typing.TypingResult;
import seedu.keyboardwarrior.ui.CombatUi;

/** Calculates and applies the damage produced by a selected combat skill. */
public class SkillResolver {
    private CombatUi ui;

    /**
     * Creates a resolver that reports combat results through the given interface.
     *
     * @param ui interface used to display damage and blocked attacks
     */
    public SkillResolver(CombatUi ui) {
        this.ui = ui;
    }

    /**
     * Resolves one skill use against a target.
     * Player damage is scaled using a typing result, while a {@code null} result
     * applies the base damage directly for automated enemy turns.
     *
     * @param source entity using the skill
     * @param target entity receiving any damage
     * @param chosenSkill skill being used
     * @param result typing performance, or {@code null} to use base damage
     */
    public void execute(Entity source, Entity target, Skill chosenSkill, TypingResult result) {
        if (result == null) {
            applyDamage(target, chosenSkill.getBaseDamage());
            return;
        }

        if (!result.canDealDamage()) {
            ui.showTypingSlow();
            return;
        }

        double multiplier = (result.getAccuracyPercent() + result.getWpm()) / 100.0;
        int calculatedDamage = (int) (chosenSkill.getBaseDamage() * multiplier);
        applyDamage(target, calculatedDamage);
    }

    /** Applies damage without allowing the target's health to fall below zero. */
    private void applyDamage(Entity target, int damage) {
        int currentHealth = target.getHealth();
        int newHealth = Math.max(0, currentHealth - damage);
        target.setHealth(newHealth);

        ui.showDamage(target.getName(), damage);
    }
}
