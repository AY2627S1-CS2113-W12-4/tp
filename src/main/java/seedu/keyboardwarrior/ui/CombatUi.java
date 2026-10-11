package seedu.keyboardwarrior.ui;

import java.util.ArrayList;
import java.util.Scanner;

import seedu.keyboardwarrior.combat.Enemy;
import seedu.keyboardwarrior.combat.Player;
import seedu.keyboardwarrior.combat.Skill;

/** Handles console input and output for interactive combat encounters. */
public class CombatUi {
    /** Reads skill choices and typing submissions from standard input. */
    private Scanner scanner;

    /** Creates a combat interface connected to standard input. */
    public CombatUi() {
        this.scanner = new Scanner(System.in);
    }

    /**
     * Announces a new encounter and displays the enemy's initial health.
     *
     * @param enemy enemy entering combat
     */
    public void showCombatStart(Enemy enemy) {
        System.out.println("\n==== COMBAT STARTED ====");
        System.out.println("A wild " + enemy.getName() + " appears!");
        showEnemyStats(enemy);
    }

    /**
     * Announces the enemy turn and displays its current health.
     *
     * @param enemy enemy taking its turn
     */
    public void showEnemyTurn(Enemy enemy) {
        System.out.println("---- Enemy Turn ----");
        System.out.println(enemy.getName() + " attacks for ");
        showEnemyStats(enemy);
    }

    /**
     * Displays an enemy's name and current health.
     *
     * @param enemy enemy whose statistics are displayed
     */
    public void showEnemyStats(Enemy enemy) {
        System.out.println(enemy.getName() + "| HP: " + enemy.getHealth());
    }

    /**
     * Announces the player turn and lists the player's status and skills.
     *
     * @param player player taking their turn
     * @param enemy opposing enemy
     */
    public void showPlayerTurn(Player player, Enemy enemy) {
        System.out.println("\n---- Your Turn ----");
        showPlayerStats(player);
        showPlayerSkills(player);
    }

    /**
     * Displays the player's current health.
     *
     * @param player player whose health is displayed
     */
    public void showPlayerStats(Player player) {
        System.out.println("HP: " + player.getHealth());
    }

    /**
     * Displays the names of every skill currently available to the player.
     *
     * @param player player whose skills are displayed
     */
    public void showPlayerSkills(Player player) {
        ArrayList<Skill> activeSkills = player.getActiveSkills();
        StringBuilder skillString = new StringBuilder();
        for (int i = 0; i < activeSkills.size(); i++) {
            skillString.append(activeSkills.get(i).getName());
            if (i < activeSkills.size() - 1) {
                skillString.append(" | ");
            }
        }
        System.out.println(skillString.toString());
    }

    /**
     * Reads input until it matches the display name of an available skill.
     *
     * @param activeSkills skills the player may select
     * @return selected skill
     */
    public Skill getSkillChoice(ArrayList<Skill> activeSkills) {
        while (true) {
            System.out.println("> ");

            String input = scanner.nextLine().trim();

            for (Skill skill : activeSkills) {
                if (skill.getName().equalsIgnoreCase(input)) {
                    return skill;
                }
            }

            System.out.println("Invalid Skill. Type the exact name of a skill.");
        }
    }

    /** Tells the player to prepare for the typing challenge. */
    public void showTypingPrep() {
        System.out.println("\nGet ready!");
    }

    /**
     * Displays a typing prompt and reads one submitted line.
     *
     * @param typingChallenge text the player must type
     * @return submitted text
     */
    public String getTypingSubmission(String typingChallenge) {
        System.out.println(typingChallenge + "\n");
        return scanner.nextLine();
    }

    /** Explains that an overdue typing attempt caused the attack to be blocked. */
    public void showTypingSlow() {
        System.out.println("Too Slow! Attack got blocked!");
    }

    /**
     * Reports damage applied to a combat participant.
     *
     * @param name name of the damaged participant
     * @param damage damage applied
     */
    public void showDamage(String name, int damage) {
        System.out.println("Dealt " + damage + " damage to " + name);
    }

    /**
     * Announces that the enemy was defeated.
     *
     * @param enemy defeated enemy
     */
    public void showVictory(Enemy enemy) {
        System.out.println("\n" + enemy.getName() + " has been defeated!");
    }

    /** Announces that the player was defeated. */
    public void showDefeat() {
        System.out.println("\nYou have been defeated...");
    }
}
