package no.uib.cipr.rs.meshgen.structured;

import no.uib.cipr.rs.geometry.Point3D;
import no.uib.cipr.rs.geometry.Vector3D;

/**
 * A structured geometry specified by parts. No numbering is assumed.
 */
public abstract class StructuredGeometry extends RegionGeometry {

    protected CartesianTopology topology;

    /**
     * Returns the center point of the element with the given ijk-index.
     */
    public Point3D getElementCenter(IJK ijk) {
        return getElementCenter(topology.getLinearElement(ijk));
    }

    /**
     * Returns the volume of the element with the given ijk-index.
     */
    public double getElementVolume(IJK ijk) {
        return getElementVolume(topology.getLinearElement(ijk));
    }

    /**
     * Returns the area of the interface of the given element with the given
     * orientation.
     */
    public double getInterfaceArea(IJK ijk, Orientation orientation) {
        return getInterfaceArea(topology.getLinearInterface(ijk, orientation));
    }

    /**
     * Returns the normal of the interface of the given element with the given
     * orientation.
     */
    public Vector3D getNormal(IJK ijk, Orientation orientation) {
        return getNormal(topology.getLinearInterface(ijk, orientation));
    }

    /**
     * Returns the center point of the interface of the given element and
     * orientation.
     */
    public Point3D getInterfaceCenter(IJK ijk, Orientation orientation) {
        return getInterfaceCenter(topology.getLinearInterface(ijk, orientation));
    }

    /**
     * Returns the point coordinates of the point with the given ijk-index.
     */
    public Point3D getPoint(IJK ijk) {
        return getPoint(topology.getLinearPoint(ijk));
    }

}