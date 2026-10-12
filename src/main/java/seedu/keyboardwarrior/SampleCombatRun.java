package seedu.keyboardwarrior;

import java.util.Random;

import seedu.keyboardwarrior.combat.Enemy;
import seedu.keyboardwarrior.combat.EnemyFactory;
import seedu.keyboardwarrior.combat.Player;
import seedu.keyboardwarrior.encounter.CombatEncounter;
import seedu.keyboardwarrior.ui.CombatUi;

/** Demonstrates how to create and run a single interactive combat encounter. */
public class SampleCombatRun {

    /**
     * Creates a player and a random enemy, then runs combat to completion.
     *
     * @param args command-line arguments; currently unused
     */
    public static void main(String[] args) {
        Random random = new Random();

        CombatUi ui = new CombatUi();
        Player player = new Player("Player", 100);

        System.out.println("THIS IS A SINGLE SAMPLE COMBAT RUN");

        String[] enemyNames = { "rogue_ai", "malware_bot" };
        Enemy enemy = EnemyFactory.createEnemy(enemyNames[random.nextInt(2)]);

        CombatEncounter encounter = new CombatEncounter(player, enemy, ui);

        encounter.start();

        if (player.getIsDead()) {
            System.out.println("You lost");
        } else {
            System.out.println("You win");
        }
    }
}
