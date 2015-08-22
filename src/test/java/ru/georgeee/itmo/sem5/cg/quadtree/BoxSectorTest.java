package ru.georgeee.itmo.sem5.cg.quadtree;

import junit.framework.AssertionFailedError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.georgeee.itmo.sem5.cg.common.Point2d;

import java.util.*;
import java.util.function.Function;

public class BoxSectorTest extends AbstractTest {
    private static final Logger log = LoggerFactory.getLogger(BoxSectorTest.class);
    private final Random random = new Random(System.currentTimeMillis());
    private static final int[] RANDOM_TEST_COUNTS = {3, 5, 10, 100, 1000};
    private static final int RANDOM_TEST_REPEAT = 10;
    private static final int TEST_POINT_EXISTENCE_THRESHOLD = 5000;

    private static final double[][] CALC_DEPTH_DATA_SETS = {
            {0, 0.9, 0},
            {0, 0.0156, 6},
            {.5, .7, 2},
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
        testOnComparators(this::testRandomPoints);
    }

    public void testManual() {
        log.info("Testing manually set point sets");
        testOnComparators(this::testManual);
    }

    Void testManual(double precision) {
        for (double[] data : MANUAL_DATA_SETS) {
            List<Point2d> points = new ArrayList<>();
            for (int i = 0; i < data.length / 2; ++i) {
                points.add(new Point2d(data[2 * i], data[2 * i + 1]));
            }
            testPoints(points, precision);
        }
        return null;
    }

    void testOnComparators(Function<Double, Void> f) {
        log.info("Testing on fair comparator");
        f.apply(0.0);
        log.info("Testing on precision 10^-9");
        f.apply(1.0E-9);
        log.info("Testing on precision 10^-5");
        f.apply(1.0E-5);
        log.info("Testing on precision 10^-3");
        f.apply(1.0E-3);
        log.info("Testing on precision 10^-1");
        f.apply(1.0E-1);
    }

    Void testRandomPoints(double precision) {
        for (int count : RANDOM_TEST_COUNTS) {
            testRandomPoints(count, precision);
        }
        return null;
    }

    void testRandomPoints(int count, double precision) {
        log.info("Random: testing count {}", count);
        for (int i = 0; i < RANDOM_TEST_REPEAT; ++i) {
            log.info("Random: test round #{}", i);
            List<Point2d> points = new ArrayList<>();
            for (int j = 0; j < count; ++j) {
                points.add(genRandomPoint());
            }
            testPoints(points, precision);
        }
    }

    void testPoints(List<Point2d> points, double precision) {
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
                        double[] bounds = new double[3];
                        int depth = BoxSector.findMinEnclosing(bounds, p, q);
                        if (depth == Integer.MAX_VALUE || bounds[2] < precision) {
                            isNewPoint = false;
                            break;
                        }
                    }
                    if(isNewPoint){
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
    }

    private PointSector genRandomPointSector(double precision) {
        return new PointSector(genRandomPoint(), precision);
    }

    private Point2d genRandomPoint() {
        return new Point2d(random.nextDouble(), random.nextDouble());
    }


}