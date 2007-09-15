package no.uib.cipr.rs.geometry;

import java.util.HashSet;
import java.util.Set;

import no.uib.cipr.rs.util.Configuration;

/**
 * BoundaryCellPicker selects all cells belonging to the outer boundary of the
 * mesh.
 */
public class BoundaryCellPicker extends CellPicker {

    private final int[] elements;

    public BoundaryCellPicker(Configuration config, Mesh mesh) {
        // we do not yet know how many boundary cells there are.
        Set<Element> bdElem = new HashSet<Element>();

        // boundary cells are determined from boundary interfaces
        for (Interface intf : mesh.interfaces()) {
            // add element if not already present
            if (intf.boundary)
                bdElem.add(mesh.element(intf));
        }

        // copy element indices
        elements = new int[bdElem.size()];
        int i = 0;
        for (Element e : bdElem)
            elements[i++] = e.index;

    }

    @Override
    public int[] elements() {
        return elements;
    }
}