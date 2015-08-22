package ru.georgeee.itmo.sem5.cg.quadtree;

import junit.framework.AssertionFailedError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.georgeee.itmo.sem5.cg.common.EqualComparator;
import ru.georgeee.itmo.sem5.cg.common.Point2d;
import ru.georgeee.itmo.sem5.cg.common.PrecisionComparator;

import java.util.*;
import java.util.function.Function;

public class BoxSectorTest extends AbstractTest {
    private static final Logger log = LoggerFactory.getLogger(BoxSectorTest.class);
    private final Random random = new Random(System.currentTimeMillis());
    private static final int RANDOM_TEST_COUNT = 100;
    private static final int RANDOM_TEST_REPEAT = 10;

    public void testRandom() {
        log.info("Testing random points");
        testOnComparators(this::testRandomPoints);
    }

    void testOnComparators(Function<EqualComparator<Point2d>, Void> f) {
        log.info("Testing on fair comparator");
        f.apply(Point2d::equals);
        log.info("Testing on precision 10^-9");
        f.apply(new PrecisionComparator(1.0E-9));
        log.info("Testing on precision 10^-5");
        f.apply(new PrecisionComparator(1.0E-5));
        log.info("Testing on precision 10^-3");
        f.apply(new PrecisionComparator(1.0E-3));
        log.info("Testing on precision 10^-1");
        f.apply(new PrecisionComparator(1.0E-1));
    }

    Void testRandomPoints(EqualComparator<Point2d> equalComparator) {
        for (int i = 0; i < RANDOM_TEST_REPEAT; ++i) {
            log.info("Random: test set #{}", i);
            testRandomPoints(RANDOM_TEST_COUNT, equalComparator);
        }
        return null;
    }

    void testRandomPoints(int count, EqualComparator<Point2d> equalComparator) {
        List<Point2d> points = new ArrayList<>();
        for (int i = 0; i < count; ++i) {
            points.add(genRandomPoint());
        }
        testPoints(points, equalComparator);
    }

    void testPoints(List<Point2d> points, EqualComparator<Point2d> equalComparator) {
        try {
            Sector sector = null;
            Set<Point2d> pointSet = new TreeSet<>(equalComparator);
            for (Point2d p : points) {
                PointSector pointSector = new PointSector(p, equalComparator);
                boolean contains = pointSet.add(p);
                if (sector == null) {
                    sector = pointSector;
                } else {
                    try {
                        sector = sector.add(pointSector);
                        assertFalse(contains);
                    } catch (PointAlreadyExistsException e) {
                        assertTrue(equalComparator.test(e.getPoint(), p));
                        assertTrue(contains);
                    }
                }
            }
            validateSector(sector, pointSet);
        } catch (AssertionFailedError e) {
            log.info("Points: {}", points);
        }
    }

    private PointSector genRandomPointSector(EqualComparator<Point2d> equalComparator) {
        return new PointSector(genRandomPoint(), equalComparator);
    }

    private Point2d genRandomPoint() {
        return new Point2d(random.nextDouble(), random.nextDouble());
    }


}