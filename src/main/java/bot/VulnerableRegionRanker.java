package bot;

import log.Logger;
import map.Region;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by johnunderwood on 7/15/15.
 */
public class VulnerableRegionRanker {
    private static final Logger log = new Logger(VulnerableRegionRanker.class.getSimpleName());
    private final BotState state;
    private final String myName;
    private final String enemyName;

    public VulnerableRegionRanker(BotState state) {
        this.state = state;
        this.myName = state.getMyPlayerName();
        this.enemyName = state.getOpponentPlayerName();
    }

    public List<Region> getRankedVulnerableRegionsList() {
        List<Region> vulnerableRegions = state.getVisibleGameBoard().getRegions().stream()
                .filter(r -> r.ownedByPlayer(myName) &&
                        r.getNeighbors().stream().anyMatch(n -> n.ownedByPlayer(enemyName)))
                .collect(Collectors.toList());

        vulnerableRegions.sort((r1, r2) -> {
            int r1Threat = r1.getNeighbors().stream().mapToInt(Region::getArmies).max().getAsInt();
            int r2Threat = r2.getNeighbors().stream().mapToInt(Region::getArmies).max().getAsInt();
            return r2Threat - r1Threat;
        });

        log.debug("Ranked Defense List: %s", vulnerableRegions);
        return vulnerableRegions;
    }
}
