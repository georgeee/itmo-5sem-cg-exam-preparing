package ru.georgeee.itmo.sem5.cg.quadtree;

import junit.framework.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.georgeee.itmo.sem5.cg.common.Point2d;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

abstract class AbstractTest extends TestCase {
    private static final Logger log = LoggerFactory.getLogger(AbstractTest.class);
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
            Predicate<SubSectorType> notNull = t -> boxSector.getSubSector(t) != null;
            assertTrue(allTypes.stream().filter(notNull).count() >= 2);
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

    Point2d genRandomPoint() {
        return new Point2d(random.nextDouble(), random.nextDouble());
    }

    Void testOnPrecisions(Function<Double, Void> f) {
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

    Void testRandomPoints(Function<List<Point2d>, Void> testFunction) {
        for (int count : RANDOM_TEST_COUNTS) {
            testRandomPoints(count, testFunction);
        }
        return null;
    }

    void testRandomPoints(int count, Function<List<Point2d>, Void> testFunction) {
        log.info("Random: testing count {}", count);
        for (int i = 0; i < RANDOM_TEST_REPEAT; ++i) {
            log.info("Random: test round #{}", i);
            List<Point2d> points = new ArrayList<>();
            for (int j = 0; j < count; ++j) {
                points.add(genRandomPoint());
            }
            testFunction.apply(points);
        }
    }

}
