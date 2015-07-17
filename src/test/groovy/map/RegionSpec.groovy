package map

import spock.lang.Specification

/**
 * Created by johnunderwood on 7/17/15.
 */
class RegionSpec extends Specification {
    void "test distanceTo"() {
        given:
        SuperRegion sr = new SuperRegion(1, 1)
        Region r1 = new Region(1, sr)
        Region r2 = new Region(2, sr)
        r1.addNeighbor(r2)

        when:
        int distance = r1.distanceTo(r2)

        then:
        distance == 1
    }

}
