package bot;

import log.Logger;
import map.GameBoard;
import map.Region;
import map.SuperRegion;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static bot.Settings.ENEMY_OWNERSHIP_FACTOR;

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
            boolean isR1NeutralInEnemySr = isNeutralRegionInEnemySuperRegion(r1);
            boolean isR2NeutralInEnemySr = isNeutralRegionInEnemySuperRegion(r2);
            if (isR1NeutralInEnemySr && !isR2NeutralInEnemySr) {
                return 1;
            } else if (isR2NeutralInEnemySr && !isR1NeutralInEnemySr) {
                return -1;
            } else {
                int r1SrRank = rankedSuperRegions.indexOf(r1.getSuperRegion());
                int r2SrRank = rankedSuperRegions.indexOf(r2.getSuperRegion());
                if (r1SrRank == r2SrRank) {
                    if (r1.getSuperRegion().dominatedByPlayer(myName) && r2.getSuperRegion().dominatedByPlayer(myName)) {
                        int weightedR1Armies = r1.ownedByPlayer(enemyName) ?
                                (int) Math.floor(r1.getArmies() * ENEMY_OWNERSHIP_FACTOR) : r1.getArmies();
                        int weightedR2Armies = r2.ownedByPlayer(enemyName) ?
                                (int) Math.floor(r2.getArmies() * ENEMY_OWNERSHIP_FACTOR) : r2.getArmies();
                        return weightedR1Armies - weightedR2Armies;
                    } else {
                        return dominatorScore(r2) - dominatorScore(r1);
                    }
                } else {
                    return r1SrRank - r2SrRank;
                }
            }
        });

        Map<SuperRegion,Integer> srAttackCounts = new HashMap<>();
        List<Region> filteredAttackList = new ArrayList<>();

        for(Region r : unownedNeighbors) {
            if(r.getSuperRegion().dominatedByPlayer(myName)) {
                filteredAttackList.add(r);
            } else {
                if(!srAttackCounts.containsKey(r.getSuperRegion())){
                    srAttackCounts.put(r.getSuperRegion(), 0);
                }

                if(srAttackCounts.get(r.getSuperRegion()) < 2) {
                    filteredAttackList.add(r);
                    srAttackCounts.put(r.getSuperRegion(), srAttackCounts.get(r.getSuperRegion()) + 1);
                }
            }
        }

        log.debug("RankedAttackList: %s", filteredAttackList);

        return filteredAttackList;
    }

    public int dominatorScore(Region region) {
        int dominatorScore = (int) region.getSuperRegion().getMinDominatorSets().stream().filter(s -> s.contains(region)).count();
        log.debug("Dominator score for %s : %d", region, dominatorScore);
        return dominatorScore;
    }

    public boolean isNeutralRegionInEnemySuperRegion(Region region) {
        return region.ownedByPlayer("neutral") && region.getSuperRegion().getSubRegions().stream()
                .anyMatch(r -> r.ownedByPlayer(enemyName));
    }
}
