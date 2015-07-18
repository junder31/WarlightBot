package bot

import map.Region
import map.SuperRegion
import spock.lang.Specification

/**
 * Created by johnunderwood on 7/17/15.
 */
class StartingRegionRankerSpec extends Specification {

    void "test getTurnsToTakeSR two moves"() {
        given:
        SuperRegion sr = new SuperRegion(1, 1)
        Region r1 = new Region(1, sr)
        Region r2 = new Region(2, sr)
        Region r3 = new Region(3, sr)
        r1.addNeighbor(r2)
        r2.addNeighbor(r3)

        when:
        int turnsToTakeSr = StartingRegionRanker.getTurnsToTakeSR(r1);

        then:
        turnsToTakeSr == 2;
    }

    void "test getTurnsToTakeSR one move"() {
        given:
        SuperRegion sr = new SuperRegion(1, 1)
        Region r1 = new Region(1, sr)
        Region r2 = new Region(2, sr)
        Region r3 = new Region(3, sr)
        r1.addNeighbor(r2)
        r2.addNeighbor(r3)

        when:
        int turnsToTakeSr = StartingRegionRanker.getTurnsToTakeSR(r2);

        then:
        turnsToTakeSr == 1;
    }

    void "test getTurnsToTakeSR armies increase turns"() {
        given:
        SuperRegion sr = new SuperRegion(1, 1)
        Region r1 = new Region(1, sr)
        Region r2 = new Region(2, sr)
        Region r3 = new Region(3, sr)
        r1.addNeighbor(r2)
        r2.addNeighbor(r3)
        r3.setIsWasteland(true);

        when:
        int turnsToTakeSr = StartingRegionRanker.getTurnsToTakeSR(r2);

        then:
        turnsToTakeSr == 3;
    }
}
