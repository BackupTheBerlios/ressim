package no.uib.cipr.rs.meshgen.eclipse.bsp;

import no.uib.cipr.rs.geometry.Point3D;

public class Pillar {

    private Point3D c2;

    private Point3D c1;

    public Pillar(Point3D c1, Point3D c2) {
        this.c1 = c1;
        this.c2 = c2;
    }

    public Point3D getPoint(double a) {
        return c1.scale(1 - a).plus(c2.scale(a));
    }
}
