package seedu.keyboardwarrior.combat;

import seedu.keyboardwarrior.typing.TypingResult;
import seedu.keyboardwarrior.ui.CombatUi;

public class SkillResolver {
    private CombatUi ui;

    public SkillResolver(CombatUi ui) {
        this.ui = ui;
    }

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

    private void applyDamage(Entity target, int damage) {
        int currentHealth = target.getHealth();
        int newHealth = Math.max(0, currentHealth - damage);
        target.setHealth(newHealth);

        ui.showDamage(target.getName(), damage);
    }
}
