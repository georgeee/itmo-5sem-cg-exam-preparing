package ru.georgeee.itmo.sem5.cg.quadtree;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.georgeee.itmo.sem5.cg.common.Point2d;

import java.util.Objects;

@ToString(of = {"point"})
class PointSector implements Sector {
    @Getter
    private final Point2d point;
    private final double precision;
    @Getter
    @Setter
    private BoxSector parent;

    public PointSector(Point2d point, double precision) {
        if (point == null) {
            throw new NullPointerException();
        }
        this.point = point;
        this.precision = precision;
    }

    @Override
    public Point2d getTopLeft() {
        return point;
    }

    @Override
    public boolean contains(Point2d point) {
        return BoxSector.checkEquals(this.point, point, precision);
    }

    @Override
    public BoxSector add(PointSector pointSector) {
        return new BoxSector(this, pointSector, precision);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PointSector)) return false;
        PointSector that = (PointSector) o;
        return Objects.equals(point, that.point);
    }

    @Override
    public int hashCode() {
        return Objects.hash(point);
    }
}
