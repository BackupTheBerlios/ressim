package no.uib.cipr.rs.meshgen.grdecl;

/**
 * Direction of movement along an axis. Declared as an enumeration to ease
 * readability of code that works with each side of block (along a given
 * dimension).
 * 
 * @author roland.kaufmann@cipr.uib.no
 */
enum Direction {
    Decrease,   // = 0
    Increase;   // = 1
    
    /**
     * Convert the ordinal into a sign that represents the direction, i.e. minus
     * for the decrease and plus for the increase.
     */
    int sign() {
        return 2 * this.ordinal() - 1;
    }
    
    /**
     * Relative index in an array to find this component of the axis. Usually we 
     * store the lower-most/left-most/front-most value first, and then the other 
     * side, so the ordinal of the enumeration is the index that should be used.
     */
    int offset() {
        return this.ordinal();
    }
}
