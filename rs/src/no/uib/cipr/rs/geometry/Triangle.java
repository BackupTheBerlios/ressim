package no.uib.cipr.rs.geometry;


/**
 * Data structure to encapsulate the geometry of a triangle surface (side of a
 * tetrahedra).
 * 
 * @author roland.kaufmann@cipr.uib.no
 */
public class Triangle {
    /**
     * Three corner points of the triangle. Generally, we consider the first
     * of the points to be the base point and the other two to be apices.
     * Vectors from the first point to the other two will spawn the plane of
     * the triangle.
     */
    Point3D a;
    Point3D b;
    Point3D c;
    
    /**
     * Initialize triangle from three points in space. (Triangles are planes)
     * Points should be specified in counter-clockwise order (so that the
     * normal vector points "out of" the plane, using the right-hand rule) 
     */
    public Triangle(Point3D p1, Point3D p2, Point3D p3) {
        this.a = p1;
        this.b = p2;
        this.c = p3;
    }
    
    /**
     * Barycenter of the triangle. 
     */
    public Point3D getCenter() {
        Point3D center = Point3D.center(a, b, c);        
        return center;
    }
    
    /**
     * Private properties to generate the vectors that spawns the plane of this
     * triangle. At a later time, we may cache these vectors as weak references
     * if computing them on demand takes too much time. 
     */
    private Vector3D ab() {
        Vector3D ab = new Vector3D(a, b);
        return ab;
    }
    private Vector3D ac() {
        Vector3D ac = new Vector3D(a, c);
        return ac;
    }
    
    /**
     * Calculate surface area, 
     */
    public double getArea() {
        // area of a triangle is half the area of the parallelogram
        // that is formed by crossing the two side vectors. the
        // magnitude of the inner product is the area.
        // see http://mcraefamily.com/MathHelp/GeometryVectorCrossProduct.htm
        double area = 0.5 * ab().cross(ac()).norm2();        
        return area;
    }
    
    /**
     * Normal vector from the surface. (Normal vectors are thought to start
     * at the center of the triangle). 
     */
    public Vector3D getNormal() {
        // get the direction of the normal vector
        Vector3D normal = ab().cross(ac());
        
        // scale such that the length is one, to get a true normal vector
        normal = normal.mult(1. / normal.norm2());        
        return normal;
    }
}
