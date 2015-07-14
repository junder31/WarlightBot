package bot;

import log.Logger;
import map.GameBoard;
import map.Region;
import move.AttackTransferMove;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Created by johnunderwood on 7/13/15.
 */
public class TroopMovePlanner {
    private static final Logger log = new Logger(TroopMovePlanner.class.getSimpleName());
    private final String myName;
    private final String enemyName;
    private final GameBoard gameBoard;

    public TroopMovePlanner(BotState state) {
        this.myName = state.getMyPlayerName();
        this.enemyName = state.getOpponentPlayerName();
        this.gameBoard = state.getVisibleGameBoard();
    }

    public List<AttackTransferMove> getTransferMoves() {
        List<Region> sourceTransferRegions = gameBoard.getRegions().stream()
                .filter(r ->
                        r.getArmies() > 1 && r.ownedByPlayer(myName) &&
                                r.getNeighbors().stream().allMatch(n ->
                                        n.ownedByPlayer(myName) || !n.ownedByPlayer(enemyName)))
                .collect(Collectors.toList());
        List<AttackTransferMove> transferMoves = new ArrayList<>();

        if(sourceTransferRegions.size() > 0) {
            Map<Region, Integer> distanceToEnemyMap = getDistanceToEnemyPlayer();
            log.trace("Distance To Enemies: %s", distanceToEnemyMap);
            transferMoves.addAll(getMoves(sourceTransferRegions, distanceToEnemyMap));

            sourceTransferRegions.removeAll(transferMoves.stream()
                    .map(AttackTransferMove::getFromRegion)
                    .collect(Collectors.toList()));
        }

        if(sourceTransferRegions.size() > 0) {
            Map<Region,Integer> distanceToBorderMap = getDistanceToBorder();
            log.trace("Distance To Border: %s", distanceToBorderMap);
            transferMoves.addAll(getMoves(sourceTransferRegions, distanceToBorderMap));
        }

        return transferMoves;
    }

    public List<AttackTransferMove> getMoves(List<Region> sourceRegions, Map<Region,Integer> dMap) {
        List<AttackTransferMove> transferMoves = new ArrayList<>();

        for(Region source : sourceRegions) {
            List<Region> destinations = source.getNeighbors().stream()
                    .filter(r -> dMap.containsKey(r))
                    .collect(Collectors.toList());
            if(destinations.size() > 0) {
                destinations.sort( (r1, r2) -> dMap.get(r1) - dMap.get(r2) );
                Region dest = destinations.get(0);
                if(dMap.get(dest) < dMap.get(source)) {
                    //if some destinations directly border the enemy transfer to dest with most armies
                    int i = 1;
                    while(dMap.get(dest) == 0  && i < destinations.size() ) {
                        if(dest.getArmies() < destinations.get(i).getArmies()) {
                            dest = destinations.get(i);
                        }
                        i++;
                    }

                    AttackTransferMove move = new AttackTransferMove(myName, source, dest, source.getArmies() - 1);
                    log.info("Moving Troops: %s", move);
                    transferMoves.add(move);
                }
            }
        }

        return transferMoves;
    }

    public Map<Region,Integer> getDistanceToEnemyPlayer() {
        return getDistanceMap(n -> n.ownedByPlayer(enemyName));
    }

    public Map<Region,Integer> getDistanceToBorder() {
        return getDistanceMap(n -> !n.ownedByPlayer(myName));
    }

    public Map<Region,Integer> getDistanceMap(Predicate<Region> regionPredicate) {
        List<Region> enemyBorderingRegions = gameBoard.getRegions().stream()
                .filter(r -> r.ownedByPlayer(myName))
                .filter( r -> r.getNeighbors().stream().anyMatch(regionPredicate) )
                .collect(Collectors.toList());
        Map<Region,Integer> dMap = new HashMap<>();

        for(Region r : enemyBorderingRegions) {
            dMap.put(r, 0);
        }

        for(Region r : enemyBorderingRegions) {
            getDistanceMapHelper(dMap, r);
        }

        return dMap;
    }

    private void getDistanceMapHelper(Map<Region,Integer> dMap, Region r) {
        List<Region> ownedNeighbors = r.getNeighbors().stream()
                .filter( n -> n.ownedByPlayer(myName) ).collect(Collectors.toList());
        int rDistance = dMap.get(r);

        for(Region neighbor : ownedNeighbors) {
            if( !dMap.containsKey(neighbor) || dMap.get(neighbor) > rDistance + 1) {
                dMap.put(neighbor, rDistance + 1);
                getDistanceMapHelper(dMap, neighbor);
            }
        }
    }
}
