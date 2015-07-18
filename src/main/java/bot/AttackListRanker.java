package bot;

import log.Logger;
import map.GameBoard;
import map.Region;
import map.SuperRegion;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import static bot.Settings.*;

/**
 * Created by johnunderwood on 7/11/15.
 */
public class AttackListRanker {
    public static final Logger log = new Logger(AttackListRanker.class.getSimpleName());
    private final BotState state;
    private final GameBoard gameBoard;
    private final String myName;
    private final String enemyName;

    public AttackListRanker(BotState state) {
        this.state = state;
        this.gameBoard = state.getVisibleGameBoard();
        this.myName = state.getMyPlayerName();
        this.enemyName = state.getOpponentPlayerName();
    }

    public List<Region> getRankedAttackList() {
        List<Region> unownedNeighbors = gameBoard.getRegions().stream()
                .filter(region -> !region.ownedByPlayer(myName)).collect(Collectors.toList());
        List<SuperRegion> rankedSuperRegions = new AttackSuperRegionRanker(state).getRankedSuperRegions();

        unownedNeighbors.sort((r1, r2) -> {
            if( isNeutralRegionInEnemySuperRegion(r1) && !isNeutralRegionInEnemySuperRegion(r2) ) {
                return 1;
            } else if( isNeutralRegionInEnemySuperRegion(r2) && !isNeutralRegionInEnemySuperRegion(r1) ) {
                return -1;
            } else if(rankedSuperRegions.indexOf(r1.getSuperRegion()) == rankedSuperRegions.indexOf(r2.getSuperRegion()) ) {
                int weightedR1Armies = r1.ownedByPlayer(enemyName) ?
                        (int)Math.floor(r1.getArmies() * ENEMY_OWNERSHIP_FACTOR) : r1.getArmies();
                int weightedR2Armies = r2.ownedByPlayer(enemyName) ?
                        (int)Math.floor(r2.getArmies() * ENEMY_OWNERSHIP_FACTOR) : r2.getArmies();
                return weightedR1Armies - weightedR2Armies;
            } else {
                return rankedSuperRegions.indexOf(r1.getSuperRegion()) - rankedSuperRegions.indexOf(r2.getSuperRegion());
            }
        });

        log.debug("RankedAttackList: %s", unownedNeighbors);

        return unownedNeighbors;
    }

//    public boolean isADominator(Region r) {
//        Set<Set<Region>> dominatorSets = r.getSuperRegion().getMinDominatorSets();
//        for(Set<R>)
//    }

    public boolean isNeutralRegionInEnemySuperRegion(Region region) {
        return region.ownedByPlayer("neutral") && region.getSuperRegion().getSubRegions().stream()
                .anyMatch(r -> r.ownedByPlayer(enemyName));
    }
}
