package no.uib.cipr.rs.meshgen.grdecl;

/**
 * Principles sides of a hexahedral cell. Initially, it is assumed that each
 * block have these cells. The corners in each cell are specified in surface-
 * calculating order, i.e. first the principal axis (x) varies, then the
 * secondary (y) axis. The order of the points is otherwise such that the
 * normal vector of the surface will be pointing outwards if using the left-
 * hand-side rule.
 * 
 * @author roland.kaufmann@cipr.uib.no
 */
enum Side {
    Top     (Corner.LeftFrontUpper, Corner.RightFrontUpper,
             Corner.LeftBackUpper,  Corner.RightBackUpper),    // = 0

    Bottom  (Corner.RightFrontLower,Corner.LeftFrontLower,
             Corner.RightBackLower, Corner.LeftBackLower),     // = 1

    Front   (Corner.LeftFrontLower, Corner.RightFrontLower,
             Corner.LeftFrontUpper, Corner.RightFrontUpper),   // = 2
             
    Back    (Corner.LeftBackUpper,  Corner.RightBackUpper,
             Corner.LeftBackLower,  Corner.RightBackLower),    // = 3
    
    Left    (Corner.LeftBackLower,  Corner.LeftFrontLower,
             Corner.LeftBackUpper,  Corner.LeftFrontUpper),    // = 4
             
    Right   (Corner.RightFrontLower,Corner.RightBackLower,
             Corner.RightFrontUpper,Corner.RightBackUpper);    // = 5
    
    private Corner[] c;
    
    private Side(Corner a, Corner b, Corner c, Corner d) {
        this.c = new Corner[] {a, b, c, d};
    }
    
    /**
     * Side that is normal to the same dimension as this one, but at the
     * opposite side of the center of the block. Two and two sides are opposites
     * to each other, e.g. left is opposite to right and right is opposite to
     * left. In the context of this relation, no other sides are opposite to left. 
     */
    Side opposite() {
        // since the sides are listed in each dimension and pairwise with their
        // opposite, then we can just figure out in which pair the side is
        // (the dimension) and then which of the pair (the direction).
        int dimension = this.ordinal() / 2;
        int direction = this.ordinal() % 2;
        
        // since the direction is either 0 or 1, we can use it as a boolean flag
        // and turn the direction with a two's complement scheme
        direction = -( direction - 1 );
        
        // compose the new side value and return the object that is representing
        // this value from the function.
        Side opposite = Side.values()[dimension * 2 + direction];
        return opposite;
    }

    /**
     * All corners for this particular side.
     */
    int[] corners() {
        // Cartesian ordering usid in surface-area calculation to counter-
        // clockwise right-hand rule ordering used for visualization of the 
        // element. Element topology require the latter, which is why the index
        // 3 and 2 appearently are swapped below.
        int[] ordinals = new int[] {
                c[0].ordinal(), c[1].ordinal(), c[3].ordinal(), c[2].ordinal()
        };
        return ordinals;
    }
}
