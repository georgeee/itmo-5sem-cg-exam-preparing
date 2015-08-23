package ru.georgeee.itmo.sem5.cg.quadtree;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SkipQuadTreeLargeTest extends AbstractTest {
    private static final Logger log = LoggerFactory.getLogger(SkipQuadTreeLargeTest.class);

    public void testRandom() {
        log.info("Testing random points");
        testRandomPointsLarge(ps -> testSkipQuadTree(ps, 0, null));
    }

}
