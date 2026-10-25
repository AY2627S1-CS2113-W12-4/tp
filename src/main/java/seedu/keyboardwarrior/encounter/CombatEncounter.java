package seedu.keyboardwarrior.encounter;

import java.util.ArrayList;
import java.util.Objects;
import java.util.logging.Logger;

import seedu.keyboardwarrior.combat.Enemy;
import seedu.keyboardwarrior.combat.Player;
import seedu.keyboardwarrior.combat.Skill;
import seedu.keyboardwarrior.combat.SkillResolver;
import seedu.keyboardwarrior.typing.TypingChallenge;
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
    /** Diagnostic encounter lifecycle events, hidden at the default logging level. */
    private static final Logger LOGGER = Logger.getLogger(CombatEncounter.class.getName());
    private Player player;
    private Enemy enemy;
    private CombatUi ui;
    private SkillResolver skillResolver;
    private TypingChallengeRunner challengeRunner;
    private TypingChallengeFactory challengeFactory;
    /** Skill awaiting a typing result from the current player turn. */
    private Skill pendingSkill;
    /** Index of the enemy skill to use on its next turn. */
    private int enemySkillIndex;
    /** Whether combat has begun; prevents attacks before setup and repeated starts. */
    private boolean isStarted;

    /**
     * Creates a combat encounter for the supplied participants and interface.
     *
     * @param player player taking part in the encounter
     * @param enemy enemy taking part in the encounter
     * @param ui interface used for combat input and output
     * @throws NullPointerException if a participant or interface is null
     */
    public CombatEncounter(Player player, Enemy enemy, CombatUi ui) {
        this(player, enemy, ui, new TypingChallengeFactory());
    }

    /**
     * Creates an encounter using a prompt factory shared by the current run.
     *
     * @param player player taking part in the encounter
     * @param enemy enemy taking part in the encounter
     * @param ui combat interface
     * @param challengeFactory factory retaining prompt selection history between encounters
     * @throws NullPointerException if any dependency is null
     */
    public CombatEncounter(Player player, Enemy enemy, CombatUi ui, TypingChallengeFactory challengeFactory) {
        super();
        this.player = Objects.requireNonNull(player, "Player must not be null");
        this.enemy = Objects.requireNonNull(enemy, "Enemy must not be null");
        this.ui = Objects.requireNonNull(ui, "Combat interface must not be null");
        this.skillResolver = new SkillResolver(ui);
        this.challengeRunner = new TypingChallengeRunner(ui);
        this.challengeFactory = Objects.requireNonNull(challengeFactory, "Challenge factory must not be null");
    }

    /**
     * Alternates player and enemy turns until one participant is defeated.
     */
    @Override
    public void start() {
        begin();
        while (!isCompleted) {
            Skill chosenSkill = ui.getSkillChoice(player.getActiveSkills());
            TypingChallenge challenge = preparePlayerTurn(chosenSkill);
            challengeRunner.init(challenge);
            submitTypingResult(challengeRunner.run());
        }
    }

    /**
     * Announces combat and the first player turn without reading console input.
     * The application can then collect input while continuing timer updates.
     *
     * @throws IllegalStateException if combat already began or a living enemy cannot take a turn
     */
    public void begin() {
        if (isStarted) {
            throw new IllegalStateException("Combat has already begun.");
        }
        assert !isCompleted : "Combat must be incomplete before it begins";
        assert pendingSkill == null && enemySkillIndex == 0 : "New combat must start with a clean turn state";
        if (player.getHealth() > 0 && enemy.getHealth() > 0) {
            requireEnemySkills();
        }
        isStarted = true;
        LOGGER.fine(() -> "Beginning combat against " + enemy.getName());
        ui.showCombatStart(enemy);
        if (player.getHealth() <= 0) {
            executeDefeat();
        } else if (enemy.getHealth() <= 0) {
            executeVictory();
        } else {
            ui.showPlayerTurn(player, enemy);
        }
    }

    /**
     * Prepares a chosen skill's challenge without blocking for a submission.
     *
     * @param chosenSkill skill selected by the player
     * @return challenge to display and time before submitting its result
     * @throws NullPointerException if the chosen skill is null
     * @throws IllegalArgumentException if the player does not have the chosen skill
     * @throws IllegalStateException if combat has not begun, ended or a result is already pending
     */
    public TypingChallenge preparePlayerTurn(Skill chosenSkill) {
        if (!isStarted || isCompleted || pendingSkill != null) {
            throw new IllegalStateException("Cannot prepare a turn before combat, after it ended or while pending.");
        }
        Objects.requireNonNull(chosenSkill, "Chosen skill must not be null");
        if (!player.getActiveSkills().contains(chosenSkill)) {
            throw new IllegalArgumentException("Chosen skill must be available to the player.");
        }
        assert player.getHealth() > 0 && enemy.getHealth() > 0 : "A player turn requires living participants";
        TypingChallenge challenge = challengeFactory.generateForSkill(chosenSkill);
        assert challenge != null : "The challenge factory must generate a challenge";
        ui.showTypingPrep();
        pendingSkill = chosenSkill;
        return challenge;
    }

    /**
     * Resolves the pending attack and the enemy response, then opens the next turn.
     * A defeated enemy does not retaliate, and a late attempt still consumes a turn.
     *
     * @param result evaluated result of the pending typing challenge
     * @throws NullPointerException if the result is null
     * @throws IllegalStateException if there is no pending player turn
     */
    public void submitTypingResult(TypingResult result) {
        if (pendingSkill == null || isCompleted) {
            throw new IllegalStateException("No player turn is awaiting a typing result.");
        }
        Objects.requireNonNull(result, "Typing result must not be null");
        assert player.getHealth() > 0 && enemy.getHealth() > 0 : "A pending attack requires living participants";
        skillResolver.execute(player, enemy, pendingSkill, result);
        pendingSkill = null;

        if (enemy.getHealth() <= 0) {
            executeVictory();
            return;
        }

        executeEnemyTurn();
        if (player.getHealth() <= 0) {
            executeDefeat();
            return;
        }
        ui.showPlayerTurn(player, enemy);
    }

    /** Uses the enemy's next skill without requiring a typing challenge. */
    private void executeEnemyTurn() {
        assert isStarted && !isCompleted : "An enemy turn requires active combat";
        assert pendingSkill == null : "The player attack must resolve before the enemy turn";
        assert player.getHealth() > 0 && enemy.getHealth() > 0 : "An enemy turn requires living participants";
        requireEnemySkills();
        ArrayList<Skill> enemySkills = enemy.getActiveSkills();
        assert enemySkillIndex >= 0 && enemySkillIndex < enemySkills.size()
                : "Enemy skill index must identify an available skill";
        ui.showEnemyTurn(enemy);
        Skill enemySkill = enemySkills.get(enemySkillIndex);
        skillResolver.execute(enemy, player, enemySkill, null);
        enemySkillIndex = (enemySkillIndex + 1) % enemySkills.size();
        assert enemySkillIndex >= 0 && enemySkillIndex < enemySkills.size()
                : "Enemy skill rotation must remain within the loadout";
    }

    /** Rejects unusable enemy loadouts before a turn can partially execute. */
    private void requireEnemySkills() {
        ArrayList<Skill> enemySkills = enemy.getActiveSkills();
        if (enemySkills.isEmpty() || enemySkills.contains(null) || enemySkillIndex >= enemySkills.size()) {
            throw new IllegalStateException("Enemy must have valid skills for its next turn.");
        }
    }

    /** Marks the enemy as defeated and completes the encounter as a victory. */
    private void executeVictory() {
        assert enemy.getHealth() == 0 : "Victory requires the enemy to have no health";
        assert player.getHealth() > 0 : "The player must be alive on victory";
        assert pendingSkill == null : "Victory must follow a resolved attack";
        assert !isCompleted : "Victory must only complete an active encounter";
        LOGGER.fine(() -> "Player defeated " + enemy.getName());
        enemy.setIsDead(true);
        ui.showVictory(enemy);

        this.outcome.setVictory(true);
        this.isCompleted = true;
        assert enemy.getIsDead() && outcome.getIsVictory() && isCompleted
                : "Victory must finalize the enemy and encounter state";
    }

    /** Marks the player as defeated and completes the encounter as a loss. */
    private void executeDefeat() {
        assert player.getHealth() == 0 : "Defeat requires the player to have no health";
        assert pendingSkill == null : "Defeat must follow a resolved attack";
        assert !isCompleted : "Defeat must only complete an active encounter";
        LOGGER.fine(() -> "Player was defeated by " + enemy.getName());
        player.setIsDead(true);
        ui.showDefeat();

        this.outcome.setVictory(false);
        this.isCompleted = true;
        assert player.getIsDead() && !outcome.getIsVictory() && isCompleted
                : "Defeat must finalize the player and encounter state";
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
