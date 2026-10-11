package seedu.keyboardwarrior.ui;

import java.util.ArrayList;
import java.util.Scanner;

import seedu.keyboardwarrior.combat.Enemy;
import seedu.keyboardwarrior.combat.Player;
import seedu.keyboardwarrior.combat.Skill;

public class CombatUi {
    private Scanner scanner;

    public CombatUi() {
        this.scanner = new Scanner(System.in);
    }

    public void showCombatStart(Enemy enemy) {
        System.out.println("\n==== COMBAT STARTED ====");
        System.out.println("A wild " + enemy.getName() + " appears!");
        showEnemyStats(enemy);
    }

    public void showEnemyTurn(Enemy enemy) {
        System.out.println("---- Enemy Turn ----");
        System.out.println(enemy.getName() + " attacks for ");
        showEnemyStats(enemy);
    }

    public void showEnemyStats(Enemy enemy) {
        System.out.println(enemy.getName() + "| HP: " + enemy.getHealth());
    }

    public void showPlayerTurn(Player player, Enemy enemy) {
        System.out.println("\n---- Your Turn ----");
        showPlayerStats(player);
        showPlayerSkills(player);
    }

    public void showPlayerStats(Player player) {
        System.out.println("HP: " + player.getHealth());
    }

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

    public Skill getSkillChoice(ArrayList<Skill> activeSkills) {
        while (true) {
            System.out.println("> ");

            String input = scanner.nextLine().trim();

            for (Skill skill : activeSkills) {
                if (skill.getName().equalsIgnoreCase(input))
                    return skill;
            }

            System.out.println("Invalid Skill. Type the exact name of a skill.");
        }
    }

    public void showTypingPrep() {
        System.out.println("\nGet ready!");
    }

    public String getTypingSubmission(String typingChallenge) {
        System.out.println(typingChallenge + "\n");
        return scanner.nextLine();
    }

    public void showTypingSlow() {
        System.out.println("Too Slow! Attack got blocked!");
    }

    public void showDamage(String name, int damage) {
        System.out.println("Dealt " + damage + " damage to " + name);
    }

    public void showVictory(Enemy enemy) {
        System.out.println("\n" + enemy.getName() + " has been defeated!");
    }

    public void showDefeat() {
        System.out.println("\nYou have been defeated...");
    }
}
