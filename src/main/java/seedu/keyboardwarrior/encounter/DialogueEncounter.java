package seedu.keyboardwarrior.encounter;

import java.util.Objects;

import seedu.keyboardwarrior.combat.Player;
import seedu.keyboardwarrior.combat.Skill;
import seedu.keyboardwarrior.typing.TypingChallenge;
import seedu.keyboardwarrior.typing.TypingChallengeFactory;
import seedu.keyboardwarrior.ui.EventUi;

/**
 * DialogueEncounter
 */
public class DialogueEncounter extends EventEncounter {

    private final RewardPool rewardPool;

    private static final int HEALTH_REWARD = 20;
    private static final int TIME_PENALTY = -5;
    private static final int DAMAGE_PENALTY = 20;

    private int dialogueNode = 0;
    private String currentExpectedAnswer;

    public DialogueEncounter(Player player, EventUi ui, RewardPool rewardPool) {
        super(player, ui);
        this.rewardPool = rewardPool;
    }

    @Override
    public TypingChallenge prepareEvent(TypingChallengeFactory factory) {
        requirePhase(EventPhase.NOT_STARTED);

        ui.showDialogueText("A suspicious merchant blocks your path, demanding a toll!");
        ui.showDialogueText("Stand your ground and haggle:");

        currentExpectedAnswer = "I will not be extorted";

        phase = EventPhase.ACCEPTING_ANSWER;
        return factory.generateCustom(currentExpectedAnswer, 10);
    }

    @Override
    public TypingChallenge submitAnswer(String input, TypingChallengeFactory factory) {
        requirePhase(EventPhase.ACCEPTING_ANSWER);
        String answer = Objects.requireNonNull(input, "Dialogue answer must not be null").trim();

        phase = EventPhase.RESOLVING;

        switch (dialogueNode) {
        case 0:
            if (answer.equalsIgnoreCase(currentExpectedAnswer)) {
                dialogueNode = 1;
                ui.showDialogueText("Merchant: 'Fine, fine! How about a trade instead?'");
                ui.showDialogueText("Secure the deal");

                currentExpectedAnswer = "Show me your rare wares";
                phase = EventPhase.ACCEPTING_ANSWER;

                return factory.generateCustom(currentExpectedAnswer, 10);
            } else {
                dialogueNode = 2;
                ui.showDialogueText("Merchant: 'You stuttered! Guards, attack this cheapskate!'");
                ui.showDialogueText("Dodge the guards:");

                currentExpectedAnswer = "flee";
                phase = EventPhase.ACCEPTING_ANSWER;

                return factory.generateCustom(currentExpectedAnswer, 5);
            }
        case 1:
            if (answer.equalsIgnoreCase(currentExpectedAnswer)) {
                Skill reward = rewardPool.popRandomReward();
                if (reward != null) {
                    player.addSkill(reward);
                    ui.showDialogueVictory(reward);
                } else {
                    player.setHealth((int) Math.min(Integer.MAX_VALUE, (long) player.getHealth()));
                    ui.showAltDialogueVictory(HEALTH_REWARD, player.getHealth());
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
                ui.showDialogueDefeat2(DAMAGE_PENALTY, player.getHealth());
                if (player.getHealth() == 0) {
                    player.setIsDead(true);
                }
            }
            this.outcome.setVictory(player.getHealth() > 0);
            break;
        }

        completeEvent();
        return null;
    }

    @Override
    public String getDisplayText() {
        return "Event: A Suspicious Merchant";
    }
}
