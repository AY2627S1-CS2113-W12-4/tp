package seedu.keyboardwarrior.combat;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/** Tests signed countdown accumulation and the corrected reward skill name. */
class PlayerTimeBonusTest {
    /** Time changes retain their sign and report only the applied amount at integer limits. */
    @Test
    void addBonusTime_integerBoundaries_saturatesWithoutWrapping() {
        Player player = new Player("Player", 100);
        player.setBonusTime(Integer.MAX_VALUE - 5);
        assertEquals(5, player.addBonusTime(20));
        assertEquals(Integer.MAX_VALUE, player.getBonusTime());
        assertEquals(0, player.addBonusTime(20));

        player.setBonusTime(Integer.MIN_VALUE + 1);
        assertEquals(-1, player.addBonusTime(-2));
        assertEquals(Integer.MIN_VALUE, player.getBonusTime());
        assertEquals(0, player.addBonusTime(-2));

        player.setBonusTime(0);
        assertEquals(Integer.MIN_VALUE, player.addBonusTime(Integer.MIN_VALUE));
        assertEquals(Integer.MAX_VALUE, player.addBonusTime(Integer.MAX_VALUE));
        assertEquals(-1, player.getBonusTime());
    }

    /** The registry ID and the name used for skill selection must spell Overclock consistently. */
    @Test
    void overclock_hasCorrectDisplayName() {
        assertEquals("Overclock", SkillRegistry.getSkill("overclock").getName());
    }
}
