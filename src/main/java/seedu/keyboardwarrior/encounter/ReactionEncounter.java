package seedu.keyboardwarrior.encounter;

import java.util.Random;

import seedu.keyboardwarrior.combat.Player;
import seedu.keyboardwarrior.typing.TypingChallenge;
import seedu.keyboardwarrior.typing.TypingChallengeFactory;
import seedu.keyboardwarrior.ui.EventUi;

/**
 * Waits for a random delay before presenting a reaction command, then rewards
 * or damages the player.
 * The caller is responsible for polling updates and enforcing the returned
 * challenge's deadline.
 */
public class ReactionEncounter extends EventEncounter {

    /** Command the player must enter; it may contain more than one word. */
    private final String targetWord;
    /** Same master random instance used to select the encounter and its command. */
    private final Random random;
    /** Wall-clock time in milliseconds at which preparation began. */
    private long startTime;
    /** Delay before revealing the command, in milliseconds. */
    private long delayMillis;
    /**
     * Whether an update has already revealed the command and returned its
     * challenge.
     */
    private boolean isPromptActive = false;

    /** Health awarded for a matching response. */
    private static final int HEALTH_INCREASE = 10;
    /** Health removed for a nonmatching response. */
    private static final int BASE_DAMAGE = 30;

    /**
     * Creates a reaction event without consuming randomness until preparation.
     *
     * @param player     player whose health changes after the response
     * @param ui         interface for reaction prompts and results
     * @param targetWord command to enter when revealed
     * @param random     nonnull master random instance shared with the encounter
     *                   factory
     */
    public ReactionEncounter(Player player, EventUi ui, String targetWord, Random random) {
        super(player, ui);
        this.targetWord = targetWord;
        this.random = random;
    }

    /**
     * Announces the ambush and draws a delay from 5,000 through 29,999
     * milliseconds.
     *
     * @param factory shared factory; no challenge is generated during preparation
     * @return {@code null} while the event waits to reveal its command
     */
    @Override
    public TypingChallenge prepareEvent(TypingChallengeFactory factory) {
        ui.showReaction();

        startTime = System.currentTimeMillis();
        delayMillis = 5000 + random.nextInt(25000);
        assert delayMillis >= 5000 && delayMillis < 30000 : "Reaction delay must be within its configured range";

        return null;
    }

    /**
     * Reveals the command once the waiting period has elapsed.
     * The response uses a two-second base countdown scaled by the factory's
     * difficulty.
     *
     * @param factory run's shared challenge factory
     * @return the newly revealed challenge, or {@code null} before reveal or on
     *         later updates
     */
    @Override
    public TypingChallenge updateEvent(TypingChallengeFactory factory) {
        if (!isPromptActive && System.currentTimeMillis() - startTime > delayMillis) {
            isPromptActive = true;
            ui.showReactionWord(targetWord);

            return factory.generateCustom(targetWord, 2);
        }
        return null;
    }

    /**
     * Rewards a matching response with ten health or applies thirty damage
     * otherwise.
     * Matching ignores case and surrounding whitespace; timing is handled by the
     * caller.
     * A nonmatching response still records victory if the player survives the
     * damage.
     *
     * @param input   submitted reaction command
     * @param factory shared factory; unused because this event has no follow-up
     *                challenge
     * @return {@code null}, as this event has finished
     * @throws IllegalArgumentException if the damage would set the player's health
     *                                  below zero
     */
    @Override
    public TypingChallenge submitAnswer(String input, TypingChallengeFactory factory) {
        if (input.trim().equalsIgnoreCase(targetWord)) {
            assert (long) player.getHealth() + HEALTH_INCREASE <= Integer.MAX_VALUE
                    : "Reaction rewards must not overflow player health";
            player.setHealth(player.getHealth() + HEALTH_INCREASE);
            ui.showReactionVictory(HEALTH_INCREASE, player.getHealth());
            this.outcome.setVictory(true);
        } else {
            player.setHealth(player.getHealth() - BASE_DAMAGE);
            ui.showReactionDefeat(BASE_DAMAGE, player.getHealth(), targetWord);
            this.outcome.setVictory(player.getHealth() > 0);
        }

        this.isCompleted = true;
        return null;
    }

    /**
     * Returns the label shown when this event is offered.
     *
     * @return reaction encounter label
     */
    @Override
    public String getDisplayText() {
        return "Event: A Sudden Ambush";
    }
}
