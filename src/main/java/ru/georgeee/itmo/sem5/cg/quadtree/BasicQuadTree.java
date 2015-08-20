package ru.georgeee.itmo.sem5.cg.quadtree;

import ru.georgeee.itmo.sem5.cg.common.Point2d;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.BiPredicate;

/**
 * Basic compressed QuadTree for 1x1 square
 */

public class BasicQuadTree implements QuadTree {
    private final BiPredicate<Point2d, Point2d> equalComparator;
    private final Random random = new Random(System.currentTimeMillis());

    private final List<Sector> layers = new ArrayList<>();
    ;

    public BasicQuadTree() {
        this(Point2d::equals);
    }

    public BasicQuadTree(BiPredicate<Point2d, Point2d> equalComparator) {
        this.equalComparator = equalComparator;
    }

    @Override
    public boolean contains(Point2d point) {
        return false;
    }

    @Override
    public boolean add(Point2d point) {
        try {
            List<Sector> predChain = new ArrayList<>();
            for (int i = layers.size() - 1; i >= 0; ++i) {

            }
            //@TODO findSector(Point2d p)
            //Then we jump by link and so on
            //after that we add sector, renew links
            //then iterate till top while coin gives success
            if (rootSector == null) {
                rootSector = new PointSector(point, equalComparator);
            } else {
                PointSector pointSector = new PointSector(point, equalComparator);
                rootSector = rootSector.add(pointSector);
            }
            return true;
        } catch (PointAlreadyExistsException e) {
            return false;
        }
    }

    @Override
    public boolean remove(Point2d point) {
        return false;
    }

//    private void locateTargetSector(Point2d point, ){
//
//    }

}
