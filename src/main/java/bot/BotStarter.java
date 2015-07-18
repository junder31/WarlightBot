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
import move.AttackTransferMove;
import move.PlaceArmiesMove;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class BotStarter implements Bot {

    private static Logger log = new Logger(BotStarter.class.getSimpleName());
    private List<AttackTransferMove> attackMoves = new ArrayList<>();
    private int roundNum = 0;
    private int armiesLeft = 0;

    public BotStarter() {

    }

    public static void main(String[] args) {
        log.info("Bot Started");
        BotParser parser = startBot(System.in, System.out, System.err);
        parser.run();
    }

    public static BotParser startBot(InputStream in, PrintStream out, PrintStream err) {
        return new BotParser(in, out, err, new BotStarter());
    }

    /**
     * A method that returns which region the bot would like to start on, the pickable regions are stored in the BotState.
     * The bots are asked in turn (ABBAABBAAB) where they would like to start and return a single region each time they are asked.
     * This method returns one random region from the given pickable regions.
     */
    public Region getStartingRegion(BotState state, Long timeOut) {
        Region selectedRegion = new StartingRegionRanker(state).getRankedList().get(0);
        return selectedRegion;
    }

    @Override
    /**
     * This method is called for at first part of each round. This example puts two armies on random regions
     * until he has no more armies left to place.
     * @return The list of PlaceArmiesMoves for one round
     */
    public List<PlaceArmiesMove> getPlaceArmiesMoves(BotState state, Long timeOut) {
        log.info("Round %d started", ++roundNum);
        List<PlaceArmiesMove> placeArmiesMoves = new ArrayList<>();

        try {
            List<Region> attackRegions = new AttackListRanker(state).getRankedAttackList();
            attackMoves = new ArrayList<>();
            armiesLeft = state.getStartingArmies();

            String myName = state.getMyPlayerName();

            for (Region attackRegion : attackRegions) {
                log.debug("Selected best region to attack " + attackRegion);
                int requiredArmies = AttackLookup.getUnitRequiredToAttack(attackRegion.getArmies());
                log.debug("Armies required to attack %d", requiredArmies);
                List<Region> neighbors = attackRegion.getNeighbors().stream()
                        .filter(r -> r.ownedByPlayer(myName)).collect(Collectors.toList());
                neighbors.sort((r1, r2) -> r2.getArmies() - r1.getArmies());
                Region sourceRegion = neighbors.get(0);

                if (armiesLeft > 0 && sourceRegion.getArmies() <= requiredArmies) {
                    int armiesToRecruit = (requiredArmies - sourceRegion.getArmies()) + 1;
                    if (armiesToRecruit > armiesLeft) {
                        armiesToRecruit = armiesLeft;
                    }
                    placeArmiesMoves.add(new PlaceArmiesMove(myName, sourceRegion, armiesToRecruit));
                    sourceRegion.setArmies(sourceRegion.getArmies() + armiesToRecruit);
                    armiesLeft -= armiesToRecruit;
                    log.info("Recruiting %d armies in %s to attack %s", armiesToRecruit, sourceRegion, attackRegion);
                }

                if (sourceRegion.getArmies() > requiredArmies) {
                    attackMoves.add(new AttackTransferMove(myName, sourceRegion, attackRegion, requiredArmies));
                    sourceRegion.setArmies(sourceRegion.getArmies() - requiredArmies);
                    log.info("Attacking from %s to %s with %d armies", sourceRegion, attackRegion, requiredArmies);
                }
            }

            if (armiesLeft > 0) {
                log.warn("No region to recruit for found.");
                placeArmiesMoves.addAll(distributeRemainingArmies(state, armiesLeft));
            }

            for (AttackTransferMove attackMove : attackMoves) {
                Region fromRegion = attackMove.getFromRegion();
                Region toRegion = attackMove.getToRegion();
                if (fromRegion.getArmies() > 1 && toRegion.ownedByPlayer(state.getOpponentPlayerName())) {
                    attackMove.setArmies(attackMove.getArmies() + fromRegion.getArmies() - 1);
                    fromRegion.setArmies(1);
                }
            }
        } catch (Exception ex) {
            log.error("Exception while generating place army moves.", ex);
        }

        return placeArmiesMoves;
    }

    private List<PlaceArmiesMove> distributeRemainingArmies(BotState state, int armiesLeft) {
        List<PlaceArmiesMove> moves = new ArrayList<>();

        String myName = state.getMyPlayerName();
        List<Region> borderRegions = getRegionsToDistributeTo(state);

        log.info("Dividing remaining armies %d between all border regions", armiesLeft);
        int armiesPerRegion = armiesLeft / borderRegions.size();
        int leftOvers = armiesLeft % borderRegions.size();
        if (armiesPerRegion < 2) {
            armiesPerRegion = 2;
            leftOvers = armiesLeft % 2;
        }

        for (Region r : borderRegions) {
            int armiesToRecruit = armiesPerRegion;
            if (leftOvers > 0) {
                armiesToRecruit++;
                leftOvers--;
            }
            PlaceArmiesMove move = new PlaceArmiesMove(myName, r, armiesToRecruit);
            moves.add(move);
            r.setArmies(r.getArmies() + armiesToRecruit);
            armiesLeft -= armiesToRecruit;
            log.info("Placing extra remaining armies " + move);
            if (armiesLeft == 0) {
                break;
            }
        }

        return moves;
    }

    private List<Region> getRegionsToDistributeTo(BotState state) {
        String myName = state.getMyPlayerName();
        String enemyName = state.getOpponentPlayerName();
        //Get regions that border enemy
        List<Region> regions = state.getVisibleGameBoard().getRegions().stream()
                .filter(r -> r.ownedByPlayer(myName))
                .filter(r -> r.getNeighbors().stream().anyMatch(n -> n.ownedByPlayer(enemyName)))
                .collect(Collectors.toList());

        if (regions.size() == 0) {
            log.debug("No enemies within range.  Distributing remaining troops to any border region.");
            //Get regions not owned by me
            regions = state.getVisibleGameBoard().getRegions().stream()
                    .filter(r -> r.ownedByPlayer(myName))
                    .filter(r -> r.getNeighbors().stream().anyMatch(n -> !n.ownedByPlayer(myName)))
                    .collect(Collectors.toList());
        } else {
            log.debug("Distributing remaining troops amonst all enemy border regions.");
        }

        return regions;
    }

    @Override
    /**
     * This method is called for at the second part of each round. This example attacks if a region has
     * more than 6 armies on it, and transfers if it has less than 6 and a neighboring owned region.
     * @return The list of PlaceArmiesMoves for one round
     */
    public List<AttackTransferMove> getAttackTransferMoves(BotState state, Long timeOut) {
        ArrayList<AttackTransferMove> attackTransferMoves = new ArrayList<>();
        attackTransferMoves.addAll(new TroopMovePlanner(state).getTransferMoves());
        Collections.reverse(attackMoves);
        attackTransferMoves.addAll(attackMoves);

        log.info("Round %d done", roundNum);
        return attackTransferMoves;
    }
}
