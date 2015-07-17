package map

import spock.lang.Specification

/**
 * Created by johnunderwood on 7/17/15.
 */
class SuperRegionSpec extends Specification {

    void "test getDominatorSet"() {
        given:
        SuperRegion sr = new SuperRegion(1, 1)
        Region r1 = new Region(1, sr)
        Region r2 = new Region(2, sr)
        Region r3 = new Region(3, sr)
        r1.addNeighbor(r2)
        r2.addNeighbor(r3)

        when:
        Set<Region> dominatorRegions = sr.getMinDominatorSets();

        then:
        dominatorRegions.size() == 1;
        dominatorRegions.first() == new HashSet<Region>([r2]);
    }

    void "test getDominatorSet empty SR"() {
        given:
        SuperRegion sr = new SuperRegion(1, 1)

        when:
        Set<Region> dominatorRegions = sr.getMinDominatorSets();

        then:
        dominatorRegions.size() == 0;
    }

    void "test getDominatorSet size 1 SR"() {
        given:
        SuperRegion sr = new SuperRegion(1, 1)
        Region r1 = new Region(1, sr)

        when:
        Set<Region> dominatorRegions = sr.getMinDominatorSets();

        then:
        dominatorRegions.size() == 1;
        dominatorRegions.first() == new HashSet<Region>([r1]);
    }

    void "test getDominatorSet for SR that requires more than 1 dominator"() {
        given:
        SuperRegion sr = new SuperRegion(1, 1)
        Region r1 = new Region(1, sr)
        Region r2 = new Region(2, sr)
        Region r3 = new Region(3, sr)
        Region r4 = new Region(4, sr)
        Region r5 = new Region(5, sr)
        Region r6 = new Region(6, sr)
        r1.addNeighbor(r2)
        r2.addNeighbor(r3)
        r3.addNeighbor(r4)
        r4.addNeighbor(r5)
        r5.addNeighbor(r6)

        when:
        Set<Region> dominatorRegions = sr.getMinDominatorSets();

        then:
        dominatorRegions.size() == 1;
        dominatorRegions.first() == new HashSet<Region>([r2, r5]);
    }

    void "test getDominatorSet for SR that has multiple tied dominator sets"() {
        given:
        SuperRegion sr = new SuperRegion(1, 1)
        Region r1 = new Region(1, sr)
        Region r2 = new Region(2, sr)
        Region r3 = new Region(3, sr)
        Region r4 = new Region(4, sr)
        Region r5 = new Region(5, sr)
        r1.addNeighbor(r2)
        r2.addNeighbor(r3)
        r3.addNeighbor(r4)
        r4.addNeighbor(r5)

        when:
        Set<Region> dominatorRegions = sr.getMinDominatorSets();

        then:
        dominatorRegions.size() == 3;
        dominatorRegions.contains(new HashSet<Region>([r2, r5]));
        dominatorRegions.contains(new HashSet<Region>([r2, r4]));
        dominatorRegions.contains(new HashSet<Region>([r1, r4]));
    }
}
