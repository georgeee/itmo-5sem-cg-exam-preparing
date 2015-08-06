package ru.georgeee.itmo.sem5.cg.quadtree;

import lombok.Getter;
import lombok.ToString;
import ru.georgeee.itmo.sem5.cg.common.Point2d;

@ToString
public class PointSector implements Sector {
    @Getter
    private final Point2d point;

    public PointSector(Point2d point) {
        if (point == null) {
            throw new NullPointerException();
        }
        this.point = point;
    }

    @Override
    public Point2d getMin() {
        return point;
    }

    @Override
    public Point2d getMax() {
        return point;
    }


}
