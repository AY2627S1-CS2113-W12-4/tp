package seedu.keyboardwarrior.encounter;

import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.function.LongSupplier;

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

    /** Health awarded for a matching response. */
    private static final int HEALTH_INCREASE = 10;
    /** Health removed for a nonmatching response. */
    private static final int BASE_DAMAGE = 30;

    /** Command the player must enter; it may contain more than one word. */
    private final String targetWord;
    /** Same master random instance used to select the encounter and its command. */
    private final Random random;
    /**
     * Supplies wall-clock milliseconds; tests can advance time without sleeping.
     */
    private final LongSupplier currentTimeMillis;
    /** Wall-clock time in milliseconds at which preparation began. */
    private long startTime;
    /** Delay before revealing the command, in milliseconds. */
    private long delayMillis;

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
        this(player, ui, targetWord, random, System::currentTimeMillis);
    }

    /**
     * Creates a reaction event with a controllable clock for lifecycle tests.
     *
     * @param player            player whose health changes after the response
     * @param ui                interface for reaction prompts and results
     * @param targetWord        command to enter when revealed
     * @param random            run's shared random instance
     * @param currentTimeMillis supplier of wall-clock milliseconds
     */
    ReactionEncounter(Player player, EventUi ui, String targetWord, Random random, LongSupplier currentTimeMillis) {
        super(player, ui);
        this.targetWord = targetWord;
        this.random = random;
        this.currentTimeMillis = Objects.requireNonNull(currentTimeMillis, "Reaction clock must not be null");
    }

    /**
     * Announces the ambush and draws a delay from 5,000 through 29,999
     * milliseconds.
     *
     * @param factory shared factory; no challenge is generated during preparation
     * @return {@code null} while the event waits to reveal its command
     * @throws IllegalStateException if this event has already been prepared
     */
    @Override
    public TypingChallenge prepareEvent(TypingChallengeFactory factory) {
        requirePhase(EventPhase.NOT_STARTED);
        startTime = currentTimeMillis.getAsLong();
        delayMillis = 5000 + random.nextInt(25000);
        assert delayMillis >= 5000 && delayMillis < 30000 : "Reaction delay must be within its configured range";
        phase = EventPhase.WAITING;
        ui.showReaction();

        return null;
    }

    /**
     * Reveals the command once the waiting period has elapsed.
     * The response uses a two-second base countdown scaled by the factory's
     * difficulty.
     *
     * @param factory run's shared challenge factory
     * @return the newly revealed challenge, or {@code null} before reveal or on
     *         later updates, including updates outside the waiting phase
     */
    @Override
    public TypingChallenge updateEvent(TypingChallengeFactory factory) {
        if (phase == EventPhase.WAITING && currentTimeMillis.getAsLong() - startTime > delayMillis) {
            TypingChallenge challenge = factory.generateCustom(targetWord, 2);
            phase = EventPhase.ACCEPTING_ANSWER;
            ui.showReactionWord(targetWord);

            return challenge;
        }
        return null;
    }

    /**
     * Rewards a matching response with ten health or applies thirty damage
     * otherwise. Healing saturates at the integer health limit without wrapping.
     * Matching ignores case and surrounding whitespace; timing is handled by the
     * caller.
     * A nonmatching response still records victory if the player survives the
     * damage. Lethal damage is clamped at zero health and marks the player as dead.
     *
     * @param input   submitted reaction command
     * @param factory shared factory; unused because this event has no follow-up
     *                challenge
     * @return {@code null}, as this event has finished
     * @throws IllegalStateException if the reaction prompt is not currently
     *                               accepting an answer
     * @throws NullPointerException  if the submitted input is null
     */
    @Override
    public TypingChallenge submitAnswer(String input, TypingChallengeFactory factory) {
        requirePhase(EventPhase.ACCEPTING_ANSWER);
        String answer = Objects.requireNonNull(input, "Reaction answer must not be null").trim();
        // Consume the answer before applying effects so callbacks or failures cannot
        // repeat rewards.
        phase = EventPhase.RESOLVING;
        if (answer.equalsIgnoreCase(targetWord)) {
            int previousHealth = player.getHealth();
            player.setHealth((int) Math.min(Integer.MAX_VALUE, (long) previousHealth + HEALTH_INCREASE));
            ui.showReactionVictory(player.getHealth() - previousHealth, player.getHealth());
            this.outcome.setVictory(true);
        } else {
            player.setHealth(Math.max(0, player.getHealth() - BASE_DAMAGE));
            if (player.getHealth() == 0) {
                player.setIsDead(true);
            }
            ui.showReactionDefeat(BASE_DAMAGE, player.getHealth(), targetWord);
            this.outcome.setVictory(player.getHealth() > 0);
        }

        completeEvent();
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

    /** Saves the chosen command and remaining wait without drawing another delay. */
    @Override
    public EncounterSnapshot snapshot() {
        requireStablePhase();
        long remainingDelay = phase == EventPhase.WAITING
                ? Math.max(0, delayMillis - Math.max(0, currentTimeMillis.getAsLong() - startTime)) : 0;
        return new EncounterSnapshot("reaction", Map.of("phase", phase.name(), "targetWord", targetWord,
                "delayMillis", Long.toString(remainingDelay)));
    }

    /** Resumes a pending ambush or redisplays its active command with a fresh response deadline. */
    @Override
    public TypingChallenge resumeEvent(TypingChallengeFactory factory) {
        if (phase == EventPhase.WAITING) {
            ui.showReaction();
            return null;
        }
        requirePhase(EventPhase.ACCEPTING_ANSWER);
        TypingChallenge challenge = factory.generateCustom(targetWord, 2);
        ui.showReactionWord(targetWord);
        return challenge;
    }

    /** Restarts only the saved remaining wait; elapsed offline time does not consume the ambush delay. */
    void restorePhase(String savedPhase, long remainingDelay) {
        requirePhase(EventPhase.NOT_STARTED);
        phase = EventPhase.valueOf(savedPhase);
        delayMillis = remainingDelay;
        startTime = currentTimeMillis.getAsLong();
    }
}
