package ru.georgeee.itmo.sem5.cg.quadtree;

import ru.georgeee.itmo.sem5.cg.common.Point2d;

@FunctionalInterface
public interface Coin {
    boolean decide(int layer, Point2d point);
}
