package no.uib.cipr.rs.meshgen;

import no.uib.cipr.rs.geometry.Mesh;
import no.uib.cipr.rs.util.Configuration;

/**
 * Generates meshes from either a specification or by importing
 */
public abstract class MeshGenerator {

    protected Mesh mesh;

    public Mesh getMesh() {
        return mesh;
    }

    /**
     * Generates a mesh
     */
    public static Mesh generate(Configuration config) {
        return config.getObject("MeshGenerator", MeshGenerator.class).getMesh();
    }

}
