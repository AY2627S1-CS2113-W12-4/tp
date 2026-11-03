package seedu.keyboardwarrior.ui;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.jupiter.api.parallel.Resources;

import seedu.keyboardwarrior.combat.SkillRegistry;

/** Checks player-visible dialogue reward feedback. */
class EventUiTest {
    /** A skill reward must identify which skill the player received. */
    @Test
    @ResourceLock(Resources.SYSTEM_OUT)
    void showDialogueVictory_awardedSkill_displaysItsName() {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream originalOutput = System.out;
        try (PrintStream capture = new PrintStream(output, true, StandardCharsets.UTF_8)) {
            System.setOut(capture);
            new EventUi().showDialogueVictory(SkillRegistry.getSkill("overclock"));
        } finally {
            System.setOut(originalOutput);
        }
        assertTrue(output.toString(StandardCharsets.UTF_8).contains("You have received: Overclock!"));
    }
}
