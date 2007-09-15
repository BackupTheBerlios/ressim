package no.uib.cipr.rs.meshgen.dfn;

abstract class CV {

    private int code;

    public CV(int code) {
        this.code = code;
    }

    public boolean isActive() {
        return code == -1 ? false : true;
    }

    public int getCode() {
        return code;
    }
}
