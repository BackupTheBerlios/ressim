package no.uib.cipr.rs.meshgen.grdecl;

import no.uib.cipr.rs.geometry.Vector3D;

/**
 * Orientations of a coordinate system. Thumb follows x axis, index finger
 * follows y axis, and middle finger follows z axis. (Origo is at the hand;
 * the fingers point in the direction of each of the axis).
 * 
 * @author roland.kaufmann@cipr.uib.no
 */
enum Rule {
    LeftHand,   // = 0
    RightHand;  // = 1
    
    double y() {
        double y = 2 * this.ordinal() - 1;
        return y;
    }

    // transformation functions; each of the components should be multiplied
    // with the results of these methods to get a "standard" coordinate.
    Vector3D adjust(Vector3D v) {        
        return v.mult(y());
    }
}
