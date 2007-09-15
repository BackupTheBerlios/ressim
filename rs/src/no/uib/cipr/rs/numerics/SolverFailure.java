package no.uib.cipr.rs.numerics;

/**
 * Signals an unexcepted failure of a solver (linear or non-linear)
 */
class SolverFailure extends Exception {

    private static final long serialVersionUID = -6395761407879004484L;

    public SolverFailure(String cause) {
        super(cause);
    }

    @Override
    public String toString() {
        return getMessage();
    }
}
