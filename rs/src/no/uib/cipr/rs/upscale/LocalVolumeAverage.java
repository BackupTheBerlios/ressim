package no.uib.cipr.rs.upscale;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import no.uib.cipr.matrix.DenseMatrix;
import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.EVD;
import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.NotConvergedException;
import no.uib.cipr.matrix.Vector;
import no.uib.cipr.matrix.sparse.IterativeSolverNotConvergedException;
import no.uib.cipr.rs.geometry.Connection;
import no.uib.cipr.rs.geometry.Element;
import no.uib.cipr.rs.geometry.Interface;
import no.uib.cipr.rs.geometry.Mesh;
import no.uib.cipr.rs.geometry.NeighbourConnection;
import no.uib.cipr.rs.geometry.CornerPoint;
import no.uib.cipr.rs.geometry.Point3D;
import no.uib.cipr.rs.geometry.Tensor3D;
import no.uib.cipr.rs.geometry.Vector3D;
import no.uib.cipr.rs.geometry.flux.Transmissibility;
import no.uib.cipr.rs.util.Configuration;

/**
 * Class for computing numerically upscaled permeability based on a local
 * upscaling method relating coarse and fine scale volume averaged fluxes and
 * pressure gradients.
 * 
 * TODO Various boundary conditions can be used.
 */
public class LocalVolumeAverage extends UpscalingMethod {

    // a map from domain index to array of interface indices
    private Map<Integer, List<Integer>> outwardDomainInterfaces;

    // pressure boundary conditions
    private PressureBC pressureBC;

    public LocalVolumeAverage(Configuration config) {
        super();

        // create additional topology information needed
        outwardDomainInterfaces = getOutwardDomainInterfaces();

        pressureBC = createPressureBC(config);

        computeAveragedPorosity();

        computeAveragedPermeability();
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

    /**
     * Returns a map from domain index to array of interface indices associated
     * to the corresponding inner domain boundary.
     */
    private Map<Integer, List<Integer>> getOutwardDomainInterfaces() {
        Map<Integer, List<Integer>> map = new HashMap<Integer, List<Integer>>();

        // for each connection of the subdomain mesh, determine which elements
        // are inside. Determine closest boundary orientation.
        for (int i = 0; i < numDomains; i++) {
            Mesh mesh = meshes[i];
            Subdomain subdomain = subdomains[i];

            List<Integer> border = getList(subdomain.borderElements());
            List<Integer> inner = getList(subdomain.innerElements());

            List<Integer> list = new ArrayList<Integer>();

            for (NeighbourConnection c : mesh.neighbourConnections()) {
                int here = mesh.here(c).index;
                int there = mesh.there(c).index;

                if (inner.contains(here)) {
                    if (border.contains(there))
                        list.add(mesh.hereInterface(c).index);
                } else if (border.contains(here)) {
                    if (inner.contains(there))
                        list.add(mesh.thereInterface(c).index);
                }
            }
            map.put(i, list);
        }
        return map;
    }

    /**
     * Returns a list of integers from the given array of ints.
     */
    private List<Integer> getList(int[] val) {
        List<Integer> list = new ArrayList<Integer>(val.length);

        for (int i : val)
            list.add(i);

        return list;
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

            List<Integer> outwardInterfaces = outwardDomainInterfaces.get(i);

            // compute x-direction flow solution
            Vector px = computePressure(bc.getXDirectionPressureBC(), mesh);

            // compute y-direction flow solution
            Vector py = computePressure(bc.getYDirectionPressureBC(), mesh);

            // post-process x- and y-direction
            // TODO make symmetrization method user defined
            Matrix K = postProcess(px, py, subdomain, mesh, outwardInterfaces);

            // output symmetry
            System.out.println("symmetry: k12 = " + K.get(0, 1) + ", k21 = "
                    + K.get(1, 0));
            System.out.println("symmetry: using kxy = kyx = k12");

            // output eigenvalues
            EVD evd = null;
            try {
                evd = EVD.factorize(K);
            } catch (NotConvergedException e) {
                System.err.println("Error computing eigenvalues of K matrix");
            }
            double[] ev = evd.getRealEigenvalues();
            System.out.print("eigenvalues: ");
            for (double v : ev)
                System.out.print(v + " ");
            System.out.println("");

            permeability[i] = new Tensor3D(K.get(0, 0), K.get(1, 1), 0, K.get(
                    0, 1), 0, 0);
        }
    }

    private Matrix postProcess(Vector px, Vector py, Subdomain subdomain,
            Mesh mesh, List<Integer> outwardInterfaces) {

        Vector gradPx = getAveragePressureGradient(px, subdomain, mesh,
                outwardInterfaces);
        Vector3D[] fx = computeDarcyFlux(px, mesh); // [m^3/s]
        Vector vx = getAverageVelocity(fx, subdomain, mesh);

        Vector gradPy = getAveragePressureGradient(py, subdomain, mesh,
                outwardInterfaces);
        Vector3D[] fy = computeDarcyFlux(py, mesh); // [m^3/s]
        Vector vy = getAverageVelocity(fy, subdomain, mesh);

        Matrix A = new DenseMatrix(5, 4);
        Vector b = new DenseVector(5);
        Vector x = new DenseVector(4);

        // build system matrix
        A.add(0, 0, gradPx.get(0));
        A.add(0, 1, gradPx.get(1));
        A.add(1, 2, gradPx.get(0));
        A.add(1, 3, gradPx.get(1));

        A.add(2, 0, gradPy.get(0));
        A.add(2, 1, gradPy.get(1));
        A.add(3, 2, gradPy.get(0));
        A.add(3, 3, gradPy.get(1));

        b.add(0, vx.get(0));
        b.add(1, vx.get(1));
        b.add(2, vy.get(0));
        b.add(3, vy.get(1));

        b.scale(-1.0);

        // add symmetry condition
        A.add(4, 1, 1.0);
        A.add(4, 2, -1.0);

        b.add(4, 0.0);

        // solve the system
        A.solve(b, x);

        // store the permeability matrix
        Matrix K = new DenseMatrix(2, 2);

        K.set(0, 0, x.get(0));
        K.set(0, 1, x.get(1));
        K.set(1, 0, x.get(2));
        K.set(1, 1, x.get(3));

        return K;
    }

    /**
     * See Hægland et al. 2005
     */
    private Vector3D getCornerInterpolatedVelocity(Element element,
            Vector3D[] flux) {
        if (element.points().size() != 4)
            throw new IllegalArgumentException(
                    "Corner interpolated velocity only valid for quadrilaterals");

        // element interface indices
        List<Integer> interfaces = new ArrayList<Integer>();
        for (Interface intf : mesh.interfaces(element))
            interfaces.add(intf.index);

        // corner velocities
        Vector3D[] q = new Vector3D[4];

        // element corner point indices
        List<CornerPoint> points = element.points();

        // loop through all the four corner points and compute corner velocities
        for (int i = 0; i < points.size(); i++) {

            List<Interface> localIntf = new ArrayList<Interface>();

            for (Interface s : points.get(i).interfaces())
                if (element.interfaces().contains(s))
                    localIntf.add(s);

            if (localIntf.size() != 2)
                throw new IllegalArgumentException(
                        "Only two interfaces can be associated with corner point");

            Interface ix = localIntf.get(0);
            Interface iy = localIntf.get(1);

            double ax = ix.area;
            double ay = iy.area;

            Vector3D nx = ix.normal;
            Vector3D ny = iy.normal;

            int cx = ix.connection().index;
            int cy = iy.connection().index;

            Matrix A = new DenseMatrix(2, 2);
            Vector x = new DenseVector(2);
            Vector b = x.copy();

            A.set(0, 0, nx.getComp(1));
            A.set(0, 1, nx.getComp(2));
            A.set(1, 0, ny.getComp(1));
            A.set(1, 1, ny.getComp(2));

            b.set(0, flux[cx].dot(nx) / ax);
            b.set(1, flux[cy].dot(ny) / ay);

            A.solve(b, x);

            q[i] = new Vector3D(x.get(0), x.get(1), 0.);
        }

        // compute cell center velocity value
        Vector3D vel = new Vector3D(0, 0, 0);
        for (Vector3D qi : q)
            vel = vel.plus(qi.mult(0.25));

        return vel;
    }

    private Vector getAverageVelocity(Vector3D[] flux, Subdomain subdomain,
            Mesh mesh) {
        // total volume
        double V = 0.0;

        // storage for average velocity
        Vector3D average = new Vector3D(0, 0, 0);

        // average cell face fluxes into cell flux
        for (int i : subdomain.innerElements()) {
            Element element = mesh.elements().get(i);

            double dV = element.volume;

            // Vector3D localVel = getLocalVelocity(element, flux);
            Vector3D localVel = getCornerInterpolatedVelocity(element, flux);

            average = average.plus(localVel.mult(dV));

            V += dV;
        }

        average = average.mult(1.0 / V);

        return new DenseVector(new double[] { average.x(), average.y(),
                average.z() });
    }

    @SuppressWarnings("unused")
    private Vector3D getLocalVelocity(Element element, Vector3D[] flux) {
        Vector3D localVel = new Vector3D(0, 0, 0);

        // coordinate direction vectors
        Vector3D ex = new Vector3D(1, 0, 0);
        Vector3D ey = new Vector3D(0, 1, 0);
        Vector3D ez = new Vector3D(0, 0, 1);

        // int n = element.interfaces().size();
        double S = 0.0;
        double Sx = 0.0;
        double Sy = 0.0;
        double Sz = 0.0;

        for (Interface intf : element.interfaces()) {
            Vector3D normal = intf.normal;

            double dS = intf.area;
            double dSx = Math.abs(dS * normal.dot(ex));
            double dSy = Math.abs(dS * normal.dot(ey));
            double dSz = Math.abs(dS * normal.dot(ez));

            S += dS;
            Sx += dSx;
            Sy += dSy;
            Sz += dSz;

            int c = intf.connection()index;

            // compute Darcy velocity [m/s]
            Vector3D u = flux[c].mult(1.0 / dS);

            localVel = localVel.plus(u);
        }

        localVel = new Vector3D(localVel.x() * Sx / S, localVel.y() * Sy / S,
                localVel.z() * Sz / S);

        return localVel;
    }

    /**
     * Compute the average pressure gradient using Green's formula and two-point
     * approximation of continuity point pressure.
     */
    private Vector getAveragePressureGradient(Vector p, Subdomain subdomain,
            Mesh mesh, List<Integer> outwardInterfaces) {
        Vector3D gradP = new Vector3D(0, 0, 0);

        for (int index : outwardInterfaces) {
            Interface intf = mesh.interfaces().get(index);

            Vector3D n = intf.normal;

            NeighbourConnection c = intf.connection();

            Interface is = mesh.hereInterface(c);
            Interface js = mesh.thereInterface(c);

            Element ei = is.element();
            Element ej = js.element();

            int i = eiindex;
            int j = ejindex;

            Tensor3D Ki = ei.rock.getAbsolutePermeability();
            Tensor3D Kj = ej.rock.getAbsolutePermeability();

            double Kin = Ki.multNorm(is.normal);
            double Kjn = Kj.multNorm(js.normal);

            double ui = p.get(i);
            double uj = p.get(j);

            double li = distance(ei, is);
            double lj = distance(ej, js);

            double u = (lj * Kin * ui + li * Kjn * uj) / (lj * Kin + li * Kjn);

            double A = intf.area;
            Vector3D pndS = n.mult(u * A);

            gradP = gradP.plus(pndS);
        }

        // compute inner domain volume
        double volume = 0.0;
        for (int elem : subdomain.innerElements())
            volume += mesh.elements().get(elem).volume;

        if (volume == 0.0)
            throw new IllegalArgumentException(
                    "Domain volume should not be zero");

        gradP = gradP.mult(1.0 / volume);

        return new DenseVector(new double[] { gradP.x(), gradP.y(), gradP.z() });
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

    private void computeAveragedPorosity() {
        // TODO Auto-generated method stub
        // TODO split permeability and porosity upscaling
    }

    /**
     * Computes the Darcy flux vector for all neighbour connections for the
     * given pressure solution and mesh
     * 
     * @param pressure
     *            Pressure solution
     * @param mesh
     *            Computational mesh
     */
    private Vector3D[] computeDarcyFlux(Vector pressure, Mesh mesh) {
        Vector3D[] flux = new Vector3D[mesh.connections().size()];

        for (Connection c : mesh.neighbourConnections()) {
            double D = 0;
            for (Transmissibility t : c.MD) {
                Element ek = mesh.element(t);
                double tk = t.k;

                D += tk * pressure.get(ekindex);
            }
            Vector3D normal = ((NeighbourConnection) c).hereInterface()
                    .normal;
            flux[cindex] = normal.mult(D);
        }
        return flux;
    }

    /**
     * Computes the distance between the centers of the given element and
     * interface
     * 
     * @return The Euclidian distance (2-norm)
     */
    private double distance(Element el, Interface intf) {
        Point3D elp = el.center, intp = intf.center;

        Vector3D difference = new Vector3D(elp, intp);

        return difference.norm2();
    }
}
