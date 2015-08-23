package ru.georgeee.itmo.sem5.cg.quadtree;

import junit.framework.AssertionFailedError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.georgeee.itmo.sem5.cg.common.Point2d;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BoxSectorTest extends AbstractTest {
    private static final Logger log = LoggerFactory.getLogger(BoxSectorTest.class);
    private static final int TEST_POINT_EXISTENCE_THRESHOLD = 5000;

    private static final double[][] CALC_DEPTH_DATA_SETS = {
            {0, 0.9, 0},
            {0, 0.0156, 6},
            {.5, .7, 2},
            {0, 0.999999, 0},
            {0.000001, 1, -1},
            {0, 1, -1},
            {.05155373974270416, .91407266328026970, 0}
    };

    private static final double[][] FIND_MIN_ENCLOSING_DATA_SETS = {
            {.05155373974270416, .5999982325496052, .91407266328026970, .8839799129457002},
            {.04837657840696974, .6440639641953596, .7444731761131651, .6875739929088972},
    };

    private static final double[][] MANUAL_DATA_SETS = {
            {0.5368409940464363, 0.2509222975748724, 0.09298982694757507, 0.19240413130567413, 0.2389966097880203, 0.12708945024774643, 0.17238683170938662, 0.31253707450137935, 0.22651574355740933, 0.5427383332937834},
            {0.92, 0.192, 0.54, 0.251, 0.239, 0.127, 0.172, 0.3125, 0.2265, 0.5427},
    };

    public void testFindMinEnclosing() {
        for (double[] data : FIND_MIN_ENCLOSING_DATA_SETS) {
            testFindMinEnclosing(new Point2d(data[0], data[1]), new Point2d(data[2], data[3]));
        }
    }

    public void testCalcDepth() {
        for (double[] data : CALC_DEPTH_DATA_SETS) {
            testCalcDepth(data[0], data[1], (int) data[2]);
        }
    }

    void testCalcDepth(double x, double y, int answer) {
        int depth = BoxSector.calcDepth(x, y);
        assertEquals(String.format("Testing %f, %f", x, y), answer, depth);
    }

    void testFindMinEnclosing(Point2d min, Point2d max) {
        double[] bounds = new double[3];
        BoxSector.findMinEnclosing(bounds, min, max);
        try {
            checkWithin(min, null, bounds);
            checkWithin(max, null, bounds);
        } catch (AssertionFailedError e) {
            log.error("Wrong enclosing found: min={} max={} bounds={}", min, max, bounds, e);
            throw e;
        }
    }

    public void testRandom() {
        log.info("Testing random points");
        testOnPrecisions(prec -> testRandomPoints(ps -> testPoints(ps, prec)));
    }

    public void testManual() {
        log.info("Testing manually set point sets");
        testOnPrecisions(prec -> testManual(ps -> testPoints(ps, prec), MANUAL_DATA_SETS));
    }

    Void testPoints(List<Point2d> points, double precision) {
        boolean testIsNew = points.size() <= TEST_POINT_EXISTENCE_THRESHOLD;
        Set<Point2d> pointSet = new HashSet<>();
        Sector sector = null;
        try {
            for (int i = 0; i < points.size(); ++i) {
                Point2d p = points.get(i);
                PointSector pointSector = new PointSector(p, precision);
                boolean isNewPoint = true;
                if (testIsNew) {
                    for (int j = 0; j < i; ++j) {
                        Point2d q = points.get(j);
                        if(BoxSector.checkEquals(p, q, precision)){
                            isNewPoint = false;
                            break;
                        }
                    }
                    if (isNewPoint) {
                        pointSet.add(p);
                    }
                }
                if (sector == null) {
                    sector = pointSector;
                } else {
                    try {
                        sector = sector.add(pointSector);
                        if (testIsNew) {
                            assertTrue(isNewPoint);
                        }
                    } catch (PointAlreadyExistsException e) {
//                        assertTrue(precision.test(e.getPoint(), p));
                        if (testIsNew) {
                            assertFalse(isNewPoint);
                        }
                    }
                }
            }
            validateSector(sector, testIsNew ? pointSet : null);
        } catch (AssertionFailedError | RuntimeException e) {
            log.error("Assertion failed, points: {}", points.toString(), e);
            throw e;
        }
        return null;
    }


}