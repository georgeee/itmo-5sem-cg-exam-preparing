package ru.georgeee.itmo.sem5.cg.quadtree;

import junit.framework.TestCase;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import ru.georgeee.itmo.sem5.cg.common.Point2d;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BoxSectorTest extends TestCase {

    private List<Pair<BoxSector, List<Point2d>>> getTestPairs() {
        List<Pair<BoxSector, List<Point2d>>> testPairs = new ArrayList<>();
        testPairs.add(createBoxSectorForNWAndSE(0.1, 0.1, 0.8, 0.8));
        testPairs.add(createBoxSectorForNWAndSE(0.38, 0.18, 0.45, 0.2));
        return testPairs;
    }

    private Pair<BoxSector, List<Point2d>> createBoxSectorForNWAndSE(double x1, double y1, double x2, double y2) {
        BoxSector boxSector = new BoxSector();
        Point2d p1 = new Point2d(x1, y1);
        Point2d p2 = new Point2d(x2, y2);
        boxSector.setNW(new PointSector(p1));
        boxSector.setSE(new PointSector(p2));
        List<Point2d> points = new ArrayList<>();
        points.add(p1);
        points.add(p2);
        return new ImmutablePair<>(boxSector, points);
    }

    public void testCalcEffectiveBoundaries() throws Exception {
        for (Pair<BoxSector, List<Point2d>> testPair : getTestPairs()) {
            testCalcEffectiveBoundaries(testPair.getLeft(), testPair.getRight());
        }
    }

    private void testCalcEffectiveBoundaries(BoxSector boxSector, List<Point2d> points) {
        double[] res = new double[3];
        int t = boxSector.calcEffectiveBoundaries(res);
        double lx = res[0];
        double ly = res[1];
        double rTwoT = res[2];
        int counters [] = new int[4];
        points.forEach(p -> {
            assertTrue(lx <= p.getX() && p.getX() < lx + rTwoT);
            assertTrue(ly <= p.getY() && p.getY() < ly + rTwoT);
            if (p.getY() < ly + rTwoT / 2) {
                if (p.getX() < lx + rTwoT / 2) {
                    ++counters[0];
                } else {
                    ++counters[1];
                }
            } else {
                if (p.getX() < lx + rTwoT / 2) {
                    ++counters[2];
                } else {
                    ++counters[3];
                }
            }
        });
        int nonNull = 0;
        for (int counter : counters) {
            nonNull += counter > 0 ? 1 : 0;
        }
        System.out.println("==============");
        System.out.println(boxSector);
        System.out.println(points);
        System.out.println(t);
        System.out.println(Arrays.toString(res));
        assertTrue(nonNull > 1);
    }


}