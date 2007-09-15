package no.uib.cipr.rs.upscale;

import java.util.Map;

import no.uib.cipr.matrix.DenseMatrix;
import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.Vector;
import no.uib.cipr.matrix.sparse.IterativeSolverNotConvergedException;
import no.uib.cipr.rs.geometry.Connection;
import no.uib.cipr.rs.geometry.Element;
import no.uib.cipr.rs.geometry.Mesh;
import no.uib.cipr.rs.geometry.Subdomain;
import no.uib.cipr.rs.geometry.Tensor3D;
import no.uib.cipr.rs.geometry.flux.Transmissibility;
import no.uib.cipr.rs.meshgen.structured.Orientation;
import no.uib.cipr.rs.util.Configuration;

/**
 * Class for computing numerically upscaled permeability based on a local
 * upscaling method relating coarse and fine scale flow rates.
 * 
 * TODO Various boundary conditions can be used.
 */
public class LocalFlowRateAverage extends UpscalingMethod {

    private PressureBC[] pressureBC;

    public LocalFlowRateAverage(Configuration config) {
        super();

        pressureBC = createPressureBC(config);

        computeAveragedPorosity();

        computeAveragedPermeability();
    }

    /**
     * Sets up local system of equations for each domain. Performs one flow
     * simulation in each coordinate direction. Then fluxes are summed and
     * coarse permeability computed.
     */
    private void computeAveragedPermeability() {
        for (int i = 0; i < numDomains; i++) {
            Subdomain subdomain = subdomains[i];
            Mesh mesh = meshes[i];
            PressureBC bc = pressureBC[i];

            // compute x-direction flow solution
            Vector px = computePressure(bc.getXDirectionPressureBC(), mesh);

            // compute y-direction flow solution
            Vector py = computePressure(bc.getYDirectionPressureBC(), mesh);

            // post-process x-direction, gets [k11, k21]
            Vector kx = postProcessXDirection(px, subdomain, mesh);

            // post-process y-direction, gets [k12, k22]
            Vector ky = postProcessYDirection(py, subdomain, mesh);

            // finished post-processing
            Matrix K = new DenseMatrix(new Vector[] { kx, ky });

            // TODO make symmetry-method use defined, now k21 = k12.
            // output symmetry
            System.out.println("symmetry: k12 = " + K.get(0, 1) + ", k21 = "
                    + K.get(1, 0));
            System.out.println("symmetry: using Kxy = Kyx = k12");

            permeability[i] = new Tensor3D(K.get(0, 0), K.get(1, 1), 0, K.get(
                    0, 1), 0, 0);
        }
    }

    private Vector postProcessYDirection(Vector py, Subdomain subdomain,
            Mesh mesh) {
        double[] uy = computeDarcyFlux(py, mesh);

        double q22 = getCoarseFlowRate(Orientation.BACK, uy, subdomain);
        double q12 = getCoarseFlowRate(Orientation.LEFT, uy, subdomain);

        double L2 = subdomain.getY1() - subdomain.getY0();
        double L1 = subdomain.getX1() - subdomain.getX0();
        double dp = 1.0;

        double k22 = q22 * L2 / (L1 * dp);
        double k12 = q12 / dp;

        return new DenseVector(new double[] { k12, k22 });
    }

    private Vector postProcessXDirection(Vector px, Subdomain subdomain,
            Mesh mesh) {
        double[] ux = computeDarcyFlux(px, mesh);

        // compute coarse scale fluxes
        double q11 = getCoarseFlowRate(Orientation.RIGHT, ux, subdomain);
        double q21 = getCoarseFlowRate(Orientation.BACK, ux, subdomain);

        double L1 = subdomain.getX1() - subdomain.getX0();
        double L2 = subdomain.getY1() - subdomain.getY0();

        double dp = 1.0;

        double k11 = q11 * L1 / (L2 * dp);
        double k21 = q21 / dp;

        return new DenseVector(new double[] { k11, k21 });
    }

    /**
     * Computes the Darcy flux for all neighbour connections for the given
     * pressure solution vector and mesh
     * 
     * @param pressure
     *            Pressure solution
     * @param mesh
     *            Computational mesh
     */
    private double[] computeDarcyFlux(Vector pressure, Mesh mesh) {
        double[] flux = new double[mesh.connections().size()];

        for (Connection c : mesh.neighbourConnections()) {
            double D = 0;
            for (Transmissibility t : c.MD) {
                Element ek = mesh.element(t);
                double tk = t.k;

                D += tk * pressure.get(ek.index);
            }
            flux[c.index] = D;
        }
        return flux;
    }

    /**
     * Sums the volumetric Darcy fluxes times area to return a coarse volumetric
     * flux
     */
    private double getCoarseFlowRate(Orientation orient, double[] u,
            Subdomain subdomain) {
        double q = 0;

        for (int i : subdomain.innerConnections().get(orient)) {
            // TODO adjust for non-aligned fine and coarse interfaces: n * n_i
            q += u[i];
        }
        return q;
    }

    private void computeAveragedPorosity() {
        // TODO Auto-generated method stub
        // TODO split permeability and porosity upscaling
    }

    /**
     * Returns an array of pressure boundary conditions, one for each subdomain,
     * from the given configuration, subdomain and mesh.
     */
    private PressureBC[] createPressureBC(Configuration config) {
        PressureBC[] pbc = new PressureBC[numDomains];

        for (int i = 0; i < pbc.length; i++)
            pbc[i] = new PressureBC(config, subdomains[i], meshes[i]);

        return pbc;
    }

    private Vector computePressure(Map<Integer, Double> bc, Mesh mesh) {
        PressureDiscretisation pressure = null;
        try {
            pressure = new PressureDiscretisation(mesh, bc);
        } catch (IterativeSolverNotConvergedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return pressure.getSolution();
    }

}
