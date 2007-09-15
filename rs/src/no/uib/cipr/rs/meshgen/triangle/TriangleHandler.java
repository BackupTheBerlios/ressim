package no.uib.cipr.rs.meshgen.triangle;

/**
 * Logically the TriangleHandler object encapsulates a function with the
 * signature:
 * 
 *      Point x Point x Point -&gt; Triangle
 * 
 * @author roland.kaufmann@cipr.uib.no
 */
public interface TriangleHandler {
    /**
     * Signal the number of triangles that the sink intends to create.
     * The creator is allowed to balk if the sink creates fewer or more
     * triangles that this amount.
     * 
     * @param count
     *  Number of triangles. May not be negative.
     */
    void prepareForTriangles(int count) throws TriExc;
    
    /**
     * Generate a triangle in the structure from three known points.
     * 
     * @param a
     *  First corner of the triangle. This must be the handle returned
     *  by the PointHandler of the structure.
     * @param b
     *  Second corner of the triangle.
     * @param c
     *  Third corner of the triangle.
     * @return
     *  Handle to the triangle that was created. The handle itself does
     *  not mean anything outside the context of the structure. 
     */
    int onTriangle(int a, int b, int c) throws TriExc;
    
    /**
     * Signal that the source is done creating objects, and that the
     * sink may finalize its structures. 
     */
    void closeTriangles() throws TriExc;    
}
