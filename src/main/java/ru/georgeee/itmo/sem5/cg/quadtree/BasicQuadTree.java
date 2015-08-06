package ru.georgeee.itmo.sem5.cg.quadtree;

import ru.georgeee.itmo.sem5.cg.common.Point2d;

/**
 * Basic compressed QuadTree for 1x1 square
 */

public class BasicQuadTree implements QuadTree{
    private Sector rootSector;

    @Override
    public boolean contains(Point2d point) {
        return false;
    }

    @Override
    public boolean add(Point2d point) {

        return false;
    }

    @Override
    public boolean remove(Point2d point) {
        return false;
    }

//    private void locateTargetSector(Point2d point, ){
//
//    }

}
