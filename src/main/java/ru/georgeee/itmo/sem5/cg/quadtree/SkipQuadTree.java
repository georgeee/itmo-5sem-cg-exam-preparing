package ru.georgeee.itmo.sem5.cg.quadtree;

import ru.georgeee.itmo.sem5.cg.common.Point2d;
import ru.georgeee.itmo.sem5.cg.common.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Basic compressed QuadTree for 1x1 square
 */

public class SkipQuadTree implements QuadTree {
    private final Coin coin;
    private final double precision;

    private final List<Sector> layers = new ArrayList<>();

    public SkipQuadTree() {
        this(createRandomCoin(), 0);
    }

    public SkipQuadTree(Coin coin) {
        this(coin, 0);
    }

    public SkipQuadTree(Coin coin, double precision) {
        this.coin = coin;
        this.precision = precision;
    }

    private static Coin createRandomCoin() {
        Random random = Utils.createRandom();
        return (i, p) -> random.nextBoolean();
    }

    @Override
    public boolean contains(Point2d point) {
        return false;
    }

    @Override
    public boolean add(Point2d point) {
        List<Sector> predChain = getPredecessorChain(point);
        for (int i = 0; i < layers.size() + 1; ++i) {
            if (i > 0 && coin.decide(i, point)) break;
            PointSector pointSector = new PointSector(point, precision);
            if (i == layers.size()) {
                layers.add(pointSector);
                if (i > 0) {
                    pointSector.setLink(layers.get(i - 1));
                }
            } else {
                try {
                    Sector pred = predChain.get(i);
                    if (pred == null) {
                        Sector newSector = layers.get(i).add(pointSector);
                        if (i > 0) {
                            newSector.setLink(layers.get(i - 1));
                        }
                        layers.set(i, newSector);
                    } else {
                        Sector pred2 = pred.add(pointSector);
                        if (pred2 != pred) {
                            throw new IllegalStateException("Predecessor shouldn't change when adding: " + pred + " != " + pred2);
                        }
                    }
                } catch (PointAlreadyExistsException e) {
                    if (i == 0) {
                        return false;
                    } else {
                        throw new IllegalStateException("Point exists in upper layer, being absent at lower", e);
                    }
                }
            }
        }
        return true;
    }

    private List<Sector> getPredecessorChain(Point2d point) {
        List<Sector> predChain = new ArrayList<>(layers.size());
        Sector lastLink = null;
        for (int i = layers.size() - 1; i >= 0; ++i) {
            if (lastLink == null) {
                lastLink = layers.get(i);
                assert i == layers.size() - 1;
            }
            Sector pred = lastLink.findLowestPredecessor(point);
            predChain.add(pred);
            lastLink = pred.getLink();
        }
        return predChain;
    }

    @Override
    public boolean remove(Point2d point) {
        return false;
    }

}
