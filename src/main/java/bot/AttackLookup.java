package bot;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by johnunderwood on 7/17/15.
 */
public class AttackLookup {
    private static Map<Integer, Integer> defenseLookup;
    private static Map<Integer, Integer> attackLookup;

    private static void init() {
        if(defenseLookup == null) {
            Map<Integer, Integer> workDefenseLookup = new HashMap<>();
            Map<Integer, Integer> workAttackLookup = new HashMap<>();
            for (int i = 0; i < 1000; i++) {
                int worstCaseAttack = (int) Math.round(((i * Settings.DEFENSE_KILL_RATE) * (1 - Settings.LUCK_FACTOR)));
                workDefenseLookup.put(i, worstCaseAttack);
            }

            for (int key : workDefenseLookup.keySet()) {
                int value = workDefenseLookup.get(key);
                if (!workAttackLookup.containsKey(value) || workAttackLookup.get(value) > key) {
                    workAttackLookup.put(value, key);
                }
            }
            workAttackLookup.put(1, 2);
            defenseLookup = workDefenseLookup;
            attackLookup = workAttackLookup;
        }
    }

    public static int getUnitRequiredToAttack(int defendingUnits) {
        init();
        return attackLookup.get(defendingUnits);
    }

    public static int getUnitRequiredToDefend(int attackingUnits) {
        init();
        return defenseLookup.get(attackingUnits);
    }
}
