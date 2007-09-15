package no.uib.cipr.rs.geometry;

import java.util.HashMap;
import java.util.Map;

import no.uib.cipr.rs.util.Pair;
import no.uib.cipr.rs.util.PriorityQueue;
import no.uib.cipr.rs.util.Tolerances;

import org.junit.Assert;
import org.junit.Test;

/**
 * Triangularize a surface. Only the two first components of a point will be
 * used, i.e. a two-dimensional set is assumed. If you have three-dimensional
 * data, then you should project the points to their natural plane first.
 * 
 * Subclass this type and overload the callback to implement the actual
 * processing/registring of the triangles. This is akin to the yield()-construct
 * found in many other languages. The callbacks either receives edges as a set
 * of references to points, or as a user-defined type EdgeData. 
 * 
 * This version is adapter from the algorithm in "Computational Geometry and
 * Computer Graphics in C++" (ISBN: 0-13-290842-5) pp. 162--170 by Michael 
 * Laszlo. It has an inefficient time complexity, being O(n^2), but it is easy
 * to read, to implement and to adapt to constrained Delaunay problems. 
 * 
 * Implementer's note: In this code, int is used to address vertices; in
 * languages with pointer arithmetic, a pointer directly to the vertex could be
 * used and then the difference between this and the base pointer of the array
 * passed to the callback. Datatype long is used to represent a pair of integers
 * without allocating any heap memory. Languages which allows stack-based record
 * allocation and/or multiple return types should rather use those constructs.
 * 
 * @author roland.kaufmann@cipr.uib.no
 */
public abstract class Delaunay2D<EdgeData> {
    // bottom/null value for vertex identifiers
    private static final int NULL = -1;
    
    /**
     * Vertices in the problem. Since we operate in two dimensions only, then
     * this will actually be the projection of the points into their natural
     * plane. However, that transformation is done so that the order of the
     * points are the same.
     */
    private final Point3D[] vertices;
    
    /**
     * Start and stop indices for the vertex array; allows us to only
     * triangulate a subset, if we so desire.
     */
    private final int si;
    private final int ti;
    
    /**
     * Mini-algebra for vertices. 
     * Accessor methods to save us typing the indexing operation; this allows us
     * to treat integers as placeholders for point references, and thus operate
     * on a homomorphic image of the set instead of reporting true references.
     * These are made available for subclasses so that they don't need to
     * maintain their own reference to the point selection. 
     */
    protected final double x(int v) {
        final double x = this.vertices[v].x();
        return x;
    }
    protected final double y(int v) {
        final double y = this.vertices[v].y();
        return y;
    }
    
    /**
     * Mini-algebra for edges.
     * encode a directed edge between two points as a double-word value
     * accessor functions that allows us to get the individual vertex endpoints
     * out of a value representing an edge.
     */
    private final long makeEdge(int a, int b) {
        final long edge = (long) a << Integer.SIZE | b;
        return edge;
    }
    
    private final int a(long e) {
        final int a = (int) (e >> Integer.SIZE);
        return a;
    }
    
    private final int b(long e) {
        final int b = (int) (e & 0xFFFFFFFF);
        return b;
    }
    
    /**
     * An edge can be in one of three states; (1) unborn, where it will exist at 
     * a future time in the triangulation but hasn't been seen yet, (2) live, 
     * where one of its triangles are found but not the other, and (3) dead, 
     * where both triangles at each side have been found and reported (or it has 
     * been detected that the edge is on the boundary). An edge can only move 
     * one way through its lifecycle.
     *  
     * The frontier is the edges for which we have found all triangles 'behind' 
     * but none 'in front'. All edges should be oriented so that the unborn 
     * edges are to the right of the edge, whereas the dead edges are to the 
     * left. The frontier is a list of all the live edges. 
     */
    private final PriorityQueue<Long> frontier = new PriorityQueue<Long>();
    
    /**
     * Values that has been returned by the callback for each edge. This vector
     * also doubles as a flag for whether the edge is truly alive or if it
     * really has been deleted and should just be removed from the frontier.
     * 
     * The first item in the pair is the auxiliary data returned from the
     * client that is associated with the edge, the second is the index of the
     * mate of this edge.
     */
    private final Map<Long, Pair<EdgeData, Integer>> values = 
        new HashMap<Long, Pair<EdgeData, Integer>>();

    /**
     * Number of times a point currently occurs on the frontier. This counter is
     * maintained as edges are inserted and removed from the front. Initially,
     * all values are zero (except for those on the initial edge, which are
     * immediately assigned). This field is not alone in causing O(n) space 
     * complexity, as the frontier must also be maintained. However, by using 
     * this extra flag, we can lookup if a point is on the frontier in constant 
     * time.
     * Vertices that are not yet discovered, have a default count of zero. As
     * they are put on the frontier, they are being reference counted. When they
     * are taken off the frontier (and thus behind it), they are given the value
     * below to indicate so.
     */
    private int[] usage; // not final since it will be cleared in triangulate
    
    // special count used for vertices detected to now be behind the frontier
    private static final int BEHIND = -1;
    
    /**
     * Adjust the stage of the edge from unborn to live, when we have found that
     * it should be a part of the triangularization (only a subset of the
     * universe of all possible edges will be born). The edge should be directed
     * so that the frontier points outwards to the right of the segment (a, b).
     * 
     * An edge is created when the frontier is updated; it will always point
     * away from the triangle that caused this edge to be created (the edge is
     * really created for the *neighbour* to the one to which it is being
     * associated here), except for the first edge which bootstraps the process.
     * Thus, the initial edge will be directed opposite of the 
     */
    private EdgeData birth(int a, int b, boolean initial) {
        // find the mate of this edge, which we use to determine whether this is
        // a boundary edge or an interior edge. note that the initial edge will
        // always have a mate, because it is in connection to some imaginary
        // triangle to the left of our points, which bootstraps the process. 
        // we can never recombine with ourself, so we can find our mate before 
        // we are put on the boundary.
        final int c = mate(a, b);      
        final boolean boundary = (c == NULL);
        
        // associate some user data with this edge. the initial edge must be
        // regarded as a boundary. note that the order of the arguments a and b
        // is swapped, as the frontier is to the right of (a,b), and thus the
        // triangle we are finding is to the right of (b,a). 
        final EdgeData data;
        if(initial) {
            data = forEachBoundary(a, b);
        }
        else {
            if(boundary) {
                data = forEachBoundary(b, a);
            }
            else {
                data = forEachInterior(b, a);
            }
        }
        
        // only put those edges for which there is a mate (i.e. further
        // triangulation) on the frontier; the boundary is not interesting (the
        // initial edge does not count as a boundary here (because it has a
        // mate)).
        if(!boundary) {
            // make it part of the frontier, so that it is processed later
            final long edge = makeEdge(a, b);
            frontier.push(edge);
            
            // indicate that these points are now in use on the frontier (notice 
            // that the indices in the usage array is different from that used to 
            // address the main vertex array.
            usage[a-this.si]++;
            usage[b-this.si]++;

            values.put(edge, new Pair<EdgeData, Integer>(data, c));
        }
        
        return data;
    }
    
    /**
     * Adjust the stage of the edge from live to dead, when we have discovered
     * the triangle on the other side of the edge, or that it is a boundary. 
     */
    private Pair<EdgeData, Integer> death(Long edge, boolean initial) {
        // just remove it from the set of associated values; this will serves as
        // as signal to the outer loop to also remove it from the priority queue
        // when it appears. (removing it here from the priority would require to
        // walk through all the elements).
        final Pair<EdgeData, Integer> p = values.get(edge);
        values.remove(edge);
        
        // decompose the data associated with the edge
        EdgeData ab = p.x();
        final int c = p.y();
        
        // decompose the edge into its components, since we are going to give
        // these indices to the callbacks
        final int a = a(edge);
        final int b = b(edge);
        
        // indicate that one edge on the frontier less is using these points.
        // note that it is important that we identify which points that are no
        // longer on the frontier, since we don't want to match points behind it
        // if the value was positive, but now becomes zero, it means that the
        // point has fallen behind the frontier. it cannot be rediscovered,
        // because it is obviously not on the frontier available for 
        // reconnection nor can a new probe seek across the frontier for it
        if(--usage[a-this.si] == 0) {
            usage[a-this.si] = BEHIND;
        }
        if(--usage[b-this.si] == 0) {
            usage[b-this.si] = BEHIND;
        }        
        
        // allow the client to do processing on the edge structure, depending
        // on which kind of edge it is. we don't have to worry about boundaries
        // here, because they are never put on the frontier, except for the
        // initial edge (which would then be reported twice).
        if(!initial) {
            ab = forEachInterior(a, b, ab);
        }
        
        // return a new pair, since the auxiliary data may have changed
        return new Pair<EdgeData, Integer>(ab, c);
    }
    
    /**
     * E.g. to find the area of a polygon represented by the cloud p:
     *  <code>
     *  double area = 0.;
     *  new Delaunay2D&lt;Object&gt;(Projection.transform(p)) {
     *      protected forEachTriangle(int a, int b, int c) {
     *          Triangle t = new Triangle(p[a], p[b], p[c]);
     *          area += t.area;
     *      }
     *  };
     *  </code>
     * 
     * @param p
     *  (Super)set of vertices that should be triangulated. The contents of this
     *  array should not be changed in the callbacks.  
     * @param index
     *  Index of the first element that should be considered part of the subset
     *  to be triangulated.
     * @param length
     *  Number of points in the subset, continuously from the first point.
     */
    public Delaunay2D(Point3D[] p, int index, int length) {        
        // vertices are input argument to the algorithm; we could clone all of
        // them here to make sure that the callbacks didn't modify them, but
        // usually a temp. copy is already being passed (through a projection)
        this.vertices = p;
        this.si = index;
        this.ti = index + length;        
    }
    
    public Delaunay2D(Point3D[] p) {
        this(p, 0, p.length);
    }

    /**
     * Find a fixed point for the rest of the triangularization to start.
     * 
     * @return
     *  Point which is at one extreme of the vertex set, i.e. there is no
     *  vertices that are more left- and lowermost than this one.
     */
    private int minimal() {
        // find the minimal vertex, i.e. the one that is to the most lower left
        int minimalIndex = this.si;
        for(int i = this.si + 1; i < this.ti; i++) {
            final Point3D minimalPoint = vertices[minimalIndex];
            final Point3D candidate    = vertices[i];
            if(candidate.compareTo(minimalPoint) < 0) {
                minimalIndex = i;
            }
        }
        return minimalIndex;
    }
    
    /**
     * Tristate which describes the orientation of a point relative to a line.
     * The middle option, between, can further be divided into two; on the 
     * segment between the end-points or outside the segment but on the 
     * extension of the plane (i.e. the line could not decide in which half-
     * plane this point is located).
     */
    protected static final int LEFT    = -1;
    protected static final int BETWEEN =  0;
    protected static final int RIGHT   =  1;
    
    /**
     * Classify a third point according to its position relative to two other
     * points; it checks if the third point is to the right or on the line 
     * between the two first points.
     * 
     * @param a
     *  Origin of the line
     * @param b
     *  End of the line
     * @param c
     *  Point outside of the line
     * @param relation
     *  If false, the function acts as a classifier where the points are
     *  ranked after at which side they are of the half-plane spanned by the
     *  (unbounded) line between a and b. This is the default.   
     *  If true, the function acts as a relation that ranks points b and c
     *  based on which is most to the left and below a. I.e. points that are on
     *  the extension of the line but outside of the segment itself are
     *  considered to be on the "right" (not less-or-equal).
     *  This distinction has been made to enable the algorithm to pick out the 
     *  smallest initial edge while still not erraneously accept three points 
     *  on a row when finding triangles.
     * @return
     *  True if point C is to the right of (or not on, if the truly flag is not
     *  set) the line AB, when viewed along the line from its origin A.
     */
    protected int side(int a, int b, int c) { return side(a, b, c, false); }
    private int side(int a, int b, int c, boolean relation) {
        // vector AB, i.e. the line
        final double ab_x = x(b) - x(a);
        final double ab_y = y(b) - y(a);
        
        // normal vector, pointing to the left of the line
        final double n_x = -ab_y;
        final double n_y = +ab_x;
        
        // vector AC, i.e. from the origin to the point outside of the line
        final double ac_x = x(c) - x(a);
        final double ac_y = y(c) - y(a);
        
        // dot product between the normal vector and the line to the point; from
        // this we can determine the angle between the two lines
        final double n_dot_ac = n_x * ac_x + n_y * ac_y;
        
        // if the dot product is zero, then the point is perpendicular to the
        // normal, i.e. it is on the line; we'll need to check the length of the
        // two lines to see if it is between the two points or further out.
        // since the square root has a monotone derivative, we can compare the
        // squared values instead of the square roots.
        final int side; // return value of this function
        if(Math.abs(n_dot_ac) < Tolerances.smallEps) {
            if(relation) {
                final double len_ab2 = ab_x * ab_x + ab_y * ab_y;
                final double len_ac2 = ac_x * ac_x + ac_y * ac_y;
                final boolean between = len_ac2 < len_ab2;
                side = between ? BETWEEN : RIGHT;
            }
            else {
                side = BETWEEN;
            }
        }
        // cosine of the angle has the same sign as the dot product; if it is
        // positive, then the point must be to the left, because we constructed
        // the normal that was on the left side
        else {
            side = n_dot_ac < 0. ? RIGHT : LEFT;
        }
        return side;
    }
    
    /**
     * Find an edge that is guaranteed to be on the hull of the set of vertices
     * and thus known to be part of the final triangularization.
     * 
     * @return
     *  An edge that is part of the triangulation and that can serve as the
     *  initial frontier. The frontier will always be to the right of this edge.
     */
    private long hullEdge() {
        // start out with the very minimal point and nothing else
        final int minimal = minimal();
        int second  = NULL;
        
        // find the vertex that together with the lower left is part of the hull
        // if we have not yet any candidate, then the first we find will suffice
        // we cannot use the same point twice in the edge (because then the edge
        // would only be a single point).
        // the second term uses the ordering relation to get points that are to
        // the left or between.
        for(int p = this.si; p < this.ti; p++) {
            if(p != minimal) {
                if(second == NULL || side(minimal, second, p, true) <= 0) {
                    second = p;
                }
            }
        }
        
        // since 'second' is the point that is most to the left of the minimal
        // anchoring point, this edge must be at the left side of the graph; all
        // other triangles will be to the right of this edge (important, since
        // it determines how we form the normal search vector). we don't need
        // the return value here, it is queued and will be processed at first
        // iteration in the matching loop.
        birth(minimal, second, true);
                
        // report which edge was the initial, so that we can signal the 
        // appropriate slot for it when it is finalized.
        final long initial = makeEdge(minimal, second);
        return initial;        
    }
    
    
    /**
     * Measure the distance from the edge to a point by a size proportional to
     * the radius the the circle that circumscribe the triangle formed by the
     * three points.
     *  
     * See http://www.ics.uci.edu/~eppstein/junkyard/circumcenter.html
     * 
     * @param a
     * @param b
     *  Endpoints for the edge that is part of the triangulation's frontier.
     * @param c
     *  Point that is currently outside of the triangulation but is a candidate
     *  to be added.
     * @return
     *  Squared radius in the circumcircle between the three points formed by
     *  the circumcircle. These values can be compared to find the closest of
     *  two candidate points.
     */
    @SuppressWarnings("unused")
    private double circum(int a, int b, int c) {
        // describe the other two points in the triangle as coordinates relative
        // to the first vertex a. if we are calculating this for a tetrahedra,
        // then we should consider reordering the vertices so that a is the
        // center in a minimum spanning tree.
        final double ab_x = x(b) - x(a);
        final double ab_y = y(b) - y(a);
        final double ac_x = x(c) - x(a);
        final double ac_y = y(c) - y(a);
        
        // squares of lengths of the edges incident to a
        final double ab = ab_x * ab_x + ab_y * ab_y;
        final double ac = ac_x * ac_x + ac_y * ac_y;

        // denominator in Cramer's rule, which is twice the determinant of the
        // matrix of two edges by two coordinates. notice the similarities
        // between this expression and the first part of the calculations that
        // are done in the isLeftOrBetween method. in a more efficient
        // implementation, this could be passed as a parameter to us so that we
        // didn't have to do it again here.
        final double determinant = ab_x * ac_y - ab_y * ac_x;
        
        // if the determinant becomes zero, it means that the three points are
        // on a line and the circle becomes infinitely large.
        if(Math.abs(determinant) < Tolerances.smallEps) {
            return Double.MAX_VALUE;
        }
        final double denominator = 1. / (2. * determinant);
        
        // coordinates of the center in the circumcircle, relative to the first
        // vertex a in the triangle (i.e. add a.x and a.y respectively to get 
        // absolute positions.
        final double x = (ac_y * ab - ab_y * ac) * denominator;
        final double y = (ab_x * ac - ac_x * ab) * denominator;
        
        // square of the radius to the center
        final double r = x * x + y * y;
        
        return r;
    }
    
    /**
     * Parameter value of the intersection between the perpendicular bisection
     * of the line between a and b, and the perpendicular bisection of the line
     * between b and c, measured in terms of the first line.
     * 
     * @param a
     * @param b
     *  Endpoints for the edge that is part of the triangulation's frontier.
     * @param c
     *  Point that is currently outside of the triangulation but is a candidate
     *  to be added. Must be to the right of the edge between a and b.
     * @return
     *  Length along the line normal to the edge between a and b, to get from
     *  the edge to the point c. If the point c is to the left of the directed
     *  line starting at a and ending at b, the length will be negative.
     */
    private double parametric(int a, int b, int c) {
        // vectors that describe the edges in the triangle (assuming that the 
        // first vertex a is origo in the local coordinate system).
        final double ab_x = x(b) - x(a);
        final double ab_y = y(b) - y(a);
        final double bc_x = x(c) - x(b);
        final double bc_y = y(c) - y(b);
        
        // start of the bisection of the edge between a and b is the midpoint
        // between them and is here called p
        final double p_x = ab_x / 2.;
        final double p_y = ab_y / 2.;
        
        // vector perpendicular to the edge between a and b is here called v.
        // note that this vector goes to the RIGHT of the edge (opposite of the
        // above method).
        final double v_x = +ab_y;        
        final double v_y = -ab_x; 
        
        // start of the bisection of the edge between b and c is the midpoint
        // between them and is here called q
        final double q_x = ab_x + bc_x / 2.;
        final double q_y = ab_y + bc_y / 2.;
        
        // vector perpendicular to the edge between b and c, also to the right
        final double w_x = +bc_y;
        final double w_y = -bc_x;
        
        // intersection between these two lines occur when the components are
        // equal; thus we get the following (vector) equations for the 
        // parametric lines, which can also be arranged so that the parameters
        // both occur on the right side:
        //
        //      p + v * t = q + w * r   <=>     p - q = -t * v + r * w
        //
        // this can also be written on a matrix notation:
        //
        //      [p_x - q_x] = [-v_x, w_x] [t]
        //      [p_y - q_y]   [-v_y, w_y] [r]
        //
        // a solution to this can be found by multiplying with the inverse,
        // which is the adjugate matrix and divided by the determinant.
        // 
        // see post with title "Parametric line intersection" by Dave Langers at 
        // 2003-10-01 19:40 in Usenet newsgroup alt.math.recreational and url
        // <http://en.wikipedia.org/wiki/Invertible_matrix#Analytic_solution>
        final double adjugate_for_t = w_y * (p_x - q_x) + -w_x * (p_y - q_y);
        final double determinant = -v_x * w_y - w_x * -v_y;
        final double t = adjugate_for_t / determinant;
        return t;
    }

    /**
     * Determine if the sight between two points are occluded by a constraint.
     */    
    @SuppressWarnings("unused")
    protected boolean visible(int a, int b) { return true; }    
    
    /**
     * Find the point which are closest to an edge such that no other point is
     * within the circumcircle of these three points.
     * 
     * @param a
     * @param b
     *  End-points in the edge that are part of the current frontier.
     * @return
     *  Point that together with the edge forms a new piece of the triangulation.
     */
    private int mate(int a, int b) {                        
        // currently best fit as a mate for the two points. initially unknown.
        int mate = NULL;
        
        // best found radius so far; if a point is to become a better fit for
        // being a mate, it must provide a better radius than the previous guy.
        double bestDist = Double.MAX_VALUE;
        double bestUse  = 0;
        
        // loop through all points; this is the main driver of the time 
        // complexity for this algorithm; the same points are tested over and
        // over again for different pairs.
        for(int c = this.si; c < this.ti; c++) {
            // points that have fallen behind the frontier, will never be able
            // to satisfy the below requirement, so simply overlook them with
            // this simple test instead.
            if(usage[c-this.si] == BEHIND) {
                continue;
            }
            
            // only consider points that are outside the frontier, as seen from
            // this segment (all undiscovered points are outside the frontier as
            // a whole (see above).
            if(side(a, b, c, false) == RIGHT) {                
                // if a constraint is completely outside of the triangle, then 
                // it is of course irrelevant. if it is completely inside of the 
                // triangle, then we will find the endpoints closer than the 
                // current candidate anyhow. what is left is that we must check 
                // if there is any obstacle that runs through the triangle. we 
                // do this by checking if it intersects with any of its legs.
                if(visible(a, c) && visible(b, c)) {
                    // evaluate this particular point as the mate for the edge,
                    // finding the candidate distance and its usage count
                    final double candDist = parametric(a, b, c);
                    final double candUse  = usage[c-this.si];
                    
                    // declare this the new mate if it has a better circle. if
                    // the distance is shorter, we select this point anyway.
                    // two points that have the same distance from the edge will
                    // form a cocircular rectangle with it, giving an ambigious
                    // triangulation (it doesn't matter which one we select).
                    // we break the tie by selecting the one that is most on the
                    // frontier.
                    // if we select a point that is not on the frontier, we risk
                    // choosing a point that is behind it, crossing previous
                    // edges. thus, we always prefer to recombine with the front
                    if(candDist < bestDist || 
                            (Math.abs(candDist - bestDist) < Tolerances.smallEps &&
                            candUse > bestUse)) {
                        bestDist = candDist;
                        bestUse = candUse;
                        mate = c;
                    }
                }
            }
        }
        return mate;
    }
    
    private EdgeData update(int a, int b) {
        final EdgeData ab;
        
        // if we have (re)discovered the frontier of another triangle, then
        // remove it from the queue and stich the two triangles together;
        // otherwise, create a new edge that is facing the void.
        // each edge can be assigned an enumeration value (-1=unborn, 0=live,
        // 1=dead); when we discover the edge, its count should be increased by
        // one, reflecting this new status. note that the initial edge can never
        // be found from the frontier again (since it is to the left of all
        // other edges.
        long otherWay = makeEdge(b, a);
        if(values.containsKey(otherWay)) {
            Pair<EdgeData, Integer> p = death(otherWay, false);
            ab = p.x();
        }
        else {
            ab = birth(a, b, false);
        }
        return ab;
    }
    
    public synchronized void triangulate() {
        // initialize the array without any points on the frontier. this array
        // must be guarded with the object mutex.
        final int length = this.ti - this.si;
        this.usage = new int[length];      
        
        // initialize the frontier with an edge that is on the hull
        final long initial = hullEdge();

        // keep triangularizing while there are other sides of the edges that
        // are still undiscovered
        while(frontier.hasNext()) {
            // the edge we are extracting is live, determine whether it is on
            // the boundary or part of the triangulation and remove it from the
            // frontier (it becomes dead). each edge is only pulled from the
            // frontier once, thus this loop will amortisize to constant cost.
            // in a language that supports multiple return values, these lines 
            // should be collected into a routine reap() that belongs to the 
            // edge mini-algebra (together with birth() and death()), returning
            // both edge and ab.
            final long edge = frontier.next();
            if(!values.containsKey(edge)) {
                continue;
            }
            
            // extract each of the end-points for the edge; these will be two of
            // the corners in the triangle.
            final int a = a(edge);
            final int b = b(edge);
            
            // extract the edge from the frontier, running the appropriate 
            // callback if the edge is not on the boundary. the initial edge
            // will have mates but still be on the boundary; all other boundary
            // edges will never be seen again once they're discovered
            final Pair<EdgeData, Integer> p = death(edge, edge == initial);
            final EdgeData ab = p.x();
            
            // get the next point in the cloud that should form a triangle
            // together with this edge. if there is no such point at all, it 
            // must be because the edge is on the boundary/hull. this search has
            // already been done for us when the edge was put on the frontier.
            final int c = p.y();
            assert(c != NULL);
            
            // add the two other edges to the frontier; if they already
            // exists (found by another triangle), they will now be completed
            // note that the part of update() that kills edges will never
            // do so for boundary edges, which of course cannot be discovered
            // from more than one direction. thus, we are always correct in
            // calling forEachTriangle() on these edges afterwards.
            // note that since c is to the right of edge (a,b), then the
            // exterior of the triangle must be to the right of the directed
            // edges(c,b) and (a, c) respectively. 
            final EdgeData bc = update(c, b);
            final EdgeData ca = update(a, c);
            
            // yield another triangle. since the edges ab and bc have their
            // inward normals to the right, then abc will be a clockwise 
            // enumeration of the points in the triangle.
            forEachTriangle(a, b, c);
            forEachTriangle(ab, bc, ca);
        }
    }
    
    /**
     * Callback that creates a user-defined object for each edge. Edges will
     * always be reported so that the directed edge from a to b has the first
     * triangle created using this edge to the right. Arguments to this callback
     * are the indices into the point array. The return data is opaque to the
     * algorithm.
     * 
     * Use this function to create a new edge and a mapping from each of the two
     * points to the edges (a point may have an arbitrary positive number of 
     * edges associated) and from the edge to the two endpoints (an edge always 
     * has two and only two points).
     */
    @SuppressWarnings("unused")
    protected EdgeData forEachEdge(int a, int b) { return null; }
    
    /**
     * Callback that is signaled whenever an edge that is part of the interior
     * is first found. The default is to call the common function to create an 
     * edge.
     */
    @SuppressWarnings("unused")
    protected EdgeData forEachInterior(int a, int b) { 
        return forEachEdge(a, b); 
    }

    /**
     * Callback that gets the notification that a boundary edge has been
     * detected. The default is to call the common function to create an edge.
     */
    @SuppressWarnings("unused")
    protected EdgeData forEachBoundary(int a, int b) { 
        return forEachEdge(a, b); 
    }

    /**
     * Callback that is used when an interior edge is encountered for the
     * second time. The points are so that the directed edge from a to b has the
     * next triangle to be created to the right (i.e. opposite order of the
     * first call to forEachInterior for this edge).
     * 
     * The original object created for this edge is passed as a parameter, but
     * another edge may be returned by this callback and in that case, that is 
     * the value that is passed to forEachTriangle. The default is to return the
     * object that was first created for the edge, i.e. you'll have to overload
     * this function and call forEachEdge yourself if you want to have two
     * objects per interior edge.
     * 
     * Use this function to create a new edge between a and b (using for
     * instance forEachEdge), and then a connection between the old edge (passed 
     * as an argument) and the new edge just created. You may find a 
     * neighbouring triangle by traversing this map of neighbour edges.
     */
    @SuppressWarnings("unused")
    protected EdgeData forEachInterior(int a, int b, EdgeData e) { 
        return e; 
    }    
    
    /**
     * Callback that gets the notification that a new triangle has been found.
     * 
     * Points and edges are reported in clockwise order around the triangle.
     * 
     * Use these functions to create a new triangle, and a mapping from the
     * triangle to the three edges and from each of the three edges to the
     * triangle.
     */
    @SuppressWarnings("unused")
    protected void forEachTriangle(int a, int b, int c) {}
    @SuppressWarnings("unused")
    protected void forEachTriangle(EdgeData ab, EdgeData bc, EdgeData ca) {}
    
    public static class Tests {
        private Point3D ll = new Point3D(-1., -1.,  0.); // left lower
        private Point3D cl = new Point3D( 0., -1.,  0.); // center lower
        private Point3D rl = new Point3D( 1., -1.,  0.); // right lower
        private Point3D lm = new Point3D(-1.,  0.,  0.); // left middle
        private Point3D cm = new Point3D( 0.,  0.,  0.); // center middle
        private Point3D rm = new Point3D( 1.,  0.,  0.); // right middle
        private Point3D lu = new Point3D(-1.,  1.,  0.); // left upper
        private Point3D cu = new Point3D( 0.,  1.,  0.); // center upper
        private Point3D ru = new Point3D( 1.,  1.,  0.); // right upper
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
        public void simple() {
            final Point3D[] p = new Point3D[] { cm, cu, ru, rm, a, b, c};
            
            // triangles
            final int[] expectedTris = new int[] {
                    0, 1, 4,
                    0, 4, 6,
                    0, 6, 3,
                    3, 6, 2,
                    2, 6, 5,
                    2, 5, 1,
                    1, 5, 4,
                    4, 5, 6                    
            };
            final int[] actualTris = new int[expectedTris.length];
            
            // boundary edges
            final int[] expectedBnds = new int[] {
                    0, 1,
                    3, 0,
                    2, 3,
                    1, 2                   
            };
            final int[] actualBnds = new int[expectedBnds.length];
            
            // interior edges
            final int[] expectedInts = new int[] {
                    1, 4,
                    4, 0,
                    0, 4,
                    4, 6,
                    6, 0,
                    0, 6,
                    6, 3,
                    3, 6,
                    6, 2,
                    2, 6,
                    6, 5,
                    5, 2,
                    2, 5,
                    5, 1,
                    1, 5,
                    5, 4,
                    4, 1,
                    4, 5,
                    5, 6,
                    6, 4,                    
            };
            final int[] actualInts = new int [expectedInts.length];
            
            new Delaunay2D<Void>(p) {
                int tri = 0;
                int bnd = 0;
                int itr = 0;
                
                @Override
                protected void forEachTriangle(int a, int b, int c) {
                    //System.out.printf("tri: a=%d, b=%d, c=%d%n", a, b, c);
                    actualTris[tri*3 + 0] = a;
                    actualTris[tri*3 + 1] = b;
                    actualTris[tri*3 + 2] = c;
                    tri++;
                }
                
                @Override
                protected Void forEachBoundary(int a, int b) {
                    //System.out.printf("bnd: a=%d, a=%d%n", a, b);
                    actualBnds[bnd*2 + 0] = a;
                    actualBnds[bnd*2 + 1] = b;
                    bnd++;
                    return super.forEachBoundary(a, b);
                }
                
                @Override
                protected Void forEachInterior(int a, int b) {
                    //System.out.printf("int: a=%d, a=%d,%n", a, b);
                    actualInts[itr*2 + 0] = a;
                    actualInts[itr*2 + 1] = b;
                    itr++;
                    return super.forEachInterior(a, b);
                }
                
                @Override
                protected Void forEachInterior(int a, int b, Void e) {
                    //System.out.printf("int: a=%d, a=%d,%n", a, b);
                    actualInts[itr*2 + 0] = a;
                    actualInts[itr*2 + 1] = b;
                    itr++;
                    return super.forEachInterior(a, b, e);
                }
            }.triangulate();
            
            compare(expectedTris, actualTris);
            compare(expectedBnds, actualBnds);
            compare(expectedInts, actualInts);
        }
        
        @Test
        public void cocircular() {
            final Point3D[] p = new Point3D[] 
                                          { ll, lm, lu, cl, cm, cu, rl, rm, ru};
            
            final int[] expectedTris = new int[] {
                    0, 1, 3,
                    3, 1, 4,
                    3, 4, 6,
                    4, 1, 2,
                    4, 2, 5,
                    4, 5, 7,
                    4, 7, 6,
                    7, 5, 8
            };
            final int[] actualTris = new int[expectedTris.length];
            
            new Delaunay2D<Object>(p) {
                int tri = 0;
                
                @Override
                protected void forEachTriangle(int a, int b, int c) {
                    //System.out.printf("a=%d, b=%d, c=%d%n", a, b, c);
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