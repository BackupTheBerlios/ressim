package no.uib.cipr.rs.meshgen.structured;

public enum Direction {

    /**
     * I-direction
     */
    I,

    /**
     * J-direction
     */
    J,

    /**
     * K-direction
     */
    K;

    /**
     * Return the direction index
     */
    public int dir() {
        switch (this) {
        case I:
            return 0;
        case J:
            return 1;
        case K:
            return 2;
        default:
            throw new RuntimeException("Invalid direction");
        }
    }
}
