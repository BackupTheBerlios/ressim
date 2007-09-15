package no.uib.cipr.rs.meshgen.dfn;

class Zone {

    private double poro;

    private double perm;

    private double corr;

    public Zone(double poro, double perm, double corr) {
        this.poro = poro;
        this.perm = perm;
        this.corr = corr;
    }

    public double getCorr() {
        return corr;
    }

    public double getPerm() {
        return perm;
    }

    public double getPoro() {
        return poro;
    }

}