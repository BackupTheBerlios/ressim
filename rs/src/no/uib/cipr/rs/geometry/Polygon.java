package no.uib.cipr.rs.geometry;


import org.junit.Test;

/**
 * 
 * 
 * @author roland.kaufmann@cipr.uib.no
 */
public class Polygon {
    Point3D[] points;
    
    public Polygon(Point3D... p) {
        // points themselves are immutable but the array are not. create a copy
        // so that we are sure no-one modifies it behind our back.
        this.points = new Point3D[p.length];
        System.arraycopy(p, 0, this.points, 0, p.length);
    }
    
    /**
     * 
     * 
     * @return
     *  Area occupied by the plane spanned out by the points.
     */
    public double area() {
        // in order for us to have a 2D-simplex there must be at least three
        // points
        assert this.points.length >= 3;

        // indices of the the points, as we traverse the ring in clockwise (cw)
        // and counter-clockwise (ccw) order, respectively
        int cw = 0;
        int ccw = this.points.length - 1;
        
        // flag that determines whether we move the left foot (false) or the
        // right foot (true) for the next step (the analogy we use for which
        // of the two above counters that should be moved)
        boolean foot = false;
        
        // start out with an empty area (because we haven't processed any
        // interior yet)
        double sum = 0d;
        
        // form at least one triangle (i.e. test after the iteration)
        do {
            // to get to the next index, we either increment the index that
            // started from the beginning or decrement the one starting from
            // the end
            int apex = foot ? cw + 1 : ccw - 1;
            
            // calculate the area from this particular triangle
            Triangle t = new Triangle(this.points[cw], this.points[ccw],
                    this.points[apex]);
            sum += t.getArea();
            
            // progress the ground ridge to the other side of the triangle
            if(foot) {
                cw = apex;
            }
            else {
                ccw = apex;
            }
            
            // choose from the other side next
            foot = !foot;
        // continue until the two chains meet
        } while(cw < ccw);
        
        // area of the polygon is considered to be the sum of triangle facets
        return sum;
    }
    
    public static class Tests {        
        Point3D a = new Point3D(0d, 0d, 0d);
        Point3D b = new Point3D(0d, 1d, 0d);
        Point3D c = new Point3D(1d, 1d, 0d);
        Point3D d = new Point3D(1d, 0d, 0d);
        Point3D e = new Point3D(2d, 0d, 0d);
        
        @Test
        public void rectangularSquare() {
            Polygon p = new Polygon(a, b, c, d);
            assert p.area() == 1d;
        }
        
        @Test
        public void equilateralTriangle() {
            Polygon p = new Polygon(a, c, e);
            assert p.area() == 1d;
        }
        
        @Test
        public void rightAngledTriangle() {
            Polygon p = new Polygon(a, b, c);
            assert p.area() == 0.5d;
        }
    }
}
