package seedu.keyboardwarrior.encounter;

import java.util.ArrayList;

import seedu.keyboardwarrior.combat.Enemy;
import seedu.keyboardwarrior.combat.Player;
import seedu.keyboardwarrior.combat.Skill;
import seedu.keyboardwarrior.combat.SkillResolver;
import seedu.keyboardwarrior.ui.CombatUi;

public class CombatEncounter extends Encounter {
    private Player player;
    private Enemy enemy;
    private CombatUi ui;
    private SkillResolver skillResolver;
    // private TypingChallengeRunner challengeRunner;

    public CombatEncounter(Player player, Enemy enemy, CombatUi ui) {
        super();
        this.player = player;
        this.enemy = enemy;
        this.ui = ui;
        this.skillResolver = new SkillResolver();
        // this.challengeRunner = new TypingChallengeRunner();
    }

    @Override
    public void start() {
        ui.showCombatStart(enemy);

        ArrayList<Skill> enemySkills = enemy.getActiveSkills();
        ArrayList<Skill> playerSkills = player.getActiveSkills();

        int enemySkillIndex = 0;

        while (!enemy.getIsDead() && !player.getIsDead()) {
            ui.showPlayerTurn(player, enemy);

            Skill chosenSkill = ui.getSkillChoice(playerSkills);

            ui.showTypingPrep();
            // TypingResult result;
            skillResolver.execute(player, enemy, chosenSkill); // TODO include result

            if (enemy.getHealth() <= 0) {
                enemy.setIsDead(true);
                ui.showVictory(enemy);
                this.isCompleted = true;
                break;
            }

            ui.showEnemyTurn(enemy);
            Skill enemySkill = enemySkills.get(enemySkillIndex);
            enemySkillIndex = (enemySkillIndex + 1) % enemySkills.size();
            skillResolver.execute(enemy, player, enemySkill); // TODO include null

            if (player.getHealth() <= 0) {
                player.setIsDead(true);
                ui.showDefeat();
                this.isCompleted = true;
                break;
            }
        }
    }

    @Override
    public String getDisplayText() {
        return "Combat Encounter: " + enemy.getName();
    }
}
