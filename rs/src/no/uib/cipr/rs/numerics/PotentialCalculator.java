package no.uib.cipr.rs.numerics;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.Matrices;
import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.Vector;
import no.uib.cipr.matrix.sparse.BiCGstab;
import no.uib.cipr.matrix.sparse.CompRowMatrix;
import no.uib.cipr.matrix.sparse.DefaultIterationMonitor;
import no.uib.cipr.matrix.sparse.ILU;
import no.uib.cipr.matrix.sparse.IterativeSolver;
import no.uib.cipr.matrix.sparse.IterativeSolverNotConvergedException;
import no.uib.cipr.matrix.sparse.OutputIterationReporter;
import no.uib.cipr.matrix.sparse.Preconditioner;
import no.uib.cipr.rs.Paths;
import no.uib.cipr.rs.geometry.Connection;
import no.uib.cipr.rs.geometry.CornerPoint;
import no.uib.cipr.rs.geometry.Element;
import no.uib.cipr.rs.geometry.Interface;
import no.uib.cipr.rs.geometry.Mesh;
import no.uib.cipr.rs.geometry.NeighbourConnection;
import no.uib.cipr.rs.geometry.Point3D;
import no.uib.cipr.rs.geometry.SourceLocation;
import no.uib.cipr.rs.geometry.Tensor3D;
import no.uib.cipr.rs.geometry.Vector3D;
import no.uib.cipr.rs.geometry.flux.Transmissibility;
import no.uib.cipr.rs.output.GMVExport;
import no.uib.cipr.rs.util.Configuration;
import no.uib.cipr.rs.util.Function;

/**
 * Calculates the potential over a mesh. Basically a single-phase solver
 */
public class PotentialCalculator extends GMVExport {

    public static void main(String[] args) throws IOException,
            IterativeSolverNotConvergedException, ClassNotFoundException {
        System.out.println("\tPotential calculator\n");

        // Read in meshes
        System.out.println("Reading mesh...\n");
        PotentialCalculator pot = new PotentialCalculator();

        // Calculate potential and flux
        System.out.println("Calculating potential and flux...\n");
        pot.calculatePotential();

        // Output in GMV format
        System.out.println("Output GMV data...\n");
        pot.output();
    }

    double[] potential, flux;

    // store analytical potential for visualization purposes
    double[] analyticalPot;

    double[] potErrorL2, fluxErrorL2;

    double[] potErrorMax, fluxErrorMax;

    public PotentialCalculator() throws IOException, ClassNotFoundException {
        super();
        potential = new double[mesh.elements().size()];
        flux = new double[mesh.connections().size()];

        analyticalPot = new double[mesh.elements().size()];

        potErrorMax = new double[mesh.connections().size()];
    }

    private void calculatePotential()
            throws IterativeSolverNotConvergedException, IOException {
        // Get the source locations
        Mesh mesh = super.mesh;

        // get potential calculator configuration
        Paths.checkPresence(Paths.POTENTIAL_FILE);
        Configuration config = new Configuration(Paths.POTENTIAL_FILE);

        // Read in sources
        Map<SourceLocation, Function> sources = readSources(config,
                sourceLocations);

        // Setup the sparse linear system
        int size = mesh.elements().size();
        int[][] nz = determineSparsityPattern(mesh);
        CompRowMatrix A = new CompRowMatrix(size, size, nz);
        DenseVector b = new DenseVector(size);

        // Discretize the fluxes
        discretizeFlux(mesh, A);

        // Discretize the sources
        discretizeSources(sources, A, b);

        // Solve the linearized system for the potential
        potential = solve(A, b);

        // Post-process to determine the flux
        flux = calculateFlux(mesh, potential);

        // Compare with analytical solution if given
        if (config.containsKey("AnalyticalSolution")) {
            Function analytical = config.getFunction("AnalyticalSolution", 3);

            // store analytical solution for visualization
            for (Element e : mesh.elements()) {
                Point3D center = e.center;
                analyticalPot[e.index] = analytical.get(center.x(), center.y(),
                        center.z());
            }

            // compute potential errors
            System.out.println("\nComputing potential error...\n");
            double epL2 = computeL2ErrorPotential(analytical);
            double epMax = computeMaxErrorPotential(analytical);

            // compute flux errors
            System.out.println("\nComputing flux error...\n");
            double eqL2 = computeL2ErrorFlux(analytical);
            double eqMax = computeMaxErrorFlux(analytical);

            // dump the results
            System.out.println("u_l2 = " + epL2);
            System.out.println("u_max = " + epMax);
            System.out.println("q_l2 = " + eqL2);
            System.out.println("q_max = " + eqMax);

        }
    }

    @Override
    protected Collection<CellVariable> cellVariables() {
        Collection<CellVariable> sup = super.cellVariables();

        sup.add(new Potential());

        sup.add(new AnalyticalPotential());

        sup.add(new CellFluxX());
        sup.add(new CellFluxY());
        sup.add(new CellFluxZ());

        sup.add(new PotentialError());

        return sup;
    }

    class PotentialError extends CellVariable {
        @Override
        public String getName() {
            return "PotError";
        }

        @Override
        public double get(Element el) {
            return potErrorMax[el.index];
        }
    }

    class Potential extends CellVariable {
        @Override
        public String getName() {
            return "Potential";
        }

        @Override
        public double get(Element el) {
            return potential[el.index];
        }
    }

    class AnalyticalPotential extends CellVariable {
        @Override
        public String getName() {
            return "AnalyticalPot";
        }

        @Override
        public double get(Element el) {
            return analyticalPot[el.index];
        }
    }

    class CellFluxX extends CellVariable {
        @Override
        public double get(Element el) {
            double f = 0;

            for (Interface intf : mesh.interfaces(el)) {
                if (intf.boundary)
                    continue;

                NeighbourConnection c = mesh.connection(intf);
                f += flux[c.index] * mesh.hereInterface(c).normal.x()
                        / intf.area;
            }

            return f;
        }

        @Override
        public String getName() {
            return "flux_x";
        }
    }

    class CellFluxY extends CellVariable {
        @Override
        public double get(Element el) {
            double f = 0;

            for (Interface intf : mesh.interfaces(el)) {
                if (intf.boundary)
                    continue;

                NeighbourConnection c = mesh.connection(intf);
                f += flux[c.index] * mesh.hereInterface(c).normal.y()
                        / intf.area;
            }

            return f;
        }

        @Override
        public String getName() {
            return "flux_y";
        }
    }

    class CellFluxZ extends CellVariable {
        @Override
        public double get(Element el) {
            double f = 0;

            for (Interface intf : mesh.interfaces(el)) {
                if (intf.boundary)
                    continue;

                NeighbourConnection c = mesh.connection(intf);
                f += flux[c.index] * mesh.hereInterface(c).normal.z()
                        / intf.area;
            }

            return f;
        }

        @Override
        public String getName() {
            return "flux_z";
        }
    }

    private double[] calculateFlux(Mesh mesh, double[] potential) {
        double[] flux = new double[mesh.connections().size()];

        for (Connection c : mesh.connections()) {
            int i = c.index;

            for (Transmissibility t : c.MD) {
                double tk = t.k;
                int k = mesh.element(t).index;

                flux[i] += tk * potential[k];
            }
        }

        return flux;
    }

    private double[] solve(CompRowMatrix A, DenseVector b)
            throws IterativeSolverNotConvergedException {
        IterativeSolver solver = new BiCGstab(b);
        Preconditioner M = new ILU(A.copy());
        M.setMatrix(A);

        DefaultIterationMonitor monitor = new DefaultIterationMonitor();
        monitor.setRelativeTolerance(1.0e-18);
        monitor.setIterationReporter(new OutputIterationReporter());

        solver.setIterationMonitor(monitor);

        DenseVector x = b.copy();

        solver.solve(A, b, x);

        return x.getData();
    }

    private void discretizeSources(Map<SourceLocation, Function> sources,
            Matrix A, Vector b) {
        for (Entry<SourceLocation, Function> q : sources.entrySet()) {
            SourceLocation location = q.getKey();
            Function f = q.getValue();

            int[] elements = new int[location.numElements()];
            for (int i = 0; i < elements.length; ++i)
                elements[i] = location.getElement(i);

            Matrices.zeroRows(A, 1, elements);
            for (int i : elements) {
                Point3D p = mesh.elements().get(i).center;
                b.set(i, f.get(p.x(), p.y(), p.z()));
            }
        }
    }

    private void discretizeFlux(Mesh mesh, Matrix A) {
        for (Connection c : mesh.connections()) {
            int i = mesh.here(c).index;
            int j = mesh.there(c).index;

            for (Transmissibility t : c.MD) {

                double tk = t.k;
                int k = mesh.element(t).index;

                A.add(i, k, tk);
                A.add(j, k, -tk);
            }
        }
    }

    private Map<SourceLocation, Function> readSources(Configuration config,
            Set<SourceLocation> sourceLocations) {
        Map<SourceLocation, Function> sources = new HashMap<SourceLocation, Function>();

        for (SourceLocation location : sourceLocations) {
            Function potential = config.getFunction(location.name, 3);
            sources.put(location, potential);
        }

        return sources;
    }

    private int[][] determineSparsityPattern(Mesh mesh) {
        int numElements = mesh.elements().size();
        List<Set<Integer>> nz = new ArrayList<Set<Integer>>(numElements);

        // Initialize
        for (int i = 0; i < numElements; i++) {
            Set<Integer> set = new HashSet<Integer>();
            nz.add(set);
        }

        // Flux connections
        for (Connection c : mesh.neighbourConnections()) {

            // Darcy flux
            for (Transmissibility t : c.MD) {
                int rowHere = mesh.here(c).index;
                int rowThere = mesh.there(c).index;

                int column = mesh.element(t).index;

                nz.get(rowHere).add(column);
                nz.get(rowThere).add(column);
            }
        }

        return convertToArray(nz);
    }

    /**
     * Converts the dynamic structure into a fixed size array
     */
    private int[][] convertToArray(List<Set<Integer>> nz) {
        int rows = nz.size();
        int[][] nnz = new int[rows][];

        for (int i = 0; i < rows; ++i) {
            Set<Integer> set = nz.get(i);

            int num = set.size();
            nnz[i] = new int[num];

            int j = 0;
            for (int column : set)
                nnz[i][j++] = column;
        }

        return nnz;
    }

    private double computeL2ErrorPotential(Function analytical) {
        double l2Norm = 0;

        double V = 0;

        for (Element e : mesh.elements()) {

            int i = e.index;

            // get element data
            Point3D center = e.center;
            double Vi = e.volume;

            // get computed and analytical potential
            double p = potential[i];
            double u = analytical.get(center.x(), center.y(), center.z());

            double d = p - u;

            l2Norm += Vi * d * d;

            // accumulate total volume
            V += Vi;
        }
        if (V == 0)
            throw new IllegalArgumentException("Mesh must have non-zero volume");

        System.out.println("Total volume = " + V);

        // The L2 error is sometimes not divided by total volume
        l2Norm = Math.sqrt(l2Norm) / V;

        return l2Norm;
    }

    private double computeMaxErrorPotential(Function analytical) {
        double maxNorm = 0;

        for (Element e : mesh.elements()) {
            int i = e.index;

            // get element data
            Point3D center = e.center;

            // get computed and analytical potential
            double p = potential[i];
            double u = analytical.get(center.x(), center.y(), center.z());

            double d = p - u;

            potErrorMax[i] = d;

            maxNorm = Math.max(maxNorm, Math.abs(d));
        }

        return maxNorm;
    }

    private double computeL2ErrorFlux(Function analytical) {
        double l2Norm = 0;

        double Q = 0;

        for (NeighbourConnection c : mesh.neighbourConnections()) {
            // Skip computing error norm if boundary connection
            if (connectedToBoundary(c))
                continue;

            int j = c.index;

            // get element data
            Element here = mesh.here(c);
            Element there = mesh.there(c);

            // get here interface data
            Interface intf = mesh.hereInterface(c);

            // skip if boundary interface
            Point3D center = intf.center;

            double Qj = here.volume + there.volume;

            // compute normal flow density (m/s)
            double qj = flux[j] / intf.area;

            Vector3D gradU = computeGradient(analytical, center);

            // Assume homogeneous permeability tensor
            Tensor3D K = here.rock.getAbsolutePermeability();

            Vector3D KgradU = K.mult(gradU);

            double q = -intf.normal.dot(KgradU);

            double d = qj - q;

            l2Norm += Qj * d * d;

            // accumulate total volume
            Q += Qj;
        }
        if (Q == 0)
            throw new IllegalArgumentException("Mesh must have non-zero volume");

        l2Norm = Math.sqrt(l2Norm) / Q;

        return l2Norm;
    }

    private double computeMaxErrorFlux(Function analytical) {
        double maxNorm = 0;

        for (NeighbourConnection c : mesh.neighbourConnections()) {
            // Skip computing error norm if boundary connection
            if (connectedToBoundary(c))
                continue;

            int j = c.index;

            // get element data
            Element here = mesh.here(c);

            // get here interface data
            Interface intf = mesh.hereInterface(c);
            Point3D center = intf.center;

            // compute normal flow density (m/s)
            double qj = flux[j] / intf.area;

            Vector3D gradU = computeGradient(analytical, center);

            // Assume homogeneous permeability tensor
            Tensor3D K = here.rock.getAbsolutePermeability();

            Vector3D KgradU = K.mult(gradU);

            double q = -intf.normal.dot(KgradU);

            maxNorm = Math.max(Math.abs(qj - q), maxNorm);
        }

        return maxNorm;
    }

    private Vector3D computeGradient(Function analytical, Point3D center) {
        double ux = analytical.deriv(0, center.x(), center.y(), center.z());
        double uy = analytical.deriv(1, center.x(), center.y(), center.z());
        double uz = analytical.deriv(2, center.x(), center.y(), center.z());

        return new Vector3D(ux, uy, uz);
    }

    /**
     * Checks if the given connection is somehow attached to the boundary
     */
    private boolean connectedToBoundary(NeighbourConnection c) {
        Interface here = mesh.hereInterface(c);
        Interface there = mesh.thereInterface(c);

        return connectedToBoundary(here) || connectedToBoundary(there);
    }

    /**
     * Checks if the given interface has a neighboring interface which is a part
     * of the boundary
     */
    private boolean connectedToBoundary(Interface intf) {
        for (CornerPoint p : mesh.points(intf))
            for (Interface i : mesh.interfaces(p))
                if (i.boundary)
                    return true;

        return false;
    }

}
