package ru.georgeee.itmo.sem5.cg.quadtree;

import ru.georgeee.itmo.sem5.cg.common.Point2d;

public class CompressedQuadTree implements QuadTree {
    private final double precision;
    private Sector root;


    public CompressedQuadTree() {
        this(0);
    }

    public CompressedQuadTree(double precision) {
        this.precision = precision;
    }

    @Override
    public boolean contains(Point2d point) {
        if (root != null) {
            return root.contains(point);
        }
        return false;
    }

    @Override
    public boolean add(Point2d point) {
        PointSector newSector = new PointSector(point, precision);
        if (root == null) {
            root = newSector;
        } else {
            try {
                root = root.add(newSector);
            } catch (PointAlreadyExistsException e) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean remove(Point2d point) {
        if (root != null) {
            try {
                root = root.remove(point);
                return true;
            } catch (PointIsAbsentException e) {
            }
        }
        return false;
    }

}
