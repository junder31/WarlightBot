package bot;

import log.Logger;
import map.SuperRegion;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by johnunderwood on 7/11/15.
 */
public class AttackSuperRegionRanker {
    private static Logger log = new Logger(AttackSuperRegionRanker.class.getSimpleName());
    private final int WASTELAND_ARMIES = 6;
    private final int NORMAL_ARMIES = 2;
    private final String myName;
    private final BotState state;

    public AttackSuperRegionRanker(BotState state) {
        this.state = state;
        this.myName = state.getMyPlayerName();
    }

    public List<SuperRegion> getRankedSuperRegions() {
        List<SuperRegion> superRegions = new ArrayList<>(state.getFullGameBoard().getSuperRegions());
        superRegions.sort((sr1, sr2) -> getEnemyArmiesInSuperRegion(sr1) - getEnemyArmiesInSuperRegion(sr2) );
        log.debug("RankedSuperRegions: %s", superRegions);
        return superRegions;
    }

    public int getEnemyArmiesInSuperRegion(SuperRegion superRegion) {
        return superRegion.getSubRegions().stream()
                .filter(r -> !r.ownedByPlayer(myName))
                .mapToInt(r -> {
                    log.trace("Getting army count for Region: %s", r);
                    if (r.getArmies() == 0) {
                        return state.getWasteLands().contains(r) ? WASTELAND_ARMIES : NORMAL_ARMIES;
                    } else {
                        return r.getArmies();
                    }
                }).sum();
    }
}
