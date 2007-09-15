package no.uib.cipr.rs.meshgen;

import no.uib.cipr.rs.util.Configuration;

public class EclipseMeshImporter extends MeshGenerator {
    public EclipseMeshImporter(Configuration config) throws Exception {
        // get the filename from the configuration file; this must include the
        // extension so that we are able to branch to the correct type of grid
        String fileName = config.getString("name");
        
        // create the right kind of mesh from the file extension
        if (fileName.toLowerCase().endsWith(".grdecl")) {
            mesh = no.uib.cipr.rs.meshgen.grdecl.Grid.build(fileName);
        }
        else if (fileName.toLowerCase().endsWith(".fegrid")) {
            mesh = no.uib.cipr.rs.meshgen.fegrid.Grid.build(fileName);
        }
        else {
            System.err.format("Unknown file format: %s", fileName);
        }
    }
}
