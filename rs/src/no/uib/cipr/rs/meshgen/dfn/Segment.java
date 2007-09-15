package no.uib.cipr.rs.meshgen.dfn;

class Segment extends CV {
    private int first, second;

    public Segment(int first, int second, int code) {
        super(code);
        this.first = first;
        this.second = second;
    }

    public int[] getPointIndices() {
        return new int[] { first, second };
    }

    public int getFirst() {
        return first;
    }

    public int getSecond() {
        return second;
    }
}