package no.uib.cipr.rs.geometry;

/**
 * Connection between two neighbouring elements through their interfaces.
 */
public final class NeighbourConnection extends Connection {

    private static final long serialVersionUID = -2653366683510407006L;

    public final int hereInterface;

    public final int thereInterface;

    /**
     * Creates a connection between two neighbouring elements through their
     * connecting interfaces. The connection can not be used before it is
     * populated.
     * 
     * @param index
     *                Global index of this connection
     * @param multiplier
     *                Value of flux multiplier
     */
    NeighbourConnection(int index, double multiplier, int hereElement,
            int thereElement, int hereInterface, int thereInterface) {
        super(index, multiplier, hereElement, thereElement);

        this.hereInterface = hereInterface;
        this.thereInterface = thereInterface;
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder("Connection: ");
        s.append("i = " + index + ",\n");

        s.append("\t here: " + hereElement + ",\n");
        s.append("\t there: " + thereElement + ",\n");

        s.append("\t hereIntf: " + hereInterface + ",\n");
        s.append("\t thereIntf: " + thereInterface + "\n");

        return s.toString();
    }

}
