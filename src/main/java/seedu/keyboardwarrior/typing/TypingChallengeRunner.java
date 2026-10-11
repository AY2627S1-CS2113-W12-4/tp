package seedu.keyboardwarrior.typing;

import seedu.keyboardwarrior.ui.CombatUi;

/**
 * TypingChallengeRunner
 */
public class TypingChallengeRunner {

    private String expectedText;
    private int timeLimitSeconds;
    private ChallengeClock clock;
    private CombatUi ui;

    public TypingChallengeRunner(CombatUi ui) {
        this.clock = new ChallengeClock();
        this.ui = ui;
    }

    public void init(TypingChallenge challenge) {
        this.expectedText = challenge.getText();
        this.timeLimitSeconds = challenge.getTime();
    }

    public TypingResult run() {
        clock.start(timeLimitSeconds);
        String typedText = ui.getTypingSubmission(expectedText);
        double elapsedSeconds = clock.getElapsedSeconds();
        TypingResult result = TypingEvaluator.evaluateTyping(expectedText, typedText, elapsedSeconds, timeLimitSeconds);
        return result;
    }
}
