package no.uib.cipr.rs.geometry;

/**
 * CellPicker is a framework for selecting an element in a mesh.
 * 
 * @author roland.kaufmann@cipr.uib.no
 */
public abstract class CellPicker {

    /**
     * Returns the elements picked by a concrete subclass. The indices must be
     * zero-offset
     */
    public abstract int[] elements();

}