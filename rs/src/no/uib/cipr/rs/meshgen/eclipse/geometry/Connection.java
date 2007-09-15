package no.uib.cipr.rs.meshgen.eclipse.geometry;

/**
 * Class for storing the connection between two grid cells
 */
public class Connection {

    private Quadrilateral here, there;

    private int hereCell, thereCell;

    /**
     * @param here
     * @param there
     */
    public Connection(Quadrilateral here, Quadrilateral there) {
        this.here = here;
        this.there = there;

        hereCell = here.getCell();
        thereCell = there.getCell();
    }

    /**
     * @return Returns the here quadrilateral global index
     */
    public final int getHereIndex() {
        return here.getIndex();
    }

    /**
     * @return Returns the there quadrilateral global index
     */
    public final int getThereIndex() {
        return there.getIndex();
    }

    /**
     * @return Returns the here <code>Quadrilateral</code>.
     */
    public final Quadrilateral getHere() {
        return here;
    }

    /**
     * @return Returns the there <code>Quadrilateral</code>
     */
    public final Quadrilateral getThere() {
        return there;
    }

    public int getHereCell() {
        return hereCell;
    }

    public int getThereCell() {
        return thereCell;
    }

    @Override
    public boolean equals(Object obj) {

        if (obj == this)
            return true;

        if (!(obj instanceof Connection))
            return false;

        Connection conn = (Connection) obj;

        return (here.getIndex() == conn.here.getIndex() && there.getIndex() == conn.there
                .getIndex())
                || (here.getIndex() == conn.there.getIndex() && there
                        .getIndex() == conn.here.getIndex());
    }

    @Override
    public int hashCode() {
        int r = 17;
        r = 37 * (getHereIndex() + getThereIndex()) + r;
        return r;
    }
}
