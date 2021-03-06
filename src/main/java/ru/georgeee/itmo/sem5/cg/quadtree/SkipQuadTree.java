package ru.georgeee.itmo.sem5.cg.quadtree;

import ru.georgeee.itmo.sem5.cg.common.Point2d;
import ru.georgeee.itmo.sem5.cg.common.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class SkipQuadTree implements QuadTree {
    private final Coin coin;
    private final double precision;

    private static final double CLOSEST_TO_ONE = Double.longBitsToDouble(0x3fefffffffffffffL); //0.9999...
    private static final Point2d ZERO_POINT = new Point2d(0, 0);
    private static final Point2d ONE_POINT = new Point2d(CLOSEST_TO_ONE, CLOSEST_TO_ONE);


    private boolean containsZero, containsOne;

    final List<BoxSector> layers = new ArrayList<>();

    public SkipQuadTree() {
        this(null, 0);
    }

    public SkipQuadTree(Coin coin) {
        this(coin, 0);
    }

    public SkipQuadTree(Coin coin, double precision) {
        this.coin = coin != null ? coin : createRandomCoin();
        this.precision = precision;
    }

    private static Coin createRandomCoin() {
        Random random = Utils.createRandom();
        return (i, p) -> random.nextBoolean();
    }

    @Override
    public boolean contains(Point2d point) {
        if (BoxSector.checkEquals(ZERO_POINT, point, precision)) {
            return containsZero;
        }
        if (BoxSector.checkEquals(ONE_POINT, point, precision)) {
            return containsOne;
        }
        if (layers.isEmpty()) return false;
        List<BoxSector> predChain = getPredecessorChain(point);
        return predChain.get(0).contains(point);
    }

    @Override
    public boolean add(Point2d point) {
        if (BoxSector.checkEquals(ZERO_POINT, point, precision)) {
            boolean contained = containsZero;
            containsZero = true;
            return !contained;
        }
        if (BoxSector.checkEquals(ONE_POINT, point, precision)) {
            boolean contained = containsOne;
            containsOne = true;
            return !contained;
        }
        List<BoxSector> predChain = getPredecessorChain(point);
        int i = 0;
        try {
            for (i = 0; i < layers.size(); ++i) {
                if (i > 0 && !coin.decide(i, point)) break;
                BoxSector pred = predChain.get(i);
                if (pred == null) {
                    //Cause every layer is [0;0]-[1;1] square, we can be sure, layer's head remain invariable
                    layers.get(i).add(point);
                } else {
                    BoxSector pred2 = pred.add(point);
                    if (pred2 != pred) {
                        throw new IllegalStateException("Predecessor shouldn't change when adding: " + pred + " != " + pred2);
                    }
                }
            }
            if (i == layers.size()) {
                for (i = layers.size(); i == 0 || coin.decide(i, point); ++i) {
                    BoxSector boxSector = new BoxSector(ZERO_POINT, ONE_POINT, precision);
                    if (i > 0) {
                        boxSector.setLink(layers.get(i - 1));
                    }
                    boxSector.add(point);
                    layers.add(boxSector);
                }
            }
        } catch (PointAlreadyExistsException e) {
            if (i == 0) {
                return false;
            } else {
                throw new IllegalStateException("Point exists in upper layer, being absent at lower", e);
            }
        }
        return true;
    }

    private List<BoxSector> getPredecessorChain(Point2d point) {
        List<BoxSector> predChain = new ArrayList<>(layers.size());
        BoxSector lastLink = null;
        for (int i = layers.size() - 1; i >= 0; --i) {
            if (lastLink == null) {
                lastLink = layers.get(i);
                assert i == layers.size() - 1;
            }
            BoxSector pred = lastLink.findLowestPredecessor(point);
            predChain.add(pred);
            lastLink = pred.getLink();
        }
        Collections.reverse(predChain);
        return predChain;
    }

    @Override
    public boolean remove(Point2d point) {
        if (BoxSector.checkEquals(ZERO_POINT, point, precision)) {
            boolean contained = containsZero;
            containsZero = false;
            return contained;
        }
        if (BoxSector.checkEquals(ONE_POINT, point, precision)) {
            boolean contained = containsOne;
            containsOne = false;
            return contained;
        }
        List<BoxSector> predChain = getPredecessorChain(point);
        for (int i = 0; i < predChain.size(); ++i) {
            BoxSector pred = predChain.get(i);
            try {
                pred.remove(point);
            } catch (PointIsAbsentException e) {
                return i > 0;
            }
        }
        return true;
    }

}
