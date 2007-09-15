package no.uib.cipr.rs.meshgen.triangle;

import no.uib.cipr.rs.geometry.Point3D;

/**
 * Helper class that collects the data necessary to represent a line in
 * mesh generation. This class is not meant to be consumed outside of this
 * package (to avoid dependencies). Its purpose is not to encapsulate
 * behavior but rather to make it easier to write return signatures (instead
 * of Pair<Pair<Point3D, Point3D>, Boolean>.
 * 
 * @author roland.kaufmann@cipr.uib.no
 */
class Line {
    Point3D[] points;
    Kind kind;
    
    // convenience constructor that lets us initialize the structure
    // in one line
    Line(Point3D start, Point3D end, Kind kind) {
        this.points = new Point3D[] { start, end };
        this.kind = kind;
    }
    
    @Override
    public String toString() {
        return String.format("%s-%s", points[0], points[1]);
    }
}