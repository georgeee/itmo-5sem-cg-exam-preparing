package ru.georgeee.itmo.sem5.cg.quadtree;

import lombok.Getter;
import lombok.ToString;
import ru.georgeee.itmo.sem5.cg.common.Point2d;

import static java.lang.Math.floor;
import static java.lang.Math.pow;

/**
 * Contracts:
 * Box sector after it's initialized (i.e. links are set) contains at least two non-empty sectors
 * When one of them is to be removed, then box itself should be replaced with remaining sector (in parent)
 */
@ToString(of = {"nw", "ne", "sw", "se"})
public class BoxSector implements Sector {
    @Getter
    private Sector nw, ne, sw, se;

    @Getter
    private Point2d min, max;

    public void setNW(Sector sector) {
        this.nw = sector;
        recalculateExtremes();
    }

    public void setNE(Sector sector) {
        this.ne = sector;
        recalculateExtremes();
    }

    public void setSW(Sector sector) {
        this.sw = sector;
        recalculateExtremes();
    }

    public void setSE(Sector sector) {
        this.se = sector;
        recalculateExtremes();
    }

    private void recalculateExtremes() {
        recalculateExtremes(nw);
        recalculateExtremes(ne);
        recalculateExtremes(sw);
        recalculateExtremes(se);
    }

    public int calcEffectiveBoundaries(double [] result){
        Point2d topLeft = getMin();
        Point2d bottomRight = getMax();
        double u = topLeft.getY();
        double v = bottomRight.getY();
        if(u == v){
            u = topLeft.getX();
            v = bottomRight.getX();
        }
        int t = 0;
        double twoT = 1;
        while(floor(u) == floor(v)){
            u *= 2;
            v *= 2;
            ++t;
            twoT *= 2;
        }
        --t;
        twoT /= 2;
        // t >= 0: t < 0 => t == -1 => floor(u0) == floor(v0) => u0 = v0 = 1 => two equal points, which violates contract
        double _twoT = twoT;// / 2;
        double lx = floor(topLeft.getX() * _twoT) / _twoT;
        double ly = floor(topLeft.getY() * _twoT) / _twoT;
        result[0] = lx;
        result[1] = ly;
        result[2] = 1 / twoT;
        return t;
    }

    public Sector add(Point2d point){
        double[] boundaries = new double[3];
        int depth = calcEffectiveBoundaries(boundaries);

        double bx = boundaries[0];
        double by = boundaries[1];
        double len = boundaries[2];

        if (bx <= point.getX() && point.getX() < bx + len && by <= point.getY() && point.getY() < by + len) {
            addToSelf(point);
        }
    }

    private void addToSelf(Point2d point, double bx, double by, double len){
        if(point.getX() < bx + len/2){
            if(point.getY() < by + len/2){
                nw = addToSubSector(nw, point);
            }else{
                ne = addToSubSector(ne, point);
            }
        }else{
            if(point.getY() < by + len/2){
                sw = addToSubSector(sw, point);
            }else{
                se = addToSubSector(se, point);
            }
        }
    }

    private Sector addToSubSector(Sector sector, Point2d point) {
        if(sector == null){
            return new PointSector(point);
        }
        if(sector instanceof PointSector){

        }
        return ((BoxSector)sector).add(point);
    }

    private void recalculateExtremes(Sector s) {
        if (s != null) {
            if (min == null || min.compareTo(s.getMin()) > 0) {
                min = s.getMin();
            }
            if (max == null || max.compareTo(s.getMax()) < 0) {
                max = s.getMax();
            }
        }
    }

}
