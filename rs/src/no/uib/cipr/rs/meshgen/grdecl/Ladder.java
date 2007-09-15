package no.uib.cipr.rs.meshgen.grdecl;

class Ladder {
    int a;  // k-value for first column
    int b;  // k-value for second column
    boolean incA;   // is a incrementable
    boolean incB;   // is b incrementable
    
    /**
     * Block index of the element on one of the sides 
     */
    int here() {
        return -1;
    }
    
    int there() {
        return -1;
    }
}
