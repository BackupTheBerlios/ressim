package no.uib.cipr.rs.meshgen.structured;

import java.util.HashMap;
import java.util.Map;

import no.uib.cipr.rs.util.Configuration;

/**
 * Parses the given configuration and maps box names to boxes.
 */
public class BoxParser {

    private Map<String, Box> boxes;

    public BoxParser(Configuration config, CartesianTopology topology) {
        boxes = new HashMap<String, Box>();
        Configuration sub = config.getConfiguration("Boxes");

        for (String key : sub.keys()) {
            int[] dim = sub.getIntArray(key);

            if (dim.length != 6)
                throw new IllegalArgumentException(config.trace()
                        + "Box dimensions must be specified by 6 integers");

            boxes.put(key, new Box(dim, topology));
        }
    }

    /**
     * Returns the boxes that have been parsed.
     */
    public Map<String, Box> getBoxes() {
        return boxes;
    }

}
