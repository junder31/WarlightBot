package bot;

import log.Logger;
import map.SuperRegion;

import java.util.ArrayList;
import java.util.List;
import static bot.Settings.*;

/**
 * Created by johnunderwood on 7/11/15.
 */
public class AttackSuperRegionRanker {
    private static Logger log = new Logger(AttackSuperRegionRanker.class.getSimpleName());

    private final String myName;
    private final BotState state;

    public AttackSuperRegionRanker(BotState state) {
        this.state = state;
        this.myName = state.getMyPlayerName();
        log.debug("Wastelands: %s", state.getWasteLands());
    }

    public List<SuperRegion> getRankedSuperRegions() {
        List<SuperRegion> superRegions = new ArrayList<>(state.getVisibleGameBoard().getSuperRegions());
        superRegions.sort((sr1, sr2) -> {
            if( sr1.getArmiesReward() == 0 ) {
                if( sr2.getArmiesReward() == 0) {
                    return 0;
                } else {
                    return 1;
                }
            } else if (sr2.getArmiesReward() == 0 ) {
                return -1;
            } else {
                int armiesToTakeDif = getEnemyArmiesInSuperRegion(sr1) - getEnemyArmiesInSuperRegion(sr2);
                if( armiesToTakeDif == 0 ) {
                    return sr2.getArmiesReward() - sr1.getArmiesReward();
                } else {
                    return armiesToTakeDif;
                }
            }
        } );
        log.debug("RankedSuperRegions: %s", superRegions);
        return superRegions;
    }

    public int getEnemyArmiesInSuperRegion(SuperRegion superRegion) {
        int superRegionArmyCount = superRegion.getSubRegions().stream()
                .filter(r -> !r.ownedByPlayer(myName))
                .mapToInt(r -> {
                    log.trace("Getting army count for Region: %s", r);
                    if (r.getArmies() == 0) {
                        return state.getWasteLands().contains(r) ? WASTELAND_ARMIES : NORMAL_ARMIES;
                    } else {
                        return r.ownedByEnemyOfPlayer(myName) ?
                                (int)Math.floor(r.getArmies() * ENEMY_OWNERSHIP_FACTOR) : r.getArmies();
                    }
                }).sum();
        log.trace("Counted %d armies in superRegion %s", superRegionArmyCount, superRegion);
        return superRegionArmyCount;
    }
}
