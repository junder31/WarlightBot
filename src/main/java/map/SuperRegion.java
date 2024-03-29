/**
 * Warlight AI Game Bot
 * <p>
 * Last update: January 29, 2015
 *
 * @author Jim van Eeden
 * @version 1.1
 * @License MIT License (http://opensource.org/Licenses/MIT)
 */

package map;

import java.util.LinkedList;
import java.util.List;

public class SuperRegion {

    private int id;
    private int armiesReward;
    private LinkedList<Region> subRegions;

    public SuperRegion(int id, int armiesReward) {
        this.id = id;
        this.armiesReward = armiesReward;
        subRegions = new LinkedList<Region>();
    }

    public void addSubRegion(Region subRegion) {
        if (!subRegions.contains(subRegion))
            subRegions.add(subRegion);
    }

    /**
     * @return A string with the name of the player that fully owns this SuperRegion
     */
    public String ownedByPlayer() {
        String playerName = subRegions.getFirst().getPlayerName();
        for (Region region : subRegions) {
            if (!playerName.equals(region.getPlayerName()))
                return null;
        }
        return playerName;
    }

    /**
     * @return The id of this SuperRegion
     */
    public int getId() {
        return id;
    }

    /**
     * @return The number of armies a Player is rewarded when he fully owns this SuperRegion
     */
    public int getArmiesReward() {
        return armiesReward;
    }

    /**
     * @return A list with the Regions that are part of this SuperRegion
     */
    public List<Region> getSubRegions() {
        return subRegions;
    }

    @Override
    public String toString() {
        return "SuperRegion: [id: " + id + ", armiesReward: " + armiesReward +
                ", RegionCount: " + subRegions.size() + " ]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SuperRegion that = (SuperRegion) o;

        return id == that.id;

    }

    @Override
    public int hashCode() {
        return id;
    }
}
