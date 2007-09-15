package no.uib.cipr.rs.meshgen.grdecl;

/**
 * Description of the relative location of each point within a block. Eclipse
 * uses a right-hand side ordering where the reference point is located at the
 * left, back, upper point (!).
 * 
 * @author roland.kaufmann@cipr.uib.no
 */
public enum Corner {
    // every possibility listed in the order where the x axis varies first,
    // then the y-axis and finally the z-axis.
    LeftFrontUpper,     // 0 = (X.Left,  Y.Front, Z.Upper),
    RightFrontUpper,    // 1 = (X.Right, Y.Front, Z.Upper),    
    LeftBackUpper,      // 2 = (X.Left,  Y.Back,  Z.Upper),
    RightBackUpper,     // 3 = (X.Right, Y.Back,  Z.Upper),
    LeftFrontLower,     // 4 = (X.Left,  Y.Front, Z.Lower),
    RightFrontLower,    // 5 = (X.Right, Y.Front, Z.Lower);    
    LeftBackLower,      // 6 = (X.Left,  Y.Back,  Z.Lower),
    RightBackLower;     // 7 = (X.Right, Y.Back,  Z.Lower),
    
    // return the position of the corner of each individual axis; we shift
    // the ordinal index the number of dimensions that preceeds us (based on
    // the notion that x varies first etc.) and then bitwise-AND to make sure
    // that we're only left with the bit that interests us
    X x() { return X.values()[(this.ordinal() >> 0) & 1]; }
    Y y() { return Y.values()[(this.ordinal() >> 1) & 1]; }
    Z z() { return Z.values()[(this.ordinal() >> 2) & 1]; }
    
    // compose a corner given the direction (from the center) in each of the 
    // various dimensions
    static Corner xyz(X x, Y y, Z z) {
        int ordinal = (((                    z.ordinal())
                      *  Y.values().length + y.ordinal())
                      *  X.values().length + x.ordinal());
        return values()[ordinal];
    }
    
    /**
     * Find a corner given how one should move in each direction.
     * 
     * @param directions
     *  Array containing a direction (from the center) for each of the axis.
     *  There must be one and only one direction for each dimension (i.e. 3)
     * @return
     *  The corner located from the direction vectors (relative to center)
     */
    @SuppressWarnings("unused")
    static Corner from(Direction[] directions) {
        assert directions.length == Dimension.values().length;
        int bitmask = 0;
        for(Dimension d : Dimension.values()) {
            bitmask &= directions[d.ordinal()].ordinal() << d.ordinal(); 
        }
        Corner corner = Corner.values()[bitmask];
        return corner;
    }
    
    // compose a corner in only two directions; this is a convenience method
    // for using the enumeration in 2D cases.
    @SuppressWarnings("unused")
    private Corner xy(X x, Y y) {
        return xyz(x, y, Z.Upper);
    }
    
    /**
     * Project the corner onto the 2D case; given a point we can find the
     * relative (i,j)-index and the relative z-depth independently
     * 
     * @return
     *  New corner whose ordinal will be in the range [0..3], based on the
     *  location of this corner.
     */
    @SuppressWarnings("unused")
    private Corner project() {
        // keep only the two first dimensions (2 + 1); drop the third bit
        return values()[ordinal() & 3];
    }

    // type-safe directions for the end-point of each axis; we can iterate
    // through each of these separately    
    public enum X {
        Left,   // 0
        Right;  // 1
        
        // we have the same layout as the Direction enumeration; we can cast
        // between them using this two methods
        Direction direction() { return Direction.values()[this.ordinal()]; }
        static X from(Direction direction) { return X.values()[direction.ordinal()]; }
    }

    public enum Y {
        Front,  // 0
        Back;   // 1

        // we have the same layout as the Direction enumeration; we can cast
        // between them using this two methods
        Direction direction() { return Direction.values()[this.ordinal()]; }
        static Y from(Direction direction) { return Y.values()[direction.ordinal()]; }    
    }

    public enum Z {
        Upper,  // 0
        Lower;  // 1

        // we have the same layout as the Direction enumeration; we can cast
        // between them using this two methods
        Direction direction() { return Direction.values()[this.ordinal()]; }
        static Z from(Direction direction) { return Z.values()[direction.ordinal()]; }    
    }
    
    /**
     * Cartesian volume-area calculation ordering to right-hand rule counter-
     * clockwise ordering. Some of the data structures will require the latter 
     * and thus we need a conversion table to easily convert between them.
     */
    private final static int[] cartesian_to_ccw = new int[] {
         // 0, 1, 2, 3, 4, 5, 6, 7
            0, 3, 1, 2, 4, 7, 5, 6 
    };    
    
    /**
     * 
     */
    int rightHandIndex() {
        return cartesian_to_ccw[this.ordinal()];
    }
}
