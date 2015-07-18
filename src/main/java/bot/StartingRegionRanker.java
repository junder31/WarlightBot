package bot;

import log.Logger;
import map.Region;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by johnunderwood on 7/17/15.
 */
public class StartingRegionRanker {
    public static final Logger log = new Logger(StartingRegionRanker.class.getSimpleName());
    private final List<Region> pickableRegions;

    public StartingRegionRanker(BotState state) {
        this.pickableRegions = state.getPickableStartingRegions();
    }

    public List<Region> getRankedList() {
        pickableRegions.sort( (r1, r2) -> {
            if( r1.getSuperRegion().getArmiesReward() == 0 ) {
                if( r2.getSuperRegion().getArmiesReward() == 0) {
                    return 0;
                } else {
                    return 1;
                }
            } else if (r2.getSuperRegion().getArmiesReward() == 0 ) {
                return -1;
            } else {
                int turnsToTakeDiff = getTurnsToTakeSR(r1) - getTurnsToTakeSR(r2);
                if(turnsToTakeDiff == 0) {
                    return  r2.getSuperRegion().getArmiesReward() - r1.getSuperRegion().getArmiesReward();
                } else {
                    return turnsToTakeDiff;
                }
            }
        } );

        log.debug("Ranked Starting Regions: %s", pickableRegions);

        return pickableRegions;
    }

    public static int getTurnsToTakeSR(Region region) {
        Set<Region> subRegions = new HashSet<>(region.getSuperRegion().getSubRegions());
        Set<Region> ownedRegions = new HashSet<>();
        ownedRegions.add(region);
        int i = 0;

        while( !ownedRegions.containsAll(subRegions) ) {
            Set<Region> newOwnedRegions = new HashSet<>(ownedRegions);
            for(Region r : ownedRegions) {
                newOwnedRegions.addAll(r.getNeighbors());
            }
            ownedRegions = newOwnedRegions;
            i++;
        }

        return Math.max(i, (int)Math.ceil( (1.0 * getArmiesToTakeSR(region)) / Settings.STARTING_ARMIES_PER_TURN ) );
    }

    public static int getArmiesToTakeSR(Region region) {
        Set<Region> subRegions = new HashSet<>(region.getSuperRegion().getSubRegions());
        int armiesToTake = 0;

        for(Region r : subRegions) {
            if(r.equals(region)) {
                armiesToTake--;
            } else {
                int armiesInRegion = r.isWasteland() ? Settings.WASTELAND_ARMIES : Settings.NORMAL_ARMIES;
                armiesToTake += AttackLookup.getUnitRequiredToAttack(armiesInRegion);
            }
        }

        return armiesToTake;
    }
}
