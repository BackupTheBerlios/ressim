package no.uib.cipr.rs.geometry;

import no.uib.cipr.rs.util.Configuration;

/**
 * CenterPointCellPicker selects a cell whose center point is closest to the
 * coordinate specified. The effect of injection or production will seem to be
 * at the centroid, which is the rationale of using this as the discretization
 * of the coordinate (to elements); it doesn't matter if the coordinate
 * technically belongs to the outer rim of another element.
 */
public class CenterPointCellPicker extends CellPicker {

    private final int element;

    public CenterPointCellPicker(Configuration config, Mesh mesh) {
        // Read each of the coordinates from the file
        Double x = config.getDouble("X");
        Double y = config.getDouble("Y");
        Double z = config.getDouble("Z");

        // create a point from these coordinates
        Point3D p = new Point3D(x, y, z);

        // currently closest element found and the associated distance
        // (cached, instead of reading it again from the point every time)
        Element closest = null;
        Double record = Double.POSITIVE_INFINITY;

        // find the cell whose center is closest to our point
        for (Element e : mesh.elements()) {
            // calculate the distance from the point specified to the
            // center of this element
            Double distance = new Vector3D(p, e.center).norm2();

            // if this distance is closer than anything we have seen before
            // then we've got a new candidate
            if (distance < record) {
                closest = e;

                // we've got a new record holder; don't update again until
                // we find something that is even lower than this
                record = distance;
            }
        }

        // index of the element we found to be closest
        if (closest == null)
            throw new IllegalArgumentException(config.trace()
                    + "The mesh doesn't appear to contain any elements");
        else
            element = closest.index;
    }

    @Override
    public int[] elements() {
        return new int[] { element };
    }
}