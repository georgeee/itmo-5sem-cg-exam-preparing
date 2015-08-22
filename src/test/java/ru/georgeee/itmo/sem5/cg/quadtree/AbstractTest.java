package ru.georgeee.itmo.sem5.cg.quadtree;

import junit.framework.TestCase;
import ru.georgeee.itmo.sem5.cg.common.Point2d;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

abstract class AbstractTest extends TestCase {
    void validateSector(Sector sector, Set<Point2d> points) {
        validateSectorImpl(sector, points, null, null, null);
        assertTrue(points.isEmpty());
    }

    private void validateSectorImpl(Sector sector, Set<Point2d> points, Sector parent, Point2d min, Point2d max) {
        if (sector == null) {
            return;
        }
        assertTrue(sector.getParent() == parent);
        //Min, max transitivity
        assertTrue(sector.getMin().compareTo(min) >= 0);
        assertTrue(sector.getMax().compareTo(max) <= 0);
        if (sector instanceof PointSector) {
            Point2d point = ((PointSector) sector).getPoint();
            assertTrue(points.contains(point));
            points.remove(point);
            assertTrue(point.compareTo(min) >= 0);
            assertTrue(point.compareTo(max) <= 0);
        } else {
            assertTrue(sector instanceof BoxSector);
            BoxSector boxSector = (BoxSector) sector;
            boxSector.calcBoundaries();
            double bx = boxSector.getBoundaries()[0];
            double by = boxSector.getBoundaries()[1];
            double blen = boxSector.getBoundaries()[2];
            List<SubSectorType> allTypes = Arrays.asList(SubSectorType.values());
            Stream<SubSectorType> nonNull = allTypes.stream().filter(t -> boxSector.getSubSector(t) != null);
            assertTrue(nonNull.count() >= 2);
            nonNull.forEach(type -> {
                Sector subSector = boxSector.getSubSector(type);
                checkWithin(subSector, type, bx, by, blen);
                validateSectorImpl(subSector, points, sector, sector.getMin(), sector.getMax());
            });
        }
    }

    void checkWithin(Sector subSector, SubSectorType type, double bx, double by, double len) {
        checkWithin(subSector.getMin(), type, bx, by, len);
        checkWithin(subSector.getMax(), type, bx, by, len);
    }

    void checkWithin(Point2d point, SubSectorType type, double bx, double by, double len) {
        SubSectorType realType = BoxSector.determineType(point, bx, by, len);
        if (type == null) {
            assertNotNull(realType);
        } else {
            assertEquals(type, realType);
        }
    }
}
