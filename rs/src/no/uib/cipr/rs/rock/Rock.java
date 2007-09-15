package no.uib.cipr.rs.rock;

import java.io.Serializable;

import no.uib.cipr.rs.geometry.Tensor3D;
import no.uib.cipr.rs.util.Tolerances;

/**
 * Static rock data for a gridcell. Note that the rock object is immutable,
 * i.e. it represents a certain collection of values that will not be changed;
 * cast in stone so to say (haha).
 */
public class Rock implements Serializable {

    private static final long serialVersionUID = 8728748187560516239L;

    /**
     * Initial porosity [-]
     */
    private final double phi;

    /**
     * Rock compaction [1/Pa]
     */
    private final double cr;

    /**
     * Absolute permeability tensor [m^2]
     */
    private final Tensor3D K;

    /**
     * Rock heat conductivity tensor [W/(m*K)]
     */
    private final Tensor3D k;

    /**
     * Rock heat capacity [J(/K*m^3)]
     */
    private final double c;

    /**
     * Region name. Used for identifying the associated rock/fluid properties
     */
    private final String region;

    /**
     * Sets up the static rock data
     * 
     * @param region
     *            Region name
     * @param phi
     *            Initial porosity [-]
     * @param cr
     *            Rock compaction [1/Pa]
     * @param K
     *            Absolute permeability tensor [m^2]
     * @param k
     *            Rock heat conductivity tensor [W/(m*K)]
     * @param c
     *            Rock heat capacity [J(/K*m^3)]
     */
    public Rock(double phi, double cr, Tensor3D K, Tensor3D k, double c,
            String region) {
        // replace the region name with the canonical representation; this
        // means that two rock objects that are of the same region will have
        // the same string reference; lookup should be much faster
        this.region = region.intern();

        this.phi = phi;
        if (phi < 0 || phi > 1)
            throw new IllegalArgumentException(
                    "The porosity must be between zero and one");

        this.cr = cr;
        if (cr < 0)
            throw new IllegalArgumentException(
                    "Rock compressibility cannot be negative");

        this.K = K;

        this.k = k;

        this.c = c;
        if (c < 0)
            throw new IllegalArgumentException(
                    "Rock heat capacity cannot be negative");
    }

    /**
     * Sets up the static rock data, iso-thermal case
     * 
     * @param region
     *            Region name
     * @param phi
     *            Initial porosity [-]
     * @param cr
     *            Rock compaction [1/Pa]
     * @param K
     *            Absolute permeability tensor [m^2]
     */
    public Rock(double phi, double cr, Tensor3D K, String region) {
        this(phi, cr, K, Tensor3D.ZERO, 0, region);
    }

    /**
     * Gets the initial porosity
     * 
     * @return [-]
     */
    public double getInitialPorosity() {
        return phi;
    }

    /**
     * Gets the rock compaction
     * 
     * @return [1/Pa]
     */
    public double getRockCompaction() {
        return cr;
    }

    /**
     * Gets the absolute permeability tensor
     * 
     * @return [m^2]
     */
    public Tensor3D getAbsolutePermeability() {
        return K;
    }

    /**
     * Gets the rock heat conductivity tensor
     * 
     * @return [W/(m*K)]
     */
    public Tensor3D getRockHeatConductivity() {
        return k;
    }

    /**
     * Gets the rock heat capacity
     * 
     * @return [J/(K*m^3)]
     */
    public double getRockHeatCapacity() {
        return c;
    }

    /**
     * Gets the rock region name, for identifying the associated rock/fluid
     * properties
     */
    public String getRegion() {
        return region;
    }
    
    @Override
    public int hashCode() {
        // convert each of the members to their bitwise equivalent,
        // similar to reinterpret_cast<> in C++.
        long bitsPorosity   = Double.doubleToLongBits(this.phi);
        long bitsCompaction = Double.doubleToLongBits(this.cr );
        long bitsCapacity   = Double.doubleToLongBits(this.c  );

        // combine all components into one bit string, so that the state
        // of the entire object is encapsulated into one bit-string.
        long bitsLong = bitsPorosity ^ bitsCompaction ^ bitsCapacity;

        // a double has 64 bits precision whereas the hash code should
        // have only half of that. combine the two parts into one using
        // the same operator used to combine the various members, so we
        // get one with the appropriate length
        int bitsLo = (int) (bitsLong);
        int bitsHi = (int) (bitsLong >>> 32);
        
        // tensors are capable or calculating their own hashcode
        int bitsPermeability = this.K.hashCode();
        int bitsConductivity = this.k.hashCode();
        int bitsRegion       = 0; // this.region.hashCode();
        
        // merge all integer hashcodes for the relevant members, resulting in a
        // hash code that identifies the rock properties
        int hashCode =
            bitsLo ^ bitsHi ^ bitsPermeability ^ bitsConductivity ^ bitsRegion;
        return hashCode;
    }
    
    @Override
    public boolean equals(Object theOther) {
        // quick test; if it is the same reference, then the object must
        // be equal; the relation is always reflexive
        if (this == theOther) {
            return true;
        }

        // existientiality test; since this is an object, the other must
        // also be one -- null is never the same as anything but itself
        if (null == theOther) {
            return false;
        }

        // equivalence relation covers all kind of pairs; we are allowed
        // to subclass the pair in ways that do not affect the relation
        if (!(theOther instanceof Rock)) {
            return false;
        }
        
        // at this point we have figured out that the other object is of the
        // same type, but is not us. start looking at the members.
        Rock r = (Rock) theOther;
        
        // create a flag with the same name as the member that indicates the
        // equality between this and the other object with respect to that
        // member. (note that the string reference is compared instead of the
        // content since the string is intern()d in the constructor).
        boolean phi    = Math.abs(this.phi - r.phi) < Tolerances.smallEps;
        boolean cr     = Math.abs(this.cr  - r.cr ) < Tolerances.smallEps;
        boolean K      = this.K.equals(r.K);
        boolean k      = this.k.equals(r.k);
        boolean c      = Math.abs(this.c   - r.c  ) < Tolerances.smallEps;
        
        // don't include the name of the rock; this allows us to compare two
        // rock types regardless of their given name
        boolean region = true; // this.region == r.region;
        
        // we need all ducks in a row to say that they are equal
        return phi && cr && K && k && c && region;
    }
}
