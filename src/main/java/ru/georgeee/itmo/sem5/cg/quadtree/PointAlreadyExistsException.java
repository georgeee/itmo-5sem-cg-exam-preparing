package ru.georgeee.itmo.sem5.cg.quadtree;

import lombok.Getter;
import ru.georgeee.itmo.sem5.cg.common.Point2d;

public class PointAlreadyExistsException extends RuntimeException {
    @Getter
    private final Point2d point;

    public PointAlreadyExistsException(Point2d point) {
        this.point = point;
    }
}
