package seedu.keyboardwarrior.encounter;

import java.util.Objects;

import seedu.keyboardwarrior.combat.Player;
import seedu.keyboardwarrior.combat.Skill;
import seedu.keyboardwarrior.typing.TypingChallenge;
import seedu.keyboardwarrior.typing.TypingChallengeFactory;
import seedu.keyboardwarrior.ui.EventUi;

/**
 * Runs a two-stage merchant conversation with a trade branch and an escape
 * branch.
 * A matching opening response leads to a trade; any other response leads to a
 * guard encounter.
 * Answers ignore case and surrounding whitespace. The caller displays each
 * returned challenge,
 * starts its countdown and enforces its deadline before submitting a response.
 */
public class DialogueEncounter extends EventEncounter {

    /**
     * Health awarded when a successful trade has no skill left to offer.
     */
    private static final int HEALTH_REWARD = 20;
    /**
     * Signed adjustment to future countdowns after an unsuccessful trade response.
     */
    private static final int TIME_PENALTY = -5;
    /** Damage applied when the player fails to escape the guards. */
    private static final int DAMAGE_PENALTY = 20;

    /** Remaining skill rewards shared with other encounters in this player's run. */
    private final RewardPool rewardPool;

    /**
     * Conversation stage: zero for the opening, one for trading, or two for
     * escaping.
     */
    private int dialogueNode = 0;
    /**
     * Target response for the active stage, assigned when its challenge is
     * prepared.
     */
    private String currentExpectedAnswer;

    /**
     * Creates an unstarted merchant conversation using the run's shared reward
     * pool.
     * Construction does not draw a reward or consume randomness.
     *
     * @param player     player whose skills, health and countdown adjustment may
     *                   change
     * @param ui         interface for conversation text and outcome feedback
     * @param rewardPool remaining rewards initialized for this player with the
     *                   run's master random
     * @throws NullPointerException if any dependency is null
     */
    public DialogueEncounter(Player player, EventUi ui, RewardPool rewardPool) {
        super(Objects.requireNonNull(player, "Dialogue player must not be null"),
                Objects.requireNonNull(ui, "Dialogue interface must not be null"));
        this.rewardPool = Objects.requireNonNull(rewardPool, "Dialogue reward pool must not be null");
    }

    /**
     * Displays the merchant's opening demand and prepares the first response.
     * The challenge has a ten-second base countdown scaled by the supplied
     * factory's difficulty;
     * the player's accumulated bonus time is not included.
     * If challenge generation fails, preparation can be retried without advancing the encounter.
     *
     * @param factory run's shared factory for event challenges
     * @return opening response text and its scaled countdown, for the caller to
     *         display and time
     * @throws IllegalStateException if the encounter is no longer in its initial
     *                               phase
     */
    @Override
    public TypingChallenge prepareEvent(TypingChallengeFactory factory) {
        requirePhase(EventPhase.NOT_STARTED);
        String expectedAnswer = "I will not be extorted";
        TypingChallenge challenge = Objects.requireNonNull(factory.generateCustom(expectedAnswer, 10),
                "Dialogue factory must return a challenge");
        assert dialogueNode == 0 : "Preparation must begin at the opening node";
        // Block reentrant input while publishing the successfully generated challenge.
        phase = EventPhase.RESOLVING;
        currentExpectedAnswer = expectedAnswer;
        ui.showDialogueText("A suspicious merchant blocks your path, demanding a toll!");
        ui.showDialogueText("Stand your ground and haggle:");
        phase = EventPhase.ACCEPTING_ANSWER;
        return challenge;
    }

    /**
     * Resolves the current response and either opens the next branch or completes
     * the conversation.
     * A matching opening response starts a ten-second trade challenge; a mismatch
     * starts a
     * five-second escape challenge. Both base countdowns are scaled by the
     * factory's difficulty,
     * without adding the player's accumulated bonus time.
     * A successful trade draws a skill or heals up to twenty health when the pool is empty,
     * saturating at the integer limit. A failed trade reduces future countdowns. Escaping
     * successfully has no player effect; failing to escape applies damage clamped at zero health.
     * Terminal outcomes indicate survival, including a failed trade or nonlethal guard attack.
     * If follow-up challenge generation fails, the opening response can be retried without
     * advancing the node. Terminal responses are consumed before effects or feedback to prevent
     * duplicate rewards on retries. Timing is enforced by the caller.
     *
     * @param input   submitted response, compared without case or surrounding
     *                whitespace
     * @param factory run's shared factory, used when the opening response creates a
     *                follow-up challenge
     * @return next response challenge after the opening, or {@code null} after a
     *         terminal response
     * @throws IllegalStateException    if the encounter is not currently accepting
     *                                  an answer
     * @throws NullPointerException     if the submitted input is null
     */
    @Override
    public TypingChallenge submitAnswer(String input, TypingChallengeFactory factory) {
        requirePhase(EventPhase.ACCEPTING_ANSWER);
        String answer = Objects.requireNonNull(input, "Dialogue answer must not be null").trim();
        assert dialogueNode >= 0 && dialogueNode <= 2 : "Dialogue node must identify a supported stage";
        assert currentExpectedAnswer != null && !currentExpectedAnswer.isBlank()
                : "An active dialogue must have an expected answer";
        if (dialogueNode == 0) {
            return prepareFollowUp(answer.equalsIgnoreCase(currentExpectedAnswer), factory);
        }
        phase = EventPhase.RESOLVING;

        switch (dialogueNode) {
        case 1:
            if (answer.equalsIgnoreCase(currentExpectedAnswer)) {
                Skill reward = rewardPool.popRandomReward();
                if (reward != null) {
                    player.addSkill(reward);
                    ui.showDialogueVictory(reward);
                } else {
                    int previousHealth = player.getHealth();
                    player.setHealth((int) Math.min(Integer.MAX_VALUE, (long) previousHealth + HEALTH_REWARD));
                    assert player.getHealth() >= previousHealth : "Healing must not reduce health";
                    ui.showAltDialogueVictory(player.getHealth() - previousHealth, player.getHealth());
                }
            } else {
                player.addBonusTime(TIME_PENALTY);
                ui.showDialogueDefeat1();
            }
            break;
        case 2:
            if (answer.equalsIgnoreCase(currentExpectedAnswer)) {
                ui.showDialogueNeutral();
            } else {
                player.setHealth(player.getHealth() - DAMAGE_PENALTY);
                if (player.getHealth() == 0) {
                    player.setIsDead(true);
                }
                assert player.getHealth() > 0 || player.getIsDead() : "Lethal guard damage must mark defeat";
                ui.showDialogueDefeat2(DAMAGE_PENALTY, player.getHealth());
            }
            break;
        default:
            throw new IllegalStateException("Unsupported dialogue node: " + dialogueNode);
        }

        this.outcome.setVictory(player.getHealth() > 0);
        completeEvent();
        return null;
    }

    /**
     * Creates the follow-up challenge before committing its node or publishing branch feedback.
     * A factory failure leaves the opening response active and consumes no reward.
     *
     * @param trade whether the opening answer leads to trading rather than escaping
     * @param factory run's shared challenge factory
     * @return prepared trade or escape challenge
     */
    private TypingChallenge prepareFollowUp(boolean trade, TypingChallengeFactory factory) {
        assert dialogueNode == 0 && phase == EventPhase.ACCEPTING_ANSWER
                : "Only an active opening response can choose a branch";
        String expectedAnswer = trade ? "Show me your rare wares" : "flee";
        TypingChallenge challenge = Objects.requireNonNull(factory.generateCustom(expectedAnswer, trade ? 10 : 5),
                "Dialogue factory must return a challenge");
        phase = EventPhase.RESOLVING;
        dialogueNode = trade ? 1 : 2;
        currentExpectedAnswer = expectedAnswer;
        if (trade) {
            ui.showDialogueText("Merchant: 'Fine, fine! How about a trade instead?'");
            ui.showDialogueText("Secure the deal");
        } else {
            ui.showDialogueText("Merchant: 'You stuttered! Guards, attack this cheapskate!'");
            ui.showDialogueText("Dodge the guards:");
        }
        phase = EventPhase.ACCEPTING_ANSWER;
        return challenge;
    }

    /**
     * Returns the merchant label used when this encounter is offered as a choice.
     *
     * @return dialogue encounter label
     */
    @Override
    public String getDisplayText() {
        return "Event: A Suspicious Merchant";
    }
}
