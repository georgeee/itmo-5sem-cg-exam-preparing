package ru.georgeee.itmo.sem5.cg.quadtree;

import ru.georgeee.itmo.sem5.cg.common.Point2d;

interface Sector {
    Point2d getTopLeft();

    Sector getParent();

    void setParent(Sector s);

    Sector getLink();

    void setLink(Sector s);

    Sector add(PointSector pointSector);

    Sector findLowestPredecessor(Point2d point);
}
