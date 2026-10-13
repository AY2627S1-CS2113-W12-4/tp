package seedu.keyboardwarrior.typing;

import seedu.keyboardwarrior.ui.CombatUi;

/** Presents one typing challenge and evaluates the player's submitted text. */
public class TypingChallengeRunner {

    private String expectedText;
    private int timeLimitSeconds;
    private ChallengeClock clock;
    private CombatUi ui;

    /**
     * Creates a runner that receives submissions through the combat interface.
     *
     * @param ui interface used to show prompts and read input
     */
    public TypingChallengeRunner(CombatUi ui) {
        this(ui, new ChallengeClock());
    }

    /** Accepts a controllable clock so tests can verify timing without waiting. */
    TypingChallengeRunner(CombatUi ui, ChallengeClock clock) {
        this.clock = clock;
        this.ui = ui;
    }

    /**
     * Prepares the runner with a challenge to execute.
     *
     * @param challenge challenge used by the next call to {@link #run()}
     */
    public void init(TypingChallenge challenge) {
        this.expectedText = challenge.getText();
        this.timeLimitSeconds = challenge.getTime();
    }

    /**
     * Presents and evaluates the initialized challenge.
     *
     * @return evaluated typing performance
     */
    public TypingResult run() {
        clock.start(timeLimitSeconds);
        String typedText = ui.getTypingSubmission(expectedText);
        double elapsedSeconds = clock.getElapsedSeconds();
        TypingResult result = TypingEvaluator.evaluateTyping(expectedText, typedText, elapsedSeconds, timeLimitSeconds);
        return result;
    }
}
