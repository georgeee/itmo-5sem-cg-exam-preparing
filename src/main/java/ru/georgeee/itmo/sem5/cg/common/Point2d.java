package ru.georgeee.itmo.sem5.cg.common;

import lombok.Getter;
import lombok.ToString;

@ToString
public class Point2d implements Comparable<Point2d> {
    @Getter
    private final double x, y;

    public Point2d(double x, double y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public int compareTo(Point2d o) {
        return x != o.x ? Double.compare(x, o.x) : Double.compare(y, o.y);
    }
}
