package bot;

import map.Region;
import map.SuperRegion;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by johnunderwood on 7/17/15.
 */
public class SuperRegionAttackStrategy {
    private final int armiesPerTurn;
    private final String myName;
    private final SuperRegion superRegion;
    private int armies;
    private int turns;

    public SuperRegionAttackStrategy(SuperRegion superRegion, String myName, int armiesPerTurn) {
        this.armiesPerTurn = armiesPerTurn;
        this.myName = myName;
        this.superRegion = superRegion;
        deviseStrategy();
    }

    private void deviseStrategy() {
        List<Region> ownedRegions = superRegion.getSubRegions().stream()
                .filter(r -> r.ownedByPlayer(myName)).collect(Collectors.toList());
        int recruitedArmies = 0;
        armies = 0;
        turns = 0;

        while (!ownedRegions.containsAll(superRegion.getSubRegions())) {
            recruitedArmies += armiesPerTurn;
            List<Region> conqueredRegions = new ArrayList<>();
            for (Region region : ownedRegions) {

            }
        }
    }
}
