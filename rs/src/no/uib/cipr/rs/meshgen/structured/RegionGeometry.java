package no.uib.cipr.rs.meshgen.structured;

import no.uib.cipr.rs.geometry.Geometry;

/**
 * A geometry with elements that have regions
 */
public abstract class RegionGeometry extends Geometry {

    protected String mappingType;

    protected int[] partZ;

    /**
     * Checks if the given mapping is valid
     */
    protected void checkMappingType() {
        if (!(mappingType.equalsIgnoreCase("Uniform") || mappingType
                .equalsIgnoreCase("Layer"))) {
            throw new IllegalArgumentException(
                    "Incorrect region mapping type: " + mappingType);
        }
    }

    /**
     * Calculates the region index for the given element. Region indexing starts
     * with 1.
     */
    protected int getElementRegion(IJK ijk) {
        int k = ijk.k();

        // check the kind of mapping used.
        if (mappingType.equalsIgnoreCase("Layer"))
            return (partZ[k] + 1);
        else if (mappingType.equalsIgnoreCase("Uniform"))
            return 1;
        else
            throw new IllegalArgumentException(
                    "Incorrect region mapping type: " + mappingType);
    }

}
