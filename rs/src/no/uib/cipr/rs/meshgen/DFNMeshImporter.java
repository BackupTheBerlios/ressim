package no.uib.cipr.rs.meshgen;

import java.util.Arrays;

import no.uib.cipr.rs.geometry.Connection;
import no.uib.cipr.rs.geometry.Element;
import no.uib.cipr.rs.geometry.Mesh;
import no.uib.cipr.rs.geometry.Tensor3D;
import no.uib.cipr.rs.geometry.flux.Transmissibility;
import no.uib.cipr.rs.meshgen.dfn.DFNGeometry;
import no.uib.cipr.rs.meshgen.dfn.DFNTopology;
import no.uib.cipr.rs.meshgen.dfn.Parser;
import no.uib.cipr.rs.rock.Rock;
import no.uib.cipr.rs.util.Configuration;

/**
 * Imports DFN (discrete fracture network) meshes and associated
 * transmissibilities
 */
public class DFNMeshImporter extends MeshGenerator {

    public DFNMeshImporter(Configuration config) throws Exception {

        System.out.println(config.trace()
                + "Discrete Fracture Network importer");

        // Read in the files
        String gridFile = config.getString("GridFile");
        String transFile = config.getString("TransFile");
        Parser fileData = new Parser(gridFile, transFile);

        // Create geometry, topology, and the rocks
        DFNGeometry geometry = new DFNGeometry(fileData);
        DFNTopology topology = new DFNTopology(fileData);
        Rock[] rocks = buildRocks(fileData);

        // Create the mesh, and add in transmissibilities
        mesh = new Mesh(geometry, topology, rocks);
        setTransmissibilities(geometry, mesh);
    }

    private Rock[] buildRocks(Parser fileData) {
        Rock[] rocks = new Rock[fileData.getNumActiveCVs()];

        for (int i = 0; i < rocks.length; ++i) {
            int zone = fileData.getElementZone(i);

            String name = String.format("zone%d", zone);
            double poro = fileData.getPorosity(zone);
            double perm = fileData.getPermeability(zone);

            rocks[i] = new Rock(poro, 0, new Tensor3D(perm), name);
        }

        return rocks;
    }

    private void setTransmissibilities(DFNGeometry geometry, Mesh mesh) {
        for (Connection c : mesh.connections()) {
            int i = c.index;
            Element here = mesh.here(c);
            Element there = mesh.there(c);

            double trans = geometry.getConnectionTrans(i);
            Transmissibility t1 = new Transmissibility(here.index, trans);
            Transmissibility t2 = new Transmissibility(there.index, -trans);

            c.setDarcyTransmissibilities(Arrays.asList(t1, t2));
        }
    }

}
