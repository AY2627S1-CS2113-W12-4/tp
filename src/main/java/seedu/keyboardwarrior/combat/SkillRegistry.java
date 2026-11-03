package seedu.keyboardwarrior.combat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import seedu.keyboardwarrior.typing.TypingChallenge.Difficulty;

/**
 * Provides shared definitions for every skill identified by the game.
 */
public class SkillRegistry {

    /** Ordered starting loadout, also used when migrating older run saves. */
    private static final List<String> DEFAULT_PLAYER_SKILLS = List.of(
            "ping", "brute_force", "buffer_overflow", "sigkill", "encryption");

    private static final HashMap<String, Skill> SKILL_DATABASE = new HashMap<>();

    static {
        SKILL_DATABASE.put("ping", new Skill("Ping", 10, 0, Difficulty.EASY));
        SKILL_DATABASE.put("brute_force", new Skill("Brute Force", 20, 0, Difficulty.MEDIUM));
        SKILL_DATABASE.put("buffer_overflow", new Skill("Buffer Overflow", 40, 0, Difficulty.HARD));
        SKILL_DATABASE.put("sigkill", new Skill("SIGKILL", 80, 0, Difficulty.ULTIMATE));
        SKILL_DATABASE.put("encryption", new Skill("Encryption", 80, 0, Difficulty.ULTIMATE));
        SKILL_DATABASE.put("overclock", new Skill("Overclock", 0, 20, Difficulty.EASY));
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

    /** Returns the stable identifier of a shared registered skill for persistence. */
    public static String getId(Skill skill) {
        Objects.requireNonNull(skill, "Skill must not be null");
        return SKILL_DATABASE.entrySet().stream()
                .filter(entry -> entry.getValue() == skill)
                .map(java.util.Map.Entry::getKey)
                .findFirst().orElseThrow(() -> new IllegalArgumentException("Skill is not registered"));
    }

    /** Returns the immutable identifiers of the five skills available at the start of a run. */
    public static List<String> getDefaultPlayerSkillIds() {
        return DEFAULT_PLAYER_SKILLS;
    }

    /**
     * Returns a mutable copy of the registered skills sorted by registry identifier.
     * Removing entries from the returned list does not modify the registry.
     * Skill objects themselves are shared. Stable ordering ensures that a seeded reward
     * draw does not depend on the hash map's iteration order.
     *
     * @return a new list containing the registry's shared skill instances
     */
    public static List<Skill> getAllSkills() {
        List<String> skillIds = new ArrayList<>(SKILL_DATABASE.keySet());
        skillIds.sort(String::compareTo);
        List<Skill> skills = new ArrayList<>();
        for (String id : skillIds) {
            skills.add(SKILL_DATABASE.get(id));
        }
        return skills;
    }
}
