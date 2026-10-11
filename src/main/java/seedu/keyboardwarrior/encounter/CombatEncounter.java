package seedu.keyboardwarrior.encounter;

import java.util.ArrayList;

import seedu.keyboardwarrior.combat.Enemy;
import seedu.keyboardwarrior.combat.Player;
import seedu.keyboardwarrior.combat.Skill;
import seedu.keyboardwarrior.combat.SkillResolver;
import seedu.keyboardwarrior.typing.TypingChallenge;
import seedu.keyboardwarrior.typing.TypingChallenge.Difficulty;
import seedu.keyboardwarrior.typing.TypingChallengeFactory;
import seedu.keyboardwarrior.typing.TypingChallengeRunner;
import seedu.keyboardwarrior.typing.TypingResult;
import seedu.keyboardwarrior.ui.CombatUi;

/**
 * Runs turn-based combat between a player and an enemy.
 * Player skills are resolved through typing challenges, while enemy skills apply
 * their base damage automatically.
 */
public class CombatEncounter extends Encounter {
    private Player player;
    private Enemy enemy;
    private CombatUi ui;
    private SkillResolver skillResolver;
    private TypingChallengeRunner challengeRunner;
    private TypingChallengeFactory challengeFactory;

    /**
     * Creates a combat encounter for the supplied participants and interface.
     *
     * @param player player taking part in the encounter
     * @param enemy enemy taking part in the encounter
     * @param ui interface used for combat input and output
     */
    public CombatEncounter(Player player, Enemy enemy, CombatUi ui) {
        super();
        this.player = player;
        this.enemy = enemy;
        this.ui = ui;
        this.skillResolver = new SkillResolver(ui);
        this.challengeRunner = new TypingChallengeRunner(ui);
        this.challengeFactory = new TypingChallengeFactory();
    }

    /**
     * Alternates player and enemy turns until one participant is defeated.
     */
    @Override
    public void start() {
        ui.showCombatStart(enemy);

        ArrayList<Skill> enemySkills = enemy.getActiveSkills();
        ArrayList<Skill> playerSkills = player.getActiveSkills();

        int enemySkillIndex = 0;

        while (!enemy.getIsDead() && !player.getIsDead()) {
            executePlayerTurn(playerSkills);

            if (enemy.getHealth() <= 0) {
                executeVictory();
                break;
            }

            executeEnemyTurn(enemySkills, enemySkillIndex);
            enemySkillIndex = (enemySkillIndex + 1) % enemySkills.size();

            if (player.getHealth() <= 0) {
                executeDefeat();
                break;
            }
        }
    }

    /** Runs skill selection, the typing challenge and damage resolution for the player. */
    private void executePlayerTurn(ArrayList<Skill> playerSkills) {
        ui.showPlayerTurn(player, enemy);

        Skill chosenSkill = ui.getSkillChoice(playerSkills);

        ui.showTypingPrep();

        Difficulty difficulty = chosenSkill.getDifficulty();
        TypingChallenge challenge = challengeFactory.generate(difficulty);

        challengeRunner.init(challenge);
        TypingResult result = challengeRunner.run();
        skillResolver.execute(player, enemy, chosenSkill, result);
    }

    /** Uses the enemy's next skill without requiring a typing challenge. */
    private void executeEnemyTurn(ArrayList<Skill> enemySkills, int enemySkillIndex) {
        ui.showEnemyTurn(enemy);
        Skill enemySkill = enemySkills.get(enemySkillIndex);
        skillResolver.execute(enemy, player, enemySkill, null);
    }

    /** Marks the enemy as defeated and completes the encounter as a victory. */
    private void executeVictory() {
        enemy.setIsDead(true);
        ui.showVictory(enemy);

        this.outcome.setVictory(true);
        this.isCompleted = true;
    }

    /** Marks the player as defeated and completes the encounter as a loss. */
    private void executeDefeat() {
        player.setIsDead(true);
        ui.showDefeat();

        this.outcome.setVictory(false);
        this.isCompleted = true;
    }

    /**
     * Returns a short description containing the opposing enemy's name.
     *
     * @return combat encounter description
     */
    @Override
    public String getDisplayText() {
        return "Combat Encounter: " + enemy.getName();
    }
}
