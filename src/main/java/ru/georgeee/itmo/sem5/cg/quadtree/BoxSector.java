package ru.georgeee.itmo.sem5.cg.quadtree;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.georgeee.itmo.sem5.cg.common.Point2d;

import static java.lang.Math.floor;

/**
 * Contracts:
 * Box sector after it's initialized (i.e. links are set) contains at least two non-empty sectors
 * When one of them is to be removed, then box itself should be replaced with remaining sector (in parent)
 */
class BoxSector implements Sector {
    private static final Logger log = LoggerFactory.getLogger(BoxSector.class);
    private static final int MANTISSA_LENGTH = 52;
    private final double precision;
    @Getter(AccessLevel.PACKAGE)
    private final int depth;
    @Getter
    private final Point2d topLeft;
    @Getter(AccessLevel.PACKAGE)
    private final double len;
    private Sector nw, ne, sw, se;
    @Getter
    @Setter
    private Sector link;
    @Getter
    @Setter
    private Sector parent;

    public BoxSector(Sector a, PointSector b, double precision) {
        this.precision = precision;
        double[] bounds = new double[3];
        depth = findMinEnclosing(bounds, a.getTopLeft(), b.getPoint());
        len = bounds[2];
        if (depth == Integer.MAX_VALUE || len < precision) {
            throw new PointAlreadyExistsException(b.getPoint());
        }
        topLeft = new Point2d(bounds[0], bounds[1]);
        try {
            addToEmptySubSector(a, bounds[0], bounds[1], bounds[2]);
            addToEmptySubSector(b, bounds[0], bounds[1], bounds[2]);
        } catch (IllegalStateException e) {
            log.error("Failed to add to empty sector: a={} b={} bounds={}", a, b, bounds, e);
            throw e;
        }
    }

    static SubSectorType determineType(Point2d point, double bx, double by, double len) {
        if (bx <= point.getX() && point.getX() < bx + len && by <= point.getY() && point.getY() < by + len) {
            if (point.getX() < bx + len / 2) {
                if (point.getY() < by + len / 2) {
                    return SubSectorType.NW;
                } else {
                    return SubSectorType.SW;
                }
            } else {
                if (point.getY() < by + len / 2) {
                    return SubSectorType.NE;
                } else {
                    return SubSectorType.SE;
                }
            }
        }
        return null;
    }

    /**
     * Finds such maximal int i, that floor(x*2^i) == floor(y*2^i)
     *
     * @param x
     * @param y
     * @return
     */
    static int calcDepth(double x, double y) {
        if (x < 0 || x > 1 || y < 0 || y > 1) {
            throw new IllegalArgumentException("Passed x, y outside [0; 1]: " + x + " " + y);
        }
        if (x == y) {
            return Integer.MAX_VALUE;
        }
        if (x > y) {
            double z = x;
            x = y;
            y = z;
        }
        int yExp = -Math.getExponent(y);
        if (x == 0) {
            return yExp - 1;
        }
        int xExp = -Math.getExponent(x);
        long xMant = Double.doubleToLongBits(x) & ((1L << MANTISSA_LENGTH) - 1);
        long yMant = Double.doubleToLongBits(y) & ((1L << MANTISSA_LENGTH) - 1);
        if (xExp == yExp) {
            //+1 because first 1 isn't counted (double 1,{mant}E{exp} is kept without leading one)
            return xExp + Long.numberOfLeadingZeros(xMant ^ yMant) - (64 - MANTISSA_LENGTH);
        } else {
            return Math.min(xExp, yExp) - 1;
        }
    }

    static int findMinEnclosing(double[] result, Point2d topLeft, Point2d bottomRight) {
        int ox = calcDepth(topLeft.getX(), bottomRight.getX());
        int oy = calcDepth(topLeft.getY(), bottomRight.getY());
        int depth = Math.min(ox, oy);
        double twoT = Math.pow(2.0, depth);
        double lx = floor(topLeft.getX() * twoT) / twoT;
        double ly = floor(topLeft.getY() * twoT) / twoT;
        result[0] = lx;
        result[1] = ly;
        result[2] = 1 / twoT;
        return depth;
    }

    @Override
    public Sector add(PointSector pointSector) {
        Point2d point = pointSector.getPoint();
        SubSectorType type = determineType(point);
        if (type != null) {
            setSubSector(type, addToSubSector(type, pointSector));
            updateTreeConstraints(type);
            return this;
        } else {
            return new BoxSector(this, pointSector, precision);
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

    private SubSectorType determineType(Point2d point) {
        return determineType(point, topLeft.getX(), topLeft.getY(), len);
    }

    private Sector addToSubSector(SubSectorType type, PointSector pointSector) {
        Sector subSector = getSubSector(type);
        if (subSector == null) {
            return pointSector;
        }
        if (subSector instanceof PointSector) {
            return new BoxSector(subSector, pointSector, precision);
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
        Point2d topLeft = sector.getTopLeft();
        SubSectorType type = determineType(topLeft, bx, by, len);
        if (type == null) {
            String msg = String.format("Top left point %s (sector %s) is outside bounds: bx=%f by=%f len=%f",
                    topLeft, sector, bx, by, len);
            throw new IllegalStateException(msg);
        }
        checkSectorIsNull(type);
        setSubSector(type, sector);
        updateTreeConstraints(type);
    }

    private void checkSectorIsNull(SubSectorType type) {
        Sector sector = getSubSector(type);
        if (sector != null) {
            String msg = String.format("Sub sector must be null: type=%s, sector=%s", type, sector);
            throw new IllegalStateException(msg);
        }
    }

    private void updateTreeConstraints(SubSectorType type) {
        Sector subSector = getSubSector(type);
        if (subSector != null) {
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

    @Override
    public String toString() {
        return String.format("BoxSector(bx=%f, by=%f, len=%f, depth=%d)", topLeft.getX(), topLeft.getY(), len, depth);
    }

}
