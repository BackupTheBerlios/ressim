package no.uib.cipr.rs.meshgen;

import no.uib.cipr.rs.util.Configuration;

public class ChevronMeshImporter extends MeshGenerator {
    public ChevronMeshImporter(Configuration config) throws Exception {
        mesh = new no.uib.cipr.rs.meshgen.chevron.Grid(config).build();
    }
}
