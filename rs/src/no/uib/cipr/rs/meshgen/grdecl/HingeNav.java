package no.uib.cipr.rs.meshgen.grdecl;

/**
 * Use this class to walk through all the points on a certain pillar.
 * 
 * Navigate along each corner point that is connected to a certain coordinate
 * line. If you move to another coordinate line, then you must create a new 
 * iterator. This trade-off will cause us to allocate memory more often than
 * we would otherwise have to, but since we will do much more work than use the
 * iterator at each line, it will probably not be the bottleneck.
 * 
 * @author roland.kaufmann@cipr.uib.no
 */
class HingeNav extends CornerNav {
    // number of possible corners for this particular coordinate line. it may
    // take the values 2 (global corner), 4 (global edge), 8 (interior cells)
    Corner[] possible;
    
    // index into the array of possible corners for this hinge; we use this as
    // a sub-iterator within this layer (when we have visited all (possible)
    // corners in one layer, do we move to the next). if we change this index,
    // then we must also update the c member inherited from the base class
    // which tells us at which corner we are.
    int currentCorner;
    
    /**
     * Create an iterator to enumerate all hinges around a specified coordinate
     * line.
     * 
     * @param p
     *  Navigator that can identify the coordinate line we walk to walk along;
     *  this navigator will not be altered.
     */
    HingeNav(PillarNav p) {
        super(p.format);

        // copy the coordinate line position from the navigator. note that we
        // have overloaded the block index calculation to reflect that we are
        // now basing ourself on the nearest coordinate line instead of the
        // one at the left, front of the entire block
        this.i = p.i;
        this.j = p.j;
        
        // setup an array for each corner, where we can mark if it should be
        // included in this enumeration of not
        possible = new Corner[p.hingesPerLayer()];
        
        // number of corners found so far
        int counter = 0;

        // loop through all possible corners, determining which to include
        for(Corner c : Corner.values()) {
            // if this corner violates any of the invariants then proceed to the 
            // next candidate, 
            if(valid(c)) {
                possible[counter++] = c;
            }
        }
        
        // we should have found all corner positions
        assert (counter == possible.length);
        
        // start at the bottom of the column, working our way up
        this.k = 0;
        this.currentCorner = 0;
        this.c = possible[currentCorner];
    }
    
    // returns true if the current corner is a valid one for this coordinate
    // line, i.e. there is a block which has a point on this pillar which is
    // the current corner for that block
    boolean valid(Corner c) {
        // check all invariants about the planes in which the coordinate is
        // placed and the presence of the corner, e.g. no-one has the last
        // pillar in a row as their left corner because that would imply that
        // there was another column of blocks behind it.
        boolean valid =
            (c.x() != Corner.X.Left  || i < format.ni) &&
            (c.x() != Corner.X.Right || i > 0) &&
            (c.y() != Corner.Y.Front || j < format.nj) &&
            (c.y() != Corner.Y.Back  || j > 0);
        return valid;
    }

    /**
     * Index of the block that contains the point at which we are currently
     * looking. Do not confuse the block index of a corner navigator (which
     * attempt to walk through all the points in the same block) and the hinge
     * navigator (which jumps from block to block, having the points at the
     * same coordinate line).
     */
    @Override
    int block() {
        // when we look at the right and the back corners, we are really
        // looking at blocks that belongs to the previous columns. for those
        // blocks we subtract one in the necessary dimensions before
        // calculating the global positions in the arrays
        return block(i - c.x().ordinal(), j - c.y().ordinal());
    }
    
    /**
     * Move to the next hinge in this column. All hinges at one layer will be
     * finished (the iterator "swings" around the coordinate line) before it
     * moves up to the next layer.  
     */
    @Override
    void next() {
        // go to the next corner
        currentCorner++;
        
        // if we are through with all the corners of this layer, then move to
        // the next layer (the done() method will detect when we are done with
        // the entire column based on the layer counter alone).
        if(currentCorner == possible.length) {
            currentCorner = 0;
            k++;
        }
        
        // keep the typed member in sync with the counter
        this.c = possible[currentCorner];
    }
}
