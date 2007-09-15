package no.uib.cipr.rs.numerics;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.Matrices;
import no.uib.cipr.matrix.sparse.AMG;
import no.uib.cipr.matrix.sparse.BiCG;
import no.uib.cipr.matrix.sparse.BiCGstab;
import no.uib.cipr.matrix.sparse.CG;
import no.uib.cipr.matrix.sparse.CGS;
import no.uib.cipr.matrix.sparse.CompRowMatrix;
import no.uib.cipr.matrix.sparse.DefaultIterationMonitor;
import no.uib.cipr.matrix.sparse.DiagonalPreconditioner;
import no.uib.cipr.matrix.sparse.FlexCompRowMatrix;
import no.uib.cipr.matrix.sparse.GMRES;
import no.uib.cipr.matrix.sparse.ICC;
import no.uib.cipr.matrix.sparse.ILU;
import no.uib.cipr.matrix.sparse.ILUT;
import no.uib.cipr.matrix.sparse.IR;
import no.uib.cipr.matrix.sparse.IterativeSolver;
import no.uib.cipr.matrix.sparse.IterativeSolverNotConvergedException;
import no.uib.cipr.matrix.sparse.OutputIterationReporter;
import no.uib.cipr.matrix.sparse.Preconditioner;
import no.uib.cipr.matrix.sparse.QMR;
import no.uib.cipr.matrix.sparse.SSOR;
import no.uib.cipr.rs.geometry.Connection;
import no.uib.cipr.rs.geometry.Element;
import no.uib.cipr.rs.geometry.Mesh;
import no.uib.cipr.rs.geometry.flux.Transmissibility;
import no.uib.cipr.rs.meshgen.util.ArrayData;

/**
 * Linear solver using a sparse matrix and a Krylov method
 */
class LinearSolver {

    /**
     * Compressed row matrix
     */
    private final CompRowMatrix A;

    /**
     * Solution vector
     */
    private final DenseVector x;

    /**
     * Right hand side vector
     */
    private final DenseVector b;

    /**
     * Krylov iterative method
     */
    private final IterativeSolver solver;

    /**
     * Cells which are locked. The indices are the matrix indices
     */
    private final int[] locked;

    public LinearSolver(RunSpec runSpec, Mesh mesh, int[] locked) {
        this.locked = locked;
        int size = mesh.elements().size();

        // Determine sparsity pattern
        int[][] nz = determineSparsityPattern(mesh);

        // Create the sparse matrix
        A = new CompRowMatrix(size, size, nz);

        // Create the vectors
        b = new DenseVector(size);
        x = b.copy();

        // Create solver
        solver = createSolver(runSpec);

        // Set iteration parameters
        setIterationParameters(runSpec);

        solver.setPreconditioner(createPreconditioner(runSpec));
    }

    /**
     * Sets convergence criteria and reporting
     */
    private void setIterationParameters(RunSpec runSpec) {
        DefaultIterationMonitor monitor = new DefaultIterationMonitor();
        monitor.setMaxIterations(runSpec.getMaxNumberOfLinearIterations());
        monitor.setRelativeTolerance(runSpec.getRelativeTolerance());
        monitor.setAbsoluteTolerance(runSpec.getAbsoluteTolerance());
        monitor.setDivergenceTolerance(runSpec.getDivergenceTolerance());

        if (runSpec.reportIterations())
            monitor
                    .setIterationReporter(new OutputIterationReporter(
                            System.out));

        solver.setIterationMonitor(monitor);
    }

    /**
     * Determines the system sparsity pattern. It traverses the two
     * transmissibility lists, and adds into a row-based connection list
     */
    private int[][] determineSparsityPattern(Mesh mesh) {
        int numElements = mesh.elements().size();
        List<Set<Integer>> nz = new ArrayList<Set<Integer>>(numElements);

        // Capacity term (time derivative) connections
        for (Element el : mesh.elements()) {
            Set<Integer> set = new HashSet<Integer>();
            set.add(el.index);
            nz.add(set);
        }

        // Flux connections
        for (Connection c : mesh.connections()) {

            int rowHere = mesh.here(c).index;
            int rowThere = mesh.there(c).index;
            boolean hasHere = rowHere >= 0 && rowHere < nz.size();
            boolean hasThere = rowThere >= 0 && rowThere < nz.size();

            // Darcy flux
            for (Transmissibility t : c.MD) {

                int column = mesh.element(t).index;

                if (hasHere)
                    nz.get(rowHere).add(column);
                if (hasThere)
                    nz.get(rowThere).add(column);
            }

            // Fourier flux
            if (c.MF != null)
                for (Transmissibility t : c.MF) {

                    int column = mesh.element(t).index;

                    if (hasHere)
                        nz.get(rowHere).add(column);
                    if (hasThere)
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

        for (int i = 0; i < rows; ++i)
            nnz[i] = ArrayData.integerSetToSortedArray(nz.get(i));

        return nnz;
    }

    /**
     * Creates the Krylov subspace solver
     */
    private IterativeSolver createSolver(RunSpec runSpec) {
        String name = runSpec.getLinearSolver();

        if (name.equalsIgnoreCase("BiCG"))
            return new BiCG(b);
        if (name.equalsIgnoreCase("BiCGstab"))
            return new BiCGstab(b);
        if (name.equalsIgnoreCase("CG"))
            return new CG(b);
        if (name.equalsIgnoreCase("CGS"))
            return new CGS(b);
        if (name.equalsIgnoreCase("GMRES"))
            return new GMRES(b);
        if (name.equalsIgnoreCase("IR"))
            return new IR(b);
        if (name.equalsIgnoreCase("QMR"))
            return new QMR(b);

        throw new IllegalArgumentException("Unknown iterative solver " + name);
    }

    /**
     * Creates a preconditioner
     */
    private Preconditioner createPreconditioner(RunSpec runSpec) {
        String name = runSpec.getPreconditioner();

        if (!name.equalsIgnoreCase("none")) {
            int size = x.size();

            if (name.equalsIgnoreCase("Diagonal"))
                return new DiagonalPreconditioner(size);
            else if (name.equalsIgnoreCase("SSOR"))
                return new SSOR(new CompRowMatrix(A));
            else if (name.equalsIgnoreCase("ICC"))
                return new ICC(new CompRowMatrix(A));
            else if (name.equalsIgnoreCase("ILU"))
                return new ILU(new CompRowMatrix(A));
            else if (name.equalsIgnoreCase("ILUT"))
                return new ILUT(new FlexCompRowMatrix(A));
            else if (name.equalsIgnoreCase("AMG"))
                return new AMG();
            else
                throw new IllegalArgumentException("Unknown preconditioner "
                        + name);

        } else
            return solver.getPreconditioner();
    }

    /**
     * Adds to the Jacobian matrix
     */
    public void addToJacobian(int row, int column, double value) {
        A.add(row, column, value);
    }

    /**
     * Adds to the residual vector
     */
    public void addToResidual(int index, double value) {
        b.add(index, value);
    }

    /**
     * Solves the assembled Jacobian system. It moves the residual to the right
     * hand side (multiplies by -1), and solves for the correction vector. If
     * any cells are locked, the correction and residual in those cells will be
     * zero. After the iterative solver has finished, the Jacobian and residual
     * vector are zeroed
     */
    public void solve() throws SolverFailure {
        // Start with a zero correction vector
        x.zero();

        // Change the sign of the right-hand side vector
        b.scale(-1);

        /*
         * Disallow changes in locked grid blocks. The corresponding Jacobian
         * matrix rows are zeroed, with 1 put on the diagonal, and the right
         * hand side is set to zero. Consequently, x in those elements should be
         * zero
         */

        Matrices.zeroRows(A, 1, locked);
        for (int lock : locked)
            b.set(lock, 0);

        // Update preconditioner
        solver.getPreconditioner().setMatrix(A);

        // Start the iterative solver
        try {
            solver.solve(A, b, x);
        } catch (IterativeSolverNotConvergedException e) {
            String reason = null;

            switch (e.getReason()) {
            case Breakdown:
                reason = "Iterative solver breakdown";
                break;
            case Divergence:
                reason = "Iterative solver diverged";
                break;
            case Iterations:
                reason = "Too many iterations";
                break;
            }

            throw new SolverFailure(reason + ". Final residual = "
                    + e.getResidual());
        } finally {
            // Reset the linear system for the next time
            A.zero();
            b.zero();
        }

        // In case of round-offs, explicitly lock x
        for (int lock : locked)
            x.set(lock, 0);
    }

    /**
     * Returns the calculated solution (correction)
     */
    public double[] getCorrection(double[] dx) {
        System.arraycopy(x.getData(), 0, dx, 0, dx.length);
        return dx;
    }
}
