package seedu.keyboardwarrior.combat;

import java.util.HashMap;

import seedu.keyboardwarrior.typing.TypingChallenge.Difficulty;

/**
 * SkillRegistry
 */
public class SkillRegistry {

    private static final HashMap<String, Skill> SKILL_DATABASE = new HashMap<>();

    static {
        SKILL_DATABASE.put("ping", new Skill("Ping", 10, Difficulty.EASY));
        SKILL_DATABASE.put("brute_force", new Skill("Brute Force", 20, Difficulty.MEDIUM));
        SKILL_DATABASE.put("buffer_overflow", new Skill("Buffer Overflow", 40, Difficulty.HARD));
        SKILL_DATABASE.put("sigkill", new Skill("SIGKILL", 80, Difficulty.ULTIMATE));
    }

    public static Skill getSkill(String id) {
        if (!SKILL_DATABASE.containsKey(id))
            throw new IllegalArgumentException("Skill not found: " + id);

        return SKILL_DATABASE.get(id);
    }
}
