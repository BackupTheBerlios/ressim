package no.uib.cipr.rs.meshgen.dfn;

class Connection {

    private int cv0, cv1;

    public Connection(int cv0, int cv1) {
        this.cv0 = cv0;
        this.cv1 = cv1;
    }

    public int getCV0() {
        return cv0;
    }

    public int getCV1() {
        return cv1;
    }
}