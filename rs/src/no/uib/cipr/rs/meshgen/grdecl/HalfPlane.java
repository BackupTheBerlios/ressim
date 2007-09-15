package no.uib.cipr.rs.meshgen.grdecl;

/**
 * Each top or bottom edge is represented as a half-plane that separates the
 * surface into two; the part that is on the "inside" and the part that is on
 * the "outside". A side of a block is everything that is inside both its top
 * and bottom half-planes.
 * 
 * Initially, each half-plane goes from one side to the other. Depending on
 * which other half-planes that are also present in the figure, parts of it (or
 * even the entire line) may be obscured and no longer visible. These parts are
 * cut of.
 * 
 * Since it is intersected by other straight (in the bilinear map at least)
 * lines, then it may never be split into two, only truncated.
 * 
 * @author roland.kaufmann@cipr.uib.no
 */
class HalfPlane implements Comparable<HalfPlane> {
    // vector multiplier along each of the pillar on which this plane "hangs".
    // note that the multipliers/depths are for each their pillar, and cannot
    // be directly compared to eachother (but they can of course be compared
    // to the similar one in another half-plane).
    final Ridge ridge;

    // helper class that registers the intersection points between this line
    // and another one that truncates it, i.e. has a (partially) more 
    // restrictive half-plane.
    static class Clip {
        Clip(double at) { this.at = at; who = null; }
        double at;
        HalfPlane who;
    }
    
    // initially, the halfplane extends from the very start (no distance from
    // the first pillar) to the very end (entire vector to the other pillar
    // travelled).
    Clip start = new Clip(0.);
    Clip end   = new Clip(1.);
    
    // constants that represents up and down for the cutting (which is why they
    // are *half*-planes). the value is selected by considering that the half-
    // planes are specified with points from left to right, and we want to loop
    // through the final polygon in clockwise direction, which makes the top
    // half-plane the "natural" one. if these constants are changed, then the
    // greater-than tests in truncate() must be revisited.
    final static double DOWNWARDS =  1.0;
    final static double UPWARDS   = -1.0;
    
    // sign of difference to the other half-plane for this one to also include
    // the other, i.e. if the sign of the difference matches this value, then
    // this half-plane dominates the other (the other truncates this one,
    // because it is more restrictive).
    final double direction = 1.0;
    
    HalfPlane() {
        ridge = null;
    }
    
    /**
     * Cut of the part of this half-plane that is hidden by another half-plane.
     *
     * The letter h is chosen for the other half-plane both as a mnemonic for
     * the word "half-plane" (the name of the class) but also to remind of
     * "here" which is the opposite of "there" (t in this is supposed to remind
     * of us of the latter).
     */
    void truncate(HalfPlane h) {
        // differences for both the left side (seen from this half-plane) and 
        // the right side
        double d0 = h.ridge.y0 - this.ridge.y0;
        double d1 = h.ridge.y1 - this.ridge.y1;
        
        // the edges crosses if we are at one edge higher, and at the other end
        // lower, than the other edge
        final boolean crosses = (d0 * d1 < 0.);
        final boolean infront = (d0 * h.direction > 0.);
        
        // if they crosses, then one will partially obscure the other
        if (crosses) {
            // we have to half-planes emerging from (different places on) the
            // same two pillars; the x-coordinates of these points is thus equal.
            // vector for the first half-plane is (0,a.y_0)+t*(1,a.y_1-a.y_0)
            // and the vector for the other half-plane (called b) is
            // (0,b.y_0)+t*(1,b.y_1-b.y_0). we set those equations equal to find
            // the parameter t where the lines cross; we only have to solve the
            // second component
            double t = d0 / (d0 - d1);
            assert 0.0 <= t && t <= 1.0;
            
            // if the other edge is "in front" of us (i.e. the difference from
            // us to the other one matches its direction), then the corner that
            // we are evaluating (the left one -- y0) are behind the other one
            // and the start should be truncated. otherwise, the argument holds
            // the other way (one of the sides must be truncated since we know
            // that they are crossing).
            if (infront) {
                // only truncate at this point, if it was visible (not hidden
                // behind some other edge)
                if (t > start.at) {
                    start.at  = t;
                    start.who = h;
                }
            }
            else {
                if (t < end.at) {
                    end.at  = t;
                    end.who = h;
                }
            }
        }
        // otherwise, one will completely obscure the other
        else {
            // if we are back the other edge (see above), then we are completely 
            // obscured and should disappear from view. we max of the values of
            // the cutting points to their extreme direction, so that this edge
            // will never compete in the sorting sequence to figure out the 
            // polygon afterwards
            if (infront) {
                start.at  = Integer.MAX_VALUE;
                start.who = h;
                end.at    = Integer.MIN_VALUE;
                end.who   = h;
            }
        }
    }
    
    /**
     * Determine whether there is anything left of this half-plane or if it is
     * truncated away entirely. 
     */
    boolean present() {
        // if the half-plane has been truncated (i.e. there is another half-
        // plane in the figure that is more restrictive), then the members are
        // set so that this test will fail (and the half-planes sort correctly)
        boolean present = (start.at < end.at);
        return present;
    }

    /**
     * Compare the half-planes in a way that is consistent with a clockwise
     * enumeration of the entire figure, i.e. planes that appear before are
     * sorted lower.
     */
    public int compareTo(HalfPlane h) {
        // sort upper lines before lower lines (assuming that we start at the
        // upper, left corner of the bilinear surface)
        double diff = this.direction - h.direction;
        
        // only continue to compare lines if they are in the same direction; we
        // want to get to the very right-most end before we return and go back
        if (diff == 0.) {
            // choose the one that comes earlier in a clockwise enumeration of 
            // the points (if we are enumerating the lower lines, then we want 
            // the one with the latest end, if we are enumerating the upper 
            // lines then we want the one with the earliest start).
            if (direction == DOWNWARDS) {
                diff = this.start.at - h.start.at;
            }
            else {
                diff = h.end.at - this.end.at;
            }
        }
        
        // convert the difference into a sign only
        return (int) Math.signum(diff);
    }    
}