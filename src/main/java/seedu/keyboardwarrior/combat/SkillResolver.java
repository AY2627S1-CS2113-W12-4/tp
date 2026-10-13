package seedu.keyboardwarrior.combat;

import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import seedu.keyboardwarrior.typing.TypingResult;
import seedu.keyboardwarrior.ui.CombatUi;

/** Calculates and applies the damage produced by a selected combat skill. */
public class SkillResolver {
    /** Optional combat diagnostics; FINE messages do not appear in normal gameplay. */
    private static final Logger LOGGER = Logger.getLogger(SkillResolver.class.getName());
    private CombatUi ui;

    /**
     * Creates a resolver that reports combat results through the given interface.
     *
     * @param ui interface used to display damage and time penalties
     * @throws NullPointerException if the interface is null
     */
    public SkillResolver(CombatUi ui) {
        this.ui = Objects.requireNonNull(ui, "Combat interface must not be null");
    }

    /**
     * Resolves one skill use against a target.
     * Player damage is scaled by 0.02 times WPM times accuracy as a fraction,
     * while a {@code null} result
     * applies the base damage directly for automated enemy turns.
     * Exceeding the time limit reduces player damage by twenty percent.
     *
     * @param source entity using the skill
     * @param target entity receiving any damage
     * @param chosenSkill skill being used
     * @param result typing performance, or {@code null} to use base damage
     * @throws NullPointerException if a participant or the chosen skill is null
     */
    public void execute(Entity source, Entity target, Skill chosenSkill, TypingResult result) {
        Objects.requireNonNull(source, "Skill source must not be null");
        Objects.requireNonNull(target, "Skill target must not be null");
        Objects.requireNonNull(chosenSkill, "Chosen skill must not be null");
        if (result == null) {
            applyDamage(target, chosenSkill.getBaseDamage());
            return;
        }

        if (result.isTimeLimitExceeded()) {
            ui.showTypingSlow();
        }

        double accuracy = result.getAccuracyPercent() / 100.0;
        double multiplier = 0.02 * result.getWpm() * accuracy;
        // Apply the time penalty before rounding so fractional damage is retained until the end.
        int calculatedDamage = (int) (chosenSkill.getBaseDamage() * multiplier * result.getTimeDamageMultiplier());
        applyDamage(target, calculatedDamage);
    }

    /** Applies damage without allowing the target's health to fall below zero. */
    private void applyDamage(Entity target, int damage) {
        assert damage >= 0 : "Resolved damage must not be negative";
        int currentHealth = target.getHealth();
        int newHealth = Math.max(0, currentHealth - damage);
        assert newHealth >= 0 && newHealth <= currentHealth : "Damage must not increase or make health negative";
        target.setHealth(newHealth);

        LOGGER.log(Level.FINE, "Applied {0} damage to {1}; health changed from {2} to {3}",
                new Object[] {damage, target.getName(), currentHealth, newHealth});

        ui.showDamage(target.getName(), damage);
    }
}
