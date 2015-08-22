package ru.georgeee.itmo.sem5.cg.common;

public class PrecisionComparator implements EqualComparator<Point2d> {
    private final double eps;

    public PrecisionComparator(double eps) {
        this.eps = eps;
    }

    @Override
    public boolean test(Point2d p, Point2d q) {
        return Math.abs(p.getX()-q.getX()) < eps
                && Math.abs(p.getY()-q.getY()) < eps;
    }
}
