package no.uib.cipr.rs.upscale;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;

import no.uib.cipr.rs.Paths;
import no.uib.cipr.rs.geometry.Mesh;
import no.uib.cipr.rs.geometry.Subdomain;
import no.uib.cipr.rs.geometry.Tensor3D;
import no.uib.cipr.rs.util.Configuration;

/**
 * Class that reads in mesh and partition data files. Implementing classes
 * perform absolute permeability and porosity upscaling.
 * 
 * TODO split permeability and porosity in different classes since the porosity
 * is (almost) always computed using an volume weighted arithmetic average.
 * 
 */
abstract class UpscalingMethod {

    protected int numDomains;

    protected Mesh[] meshes;

    protected Subdomain[] subdomains;

    // Coarse scale permeability tensors. One tensor for each inner part of
    // coarse domains.
    protected Tensor3D[] permeability;

    // Coarse scale porosity values. One element for each inner part of coarse
    // domains.
    protected double[] porosity;

    /**
     * Reads number of domains together with mesh and partition information from
     * file.
     */
    public UpscalingMethod() {
        numDomains = Paths.numSubDomains();

        meshes = new Mesh[numDomains];

        subdomains = new Subdomain[numDomains];

        for (int rank = 0; rank < numDomains; rank++) {

            String meshFile = Paths.GRIDDING_OUTPUT + "/" + Paths.MESH_FILE
                    + "." + (rank + 1);
            Paths.checkPresence(meshFile);

            /*
             * Subdomain mesh
             */

            Mesh mesh = null;
            Subdomain subDomain = null;
            try {
                ObjectInput in = new ObjectInputStream(new BufferedInputStream(
                        new FileInputStream(meshFile)));

                SubdomainMesh subMesh = (SubdomainMesh) in.readObject();
                mesh = subMesh.getMesh();
                subDomain = subMesh.getSubDomain();

                in.close();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            meshes[rank] = mesh;
            subdomains[rank] = subDomain;
        }

        // initialize result arrays
        permeability = new Tensor3D[numDomains];

        porosity = new double[numDomains];
    }

    /**
     * Returns an array of upscaled tensor permeabilities. The elements of the
     * array are assigned to the coarse mesh elements with corresponding linear
     * index.
     */
    public Tensor3D[] getPermeability() {
        return permeability;
    }

    /**
     * Returns an array of upscaled porosities. The elements of the array are
     * assigned to the coarse mesh elements with corresponding linear index.
     */
    public double[] getPorosity() {
        return porosity;
    }

    public static UpscalingMethod create(Configuration config) {
        return config.getObject("UpscalingMethod", UpscalingMethod.class);
    }
}
