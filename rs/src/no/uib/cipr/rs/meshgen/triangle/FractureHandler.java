package no.uib.cipr.rs.meshgen.triangle;

/**
 * FractureHandler encapsulates a callback function that alter the state
 * of an edge from 'regular' to 'fracture', used in later calculations.
 * 
 * @author roland.kaufmann@cipr.uib.no
 */
public interface FractureHandler {
    /**
     * Absorb a report of the number of fractures found in the problem
     * and prepare the internal structures for receiving them.
     * 
     * @param count
     *  Number of fractures. Must be positive.
     */
    void prepareForFractures(int count) throws TriExc;
    
    /**
     * Accept information for a new fracture. This method assumes that a
     * fracture consists of a single, straight line between two points.
     * More complex fractures must be broken into simpler ones.
     * 
     * @param a
     *  Index of the start point of the fracture. This index must earlier
     *  have been defined using a PointHandler from the same sink.
     * @param b
     *  Index of the end point of the fracture.
     * @param kind
     *  Type of fracture. Pass the special value BOUNDARY if the constraint is 
     *  not really a fracture, but rather a boundary to the edge of the grid.
     * @throws TriExc
     *  If the data passed are invalid, for instance that the points
     *  does not exist or that another fracture has been created along
     *  this edge already.
     */
    void onFracture(int a, int b, Kind c) throws TriExc;
    
    /**
     * Finalize structure after the last fracture has been received.
     */
    void closeFractures() throws TriExc;
}
