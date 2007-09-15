package no.uib.cipr.rs.geometry;

import no.uib.cipr.rs.util.Configuration;

/**
 * Picks out a range of cells
 */
public class RangeCellPicker extends CellPicker {

    private final int[] elements;

    public RangeCellPicker(Configuration config, Mesh mesh) {
        int start = config.getInt("start");
        int delta = config.getInt("delta", 1);
        int number = config.getInt("number");

        if (start < 1)
            throw new IllegalArgumentException(config.trace()
                    + "'start' cannot be smaller than 1");

        if (delta < 1)
            throw new IllegalArgumentException(config.trace()
                    + "'delta' cannot be smaller than 1");

        if (number < 1)
            throw new IllegalArgumentException(config.trace()
                    + "'number' cannot be smaller than 1");

        elements = new int[number];

        elements[0] = start - 1;
        for (int i = 1; i < number; ++i)
            elements[i] = elements[i - 1] + delta;

        if (elements[number - 1] >= mesh.elements().size())
            throw new IllegalArgumentException(config.trace()
                    + "Last element location, " + (elements[number - 1] + 1)
                    + ", exceeds the number of mesh elements");
    }

    @Override
    public int[] elements() {
        return elements;
    }
}
