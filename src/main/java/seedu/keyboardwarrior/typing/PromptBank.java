package seedu.keyboardwarrior.typing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import seedu.keyboardwarrior.typing.TypingChallenge.Difficulty;

/**
 * PromptBank
 */

public class PromptBank {
    private final HashMap<Difficulty, List<String>> prompts;
    private final Random random;

    public PromptBank() {
        this.prompts = new HashMap<>();
        this.random = new Random();
        initializeBank();
    }

    private void initializeBank() {
        List<String> easy = new ArrayList<>();
        easy.add("The system booted successfully and local services are running on the server.");
        easy.add("A basic firewall blocks bad traffic while allowing good data to pass through.");
        prompts.put(Difficulty.EASY, easy);

        List<String> medium = new ArrayList<>();
        medium.add(
                "To compile Java code from the terminal, you must first verify that the development kit is correctly installed. Then, you can execute the compiler to generate the bytecode class files.");
        prompts.put(Difficulty.MEDIUM, medium);

        List<String> hard = new ArrayList<>();
        hard.add(
                "A buffer overflow occurs when a process attempts to write excess data to a fixed memory block. This vulnerability allows an attacker to execute malicious code, bypass security protocols, and gain root access to the entire system.");
        prompts.put(Difficulty.HARD, hard);

        List<String> ultimate = new ArrayList<>();
        ultimate.add(
                "Advanced persistent threats often utilize polymorphic malware to evade signature-based detection systems. By constantly altering their binary code during execution, these malicious payloads can bypass standard antivirus firewall. To defend against this, security engineers must implement behavioral analysis tools that monitor network traffic anomalies and memory allocation in actual real time.");
        prompts.put(Difficulty.ULTIMATE, ultimate);
    }

    public String getRandomPrompt(Difficulty difficulty) {
        List<String> availablePrompts = prompts.get(difficulty);
        if (availablePrompts == null || availablePrompts.isEmpty()) {
            return "Error loading prompt.";
        }
        return availablePrompts.get(random.nextInt(availablePrompts.size()));
    }
}
