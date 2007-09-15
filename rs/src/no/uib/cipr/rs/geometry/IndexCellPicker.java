package no.uib.cipr.rs.geometry;

import no.uib.cipr.rs.util.Configuration;

/**
 * IndexCellPicker lets us select a cell by choosing the index directly.
 */
public class IndexCellPicker extends CellPicker {

    private final int[] elements;

    public IndexCellPicker(Configuration config, Mesh mesh) {
        elements = config.getIntArray("elements");

        for (int i = 0; i < elements.length; ++i) {
            int v = elements[i] - 1;

            // Index-check
            if (v < 0 || v >= mesh.elements().size())
                throw new IllegalArgumentException(
                        config.trace()
                                + "Element location must be between 1 and the number of elements");

            elements[i] = v;
        }
    }

    @Override
    public int[] elements() {
        return elements;
    }
}