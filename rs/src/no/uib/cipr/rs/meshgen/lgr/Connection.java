package no.uib.cipr.rs.meshgen.lgr;

class Connection {

    private Face here, there;

    /**
     * Creates a connection between the given faces.
     */
    public Connection(Face here, Face there) {
        this.here = here;
        this.there = there;
    }

    /**
     * Returns the index of the here-face.
     */
    public int here() {
        return here.getIndex();
    }

    /**
     * Returns the index of the there-face.
     */
    public int there() {
        return there.getIndex();
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder(" ");
        s.append(here.toString());
        s.append(" - ");
        s.append(there.toString());
        return s.toString();
    }
}
