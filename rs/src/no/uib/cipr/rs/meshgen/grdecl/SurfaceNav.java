package no.uib.cipr.rs.meshgen.grdecl;

/**
 * Use this class to walk through all (bilinear, internal) surfaces that binds
 * together two columns of blocks in the grid.
 * 
 * Enumerator for two of the four sides on the side of each block, e.g. the
 * front and the right. Since the two other sides (e.g. back and left) are the
 * primary sides of another block, they are only included once.
 * 
 * The top and bottom sides are not enumerated because they only borders to
 * blocks in the same column (it should always be trivial to find a neighbor in
 * that direction; since the grid is semi-structured it is only to increment and
 * decrement the counter in that direction).
 * 
 * @author roland.kaufmann@cipr.uib.no
 */
class SurfaceNav extends PillarNav {
    // direction we are looking at represented by the side we are looking
    // through; this is either front or right. we start out at the first and
    // then alternate to the second before moving on to the next block.
    Side s;

    // flag that determines whether we should alternate between two sides, or if
    // we are just looking through one side all of the time (if there is only
    // one row or column). we could have replaced this flag with an interface
    // through which we call virtual methods, but since there are only two
    // variant we rely on the JIT-compiler doing some smart inlining
    // the first member s determines how we observe the navigator, whereas this
    // one determines how we traverse to the next side.
    final boolean alternate;

    SurfaceNav(Format f) {
        super(f);

        // if we are able to move to the front (more than one row), then we
        // prefer to start in that direction. if there are more directions (i.e.
        // we have more than one column also), then setup an alternating scheme.
        if (format.ni > 1) {
            s = Side.Front;
            if (format.nj > 1) {
                alternate = true;
            } else {
                alternate = false;
            }
        } else {
            // assume that we can move to the right (we'll test for a deficient
            // grid below); this is our only direction.
            s = Side.Right;
            alternate = false;

            // if we haven't any sides to explore, then quickly advance through
            // all available blocks and put the marker at a position where the
            // navigator will return that all sides are done.
            if (format.nj == 1) {
                j = format.nj;
            }
        }
    }

    /**
     * Base pillar (the one in the left, back corner) of the neighbour block
     * (the one that has the dual interface to this one).
     */
    int neighbour() {
        int ij = pillar(
                i + (s == Side.Right ? 1 : 0), 
                j + (s == Side.Front ? 1 : 0));
        return ij;
    }
    
    /**
     * 
     */
    Side here()  { return s; }
    Side there() { return s.opposite(); }

    @Override
    boolean done() {
        // we are done when exhausted all possible rows
        boolean done = (j == format.nj);
        return done;
    }

    @Override
    void next() {
        // branch to the appropriate way of going forward based on the
        // alternation scheme between sides
        if (alternate) {
            // if we got in this clause, then we always have two sides; when we
            // are returning to the first side, then increment to the next row
            if (s == Side.Front) {
                s = Side.Right;
            } else {
                s = Side.Front;
                i++;
            }
        } else {
            // if there is only one side, then keep that side fixed and just
            // advance to the next row immediately
            i++;
        }

        // test is done after increment here, so that we don't include the
        // last row and column (the front sidelright of those are included
        // as the back/left side of the latter row/column
        if (i == format.ni) {
            i = 0;
            j++;

            // nothing more to increment; when we are done with all the j
            // then our enumeration is finished!
        }
    }
}