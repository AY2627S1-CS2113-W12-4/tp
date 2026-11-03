package seedu.keyboardwarrior.encounter;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

/** Immutable content and stable stage of an offered or active encounter, without unfinished input. */
public record EncounterSnapshot(String type, Map<String, String> data) {
    /** Rejects unsupported encounter kinds, transient stages and malformed saved content. */
    public EncounterSnapshot {
        Objects.requireNonNull(type, "Encounter type must not be null");
        data = Map.copyOf(Objects.requireNonNull(data, "Encounter data must not be null"));
        switch (type) {
        case "combat":
            requireKeys(data, "enemyType", "enemyHealth", "nextEnemySkillIndex");
            requireText(data, "enemyType");
            requireNumber(data, "enemyHealth", 1, Integer.MAX_VALUE);
            requireNumber(data, "nextEnemySkillIndex", 0, Integer.MAX_VALUE);
            break;
        case "puzzle":
            requireKeys(data, "phase", "question", "answer");
            requirePhase(data, false);
            requireText(data, "question");
            requireText(data, "answer");
            break;
        case "reaction":
            requireKeys(data, "phase", "targetWord", "delayMillis");
            requirePhase(data, true);
            requireText(data, "targetWord");
            requireNumber(data, "delayMillis", 0, 29_999);
            break;
        case "dialogue":
            requireKeys(data, "phase", "node");
            requirePhase(data, false);
            requireNumber(data, "node", 0, 2);
            if (data.get("phase").equals("NOT_STARTED") && !data.get("node").equals("0")) {
                throw new IllegalArgumentException("An unstarted dialogue must be at its opening");
            }
            break;
        default:
            throw new IllegalArgumentException("Unsupported encounter type: " + type);
        }
    }

    /** Requires precisely the documented fields so incomplete or unknown saved data is rejected. */
    private static void requireKeys(Map<String, String> data, String... keys) {
        if (!data.keySet().equals(Set.of(keys))) {
            throw new IllegalArgumentException("Invalid saved encounter fields");
        }
    }

    /** Ensures saved challenge content can be displayed and evaluated. */
    private static void requireText(Map<String, String> data, String key) {
        if (data.get(key).isBlank()) {
            throw new IllegalArgumentException("Saved " + key + " must not be blank");
        }
    }

    /** Only stages that have not consumed an answer can be restored safely. */
    private static void requirePhase(Map<String, String> data, boolean allowsWaiting) {
        String phase = data.get("phase");
        if (!phase.equals("NOT_STARTED") && !phase.equals("ACCEPTING_ANSWER")
                && !(allowsWaiting && phase.equals("WAITING"))) {
            throw new IllegalArgumentException("Invalid saved event phase: " + phase);
        }
    }

    /** Parses an integral field and checks its domain before restoration changes any objects. */
    private static void requireNumber(Map<String, String> data, String key, long minimum, long maximum) {
        try {
            long value = Long.parseLong(data.get(key));
            if (value < minimum || value > maximum) {
                throw new IllegalArgumentException("Saved " + key + " is outside its allowed range");
            }
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Saved " + key + " must be an integer", exception);
        }
    }
}
