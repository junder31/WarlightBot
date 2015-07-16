import bot.BotStarter
import spock.lang.Specification

/**
 * Created by johnunderwood on 7/11/15.
 */
class AttackSuperRegionRankerSpec extends Specification {
    private final double LUCK_MODIFIER = 0.16;

    void "test defense values"() {
        when:
        printf("%5s%20s%20s%20s\n", "D", "WorstCaseDefense", "AvgCaseDefense", "BestCaseDefense")
        for(int d = 1; d <= 20; d++) {
            int best = (int) Math.round( ((d * 0.7) * (1 - LUCK_MODIFIER))
                    + (d * LUCK_MODIFIER) );
            int worst = (int) Math.round( ((d * 0.7) * (1 - LUCK_MODIFIER))
                    + (0 * LUCK_MODIFIER) );
            int average = (int) Math.round((d * 0.7))

            printf("%5d%20d%20d%20d\n", d, worst, average, best)
        }
        then:
        true
    }



    void "test attack values"() {
        when:
        printf("%5s%20s%20s%20s\n", "D", "WorstCaseAttack", "AvgCaseAttack", "BestCaseAttack")
        for(int a = 1; a <= 20; a++) {
            int best = (int) Math.round( ((a * 0.6) * (1 - LUCK_MODIFIER))
                    + (a * LUCK_MODIFIER) );
            int worst = (int) Math.round( ((a * 0.6) * (1 - LUCK_MODIFIER))
                    + (0 * LUCK_MODIFIER) );
            int average = (int) Math.round((a * 0.6))

            printf("%5d%20d%20d%20d\n", a, worst, average, best)
        }
        then:
        true
    }

    void "test something else"() {
        when:
        BotStarter starter = new BotStarter()

        then:
        1 == 1
    }
}
