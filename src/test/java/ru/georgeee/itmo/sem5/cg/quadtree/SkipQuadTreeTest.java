package ru.georgeee.itmo.sem5.cg.quadtree;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SkipQuadTreeTest extends AbstractTest {
    private static final Logger log = LoggerFactory.getLogger(SkipQuadTreeTest.class);
    private static final double[][] MANUAL_DATA_SETS = {
            {0.24277906389712256, 0.7288499200744581, 0.32330609761165474, 0.8583650947555813, 0.12094814223373274, 0.44575684846814223},
    };

    public void testRandom() {
        log.info("Testing random points");
        testOnCoins(coin -> testOnPrecisions(prec -> testRandomPoints(ps -> testSquadTree(ps, prec, coin))));
    }

    public void testManual() {
        log.info("Testing manually set point sets");
        testOnCoins(coin -> testOnPrecisions(prec -> testManual(ps -> testSquadTree(ps, prec, coin), MANUAL_DATA_SETS)));
    }

}
