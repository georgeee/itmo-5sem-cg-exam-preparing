package ru.georgeee.itmo.sem5.cg.quadtree;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.georgeee.itmo.sem5.cg.common.Point2d;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.BiPredicate;

import static java.lang.Math.floor;

/**
 * Contracts:
 * Box sector after it's initialized (i.e. links are set) contains at least two non-empty sectors
 * When one of them is to be removed, then box itself should be replaced with remaining sector (in parent)
 */
@ToString(of = {"nw", "ne", "sw", "se"})
public class BoxSector implements Sector {
    @Getter
    private Sector nw, ne, sw, se;

    @Getter @Setter
    private Sector link;

    @Getter @Setter
    private Sector parent;

    private final BiPredicate<Point2d, Point2d> equalComparator;

    private BoxSector(BiPredicate<Point2d, Point2d> equalComparator) {
        this.equalComparator = equalComparator;
    }

    @Getter
    private Point2d min, max;

    private void updateTreeConstraints() {
        updateTreeConstraints(nw);
        updateTreeConstraints(ne);
        updateTreeConstraints(sw);
        updateTreeConstraints(se);
    }

    @Override
    public Sector add(PointSector pointSector) {
        double[] boundaries = new double[3];
        findMinEnclosing(boundaries, getMin(), getMax());

        double bx = boundaries[0];
        double by = boundaries[1];
        double len = boundaries[2];
        Point2d point = pointSector.getPoint();
        if (bx <= point.getX() && point.getX() < bx + len && by <= point.getY() && point.getY() < by + len) {
            addToSelf(pointSector, bx, by, len);
            return this;
        } else {
            return createSector(this, pointSector, equalComparator);
        }
    }

    /**
     * Inserts point to one of sectors
     *
     * @param pointSector point sector to insert
     * @param bx    topLeft corner of this, x
     * @param by    topLeft corner of this, x
     * @param len   length of side
     */
    private void addToSelf(PointSector pointSector, double bx, double by, double len) {
        Point2d point = pointSector.getPoint();
        if (point.getX() < bx + len / 2) {
            if (point.getY() < by + len / 2) {
                nw = addToSubSector(nw, pointSector);
            } else {
                ne = addToSubSector(ne, pointSector);
            }
        } else {
            if (point.getY() < by + len / 2) {
                sw = addToSubSector(sw, pointSector);
            } else {
                se = addToSubSector(se, pointSector);
            }
        }
        updateTreeConstraints();
    }

    private Sector addToSubSector(Sector sector, PointSector pointSector) {
        if (sector == null) {
            return pointSector;
        }
        if (sector instanceof PointSector) {
            return createSector(sector, pointSector, equalComparator);
        }
        return sector.add(pointSector);
    }

    /**
     * Sets sector as value for appropriate nw/ne/se/sw sub sector
     *
     * @param sector sector to insert
     * @param bx     topLeft corner of this, x
     * @param by     topLeft corner of this, x
     * @param len    length of side
     */
    private void addToEmptySubSector(Sector sector, double bx, double by, double len) {
        Point2d topLeft = sector.getMin();
        if (topLeft.getX() < bx + len / 2) {
            if (topLeft.getY() < by + len / 2) {
                checkSectorIsNull(nw);
                nw = sector;
            } else {
                checkSectorIsNull(ne);
                ne = sector;
            }
        } else {
            if (topLeft.getY() < by + len / 2) {
                checkSectorIsNull(sw);
                sw = sector;
            } else {
                checkSectorIsNull(se);
                se = sector;
            }
        }
        updateTreeConstraints();
    }

    private void checkSectorIsNull(Sector sector) {
        if (sector != null) {
            throw new IllegalArgumentException("Sub sector must be null!");
        }
    }

    private void updateTreeConstraints(Sector s) {
        if (s != null) {
            if (min == null || min.compareTo(s.getMin()) > 0) {
                min = s.getMin();
            }
            if (max == null || max.compareTo(s.getMax()) < 0) {
                max = s.getMax();
            }
            s.setParent(this);
        }
    }

    private static int findMinEnclosing(double[] result, Point2d... points) {
        List<Point2d> pointList = Arrays.asList(points);
        Point2d min = Collections.min(pointList);
        Point2d max = Collections.max(pointList);
        return findMinEnclosing(result, min, max);
    }

    private static int findMinEnclosing(double[] result, Point2d topLeft, Point2d bottomRight) {
        double u = topLeft.getY();
        double v = bottomRight.getY();
        if (u == v) {
            u = topLeft.getX();
            v = bottomRight.getX();
        }
        int t = 0;
        double twoT = 1;
        while (floor(u) == floor(v)) {
            u *= 2;
            v *= 2;
            ++t;
            twoT *= 2;
        }
        --t;
        twoT /= 2;
        // t >= 0: t < 0 => t == -1 => floor(u0) == floor(v0) => u0 = v0 = 1 => two equal points, which violates contract
        double _twoT = twoT;// / 2;
        double lx = floor(topLeft.getX() * _twoT) / _twoT;
        double ly = floor(topLeft.getY() * _twoT) / _twoT;
        result[0] = lx;
        result[1] = ly;
        result[2] = 1 / twoT;
        return t;
    }

    public static BoxSector createSector(Sector a, PointSector b, BiPredicate<Point2d, Point2d> equalComparator) {
        if(a instanceof PointSector && equalComparator.test(((PointSector) a).getPoint(), b.getPoint())){
            throw new PointAlreadyExistsException(b.getPoint());
        }
        double[] boundaries = new double[3];
        findMinEnclosing(boundaries, a.getMin(), a.getMax(), b.getPoint());
        BoxSector result = new BoxSector(equalComparator);
        result.addToEmptySubSector(a, boundaries[0], boundaries[1], boundaries[2]);
        result.addToEmptySubSector(b, boundaries[0], boundaries[1], boundaries[2]);
        return result;
    }

}
