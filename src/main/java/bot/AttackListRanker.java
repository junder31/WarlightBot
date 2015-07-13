package bot;

import log.Logger;
import map.GameBoard;
import map.Region;
import map.SuperRegion;

import java.util.List;
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

    public AttackListRanker(BotState state) {
        this.state = state;
        this.gameBoard = state.getVisibleGameBoard();
        this.myName = state.getMyPlayerName();
    }

    public List<Region> getRankedAttackList() {
        List<Region> unownedNeighbors = gameBoard.getRegions().stream()
                .filter(region -> !region.ownedByPlayer(myName)).collect(Collectors.toList());
        List<SuperRegion> rankedSuperRegions = new AttackSuperRegionRanker(state).getRankedSuperRegions();

        unownedNeighbors.sort((r1, r2) -> {
            if(rankedSuperRegions.indexOf(r1.getSuperRegion()) == rankedSuperRegions.indexOf(r2.getSuperRegion()) ) {
                int weightedR1Armies = r1.ownedByEnemyOfPlayer(myName) ?
                        (int)Math.floor(r1.getArmies() * ENEMY_OWNERSHIP_FACTOR) : r1.getArmies();
                int weightedR2Armies = r2.ownedByEnemyOfPlayer(myName) ?
                        (int)Math.floor(r2.getArmies() * ENEMY_OWNERSHIP_FACTOR) : r2.getArmies();
                return weightedR1Armies - weightedR2Armies;
            } else {
                return rankedSuperRegions.indexOf(r1.getSuperRegion()) - rankedSuperRegions.indexOf(r2.getSuperRegion());
            }
        });

        log.debug("RankedAttackList: %s", unownedNeighbors);

        return unownedNeighbors;
    }
}
