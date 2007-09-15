package no.uib.cipr.rs.meshgen;

import java.util.Map;

import no.uib.cipr.rs.geometry.Mesh;
import no.uib.cipr.rs.meshgen.structured.Box;
import no.uib.cipr.rs.meshgen.structured.BoxParser;
import no.uib.cipr.rs.meshgen.structured.CartesianTopology;
import no.uib.cipr.rs.meshgen.structured.RegionMapParser;
import no.uib.cipr.rs.meshgen.structured.RockDataParser;
import no.uib.cipr.rs.meshgen.structured.RockRegionParser;
import no.uib.cipr.rs.meshgen.structured.StructuredGeometry;
import no.uib.cipr.rs.meshgen.structured.StructuredGeometry1D;
import no.uib.cipr.rs.meshgen.structured.StructuredGeometry2D;
import no.uib.cipr.rs.meshgen.structured.StructuredGeometry3D;
import no.uib.cipr.rs.rock.Rock;
import no.uib.cipr.rs.util.Configuration;

/**
 * A structured mesh implementation
 */
public class StructuredMeshGenerator extends MeshGenerator {

    private int dimension;

    /**
     * Constructs a structured mesh from the given configuration.
     */
    public StructuredMeshGenerator(Configuration config) {

        dimension = config.getInt("Dimension");
        System.out.println(config.trace() + dimension
                + "D Structured mesh generator");

        StructuredGeometry geometry = getGeometry(config);

        CartesianTopology topology = getTopology(geometry);

        // map box names to boxes
        Map<String, Box> boxes = new BoxParser(config, topology).getBoxes();

        // map region indices to rock names
        Map<Integer, String> regions = new RegionMapParser(config, topology,
                geometry).getRegionMap();

        // map rock region names to element indices
        Map<String, int[]> rockRegionMap = new RockRegionParser(config, boxes,
                regions, topology, geometry).getRockRegionMap();

        // Create the rocks
        RockDataParser rockDataParser = new RockDataParser(config, boxes,
                regions, topology, geometry, rockRegionMap);

        Rock[] rocks = new Rock[topology.getNumElements()];
        for (int i = 0; i < rocks.length; ++i)
            rocks[i] = rockDataParser.getRock(i);

        mesh = new Mesh(geometry, topology, rocks);
    }

    private StructuredGeometry getGeometry(Configuration config) {
        switch (dimension) {
        case 1:
            return new StructuredGeometry1D(config);
        case 2:
            return new StructuredGeometry2D(config);
        case 3:
            return new StructuredGeometry3D(config);
        default:
            throw new IllegalArgumentException("Invalid dimension");
        }

    }

    private CartesianTopology getTopology(StructuredGeometry geometry) {
        switch (dimension) {
        case 1:
            return ((StructuredGeometry1D) geometry).getTopology();
        case 2:
            return ((StructuredGeometry2D) geometry).getTopology();
        case 3:
            return ((StructuredGeometry3D) geometry).getTopology();
        default:
            throw new IllegalArgumentException("Invalid dimension");
        }
    }

}
