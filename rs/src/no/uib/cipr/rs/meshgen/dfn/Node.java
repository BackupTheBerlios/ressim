package no.uib.cipr.rs.meshgen.dfn;

import no.uib.cipr.rs.geometry.Point3D;

class Node extends CV {

    private Point3D coord;

    public Node(Point3D coord, int code) {
        super(code);
        this.coord = coord;
    }

    public Point3D getCoord() {
        return coord;
    }
}