package ru.georgeee.itmo.sem5.cg.quadtree;

import ru.georgeee.itmo.sem5.cg.common.Point2d;

public interface QuadTree {
    boolean contains(Point2d point);

    boolean add(Point2d point);

    boolean remove(Point2d point);
}
