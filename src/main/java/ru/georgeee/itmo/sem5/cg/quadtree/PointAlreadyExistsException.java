package ru.georgeee.itmo.sem5.cg.quadtree;

import lombok.Getter;
import ru.georgeee.itmo.sem5.cg.common.Point2d;

class PointAlreadyExistsException extends RuntimeException {
    @Getter
    private final Point2d point;

    public PointAlreadyExistsException(Point2d point) {
        super("Point already exists: " + point);
        this.point = point;
    }
}
