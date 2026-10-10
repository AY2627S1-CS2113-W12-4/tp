package seedu.keyboardwarrior;

import seedu.keyboardwarrior.typing.ChallengeClock;
import seedu.keyboardwarrior.typing.TypingEvaluator;
import seedu.keyboardwarrior.typing.TypingResult;

/**
 * Represents the state in which the player is playing the game.
 */
public class GamePlayState extends State {
    private static final int CHALLENGE_DURATION_SECONDS = 60;
    private static final String CHALLENGE_TEXT = "The quick brown fox jumps over the lazy dog.";
    private final ChallengeClock clock;
    private final StringBuilder submittedText = new StringBuilder();
    private int lastDisplayedSeconds;

    /**
     * Creates the gameplay state.
     *
     * @param stateMachine the machine that controls this state
     */
    public GamePlayState(StateMachine stateMachine) {
        this(stateMachine, new ChallengeClock());
    }

    /**
     * Accepts a clock so tests can simulate expiry without waiting a full minute.
     */
    GamePlayState(StateMachine stateMachine, ChallengeClock clock) {
        super("Gameplay", stateMachine);
        this.clock = clock;
    }

    @Override
    public void enter() {
        submittedText.setLength(0);
        clock.start(CHALLENGE_DURATION_SECONDS);
        Ui.showGameplayStart();
        Ui.showTypingChallenge(CHALLENGE_TEXT);
        Ui.showRemainingTime(clock.getRemainingSeconds());
        lastDisplayedSeconds = clock.getRemainingSeconds();
    }

    @Override
    public void exit() {
        Ui.clearCountdown();
    }

    @Override
    public void handleInput(String input) {
        if (input.equalsIgnoreCase("exit")) {
            stateMachine.stop();
            return;
        }
        if (finishIfExpired()) {
            return;
        }
        if (!submittedText.isEmpty()) {
            submittedText.append(' ');
        }
        submittedText.append(input);
    }
    
    @Override
    public void update() {
        int remainingSeconds = clock.getRemainingSeconds();
        if (remainingSeconds != lastDisplayedSeconds) {
            Ui.updateCountdown(remainingSeconds);
            lastDisplayedSeconds = remainingSeconds;
        }
        if (finishIfExpired()) {
            Ui.showInputPrompt();
        }
    }

    /**
     * Ends the challenge before processing any input received after the deadline.
     *
     * @return true if gameplay ended
     */
    private boolean finishIfExpired() {
        if (clock.getRemainingSeconds() > 0) {
            return false;
        }
        Ui.showTimeUp();
        // This is a fixed-duration round: score the text collected by the deadline,
        // using the round duration rather than any delay before the next loop update.
        TypingResult result = TypingEvaluator.evaluateTyping(CHALLENGE_TEXT, submittedText.toString(),
                CHALLENGE_DURATION_SECONDS, CHALLENGE_DURATION_SECONDS);
        Ui.showTypingResult(result);
        stateMachine.transitState(new MainMenuState(stateMachine));
        return true;
    }
}
