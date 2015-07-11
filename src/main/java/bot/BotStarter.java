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

import map.Region;
import move.AttackTransferMove;
import move.PlaceArmiesMove;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

public class BotStarter implements Bot {
    private List<Region> attackRegions = new ArrayList<>();

    @Override
    /**
     * A method that returns which region the bot would like to start on, the pickable regions are stored in the BotState.
     * The bots are asked in turn (ABBAABBAAB) where they would like to start and return a single region each time they are asked.
     * This method returns one random region from the given pickable regions.
     */
    public Region getStartingRegion(BotState state, Long timeOut) {
        List<Region> pickableRegions = state.getPickableStartingRegions();
        Region selectedRegion = null;
        double selectedRegionRank = 0;
        for (Region region : pickableRegions) {
            double regionRank = (region.getSuperRegion().getArmiesReward() * 1.0) /
                    region.getSuperRegion().getSubRegions().size();
            if (regionRank > selectedRegionRank) {
                selectedRegion = region;
                selectedRegionRank = regionRank;
            }
        }

        return selectedRegion != null ? selectedRegion : state.getPickableStartingRegions().stream().findAny().get();
    }

    @Override
    /**
     * This method is called for at first part of each round. This example puts two armies on random regions
     * until he has no more armies left to place.
     * @return The list of PlaceArmiesMoves for one round
     */
    public List<PlaceArmiesMove> getPlaceArmiesMoves(BotState state, Long timeOut) {
        attackRegions = new ArrayList<>();
        List<PlaceArmiesMove> placeArmiesMoves = new ArrayList<>();
        String myName = state.getMyPlayerName();
        int armiesLeft = state.getStartingArmies();
        List<Region> visibleRegions = state.getVisibleGameBoard().getRegions();
        List<Region> unownedNeighbors = visibleRegions.stream()
                .filter(region -> !region.ownedByPlayer(myName))
                .collect(Collectors.toList());

        while (armiesLeft > 0) {
            Region recruitRegion = null;
            long recruitRegionsNeededToOwnSuper = 1000;
            for (Region region : unownedNeighbors) {
                long regionsNeeded = region.getSuperRegion().getSubRegions().stream().
                        filter(r -> !r.ownedByPlayer(myName)).count();
                if (regionsNeeded < recruitRegionsNeededToOwnSuper &&
                        (recruitRegion == null || region.getArmies() < recruitRegion.getArmies())) {
                    recruitRegion = region;
                    recruitRegionsNeededToOwnSuper = regionsNeeded;
                }
            }

            if(recruitRegion != null){
                attackRegions.add(recruitRegion);
                unownedNeighbors.remove(recruitRegion);
                List<Region> neighbors = recruitRegion.getNeighbors().stream()
                        .filter(r -> r.ownedByPlayer(myName)).collect(Collectors.toList());
                Region neighbor = neighbors.stream().max(Comparator.comparingInt(r -> r.getArmies())).get();

                if (neighbor.getArmies() < (recruitRegion.getArmies() * 3) / 2) {
                    int armies = ((recruitRegion.getArmies() * 3) / 2) - neighbor.getArmies();
                    if( armies < 2) {
                        armies = 2;
                    }
                    if( armies > armiesLeft ) {
                        armies = armiesLeft;
                    }
                    PlaceArmiesMove move = new PlaceArmiesMove(myName, neighbor, armies);
                    neighbor.setArmies(neighbor.getArmies() + armies);
                    placeArmiesMoves.add(move);
                    armiesLeft -= armies;
                    System.err.println("Placing Armies " + move);
                    System.err.println("Armies remaining " + armiesLeft);
                }
            } else {
                System.err.println("No region to recruit for found.");
                Region randomOwnedRegion = state.getVisibleGameBoard().getRegions().stream()
                        .filter( r -> r.ownedByPlayer(myName) )
                        .filter( r -> r.getNeighbors().stream().anyMatch( n -> !n.ownedByPlayer( myName) ) )
                        .findAny().get();
                PlaceArmiesMove move = new PlaceArmiesMove(myName, randomOwnedRegion, armiesLeft);
                placeArmiesMoves.add(move);
                armiesLeft = 0;
                System.err.println("Placing all remaining armies " + move);
            }
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

        List<AttackTransferMove> attackMoves = new ArrayList<>();

        for(Region dest : attackRegions) {
            System.err.println("Creating move to attack " + dest);
            try {
                Region source = dest.getNeighbors().stream()
                        .filter(r -> r.ownedByPlayer(myName)
                                && r.getArmies() > (dest.getArmies() * 3) / 2
                                && r.getArmies() > dest.getArmies() + 2).findAny().get();
                System.err.println("Found source to attack from " + source);
                    attackMoves.add(new AttackTransferMove(myName, source, dest, source.getArmies() - 1));
                    source.setArmies(1);
            } catch (NoSuchElementException ex) {
                System.err.println("Couldn't find suitable source to attack from.");
            }
        }

        List<AttackTransferMove> transferMoves = state.getVisibleGameBoard().getRegions().stream()
                .filter( r ->
                        r.getArmies() > 1 && r.ownedByPlayer(myName) &&
                                r.getNeighbors().stream().allMatch( rr -> rr.ownedByPlayer(myName) ) )
                .map( source -> {
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
        BotParser parser = new BotParser(new BotStarter());
        parser.run();
    }

}
