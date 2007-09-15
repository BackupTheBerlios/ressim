package no.uib.cipr.rs.geometry;

import java.io.Serializable;

import no.uib.cipr.rs.util.Configuration;

/**
 * The location of a source in the mesh
 */
public class SourceLocation implements Serializable {

    private static final long serialVersionUID = -6341321979123247392L;

    /**
     * Element indices containing this source
     */
    final int[] elements;

    /**
     * Name of the source
     */
    final String name;

    public SourceLocation(Configuration config, String name, Mesh mesh) {
        // Create a cell picker, using an indexed cell picker as default
        CellPicker cellPicker = config.getObject(name, CellPicker.class,
                IndexCellPicker.class, mesh);

        // Store the determined elements
        this.elements = cellPicker.elements();
        this.name = name;
    }
}
