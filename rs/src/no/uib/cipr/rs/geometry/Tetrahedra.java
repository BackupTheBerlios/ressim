package no.uib.cipr.rs.geometry;


/**
 * Data structure to encapsulate the geometry of a tetrahedra (basic 3-simplex)
 * TODO: Rewrite the data structure so that the shape depends on the surfaces,
 * the surfaces on the ridges and the ridges on the points. It will require a
 * dictionary where a ridge, surface or shape can be recognized based on some
 * of its components. (Use factory methods, not true constructors).
 * 
 * @author roland.kaufmann@cipr.uib.no
 */
public class Tetrahedra {
    /**
     * Corner points of the tetrahedra. The first point is considered to be 
     * the "origo" of the tetrahedra and determines its location is space.
     * Since the tetrahedra is a simplex for three dimensions, we have one and 
     * exactly one vector from the origo to the other corners in each 
     * "dimension". This need not be vectors which have their principal 
     * components those dimensions, but we like to think about them as such 
     * when we set up the topology (generate triangle sides).
     */
    Point3D a;
    Point3D b;
    Point3D c;
    Point3D d;

    /**
     * Construct a tetrahedral shape based on four spatial points. These
     * points are assumed to be organized in a right-hand manner, although
     * that does not actually matter. 
     */
    public Tetrahedra(Point3D p1, Point3D p2, Point3D p3, Point3D p4) {
        this.a = p1;
        this.b = p2;
        this.c = p3;
        this.d = p4;
    }

    /**
     * Barycenter of the tetrahedra. This point will be inside the shape.
     * (Only on one of the surfaces if the shape is degenerate). 
     */
    public Point3D getCenter() {
        Point3D center = Point3D.center(a, b, c, d);
        return center;
    }
    
    /**
     * Private properties to generate the vectors that make out the frame of
     * this tetrahedra. At a later time, we may cache these vectors as weak 
     * references if computing them on demand takes too much time. 
     */
    private Vector3D ab() {
        Vector3D ab = new Vector3D(a, b);
        return ab;
    }
    private Vector3D ac() {
        Vector3D ac = new Vector3D(a, c);
        return ac;
    }
    private Vector3D ad() {
        Vector3D ad = new Vector3D(a, d);
        return ad;
    }
    
    /**
     * Volume occupied by the shape. 
     */
    public double getVolume() {
        // for a parallelepiped (three-dimensional parallelogram where four
        // and four edges are parallel), the volume would be the height in
        // each principal dimension multiplied. (see expanded explanation at
        // http://en.wikipedia.org/wiki/Parallelepiped
        // we achieve this easily be multiplying the vectors. there are six
        // tetrahedras in each such parallelogram, thus the division at the end,
        // the number of tetrahedras are d! in d dimensions (3! = 3 * 2 = 6).
        // http://www2.toki.or.id/book/AlgDesignManual/BOOK/BOOK4/NODE184.HTM
        // see also: http://mathforum.org/library/drmath/view/51837.html for an
        // interesting setup of the determinant matrix.
        double volume = ac().dot(ab().cross(ad())) / 6.;
        return volume;
    }
    
    public Triangle[] sides() {
        // decompose the tetrahedra into its various sides; note that the
        // order of the arguments is such that the triangles always have their
        // normal vectors pointing out of the shape (using the right-hand rule)
        Triangle[] triangles = new Triangle[] {
                new Triangle(a, b, d),  // front
                new Triangle(a, c, b),  // bottom
                new Triangle(a, d, c),  // left side
                new Triangle(b, c, d)   // away
        };
        return triangles;
    }
}
