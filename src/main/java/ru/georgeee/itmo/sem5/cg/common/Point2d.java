package ru.georgeee.itmo.sem5.cg.common;

import lombok.Getter;
import lombok.ToString;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Point2d)) return false;
        Point2d point2d = (Point2d) o;
        return Objects.equals(x, point2d.x) &&
                Objects.equals(y, point2d.y);
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }
}
