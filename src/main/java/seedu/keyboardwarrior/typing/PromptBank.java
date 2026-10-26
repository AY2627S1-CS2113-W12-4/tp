package seedu.keyboardwarrior.typing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

import seedu.keyboardwarrior.seed.SeedGenerator;
import seedu.keyboardwarrior.typing.TypingChallenge.Difficulty;

/**
 * Stores typing prompts by difficulty and selects prompts for new challenges.
 */
public class PromptBank {
    /** Characters available in Encryption passages; spaces separate groups of at most six. */
    private static final String ENCRYPTION_CHARACTERS = "!@#$%^&*()\\_+/*{}|:\"<>?";
    private final HashMap<Difficulty, List<String>> prompts;
    /**
     * Last selection for each difficulty, so repeated attacks receive different passages.
     */
    private final HashMap<Difficulty, Integer> previousPromptIndices;
    private final Random random;

    /**
     * Creates and populates the prompt bank.
     */
    public PromptBank() {
        this(SeedGenerator.createRandom());
    }

    /**
     * Creates a prompt bank using the supplied random source.
     *
     * @param random source used to select passages
     */
    public PromptBank(Random random) {
        this.prompts = new HashMap<>();
        this.previousPromptIndices = new HashMap<>();
        this.random = Objects.requireNonNull(random);
        initializeBank();
    }

    /** Returns immutable selection history so resumed runs avoid the same previous passages. */
    public Map<Difficulty, Integer> snapshotHistory() {
        return Map.copyOf(previousPromptIndices);
    }

    /** Restores history before generating any prompts for a resumed run. */
    public void restoreHistory(Map<Difficulty, Integer> history) {
        for (var entry : history.entrySet()) {
            if (entry.getValue() < 0 || entry.getValue() >= prompts.get(entry.getKey()).size()) {
                throw new IllegalArgumentException("Invalid saved prompt index");
            }
        }
        previousPromptIndices.clear();
        previousPromptIndices.putAll(history);
    }

    /**
     * Populates each difficulty with its available challenge text.
     */
    private void initializeBank() {
        List<String> easy = new ArrayList<>();
        easy.add("The system booted successfully and local services are running on the server.");
        easy.add("A basic firewall blocks bad traffic while allowing good data to pass through.");
        easy.add("The brave scout crossed the old bridge and found a quiet path through the woods.");
        easy.add("Bright stars guided the ship safely home as the crew watched the night sky.");
        easy.add("A small dragon guards the golden key hidden inside the ruined stone tower.");
        prompts.put(Difficulty.EASY, easy);

        List<String> medium = new ArrayList<>();
        medium.add(
                "To compile Java code from the terminal, you must first verify that the development kit is correctly "
                        + "installed. Then, you can execute the compiler to generate the bytecode class files.");
        medium.add("The ranger followed a trail of glowing stones through the forest. Beyond the ancient oak, "
                + "a hidden gate opened into a valley where the lost kingdom waited beneath the morning mist.");
        medium.add("Before launching the rescue ship, the crew checked every engine and loaded fresh supplies. "
                + "Their mission was to reach the distant station and bring its stranded explorers safely home.");
        medium.add("A secure network needs careful planning and regular checks. The team reviewed each access rule, "
                + "removed unused accounts, and saved a backup before installing the latest system updates.");
        medium.add("The young knight entered the mountain village at sunset. A merchant offered a map of the caves, "
                + "but warned that the narrow tunnels could lead to either hidden treasure or a sleeping monster.");
        prompts.put(Difficulty.MEDIUM, medium);

        List<String> hard = new ArrayList<>();
        hard.add(
                "A buffer overflow occurs when a process attempts to write excess data to a fixed memory block. This "
                        + "vulnerability allows an attacker to execute malicious code, bypass security protocols, and "
                        + "gain root access to the entire system.");
        hard.add("The expedition reached a frozen lake surrounded by towering cliffs. While the guide searched "
                + "for a safe crossing, the others secured their equipment and studied the strange markings "
                + "beneath the ice. Every step brought them closer to the abandoned fortress.");
        hard.add("When the navigation system failed during a meteor storm, the pilot switched to manual control. "
                + "The engineer restored power to the damaged sensors while the captain calculated a route "
                + "through the debris. Together, they guided the ship toward a protected landing zone.");
        hard.add("The defender noticed unusual requests arriving from several unknown devices. After comparing "
                + "the logs, she isolated the affected service and contacted the response team. They traced "
                + "the suspicious activity before restoring normal access to the network.");
        hard.add("At the entrance to the ruined library, a stone guardian demanded proof of the visitor's courage. "
                + "The adventurer read the inscription, solved its forgotten riddle, and stepped beyond the gate. "
                + "Inside, shelves of ancient books concealed the next clue to the royal treasure.");
        prompts.put(Difficulty.HARD, hard);

        List<String> ultimate = new ArrayList<>();
        ultimate.add(
                "Advanced persistent threats often utilize polymorphic malware to evade signature-based detection "
                        + "systems. By constantly altering their binary code during execution, these malicious "
                        + "payloads can bypass standard antivirus firewall. To defend against this, security engineers "
                        + "must "
                        + "implement behavioral analysis tools that monitor network traffic anomalies and memory "
                        + "allocation in actual real time.");
        ultimate.add("As the storm surrounded the floating city, its engineers coordinated an emergency repair. "
                + "One team redirected power from the upper districts while another reinforced the damaged "
                + "stabilizers. Beneath the central tower, the captain studied changing wind patterns and "
                + "guided the evacuation ships through the clouds. Their careful timing kept the city steady "
                + "until the final generator returned to service.");
        ultimate.add("The ancient dragon awoke when the expedition entered its mountain sanctuary. Instead of "
                + "raising their weapons, the travellers presented a recovered royal seal and explained their "
                + "quest. The dragon examined the inscription, then revealed a forgotten passage beneath the "
                + "stone floor. Beyond that passage lay the crystal chamber, where a powerful enchantment "
                + "had protected the kingdom's history for centuries.");
        ultimate.add("During the station's first contact with an unknown spacecraft, the communications officer "
                + "received a sequence of unfamiliar signals. She compared their rhythm with archived "
                + "transmissions while the research team prepared a cautious reply. After several exchanges, "
                + "a shared pattern emerged. The captain authorised a peaceful approach, and both crews "
                + "began planning a meeting inside the station's observation hall.");
        ultimate.add("A coordinated attack disrupted several services across the city's network. The response "
                + "team preserved the audit logs, isolated compromised machines, and checked the integrity "
                + "of its backups. Working together, the analysts identified the entry point and closed the "
                + "vulnerable connection. Only after verifying each recovered service did they restore "
                + "access and document the changes needed to prevent another intrusion.");
        prompts.put(Difficulty.ULTIMATE, ultimate);
    }

    /**
     * Generates random symbols, matching an Ultimate passage's length within six characters.
     * Each space-separated group contains between one and six symbols.
     *
     * @return a freshly generated Encryption passage
     */
    public String getEncryptionPrompt() {
        int targetLength = getRandomPrompt(Difficulty.ULTIMATE).length();
        StringBuilder text = new StringBuilder();
        while (text.length() < targetLength) {
            if (!text.isEmpty()) {
                text.append(' ');
            }
            int groupLength = random.nextInt(6) + 1;
            for (int index = 0; index < groupLength; index++) {
                text.append(ENCRYPTION_CHARACTERS.charAt(random.nextInt(ENCRYPTION_CHARACTERS.length())));
            }
        }
        return text.toString();
    }

    /**
     * Selects a random prompt, avoiding the previous passage for the same difficulty.
     *
     * @param difficulty desired challenge difficulty
     * @return a prompt, or an error message if none is available
     */
    public String getRandomPrompt(Difficulty difficulty) {
        List<String> availablePrompts = prompts.get(difficulty);
        if (availablePrompts == null || availablePrompts.isEmpty()) {
            return "Error loading prompt.";
        }
        Integer previousIndex = previousPromptIndices.get(difficulty);
        boolean excludePrevious = previousIndex != null && availablePrompts.size() > 1;
        int selectedIndex = random.nextInt(availablePrompts.size() - (excludePrevious ? 1 : 0));
        if (excludePrevious && selectedIndex >= previousIndex) {
            selectedIndex++;
        }
        previousPromptIndices.put(difficulty, selectedIndex);
        return availablePrompts.get(selectedIndex);
    }
}
