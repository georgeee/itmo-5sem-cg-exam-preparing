package ru.georgeee.itmo.sem5.cg.quadtree;

import junit.framework.TestCase;
import ru.georgeee.itmo.sem5.cg.common.Point2d;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

abstract class AbstractTest extends TestCase {
    void validateSector(Sector sector, Set<Point2d> points) {
        validateSectorImpl(sector, points, null);
        if (points != null) {
            assertTrue(points.isEmpty());
        }
    }

    private void validateSectorImpl(Sector sector, Set<Point2d> points, Sector parent) {
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
                validateSectorImpl(subSector, points, sector);
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
}
