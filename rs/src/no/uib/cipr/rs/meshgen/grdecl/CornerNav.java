package no.uib.cipr.rs.meshgen.grdecl;

/**
 * Use this class to walk through all the points in a grid.
 * 
 * Navigates through each point in a mesh in the order specified in the input
 * file. This class inherits from the block navigator since we are able to
 * observe in which block the current corner point is. Also, the coordinate line
 * reporting is restored to its original semantics; it will now return the
 * pillar on which this corner is.
 * 
 * @author roland.kaufmann@cipr.uib.no
 */
class CornerNav extends BlockNav {
    CornerNav(Format f) { super(f); }

    // start out at the very bottom of the grid
    protected Corner c = Corner.LeftFrontUpper;    
    
    /**
     * Corner within the block that the point represents. 
     */
    int corner() { return c.ordinal(); }    
    
    @Override
    int pillar() {
        // we are now adressing blocks, which means that the coordinate line
        // in question depends on which corner we are in
        return pillar(i + c.x().ordinal(), j + c.y().ordinal());
    }
    
    /**
     * Advance iterator. This pair of functions follows the Command-Query
     * idiom in order to be usable in for-loops.
     */
    @Override
    void next() {
        // break a corner down in its individual positions, so that we can
        // increment one dimension at the time (analoguous to the way we
        // increment the block index itself one at the time).
        Corner.X x = c.x();
        Corner.Y y = c.y();
        Corner.Z z = c.z();
    
        // this is basically the same increment logic as in a BlockNav, except
        // that we first try to exhaust the corners in that direction before
        // allowing to jump to another block. this makes the increment logic for
        // the corners interspersed with the increment logic for the blocks,
        // making it hard to reuse the superclass method.
        
        // increment x
        if(x == Corner.X.Left) {
            x = Corner.X.Right;
        }
        else {
            x = Corner.X.Left;
            i++;
            if(i == format.ni) {
                i = 0;
                // increment y
                if(y == Corner.Y.Front) {
                    y = Corner.Y.Back;                    
                }
                else {
                    y = Corner.Y.Front;
                    j++;
                    if(j == format.nj) {
                        j = 0;
                        // increment z
                        if(z == Corner.Z.Upper) {
                            z = Corner.Z.Lower;
                        }
                        else {
                            z = Corner.Z.Upper;
                            // no more dimensions to increment; always jump
                            // to one depth further below (termination test
                            // will become true if we are outside of grid)
                            k++;
                        }
                    }
                }
            }
        }
        
        // we always change corner position, regardless of whether we stayed
        // in the same block or jumped to another
        c = Corner.xyz(x, y, z);
    }
    
    @Override
    public String toString() {
        return String.format("%s, %s", super.toString(), c);
    }
}
