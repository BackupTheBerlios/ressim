package no.uib.cipr.rs.meshgen.partition;

import no.uib.cipr.rs.geometry.Point3D;
import no.uib.cipr.rs.meshgen.structured.Orientation;

/**
 * A segment in 3D with an additional orientation-placement attribute. This is
 * used when constructing rectangular regions.
 */
class OrientedSegment3D extends Segment3D {

    private Orientation orientation;

    public OrientedSegment3D(Point3D p0, Point3D p1, Orientation orientation) {
        super(p0, p1);

        this.orientation = orientation;
    }

    public Orientation getOrientation() {
        return orientation;
    }

    @Override
    public boolean equals(Object other) {
        // if this implementation is changed to reflect the contents of
        // this subclass, then the boundaries() methods in this package
        // must be reimplemented using a different comparator.
        return super.equals(other);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
