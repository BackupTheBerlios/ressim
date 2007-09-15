package no.uib.cipr.rs.meshgen.grdecl;

/**
 * Dimensions of the grid. This is the semantic return value of the SPECGRID 
 * keyword in Eclipse. Currently only a single, Cartesian grid is supported.
 * Other classes uses this class to determine the size of columns and rows in
 * the grid.
 * 
 * @author roland.kaufmann@cipr.uib.no
 */
class Format {
    // number of grid blocks in each direction. note that there will be one
    // more coordinate line than there will be blocks.
    final int ni, nj, nk;
    
    /**
     * @param ni
     *  Number of blocks in the x-axis direction.
     * @param nj
     *  Number of blocks in the y-axis direction.
     * @param nk
     *  Number of blocks in the z-axis direction.
     */
    Format(int ni, int nj, int nk) {
        this.ni = ni;
        this.nj = nj;
        this.nk = nk;
    }
    
    /**
     * Number of elements we need in an array to keep all of the blocks.
     */
    int numOfBlocks() {
        return ni * nj * nk;
    }  
    
    /**
     * Size of the set of coordinate lines.
     * 
     * @return
     *  Number of items that must be allocated in a flat array to hold
     *  information about all the coordinate lines in this grid.
     */
    int numOfPillars() {
        // there are one more coordinate line than blocks in each direction
        // since each block must be hinged on a coordinate line on each side
        return (ni+1) * (nj+1);
    }
}
