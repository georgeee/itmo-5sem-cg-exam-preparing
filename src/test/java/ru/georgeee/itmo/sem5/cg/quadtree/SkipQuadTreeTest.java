package ru.georgeee.itmo.sem5.cg.quadtree;

import junit.framework.AssertionFailedError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.georgeee.itmo.sem5.cg.common.Point2d;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class SkipQuadTreeTest extends AbstractTest {
    private static final Logger log = LoggerFactory.getLogger(SkipQuadTreeTest.class);
    private static final double[][] MANUAL_DATA_SETS = {
            {0.24277906389712256, 0.7288499200744581, 0.32330609761165474, 0.8583650947555813, 0.12094814223373274, 0.44575684846814223},
    };

    public void testRandom() {
        log.info("Testing random points");
        testOnCoins(coin -> testOnPrecisions(prec -> testRandomPoints(ps -> testPoints(ps, prec, coin))));
    }

    private void testOnCoins(Function<Coin, Void> tester) {
        log.info("Testing on toggle coin");
        tester.apply(new ToggleCoin());
        log.info("Testing on fair random coin");
        tester.apply(null);
    }

    public void testManual() {
        log.info("Testing manually set point sets");
        testOnCoins(coin -> testOnPrecisions(prec -> testManual(prec, coin)));
    }

    Void testManual(double precision, Coin coin) {
        for (double[] data : MANUAL_DATA_SETS) {
            List<Point2d> points = new ArrayList<>();
            for (int i = 0; i < data.length / 2; ++i) {
                points.add(new Point2d(data[2 * i], data[2 * i + 1]));
            }
            testPoints(points, precision, coin);
        }
        return null;
    }

    private Void testPoints(List<Point2d> points, double precision, Coin coin) {
        try {
            SkipQuadTree quadTree = new SkipQuadTree(coin, precision);
            for (Point2d point : points) {
                quadTree.add(point);
            }
            for (BoxSector layer : quadTree.layers) {
                validateSector(layer, null);
            }
            for (int i = 1; i < quadTree.layers.size(); ++i) {
                BoxSector layer = quadTree.layers.get(i);
                BoxSector prevLayer = quadTree.layers.get(i - 1);
                checkLinks(layer, prevLayer);
            }
        } catch (AssertionError | AssertionFailedError | RuntimeException e) {
            log.error("Assertion failed, points: {}", points.toString(), e);
            throw e;
        }
        return null;
    }

    private void checkLinks(BoxSector layer, BoxSector prevLayer) {
        BoxSector realLink = prevLayer.findUnderlying(layer.getTopLeft(), layer.getDepth());
        assertTrue(layer.getLink() == realLink);
        for (SubSectorType type : SubSectorType.values()) {
            if (layer.getSubSector(type) instanceof BoxSector) {
                BoxSector subSector = (BoxSector) layer.getSubSector(type);
                checkLinks(subSector, realLink);
            }
        }
    }


    private static class ToggleCoin implements Coin {
        private boolean toggle;

        @Override
        public boolean decide(int layer, Point2d point) {
            return toggle = !toggle;
        }
    }
}
