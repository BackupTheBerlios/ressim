package no.uib.cipr.rs.meshgen.structured;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import no.uib.cipr.rs.geometry.Tensor3D;
import no.uib.cipr.rs.rock.Rock;
import no.uib.cipr.rs.util.Configuration;

/**
 * Parses the given configuration and maps rock data keys to global values,
 * including adjustment for topology conversion
 */
public class RockDataParser {

    private Rock[] rocks;

    public RockDataParser(Configuration config, Map<String, Box> boxes,
            Map<Integer, String> regions, CartesianTopology topology,
            RegionGeometry geometry, Map<String, int[]> rockRegionMap) {

        // Read in the mapping from parameter name to its values for all the
        // grid cells
        Map<String, double[]> map = readParameterElementMapping(config, boxes,
                regions, topology, geometry);

        // Convert the region mapping to a cellwise mapping
        String[] rockRegions = new String[topology.getNumElements()];
        for (Map.Entry<String, int[]> v : rockRegionMap.entrySet()) {
            String name = v.getKey();
            int[] elements = v.getValue();
            for (int el : elements)
                rockRegions[el] = name;
        }

        // Create the rocks
        int size = topology.getNumElements();
        rocks = new Rock[size];
        double[] poro = get(map, "poro", size);
        double[] cr = get(map, "cr", size);
        double[] c = get(map, "c", size);

        Tensor3D[] perm = getTensor(map, "perm", size);
        Tensor3D[] cond = getTensor(map, "cond", size);

        for (int i = 0; i < rocks.length; ++i)
            rocks[i] = new Rock(poro[i], cr[i], perm[i], cond[i], c[i],
                    rockRegions[i]);
    }

    /**
     * Creates a tensor array. If any of the diagonal entries are not supplied,
     * the array contains the zero-tensor
     */
    private Tensor3D[] getTensor(Map<String, double[]> map, String pre, int size) {
        double[] x = map.get(pre + "x");
        double[] y = map.get(pre + "y");
        double[] z = map.get(pre + "z");

        // In case the tensor isn't specified
        if (x == null || y == null || z == null) {
            Tensor3D[] tensor = new Tensor3D[size];
            for (int i = 0; i < size; ++i)
                tensor[i] = Tensor3D.ZERO;
            return tensor;
        }

        double[] xy = get(map, pre + "xy", size);
        double[] xz = get(map, pre + "xz", size);
        double[] yz = get(map, pre + "yz", size);

        Tensor3D[] tensor = new Tensor3D[size];
        for (int i = 0; i < size; ++i)
            tensor[i] = new Tensor3D(x[i], y[i], z[i], xy[i], xz[i], yz[i]);
        return tensor;
    }

    private double[] get(Map<String, double[]> map, String key, int size) {
        double[] v = map.get(key);
        if (v == null)
            v = new double[size];
        return v;
    }

    private Map<String, double[]> readParameterElementMapping(
            Configuration config, Map<String, Box> boxes,
            Map<Integer, String> regions, CartesianTopology topology,
            RegionGeometry geometry) {
        Configuration rockData = config.getConfiguration("RockData");

        Map<String, double[]> map = new HashMap<String, double[]>();

        String type = rockData.getString("type");

        if (type.equalsIgnoreCase("Region")) {
            // initialise
            Map<Integer, Map<String, Double>> regionData = new HashMap<Integer, Map<String, Double>>();

            for (String regionKey : rockData.keys()) {
                if (regionKey.equalsIgnoreCase("type"))
                    continue;

                if (!regions.containsKey(Integer.valueOf(regionKey)))
                    throw new IllegalArgumentException(rockData.trace()
                            + "Invalid region index");

                Configuration sub = rockData.getConfiguration(regionKey);

                Map<String, Double> temp = new HashMap<String, Double>();

                for (String key : sub.keys())
                    temp.put(key, sub.getDouble(key));

                regionData.put(Integer.valueOf(regionKey), temp);
            }

            for (IJK ijk : topology.getElementsIJK()) {
                int index = topology.getLinearElement(ijk);

                int regionIndex = geometry.getElementRegion(ijk);

                for (String key : regionData.get(regionIndex).keySet()) {
                    if (!map.containsKey(key))
                        map.put(key, new double[topology.getNumElements()]);
                    map.get(key)[index] = regionData.get(regionIndex).get(key);
                }
            }
        }

        else if (type.equalsIgnoreCase("Global")) {
            int numElements = topology.getNumElements();

            for (String key : rockData.keys()) {
                if (key.equalsIgnoreCase("type"))
                    continue;

                double[] val = rockData.getDoubleArray(key);

                if (val.length == 1) {
                    double c = val[0];
                    val = new double[numElements];
                    Arrays.fill(val, c);
                } else if (val.length != numElements)
                    throw new IllegalArgumentException(rockData.trace()
                            + "Illegal length of array \"" + key + "\"");

                map.put(key, val);
            }
        } else
            throw new IllegalArgumentException(rockData.trace()
                    + "Illegal \"type\" value: " + type);

        // read boxed rock data
        Configuration rockDataBoxed = config.getConfiguration("RockDataBoxed");

        for (String key : rockDataBoxed.keys()) {
            Configuration boxConfig = rockDataBoxed.getConfiguration(key);

            for (String param : boxConfig.keys()) {
                if (!map.containsKey(param))
                    throw new IllegalArgumentException(config.trace()
                            + "Missing rock data: " + param);

                if (!boxes.containsKey(key))
                    throw new IllegalArgumentException(config.trace()
                            + "Missing box definition: " + key);

                Box box = boxes.get(key);

                double[] data = box.fill(map.get(param), boxConfig
                        .getDoubleArray(param), topology);

                // put adjusted values back into map
                map.put(param, data);
            }
        }
        return map;
    }

    public Rock getRock(int i) {
        return rocks[i];
    }
}
