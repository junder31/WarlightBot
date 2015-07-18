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

import java.util.*;
import java.util.stream.Collectors;

public class SuperRegion {

    private int id;
    private int armiesReward;
    private LinkedList<Region> subRegions;
    private Set<Set<Region>> minDominatorSets;

    public SuperRegion(int id, int armiesReward) {
        this.id = id;
        this.armiesReward = armiesReward;
        subRegions = new LinkedList<>();
    }

    public void addSubRegion(Region subRegion) {
        if (!subRegions.contains(subRegion)) {
            subRegions.add(subRegion);
        }
    }

    public Set<Set<Region>> getMinDominatorSets() {
        if (minDominatorSets == null) {
            List<Set<Region>> dominatorSets = findDominatorSetHelper(
                    new HashSet<>(), new HashSet<>(), new HashSet<>(subRegions));
            int minSize = dominatorSets.stream().mapToInt(Set::size).min().orElse(0);
            minDominatorSets = dominatorSets.stream().filter(s -> s.size() == minSize).collect(Collectors.toSet());
        }
        return minDominatorSets;
    }

    private List<Set<Region>> findDominatorSetHelper(Set<Region> dominatorSet,
                                                     Set<Region> dominatedRegions,
                                                     Set<Region> remainingRegions) {
        List<Set<Region>> workDominatorSets = new ArrayList<>();

        Set<Region> newRemainingRegions = new HashSet<>(remainingRegions);
        for (Region region : remainingRegions) {
            Set<Region> newDominatorSet = new HashSet<>(dominatorSet);
            newDominatorSet.add(region);
            Set<Region> newDominatedRegions = new HashSet<>(dominatedRegions);
            newDominatedRegions.add(region);
            newDominatedRegions.addAll(region.getNeighbors());
            newRemainingRegions.remove(region);

            if (newDominatedRegions.containsAll(subRegions)) {
                workDominatorSets.add(newDominatorSet);
            } else {
                workDominatorSets.addAll(findDominatorSetHelper(newDominatorSet,
                        newDominatedRegions, newRemainingRegions));
            }
        }

        return workDominatorSets;
    }

    public boolean dominatedByPlayer(String name) {
        Set<Region> ownedRegions = subRegions.stream().filter(r -> r.ownedByPlayer(name)).collect(Collectors.toSet());
        Set<Region> dominatedRegions = new HashSet<>();
        for (Region r : ownedRegions) {
            dominatedRegions.addAll(r.getNeighbors());
        }

        return dominatedRegions.containsAll(subRegions);
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
