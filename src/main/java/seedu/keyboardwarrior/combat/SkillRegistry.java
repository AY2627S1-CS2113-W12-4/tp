package seedu.keyboardwarrior.combat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import seedu.keyboardwarrior.typing.TypingChallenge.Difficulty;

/**
 * Provides shared definitions for every skill identified by the game.
 */
public class SkillRegistry {

    private static final HashMap<String, Skill> SKILL_DATABASE = new HashMap<>();

    static {
        SKILL_DATABASE.put("ping", new Skill("Ping", 10, 0, Difficulty.EASY));
        SKILL_DATABASE.put("brute_force", new Skill("Brute Force", 20, 0, Difficulty.MEDIUM));
        SKILL_DATABASE.put("buffer_overflow", new Skill("Buffer Overflow", 40, 0, Difficulty.HARD));
        SKILL_DATABASE.put("sigkill", new Skill("SIGKILL", 80, 0, Difficulty.ULTIMATE));
        SKILL_DATABASE.put("encryption", new Skill("Encryption", 80, 0, Difficulty.ULTIMATE));
        SKILL_DATABASE.put("overclock", new Skill("Overclick", 0, 20, Difficulty.EASY));
    }

    /**
     * Looks up a skill by its registry identifier.
     *
     * @param id skill identifier, such as {@code ping}
     * @return registered skill
     * @throws IllegalArgumentException if no skill uses the supplied identifier
     */
    public static Skill getSkill(String id) {
        if (!SKILL_DATABASE.containsKey(id)) {
            throw new IllegalArgumentException("Skill not found: " + id);
        }

        Skill skill = SKILL_DATABASE.get(id);
        assert skill != null : "Registered skill identifiers must map to a skill";
        return skill;
    }

    public static List<Skill> getAllSkills() {
        return new ArrayList<>(SKILL_DATABASE.values());
    }
}
