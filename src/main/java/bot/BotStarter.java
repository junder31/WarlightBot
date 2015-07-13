/**
 * Warlight AI Game Bot
 * <p>
 * Last update: January 29, 2015
 *
 * @author Jim van Eeden
 * @version 1.1
 * @License MIT License (http://opensource.org/Licenses/MIT)
 */

package bot;

/**
 * This is a simple bot that does random (but correct) moves.
 * This class implements the Bot interface and overrides its Move methods.
 * You can implement these methods yourself very easily now,
 * since you can retrieve all information about the match from variable “state”.
 * When the bot decided on the move to make, it returns an ArrayList of Moves.
 * The bot is started by creating a Parser to which you add
 * a new instance of your bot, and then the parser is started.
 */

import log.Logger;
import map.Region;
import map.SuperRegion;
import move.AttackTransferMove;
import move.PlaceArmiesMove;

import java.util.*;
import java.util.stream.Collectors;

public class BotStarter implements Bot {
    private static Logger log = new Logger(BotStarter.class.getSimpleName());
    private List<AttackTransferMove> attackMoves = new ArrayList<>();
    private Map<Region,Integer> extraEffort = new HashMap<>();


    @Override
    /**
     * A method that returns which region the bot would like to start on, the pickable regions are stored in the BotState.
     * The bots are asked in turn (ABBAABBAAB) where they would like to start and return a single region each time they are asked.
     * This method returns one random region from the given pickable regions.
     */
    public Region getStartingRegion(BotState state, Long timeOut) {
        state.updateMap(new String[0]);
        List<Region> pickableRegions = state.getPickableStartingRegions();
        List<SuperRegion> superRegionRank = new AttackSuperRegionRanker(state).getRankedSuperRegions();
        Region selectedRegion = null;
        int superRegionRankIdx = superRegionRank.size();
        for (Region region : pickableRegions) {
            int regionSuperRegionRankIdx = superRegionRank.indexOf(region);
            if (regionSuperRegionRankIdx < superRegionRankIdx) {
                selectedRegion = region;
                superRegionRankIdx = regionSuperRegionRankIdx;
            }
        }

        return selectedRegion != null ? selectedRegion : state.getPickableStartingRegions().stream().findAny().get();
    }

    private void updateExtraEffort(List<Region> regionsAttackedLastTurn, List<Region> regionsToAttackThisTurn) {
        for(Region region : regionsToAttackThisTurn ) {
            if(regionsToAttackThisTurn.contains(region) && regionsAttackedLastTurn.contains(region) ) {
                extraEffort.put(region, extraEffort.get(region) + 3);
                log.debug("Set extra effort %s to %d", region, extraEffort.get(region));
            } else {
                extraEffort.put(region, 0);
            }
        }
    }

    private void logExtraEffort() {

    }

    @Override
    /**
     * This method is called for at first part of each round. This example puts two armies on random regions
     * until he has no more armies left to place.
     * @return The list of PlaceArmiesMoves for one round
     */
    public List<PlaceArmiesMove> getPlaceArmiesMoves(BotState state, Long timeOut) {
        List<PlaceArmiesMove> placeArmiesMoves = new ArrayList<>();

        try {
            List<Region> attackRegions = new AttackListRanker(state).getRankedAttackList();
            List<Region> attackedRegions = attackMoves.stream()
                    .map(r -> r.getToRegion()).collect(Collectors.toList());

            updateExtraEffort(attackedRegions, attackRegions);
            logExtraEffort();
            attackMoves = new ArrayList<>();
            String myName = state.getMyPlayerName();
            int armiesLeft = state.getStartingArmies();

            for(Region attackRegion : attackRegions) {
                log.debug("Selected best region to attack " + attackRegion);
                int requiredArmies;
                if(attackRegion.getArmies() < 6) {
                    requiredArmies = (int)Math.ceil(attackRegion.getArmies() * 1.5) + extraEffort.get(attackRegion);
                } else {
                    requiredArmies = (int)Math.floor(attackRegion.getArmies() * 1.7) + extraEffort.get(attackRegion);
                }
                log.debug("Armies required to attack %d", requiredArmies);
                List<Region> neighbors = attackRegion.getNeighbors().stream()
                        .filter(r -> r.ownedByPlayer(myName)).collect(Collectors.toList());
                neighbors.sort((r1, r2) -> r2.getArmies() - r1.getArmies());
                Region sourceRegion = neighbors.get(0);

                if(armiesLeft > 0 && sourceRegion.getArmies() <= requiredArmies) {
                    int armiesToRecruit = requiredArmies - sourceRegion.getArmies() + 1;
                    if(armiesToRecruit > armiesLeft) {
                        armiesToRecruit = armiesLeft;
                    }
                    placeArmiesMoves.add(new PlaceArmiesMove(myName, sourceRegion, armiesToRecruit));
                    sourceRegion.setArmies(sourceRegion.getArmies() + armiesToRecruit);
                    armiesLeft -= armiesToRecruit;
                    log.info("Recruiting %d armies in %s to attack %s", armiesToRecruit, sourceRegion, attackRegion);
                }

                if(sourceRegion.getArmies() > requiredArmies) {
                    attackMoves.add(new AttackTransferMove(myName, sourceRegion, attackRegion, requiredArmies));
                    sourceRegion.setArmies(sourceRegion.getArmies() - requiredArmies);
                    log.info("Attacking from %s to %s with %d armies", sourceRegion, attackRegion, requiredArmies);
                }
            }

            if(armiesLeft > 0){
                log.warn("No region to recruit for found.");
                Region randomOwnedRegion = state.getVisibleGameBoard().getRegions().stream()
                        .filter(r -> r.ownedByPlayer(myName))
                        .filter(r -> r.getNeighbors().stream().anyMatch(n -> !n.ownedByPlayer(myName)))
                        .findAny().get();
                PlaceArmiesMove move = new PlaceArmiesMove(myName, randomOwnedRegion, armiesLeft);
                placeArmiesMoves.add(move);
                randomOwnedRegion.setArmies(randomOwnedRegion.getArmies() + armiesLeft);
                log.info("Placing all remaining armies " + move);
            }
        } catch (Exception ex) {
            log.error("Exception while generating place army moves.", ex);
        }

        return placeArmiesMoves;
    }

    @Override
    /**
     * This method is called for at the second part of each round. This example attacks if a region has
     * more than 6 armies on it, and transfers if it has less than 6 and a neighboring owned region.
     * @return The list of PlaceArmiesMoves for one round
     */
    public List<AttackTransferMove> getAttackTransferMoves(BotState state, Long timeOut) {
        ArrayList<AttackTransferMove> attackTransferMoves = new ArrayList<>();
        String myName = state.getMyPlayerName();

        List<AttackTransferMove> transferMoves = state.getVisibleGameBoard().getRegions().stream()
                .filter(r ->
                        r.getArmies() > 1 && r.ownedByPlayer(myName) &&
                                r.getNeighbors().stream().allMatch(rr -> rr.ownedByPlayer(myName)))
                .map(source -> {
                    Region dest = source.getNeighbors().stream()
                            .filter(neighbor -> neighbor.getNeighbors().stream()
                                    .anyMatch(nn -> !nn.ownedByPlayer(myName)))
                            .findAny().orElse(source.getNeighbors().stream().findAny().get());
                    return new AttackTransferMove(myName, source, dest, source.getArmies() - 1);
                }).collect(Collectors.toList());

        attackTransferMoves.addAll(attackMoves);
        attackTransferMoves.addAll(transferMoves);

        return attackTransferMoves;
    }

    public static void main(String[] args) {
        log.info("Bot Started");
        BotParser parser = new BotParser(new BotStarter());
        parser.run();
    }

}
