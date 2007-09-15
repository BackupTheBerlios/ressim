package no.uib.cipr.rs.meshgen.structured;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import no.uib.cipr.rs.meshgen.util.ArrayData;
import no.uib.cipr.rs.util.Configuration;

/**
 * Parses a configuration and maps rock region names to element indices
 */
public class RockRegionParser {

    private Map<String, List<Integer>> map;

    public RockRegionParser(Configuration config, Map<String, Box> boxes,
            Map<Integer, String> regions, CartesianTopology topology,
            RegionGeometry geometry) {
        // build region - element map
        map = new HashMap<String, List<Integer>>();

        for (String key : regions.values())
            map.put(key, new ArrayList<Integer>());

        for (IJK ijk : topology.getElementsIJK()) {
            String rockRegion = regions.get(geometry.getElementRegion(ijk));
            map.get(rockRegion).add(topology.getLinearElement(ijk));
        }

        // adjust for boxed regions
        Configuration rockRegionBoxed = config
                .getConfiguration("RockRegionBoxed");

        for (String key : rockRegionBoxed.keys()) {
            String boxRegion = rockRegionBoxed.getString(key).toLowerCase();

            if (!boxes.containsKey(key))
                throw new IllegalArgumentException(config.trace()
                        + "Missing box definition: " + key);

            Box box = boxes.get(key);

            if (!map.containsKey(boxRegion)) {
                map.put(boxRegion, new ArrayList<Integer>());
            }

            // add to/remove from elements
            for (String region : map.keySet()) {
                Set<Integer> el = new HashSet<Integer>(map.get(region));

                List<Integer> data = null;
                if (region.equalsIgnoreCase(boxRegion))
                    data = box.add(el, topology);
                else
                    data = box.remove(el, topology);

                Collections.sort(data);
                map.put(region, data);
            }
        }
    }

    public Map<String, int[]> getRockRegionMap() {
        Map<String, int[]> temp = new HashMap<String, int[]>();
        for (String key : map.keySet())
            temp.put(key, ArrayData.integerListToArray(map.get(key)));
        return temp;
    }

}
