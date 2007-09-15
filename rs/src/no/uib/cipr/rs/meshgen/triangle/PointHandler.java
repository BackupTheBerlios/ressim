package no.uib.cipr.rs.meshgen.triangle;

/**
 * Logically the PointHandler object encapsulates a function that performs the
 * following value mapping:
 * 
 * Coord x Coord x Coord -&gt; Point
 * 
 * @author roland.kaufmann@cipr.uib.no
 */
interface PointHandler {
    /**
     * Signal the number of points the source intends to create.
     * 
     * @param count
     *            The number of points. If the sink is exhausted (more than
     *            'count' points are created), then it is allowed to balk.
     */
    void prepareForPoints(int count) throws TriExc;

    /**
     * Create a new point object and register it for later use.
     * 
     * @param x
     *            First coordinate of the point.
     * @param y
     *            Second coordinate of the point.
     * @param z
     *            Third coordinate of the point.
     * @return Opaque index that now represents the point
     */
    int onPoint(double x, double y, double z) throws TriExc;

    /**
     * Signal that the source is done creating objects, and that the sink may
     * finalize its structures.
     */
    void closePoints() throws TriExc;
}
