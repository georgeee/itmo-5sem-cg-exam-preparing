package ru.georgeee.itmo.sem5.cg.quadtree;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.georgeee.itmo.sem5.cg.common.Point2d;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

abstract class AbstractTest extends TestCase {
    private static final Logger log = LoggerFactory.getLogger(AbstractTest.class);
    private static final int[] LARGE_RANDOM_TEST_COUNTS = {20000, 50000, 100000, 1000000};
    private static final int LARGE_RANDOM_TEST_REPEAT = 3;
    private static final int[] RANDOM_TEST_COUNTS = {3, 5, 10, 100, 1000};
    private static final int RANDOM_TEST_REPEAT = 10;
    final Random random = new Random(System.currentTimeMillis());

    void validateSector(Sector sector, Set<Point2d> points) {
        validateSectorImpl(sector, points, null);
        if (points != null) {
            assertTrue(points.isEmpty());
        }
    }

    private void validateSectorImpl(Sector sector, Set<Point2d> points, BoxSector parent) {
        if (sector == null) {
            return;
        }
        assertTrue(sector.getParent() == parent);
        //Min, max transitivity
        if (parent != null && parent.getTopLeft() != null) {
            assertTrue(sector.getTopLeft().compareTo(parent.getTopLeft()) >= 0);
        }
        if (sector instanceof PointSector) {
            Point2d point = ((PointSector) sector).getPoint();
            if (points != null) {
                assertTrue(points.remove(point));
            }
            if (parent != null && parent.getTopLeft() != null) {
                assertTrue(point.compareTo(parent.getTopLeft()) >= 0);
            }
        } else {
            assertTrue(sector instanceof BoxSector);
            BoxSector boxSector = (BoxSector) sector;
            List<SubSectorType> allTypes = Arrays.asList(SubSectorType.values());
            assertTrue(boxSector.countNonNull() >= 2);
            Predicate<SubSectorType> notNull = t -> boxSector.getSubSector(t) != null;
            allTypes.stream().filter(notNull).forEach(type -> {
                checkWithin(type, boxSector);
                Sector subSector = boxSector.getSubSector(type);
                validateSectorImpl(subSector, points, boxSector);
            });
        }
    }

    void checkWithin(SubSectorType type, BoxSector boxSector) {
        Sector subSector = boxSector.getSubSector(type);
        checkWithin(subSector.getTopLeft(), type, boxSector.getTopLeft().getX(), boxSector.getTopLeft().getY(), boxSector.getLen());
    }

    void checkWithin(Point2d point, SubSectorType type, double[] bounds) {
        checkWithin(point, type, bounds[0], bounds[1], bounds[2]);
    }

    void checkWithin(Point2d point, SubSectorType type, double bx, double by, double len) {
        SubSectorType realType = BoxSector.determineType(point, bx, by, len);
        if (type == null) {
            assertNotNull(realType);
        } else {
            assertEquals(type, realType);
        }
    }

    Void testManual(Function<List<Point2d>, ?> handler, double[][] datasets) {
        for (double[] data : datasets) {
            List<Point2d> points = new ArrayList<>();
            for (int i = 0; i < data.length / 2; ++i) {
                points.add(new Point2d(data[2 * i], data[2 * i + 1]));
            }
            handler.apply(points);
        }
        return null;
    }


    Point2d genRandomPoint() {
        return new Point2d(random.nextDouble(), random.nextDouble());
    }

    Void testOnPrecisions(Function<Double, ?> f) {
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
        return null;
    }

    Void testRandomPointsLarge(Function<List<Point2d>, ?> testFunction) {
        testRandomPoints(testFunction, LARGE_RANDOM_TEST_COUNTS, LARGE_RANDOM_TEST_REPEAT);
        return null;
    }

    Void testRandomPoints(Function<List<Point2d>, ?> testFunction) {
        testRandomPoints(testFunction, RANDOM_TEST_COUNTS, RANDOM_TEST_REPEAT);
        return null;
    }

    Void testRandomPoints(Function<List<Point2d>, ?> testFunction, int[] counts, int repeat) {
        for (int count : counts) {
            testRandomPoints(testFunction, count, repeat);
        }
        return null;
    }

    void testRandomPoints(Function<List<Point2d>, ?> testFunction, int count, int repeat) {
        log.info("Random: testing count {}", count);
        for (int i = 0; i < repeat; ++i) {
            log.info("Random: test round #{}", i);
            long time = System.currentTimeMillis();
            List<Point2d> points = new ArrayList<>();
            for (int j = 0; j < count; ++j) {
                points.add(genRandomPoint());
            }
            testFunction.apply(points);
            log.info("--- round #{} completed, {}ms", i, (System.currentTimeMillis()-time));
        }
    }

    void checkLinks(BoxSector layer, BoxSector prevLayer) {
        BoxSector realLink = prevLayer.findUnderlying(layer.getTopLeft(), layer.getDepth());
        assertTrue(layer.getLink() == realLink);
        for (SubSectorType type : SubSectorType.values()) {
            if (layer.getSubSector(type) instanceof BoxSector) {
                BoxSector subSector = (BoxSector) layer.getSubSector(type);
                checkLinks(subSector, realLink);
            }
        }
    }

    void testOnCoins(Function<Coin, ?> tester) {
        log.info("Testing on toggle coin");
        tester.apply(new ToggleCoin());
        log.info("Testing on fair random coin");
        tester.apply(null);
    }


    Void testSquadTree(List<Point2d> points, double precision, Coin coin) {
        try {
            SkipQuadTree quadTree = new SkipQuadTree(coin, precision);
            for (Point2d point : points) {
                quadTree.add(point);
            }
            for (BoxSector layer : quadTree.layers) {
                validateSector(layer, null);
            }
            for (int i = 1; i < quadTree.layers.size(); ++i) {
                BoxSector layer = quadTree.layers.get(i);
                BoxSector prevLayer = quadTree.layers.get(i - 1);
                checkLinks(layer, prevLayer);
            }
        } catch (AssertionError | AssertionFailedError | RuntimeException e) {
            log.error("Assertion failed, points: {}", points.toString(), e);
            throw e;
        }
        return null;
    }

    static class ToggleCoin implements Coin {
        private boolean toggle;

        @Override
        public boolean decide(int layer, Point2d point) {
            return toggle = !toggle;
        }
    }

}
