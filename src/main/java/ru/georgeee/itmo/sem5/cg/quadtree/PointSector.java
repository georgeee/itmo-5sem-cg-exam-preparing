package ru.georgeee.itmo.sem5.cg.quadtree;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.georgeee.itmo.sem5.cg.common.Point2d;

import java.util.Objects;
import java.util.function.BiPredicate;

@ToString
public class PointSector implements Sector {
    @Getter
    private final Point2d point;
    @Getter @Setter
    private Sector parent;
    @Getter @Setter
    private Sector link;

    private final BiPredicate<Point2d, Point2d> equalComparator;

    public PointSector(Point2d point, BiPredicate<Point2d, Point2d> equalComparator) {
        if (point == null) {
            throw new NullPointerException();
        }
        this.point = point;
        this.equalComparator = equalComparator;
    }

    @Override
    public Point2d getMin() {
        return point;
    }

    @Override
    public Point2d getMax() {
        return point;
    }

    @Override
    public Sector add(PointSector pointSector) {
        return BoxSector.createSector(this, pointSector, equalComparator);
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
