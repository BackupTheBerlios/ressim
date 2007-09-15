package no.uib.cipr.rs.meshgen;

import no.uib.cipr.rs.geometry.Mesh;
import no.uib.cipr.rs.meshgen.lgr.LGRGeometry;
import no.uib.cipr.rs.meshgen.lgr.LGRTopology;
import no.uib.cipr.rs.rock.Rock;
import no.uib.cipr.rs.util.Configuration;

public class LGRMeshGenerator extends MeshGenerator {

    /**
     * Creates the LGR mesh for the given configuration
     */
    public LGRMeshGenerator(Configuration config) {
        LGRGeometry geometry = new LGRGeometry(config);

        LGRTopology topology = geometry.getTopology();

        Rock[] rocks = new Rock[topology.getNumElements()];
        for (int i = 0; i < rocks.length; ++i)
            rocks[i] = geometry.getRock(i);

        // build the composite lgr-mesh
        mesh = new Mesh(geometry, topology, rocks);
    }

}
