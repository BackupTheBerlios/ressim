package no.uib.cipr.rs.meshgen;

import no.uib.cipr.rs.meshgen.triangle.TriExc;
import no.uib.cipr.rs.meshgen.triangle.TriangleMesh;
import no.uib.cipr.rs.util.Configuration;

/**
 * Configuration-file-visible extension of a mesh made out of triangles.
 * Reflection will only create classes that are in the main package, but we
 * prefer to have the main class in a subpackage, so it can use other classes in
 * that package without us making them public.
 * 
 * @author roland.kaufmann@cipr.uib.no
 */
public final class TriangleMeshImporter extends TriangleMesh {
    public TriangleMeshImporter(Configuration config) throws TriExc {
        super(config);
    }
}
