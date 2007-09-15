package no.uib.cipr.rs.meshgen.util;

import no.uib.cipr.rs.geometry.Point3D;
import no.uib.cipr.rs.geometry.Vector3D;

/**
 * Note that this class only implements Comparable&lt;Point3D&gt;, not
 * Comparable&lt;IndexedPoint3D&gt;. The equivalence relation does not
 * take the index of the point into consideration. (Most often, the
 * index is used to find the index into an array of an already existing
 * point that has been chosen as the canonical point (congruence over
 * the equivalence relation).
 */
public class IndexedPoint3D extends Point3D {

    private static final long serialVersionUID = -1L;

    // value used for the index if it is not yet a member of the set
    protected static final int UNKNOWN = 0;    
    
    // index given to the object when it becomes a member. when the
    // object start out, then it is not a member yet
    private int index = UNKNOWN;

    /**
     * Create a new point from each of its components 
     */
    public IndexedPoint3D(double x, double y, double z) {
        super(x, y, z);
    }

    /**
     * Create point from  
     */
    public IndexedPoint3D(Point3D p, Vector3D v) {
        super(p, v);
    }

    /**
     * Creates a comparable point from the given point.
     */
    public IndexedPoint3D(Point3D p) {
        super(p.x(), p.y(), p.z());
    }
    
    /**
     * Convenience constructors that let you set the index as part of
     * creation. Prefer to use these, as they will let us convert the
     * class to an immutable one later.
     */
    public IndexedPoint3D(double x, double y, double z, int index) {
        this(x,y,z);
        this.index = index;
    }
    public IndexedPoint3D(Point3D p, Vector3D v, int index) {
        this(p,v);
        this.index = index;    
    }
    public IndexedPoint3D(Point3D p, int index) {
        this(p);
        this.index = index;
    }
    
    /**
     * Set unique global index of this point to the given value. Attempt
     * to avoid using this method and pass the index to the constructor
     * if possible.
     */
    public void setIndex(int index) {
        this.index = index;
    }

    /**
     * Returns the unique global index of this point.
     */
    public int getIndex() {
        return this.index;
    }
}
