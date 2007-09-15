package no.uib.cipr.rs.upscale;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.Matrices;
import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.Vector;
import no.uib.cipr.matrix.sparse.CompRowMatrix;
import no.uib.cipr.matrix.sparse.DefaultIterationMonitor;
import no.uib.cipr.matrix.sparse.GMRES;
import no.uib.cipr.matrix.sparse.ILU;
import no.uib.cipr.matrix.sparse.IterativeSolver;
import no.uib.cipr.matrix.sparse.IterativeSolverNotConvergedException;
import no.uib.cipr.matrix.sparse.Preconditioner;
import no.uib.cipr.rs.geometry.Connection;
import no.uib.cipr.rs.geometry.Element;
import no.uib.cipr.rs.geometry.Mesh;
import no.uib.cipr.rs.geometry.flux.Transmissibility;

/**
 * Class for assembling and solving the Darcy flow equation.
 */
class PressureDiscretisation {

    private Mesh mesh;

    // system matrix
    private Matrix A;

    // right hand side vector
    private Vector b;

    // solution vector
    private Vector x;

    // iterative solver
    private IterativeSolver solver;

    // preconditioner
    private Preconditioner M;

    // boundary condition values
    private Map<Integer, Double> bc;

    /**
     * Creates a pressure discretisation and solver for the given mesh and
     * boundary conditions.
     * 
     * @param mesh
     *            Computational mesh
     * @param bc
     *            Map of boundary element indices to pressure values
     * @throws IterativeSolverNotConvergedException
     */
    public PressureDiscretisation(Mesh mesh, Map<Integer, Double> bc)
            throws IterativeSolverNotConvergedException {
        this.mesh = mesh;

        this.bc = bc;

        int size = mesh.elements().size();

        int[][] nz = determineSparsityPattern(mesh);

        A = new CompRowMatrix(size, size, nz);
        b = new DenseVector(size);
        x = b.copy();

        solver = new GMRES(b);
        M = new ILU(new CompRowMatrix(A));
        solver.setPreconditioner(M);

        DefaultIterationMonitor monitor = new DefaultIterationMonitor();
        monitor.setMaxIterations(10000);
        monitor.setRelativeTolerance(1e-50);
        monitor.setAbsoluteTolerance(1e-10);
        monitor.setDivergenceTolerance(1e+5);

        solver.setIterationMonitor(monitor);

        assemble();

        solve();
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

    /**
     * Solves the linear system.
     * 
     * @throws IterativeSolverNotConvergedException
     */
    private void solve() throws IterativeSolverNotConvergedException {
        M.setMatrix(A);

        solver.solve(A, b, x);
    }

    /**
     * Assembles the linear system.
     */
    private void assemble() {
        A.zero();
        b.zero();

        for (Connection c : mesh.connections()) {
            Element here = mesh.here(c), there = mesh.there(c);
            int i = here.index, j = there.index;

            for (Transmissibility t : c.MD) {
                Element ek = mesh.element(t);
                int ik = ek.index;
                double tk = t.k;

                A.add(i, ik, tk);
                A.add(j, ik, -tk);
            }

        }

        // set boundary conditions
        for (Map.Entry<Integer, Double> pbc : bc.entrySet()) {
            int i = pbc.getKey();
            double p = pbc.getValue();

            b.set(i, p);

            Matrices.zeroRows(A, 1.0, new int[] { i });
        }
    }

    /**
     * Returns the solution vector value for the given element index.
     */
    public double getSolution(int i) {
        return x.get(i);
    }

    /**
     * Returns the full solution vector
     */
    public Vector getSolution() {
        return x;
    }
}
