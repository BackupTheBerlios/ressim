package no.uib.cipr.rs.meshgen.grdecl;

/**
 * Use this class to walk through all pillars in a grid.
 * 
 * Iterates through all coordinate lines (a.k.a. pillars) in the order they
 * are found in the input file, i.e. with x-axis varying first. To iterate
 * through all coordinate lines, use the following pattern:
 * 
 * <code>
 *      int[] a = new int[f.numOfPillars()];
 *      for(PillarNav p = new PillarNav(f); !p.done(); p.next()) {
 *          a[p.pillar()] = ...;
 *      }
 * </code>
 * 
 * The class is implemented as a generic type taking the concrete subclass as a
 * parameter. This enables us to implement most of the iterator support in the
 * base class, returning the navigator itself as the data type. This allows us
 * to use the new for-each loop to write clearer code:
 * 
 * <code>
 *      int[] = new int[f.numOfPillars()];
 *      for(PillarNav p : f.pillars()) {
 *          a[p.pillar()] = ...;
 *      }
 * </code>
 * 
 * @author roland.kaufmann@cipr.uib.no
 */
class PillarNav {
    // we must know the extent of the grid to determine when to wrap over to
    // the next column
    protected final Format format;
        
    // current structured indices (the coordinate lines are organised in a
    // dimension-reduced, structured grid).
    protected int i = 0;
    protected int j = 0;
    
    /**
     * Create a navigator for iterating through all the pillars in a grid with
     * the specified format. 
     */
    PillarNav(Format format) { this.format = format; }

    // helper method that calculates the coordinate line index, in case we don't
    // have the regular way of iterating over coordinate lines (see CornerNav
    // for an example).
    protected int pillar(int i, int j) {
        return j * (format.ni+1) + i;
    }
    
    /**
     * Index of the current coordinate line; use this to index into the array
     * that holds the information for each coordinate line. Do not use this
     * method if done() returns true.
     */
    int pillar() {
        return pillar(i, j);
    }
    
    /**
     * Number of points on this pillar that corners of blocks may be "hinged"
     * on; the pillars on the edge do not have the same number as the internal
     * pillars, as they only have blocks on one side. 
     */
    int hingesPerLayer() {
        // find the number of rows before and after this coordinate line (note
        // that the las coordinate line index is ni, since there is one more
        // coordinate line than blocks); then calculate the total number of
        // blocks on each side of the line. read the variable name as "number of
        // rows (i) before" and "number of rows after".
        int iBefore = ( i == 0  ? 0 : 1 );
        int iAfter  = ( i == format.ni ? 0 : 1 );
        int iAdjacent = iBefore + iAfter;
        
        // repeat what was done for rows, now for columns
        int jBefore = ( j == 0  ? 0 : 1 );
        int jAfter  = ( j == format.nj ? 0 : 1 );
        int jAdjacent = jBefore + jAfter;
        
        // all the neighbours in the first dimension will have the neighbours 
        // of the other dimension, hence multiply the two counts. note that 
        // must always yield a result between 1 and 4.
        int adjacent = iAdjacent * jAdjacent;

        // each adjacent block will add two hinges (upper and lower), for each
        // layer, which will have its own set of corners
        return Corner.Z.values().length * adjacent;
    }
    
    /**
     * Number of hinges that will be specified for each coordinate line in the
     * input file. (Coordinate lines on the edges will not have as many hinges
     * as the lines in the middle of the grid, as they have less adjacent
     * blocks). Used to allocate the array of the corner points along each line.
     * 
     * The method is located here because we allocate the arrays while iterating
     * over the set of coordinates lines (which means that we have this object
     * handy).
     * 
     * @return
     *  Number of hinges on this coordinate line. The hinge() method of a
     *  navigator will return a result between 0 and this value minus one.
     */
    int maxNumOfHinges() {
        return format.nk * hingesPerLayer();
    }
    
    /**
     * Iteration methods, for looping over the set. This method returns true if
     * the iterator no longer points to a valid coordinate line (i.e. the value
     * returned from pillar() should no longer be trusted. 
     */
    boolean done() {
        // when we have surpassed the last column, then we're done
        boolean done = j > format.nj;
        return done;
    }
    
    /**
     * Advance the iterator to the next coordinate line.
     */
    void next() {
        // if we're at the last row, then jump to the next column
        if(i == format.ni) {
            i = 0;
            // we have no more dimensions to increment! keep walking to
            // columns outside of the grid, to indicate that we are done.
            j++;
        }
        else {
            i++;
        }
    }
}