/**
 * Warlight AI Game Bot
 * <p>
 * Last update: January 29, 2015
 *
 * @author Jim van Eeden
 * @version 1.1
 * @License MIT License (http://opensource.org/Licenses/MIT)
 */

package move;

import map.Region;

/**
 * This Move is used in the first part of each round. It represents what Region is increased
 * with how many armies.
 */

public class PlaceArmiesMove extends Move {

    private Region region;
    private int armies;

    public PlaceArmiesMove(String playerName, Region region, int armies) {
        super.setPlayerName(playerName);
        this.region = region;
        this.armies = armies;
    }

    /**
     * @param n Sets the number of armies this move will place on a Region
     */
    public void setArmies(int n) {
        armies = n;
    }

    /**
     * @return The Region this Move will be placing armies on
     */
    public Region getRegion() {
        return region;
    }

    /**
     * @return The number of armies this move will place
     */
    public int getArmies() {
        return armies;
    }

    /**
     * @return A string representation of this Move
     */
    @Override
    public String toString() {
        if (getIllegalMove().equals(""))
            return getPlayerName() + " place_armies " + region.getId() + " " + armies;
        else
            return getPlayerName() + " illegal_move " + getIllegalMove();

    }

}
