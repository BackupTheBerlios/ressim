package no.uib.cipr.rs.meshgen.grdecl;

/**
 * Use this class to walk through all the cells in a grid.
 * 
 * Iterate through all the blocks (a.k.a. cells) in a grid, based solely on
 * the structure information that can be inferred from the format, i.e. you 
 * won't get to the topological neighbour when incrementing the iterator, nor
 * does it consider which cells that are active or not. Thus, this iterator is
 * meant for performing low-level operations on the block grid.
 * 
 * It extends PillarNav so that you can get the current pillar for this block
 * (the left-most, back-most pillar is considered the "reference" pillar for a
 * block). Note that the method next() no longer takes you to the next 
 * coordinate line, but to the next block. (Instead of factoring this to two 
 * different classes that observes the current coordinate line and mutates the 
 * current coordinate line, we simply partly hide the semantics of the base
 * class, since no-one should be using a block-navigator as a pillar-navigator
 * anyways). 
 * 
 * @author roland.kaufmann@cipr.uib.no
 */
class BlockNav extends PillarNav {
    BlockNav(Format f) { super(f); }
    
    // current position along each of the axis; the i and j axis are set in
    // the superclass
    protected int k = 0;
    
    // helper method that calculates the block index in case we don't want the
    // regular world-view of adressing blocks by their left, back pillar
    // (see HingeNav for an example of this).
    protected int block(int i, int j) {
        return (format.nj * k + j) * format.ni + i;
    }

    /**
     * Index of the current block being processed, assuming that we have
     * allocated a continguous array to hold information for them all (Fortran-
     * style records).
     */
    int block() {
        return block(i, j);
    }
    
    @Override
    boolean done() {
        boolean done = k >= format.nk;
        return done;
    }
    
    @Override
    void next() {
        // note the difference between this test and the one that is done in the
        // coordinate line navigator; there are one more pillar than there are
        // blocks (in a given direction), so there we could test for equality
        // before we incremented. here, we must increment first and then test
        // (since the addressing is zero-based, we have reached the end when the
        // index becomes equal to the number of rows/columns). we also have one
        // more dimension to iterate through, since we move through each layer
        // of blocks (coordinate lines aren't stacked in layers, they are all
        // "mounted" at the bottom plane).
        i++;
        if(i == format.ni) {
            i = 0;
            j++;
            if(j == format.nj) {
                j = 0;
                k++;
            }
        }
    }

    @Override
    public String toString() {
        return String.format("(%d, %d, %d)", i, j, k);
    }
}
