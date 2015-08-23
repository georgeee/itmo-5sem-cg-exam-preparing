package ru.georgeee.itmo.sem5.cg.quadtree;

import lombok.Getter;
import ru.georgeee.itmo.sem5.cg.common.Point2d;

class PointIsAbsentException extends RuntimeException {
    @Getter
    private final Point2d point;

    public PointIsAbsentException(Point2d point) {
        super("Point is absent: " + point);
        this.point = point;
    }
}
