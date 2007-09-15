package no.uib.cipr.rs.meshgen.dfn;

class Polygon extends CV {
    private int[] nodeList;

    private int[] segList;

    public Polygon(int[] nodeList, int[] segList, int code) {
        super(code);
        this.nodeList = nodeList;
        this.segList = segList;
    }

    public int[] getSegmentIndices() {
        return segList;
    }

    public int[] getNodeIndices() {
        return nodeList;
    }
}