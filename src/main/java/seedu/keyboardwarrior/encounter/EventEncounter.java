package seedu.keyboardwarrior.encounter;

import seedu.keyboardwarrior.combat.Player;
import seedu.keyboardwarrior.typing.TypingChallenge;
import seedu.keyboardwarrior.typing.TypingChallengeFactory;
import seedu.keyboardwarrior.ui.EventUi;

/**
 * Base class for noncombat encounters driven by preparation, input and periodic updates.
 * A returned challenge starts a timed input phase; {@code null} means no new challenge.
 * Callers must inspect completion separately because a waiting event also returns {@code null}.
 */
public abstract class EventEncounter extends Encounter {
    /** Stages that control when preparation, prompt activation and answer resolution are allowed. */
    protected enum EventPhase {
        /** Preparation has not yet succeeded. */
        NOT_STARTED,
        /** Preparation succeeded, but the event has not revealed its prompt. */
        WAITING,
        /** A challenge is active and one answer may be submitted. */
        ACCEPTING_ANSWER,
        /** An answer has been consumed and its effects are being applied. */
        RESOLVING,
        /** The result and effects have been recorded. */
        COMPLETED
    }

    /** Player whose health, skills or countdown adjustment can be affected by the event. */
    protected final Player player;
    /** Interface for event-specific prompts and results. */
    protected final EventUi ui;
    /** Current lifecycle stage, including the interval before resolution finishes. */
    protected EventPhase phase = EventPhase.NOT_STARTED;

    /**
     * Creates an incomplete event for the supplied player and interface.
     *
     * @param player nonnull player participating in the event
     * @param ui nonnull interface used to display event feedback
     */
    public EventEncounter(Player player, EventUi ui) {
        super();
        this.player = player;
        this.ui = ui;
    }

    /**
     * Displays the event and prepares its first input phase or waiting period.
     *
     * @param factory run's shared factory for event countdowns
     * @return the initial challenge, or {@code null} while waiting for a later update
     * @throws IllegalStateException if this event has already been prepared
     */
    public abstract TypingChallenge prepareEvent(TypingChallengeFactory factory);

    /**
     * Resolves a submitted answer and applies the event's effects.
     * The caller manages the challenge deadline; this API supplies no elapsed time.
     *
     * @param input nonnull submitted answer
     * @param factory run's shared factory for any subsequent challenge
     * @return a subsequent challenge, or {@code null} when no new challenge is needed
     * @throws IllegalStateException if the event is not currently accepting an answer
     * @throws NullPointerException if the submitted input is null
     */
    public abstract TypingChallenge submitAnswer(String input, TypingChallengeFactory factory);

    /**
     * Advances an event that is waiting before its next input phase.
     * Events with no delayed phase inherit this no-op implementation.
     * Updates before preparation, after prompt activation or after completion do nothing.
     *
     * @param factory run's shared factory for any newly activated challenge
     * @return a newly activated challenge, or {@code null} if no challenge starts
     */
    public TypingChallenge updateEvent(TypingChallengeFactory factory) {
        return null;
    }

    /**
     * Rejects a lifecycle operation before it can change player state or consume randomness.
     *
     * @param expected phase required by the operation
     * @throws IllegalStateException if the event is in a different phase
     */
    protected final void requirePhase(EventPhase expected) {
        if (phase != expected) {
            throw new IllegalStateException("Event must be in phase " + expected + ", but is " + phase + ".");
        }
    }

    /** Marks an already consumed answer as fully resolved and exposes its outcome. */
    protected final void completeEvent() {
        requirePhase(EventPhase.RESOLVING);
        phase = EventPhase.COMPLETED;
        isCompleted = true;
    }
}
