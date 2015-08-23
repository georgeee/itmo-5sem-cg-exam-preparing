package ru.georgeee.itmo.sem5.cg.quadtree;

import ru.georgeee.itmo.sem5.cg.common.Point2d;

interface Sector {
    Point2d getTopLeft();

    BoxSector getParent();

    void setParent(BoxSector s);

    BoxSector add(PointSector pointSector);
}
