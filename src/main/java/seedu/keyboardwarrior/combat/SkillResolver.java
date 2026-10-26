package seedu.keyboardwarrior.combat;

import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import seedu.keyboardwarrior.GameDifficulty;
import seedu.keyboardwarrior.typing.TypingResult;
import seedu.keyboardwarrior.ui.CombatUi;

/**
 * Calculates and applies the damage produced by a selected combat skill.
 */
public class SkillResolver {
    /**
     * Optional combat diagnostics; FINE messages do not appear in normal gameplay.
     */
    private static final Logger LOGGER = Logger.getLogger(SkillResolver.class.getName());
    private CombatUi ui;
    private final GameDifficulty difficulty;

    /**
     * Creates a resolver that reports combat results through the given interface.
     *
     * @param ui interface used to display damage
     * @throws NullPointerException if the interface is null
     */
    public SkillResolver(CombatUi ui) {
        this(ui, GameDifficulty.HARD);
    }

    /** Uses the selected run multiplier for typing attacks while keeping enemy base damage unchanged. */
    public SkillResolver(CombatUi ui, GameDifficulty difficulty) {
        this.ui = Objects.requireNonNull(ui, "Combat interface must not be null");
        this.difficulty = Objects.requireNonNull(difficulty, "Game difficulty must not be null");
    }

    /**
     * Resolves one skill use against a target.
     * Player damage is WPM multiplied by the fourth power of character accuracy
     * as a fraction and the run difficulty multiplier, rounded down once at the end. The selected skill's base
     * damage and the time limit do not modify this result. A {@code null} result
     * applies the skill's base damage directly for automated enemy turns.
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

        double accuracy = result.getAccuracyPercent() / 100.0;
        double damage = result.getWpm() * Math.pow(accuracy, 4) * difficulty.getMultiplier();
        assert accuracy >= 0.0 && accuracy <= 1.0 : "Typing accuracy must be normalized";
        assert Double.isFinite(damage) && damage >= 0.0 : "Typing damage must be valid";
        // Retain fractional precision until the final conversion; Java caps extreme values at Integer.MAX_VALUE.
        int calculatedDamage = (int) damage;
        assert calculatedDamage >= 0 : "Calculated damage must not be negative";
        applyDamage(target, calculatedDamage);
    }

    /**
     * Applies damage without allowing the target's health to fall below zero.
     *
     * @param target entity receiving the damage
     * @param damage nonnegative damage to apply
     */
    private void applyDamage(Entity target, int damage) {
        assert damage >= 0 : "Resolved damage must not be negative";
        int currentHealth = target.getHealth();
        int newHealth = Math.max(0, currentHealth - damage);
        assert newHealth >= 0 && newHealth <= currentHealth : "Damage must not increase or make health negative";
        target.setHealth(newHealth);
        assert target.getHealth() == newHealth : "Target health must reflect the resolved damage";

        LOGGER.log(Level.FINE, "Applied {0} damage to {1}; health changed from {2} to {3}",
                new Object[] {damage, target.getName(), currentHealth, newHealth});

        ui.showDamage(target.getName(), damage);
    }
}
