package no.uib.cipr.rs.geometry;

import org.junit.Assert;
import org.junit.Test;

/**
 * In a constrained Delaunay triangulation the presence of a given set of pre-
 * defined edges are forced. This is a trivial and naive implementation of a
 * such, where each constraint is checked for each edge. 
 * 
 * @author roland.kaufmann@cipr.uib.no
 *
 * @param <EdgeData>
 *  Datatype that the client code may use to hold information for edges. The
 *  client is given the opportunity to create these edges as they are 
 *  discovered during the triangulation.
 */
public abstract class ConstrainedDelaunay2D<EdgeData> extends Delaunay2D<EdgeData> {
    // indices that indicate the subset of the constraint array to use
    final int si;
    final int ti;
    
    // reference to the array holding the points making up constraining line
    // segments. the array has been flattened, C-style.
    final int[] constraints;
    
    /**
     * Default is to use the entire arrays that are passed, i.e. no subsets. 
     */
    public ConstrainedDelaunay2D(Point3D[] p, int[] constraints) {
        this(p, 0, p.length, constraints, 0, constraints.length);
    }

    /**
     * Constraints are dimension-reduced. For a 2D triangulation, the 
     * constraints are thus 1D, and we need n+1 points to construct a simplex 
     * in the nth dimension.
     * 
     * Each constraint is stored as a pair of indices into the points array,
     * describing the source and the target of the constraining line segment.
     * The next pair follows immediately in the array. E.g. every even element
     * in the constraining array is the index of the source point and every odd
     * element is the index of the target point.
     * 
     * As the end-points of (internal, at least) constraints will be nodes in
     * the final triangulation, they must be part of the input vertex set. 
     * If you want constraints between points that are not part of the final
     * output, then put the end-points of the constraints outside the active
     * subset of the nodes to be triangulated, while still referring to their
     * indices in the constraint collection!  
     */
    public ConstrainedDelaunay2D(Point3D[] p, int p_index, int p_length, 
            int[] constraints, int c_index, int c_length) {
        // domain of points are passed unmodified to the triangulation core
        super(p, p_index, p_length);

        // store references to the array of constraints for later use. at this
        // point we could make a copy of the array in case we are concerned
        // about the client changing the points behind our back.
        // there must be an even number of indices in the constraint array that
        // make up the line segments.
        assert (c_length % 2 == 0);
        this.constraints = constraints;
        this.si = c_index;
        this.ti = c_index + c_length;
    }
    
    /**
     * Check if a point b is visible from another point a, or if it is being
     * occluded by one of the constraints.
     */
    @Override
    protected boolean visible(int a, int b) {
        for(int i = this.si; i < this.ti; i += 2) {
            // retrieve indices for the two end-points of the segment
            final int c = this.constraints[i+0];
            final int d = this.constraints[i+1];
            
            // if this line intersects the vision between the two points we have
            // been passed, then disallow this edge
            if(intersect(a, b, c, d)) {
                return false;
            }
        }
        
        // without evidence to the contrary, allow the edge
        return true; 
    }
    
    /**
     * Check if the line segment from a to b intersects with the line segment
     * from c to d. If an end-points of one of the segments ends exactly on the
     * other line, then they are not considered to intersect. 
     */
    private final boolean intersect(int a, int b, int c, int d) {
        // determine trinarily at which side the point c is of the line ab. note
        // the the return values for 'left' and 'right' has different signs, 
        // whereas the case where it is on the line is undecidable
        final int c_ab = side(a, b, c);
        final int d_ab = side(a, b, d);
        
        // product of the signs will be negative if the signs are different.
        // since we use strictly negative, then points *on* the line doesn't
        // count as crossing (since those aren't occluded by the line). 
        // we could have used boolean flags and then compared them with xor, but
        // then we would have missed the 'undecided' case of points that are
        // in between (or we would have to do twice the tests)
        final boolean cd_crosses_ab = c_ab * d_ab < 0;
        
        // same thing symmetrically for the points a and b against the line cd
        // respectively. the side test only checks in which half-plane the
        // points are, it doesn't take into account that the other line may be
        // far above or below the line segment
        final int a_cd = side(c, d, a);
        final int b_cd = side(c, d, b);
        final boolean ab_crosses_cd = a_cd * b_cd < 0;
        
        final boolean intersect = cd_crosses_ab && ab_crosses_cd;
        return intersect;
    }
    
    public static class Tests {
        private Point3D ll = new Point3D( 0.,  0.,  0.); // left lower
        private Point3D lu = new Point3D( 0.,  1.,  0.); // left upper
        private Point3D ru = new Point3D( 1.,  1.,  0.); // right upper
        private Point3D rl = new Point3D( 1.,  0.,  0.); // right lower
        private Point3D a  = new Point3D( 0.2, 0.3, 0.);
        private Point3D b  = new Point3D( 0.7, 0.8, 0.);
        private Point3D c  = new Point3D( 0.7, 0.3, 0.);
        
        // needed because JUnit < 4.3 does not handle integer arrays (!)
        private final void compare(int[] expected, int[] actual) {
            Assert.assertEquals(expected.length, actual.length);
            for(int i = 0; i < expected.length; i++) {
                Assert.assertEquals(expected[i], actual[i]);
            }
        }        
              
        @Test
        public void constrained() {
            final Point3D[] p = new Point3D[] { ll, lu, ru, rl, a, b, c};
            final int[] c = new int[] { 1 /* lu */, 3 /* rl */ };
            
            // triangles
            final int[] expectedTris = new int[] {
                    0, 1, 4,
                    0, 4, 6,
                    0, 6, 3,
                    3, 6, 2,
                    2, 6, 5,
                    2, 5, 1,
                    1, 5, 6,
                    1, 6, 4
            };
            final int[] actualTris = new int[expectedTris.length];
            
            new ConstrainedDelaunay2D<Void>(p, c) {
                int tri = 0;
                
                @Override
                protected void forEachTriangle(int a, int b, int c) {
                    //System.out.printf("tri: a=%d, b=%d, c=%d%n", a, b, c);
                    actualTris[tri*3 + 0] = a;
                    actualTris[tri*3 + 1] = b;
                    actualTris[tri*3 + 2] = c;
                    tri++;
                }                
            }.triangulate();
            
            compare(expectedTris, actualTris);
        }

        @Test
        public void verythin() {
            final Point3D[] p = new Point3D[] { ll, lu, ru, rl, a, b, c};
            final int[] c = new int[] { 0 /* ll */, 2 /* ru */ };
            
            // triangles
            final int[] expectedTris = new int[] {
                    0, 1, 4,
                    0, 4, 2,
                    0, 2, 6,
                    0, 6, 3,
                    2, 4, 5,
                    2, 5, 1,
                    1, 5, 4,
                    3, 6, 2
            };
            final int[] actualTris = new int[expectedTris.length];
            
            new ConstrainedDelaunay2D<Void>(p, c) {
                int tri = 0;
                
                @Override
                protected void forEachTriangle(int a, int b, int c) {
                    //System.out.printf("tri: a=%d, b=%d, c=%d%n", a, b, c);
                    actualTris[tri*3 + 0] = a;
                    actualTris[tri*3 + 1] = b;
                    actualTris[tri*3 + 2] = c;
                    tri++;
                }                
            }.triangulate();
            
            compare(expectedTris, actualTris);
        }    
    }
}