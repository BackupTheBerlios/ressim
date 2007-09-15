package no.uib.cipr.rs.meshgen.structured;

import java.util.HashMap;
import java.util.Map;

import no.uib.cipr.rs.util.Configuration;

/**
 * Parses a configuratioin and maps region indices to rock names.
 */
public class RegionMapParser {

    private Map<Integer, String> map;

    public RegionMapParser(Configuration config, CartesianTopology topology,
            RegionGeometry geometry) {
        Configuration sub = config.getConfiguration("RockRegionMap");

        map = new HashMap<Integer, String>();

        for (String key : sub.keys()) {
            int[] regionIndices = sub.getIntArray(key);

            for (int i : regionIndices)
                map.put(i, key);
        }

        // check
        for (IJK ijk : topology.getElementsIJK()) {
            int region = geometry.getElementRegion(ijk);

            if (!map.containsKey(region)) {
                throw new IllegalArgumentException(sub.trace()
                        + "RockRegionMap must define region " + region);
            }
        }
    }

    public Map<Integer, String> getRegionMap() {
        return map;
    }

}
