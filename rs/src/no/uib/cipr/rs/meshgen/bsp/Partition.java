package no.uib.cipr.rs.meshgen.bsp;

/**
 * Different space partition types
 */
public enum Partition {

    /**
     * <code>INSIDE</code> Partition type
     */
    INSIDE("inside"),

    /**
     * <code>OUTSIDE</code> Partition type
     */
    OUTSIDE("outside"),

    /**
     * <code>COINCIDENT</code> Partition type
     */
    COINCIDENT("coincident"),

    /**
     * <code>INTERSECTING</code> Partition type
     */
    INTERSECTING("intersecting");

    private String type;

    /**
     * @param type
     */
    private Partition(String type) {
        this.type = type;
    }

    /**
     * Returns the partition type.
     */
    public String getType() {
        return type;
    }

}
