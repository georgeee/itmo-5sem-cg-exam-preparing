package ru.georgeee.itmo.sem5.cg.quadtree;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import ru.georgeee.itmo.sem5.cg.common.EqualComparator;
import ru.georgeee.itmo.sem5.cg.common.Point2d;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static java.lang.Math.floor;

/**
 * Contracts:
 * Box sector after it's initialized (i.e. links are set) contains at least two non-empty sectors
 * When one of them is to be removed, then box itself should be replaced with remaining sector (in parent)
 */
class BoxSector implements Sector {
    @Getter
    private Sector nw, ne, sw, se;

    @Getter
    @Setter
    private Sector link;

    @Getter
    @Setter
    private Sector parent;

    private final EqualComparator<Point2d> equalComparator;

    private BoxSector(EqualComparator<Point2d> equalComparator) {
        this.equalComparator = equalComparator;
    }

    @Getter
    private Point2d min, max;

    private void updateTreeConstraints() {
        updateTreeConstraints(SubSectorType.NW);
        updateTreeConstraints(SubSectorType.NE);
        updateTreeConstraints(SubSectorType.SW);
        updateTreeConstraints(SubSectorType.SE);
    }

    @Override
    public Sector add(PointSector pointSector) {
        Point2d point = pointSector.getPoint();
        SubSectorType type = determineType(point);
        if (type != null) {
            setSubSector(type, addToSubSector(type, pointSector));
            updateTreeConstraints();
            return this;
        } else {
            return createSector(this, pointSector, equalComparator);
        }
    }

    @Override
    public Sector findLowestPredecessor(Point2d point) {
        SubSectorType type = determineType(point);
        if (type != null) {
            Sector subSector = getSubSector(type);
            if (subSector == null || subSector instanceof PointSector) {
                return this;
            }
            return subSector.findLowestPredecessor(point);
        } else {
            //We can't dig down any more, so we return parent
            //Parent is obviously a predecessor (null - whole 1x1 square), cause if it wasn't recursion would stop
            //some steps earlier
            return getParent();
        }
    }

    @Getter(AccessLevel.PACKAGE)
    private int depth;
    @Getter(AccessLevel.PACKAGE)
    private double[] boundaries;

    void calcBoundaries() {
        if (this.boundaries == null) {
            this.boundaries = new double[3];
            depth = findMinEnclosing(this.boundaries, getMin(), getMax());
        }
    }

    private SubSectorType determineType(Point2d point) {
        calcBoundaries();
        return determineType(point, boundaries[0], boundaries[1], boundaries[2]);
    }

    static SubSectorType determineType(Point2d point, double bx, double by, double len) {
        if (bx <= point.getX() && point.getX() < bx + len && by <= point.getY() && point.getY() < by + len) {
            if (point.getX() < bx + len / 2) {
                if (point.getY() < by + len / 2) {
                    return SubSectorType.NW;
                } else {
                    return SubSectorType.NE;
                }
            } else {
                if (point.getY() < by + len / 2) {
                    return SubSectorType.SW;
                } else {
                    return SubSectorType.SE;
                }
            }
        }
        return null;
    }


    private Sector addToSubSector(SubSectorType type, PointSector pointSector) {
        Sector subSector = getSubSector(type);
        if (subSector == null) {
            return pointSector;
        }
        if (subSector instanceof PointSector) {
            return createSector(subSector, pointSector, equalComparator);
        }
        return subSector.add(pointSector);
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
        SubSectorType type = determineType(topLeft, bx, by, len);
        if (type == null) {
            String msg = String.format("Top left point %s (sector %s) is outside boundaries: bx=%f by=%f len=%f",
                    topLeft, sector, bx, by, len);
            throw new IllegalStateException(msg);
        }
        checkSectorIsNull(type);
        setSubSector(type, sector);
        updateTreeConstraints();
    }

    private void checkSectorIsNull(SubSectorType type) {
        Sector sector = getSubSector(type);
        if (sector != null) {
            String msg = String.format("Sub sector must be null: type=%s, sector=%s", type, sector);
            throw new IllegalArgumentException(msg);
        }
    }

    private void updateTreeConstraints(SubSectorType type) {
        Sector subSector = getSubSector(type);
        if (subSector != null) {
            if (min == null || min.compareTo(subSector.getMin()) > 0) {
                min = subSector.getMin();
            }
            if (max == null || max.compareTo(subSector.getMax()) < 0) {
                max = subSector.getMax();
            }
            subSector.setParent(this);
            if (link != null) {
                if (!(link instanceof BoxSector)) {
                    String msg = String.format("Link from box sector should be box sector itself: this=%s link=%s", this, link);
                    throw new IllegalStateException(msg);
                }
                subSector.setLink(((BoxSector) link).getSubSector(type));
            }
        }
    }

    Sector getSubSector(SubSectorType type) {
        switch (type) {
            case NW:
                return nw;
            case NE:
                return ne;
            case SE:
                return se;
            case SW:
                return sw;
            default:
                throw new IllegalStateException("Unknown type: " + type);
        }
    }

    private void setSubSector(SubSectorType type, Sector value) {
        switch (type) {
            case NW:
                nw = value;
                break;
            case NE:
                ne = value;
                break;
            case SE:
                se = value;
                break;
            case SW:
                sw = value;
                break;
            default:
                throw new IllegalStateException("Unknown type: " + type);
        }
    }

    private static int findMinEnclosing2(double[] result, Point2d... points) {
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

    public static BoxSector createSector(Sector a, PointSector b, EqualComparator<Point2d> equalComparator) {
        if (a instanceof PointSector && equalComparator.test(((PointSector) a).getPoint(), b.getPoint())) {
            throw new PointAlreadyExistsException(b.getPoint());
        }
        double[] boundaries = new double[3];
        findMinEnclosing2(boundaries, a.getMin(), a.getMax(), b.getPoint());
        BoxSector result = new BoxSector(equalComparator);
        result.addToEmptySubSector(a, boundaries[0], boundaries[1], boundaries[2]);
        result.addToEmptySubSector(b, boundaries[0], boundaries[1], boundaries[2]);
        return result;
    }

    @Override
    public String toString() {
        calcBoundaries();
        return String.format("BoxSector(bx=%f, by=%f, len=%f, depth=%d)", boundaries[0], boundaries[1], boundaries[2], depth);
    }

}
