package no.uib.cipr.rs.meshgen.lgr;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import no.uib.cipr.rs.meshgen.structured.CartesianTopology;
import no.uib.cipr.rs.meshgen.structured.IJK;

class EdgeMap {

    private CartesianTopology topology;

    private Map<IJK, EdgeLocation> map;

    /**
     * Creates an element-edge map with the specified initial capacity based on
     * the given Cartesian topology.
     */
    public EdgeMap(int i, CartesianTopology topology) {
        this.topology = topology;
        map = new HashMap<IJK, EdgeLocation>(i);
    }

    /**
     * Puts a new map entry to the map if the given ijk-key is valid.
     */
    public void put(IJK key, EdgeLocation value) {
        if (topology.isValidElement(key))
            map.put(key, value);
    }

    /**
     * Returns the entry set of this edge map.
     */
    public Set<Map.Entry<IJK, EdgeLocation>> entrySet() {
        return map.entrySet();
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        for (Map.Entry<IJK, EdgeLocation> e : map.entrySet()) {
            s.append(e.getKey().toString());
            s.append("-");
            s.append(e.getValue().toString() + "\n");
        }
        return s.toString();
    }

}
