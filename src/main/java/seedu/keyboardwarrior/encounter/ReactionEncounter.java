package seedu.keyboardwarrior.encounter;

import java.util.Random;

import seedu.keyboardwarrior.combat.Player;
import seedu.keyboardwarrior.typing.TypingChallenge;
import seedu.keyboardwarrior.typing.TypingChallengeFactory;
import seedu.keyboardwarrior.ui.EventUi;

/**
 * ReactionEncounter
 */
public class ReactionEncounter extends EventEncounter {

    private final String targetWord;
    private final Random random;
    private long startTime;
    private long delayMillis;
    private boolean isPromptActive = false;

    private static final int HEALTH_INCREASE = 10;
    private static final int BASE_DAMAGE = 30;

    public ReactionEncounter(Player player, EventUi ui, String targetWord, Random random) {
        super(player, ui);
        this.targetWord = targetWord;
        this.random = random;
    }

    @Override
    public TypingChallenge prepareEvent(TypingChallengeFactory factory) {
        ui.showReaction();

        startTime = System.currentTimeMillis();
        delayMillis = 5000 + random.nextInt(25000);

        return null;
    }

    @Override
    public TypingChallenge updateEvent(TypingChallengeFactory factory) {
        if (!isPromptActive && System.currentTimeMillis() - startTime > delayMillis) {
            isPromptActive = true;
            ui.showReactionWord(targetWord);

            return factory.generateCustom(targetWord, 2);
        }
        return null;
    }

    @Override
    public TypingChallenge submitAnswer(String input, TypingChallengeFactory factory) {
        if (input.trim().equalsIgnoreCase(targetWord)) {
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

    @Override
    public String getDisplayText() {
        return "Event: A Sudden Ambush";
    }
}
