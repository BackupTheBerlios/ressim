package no.uib.cipr.rs.meshgen.eclipse.geometry;

import static no.uib.cipr.rs.meshgen.structured.Orientation.BACK;
import static no.uib.cipr.rs.meshgen.structured.Orientation.BOTTOM;
import static no.uib.cipr.rs.meshgen.structured.Orientation.FRONT;
import static no.uib.cipr.rs.meshgen.structured.Orientation.LEFT;
import static no.uib.cipr.rs.meshgen.structured.Orientation.RIGHT;
import static no.uib.cipr.rs.meshgen.structured.Orientation.TOP;

import java.util.Arrays;

import no.uib.cipr.rs.geometry.Point3D;
import no.uib.cipr.rs.geometry.Vector3D;
import no.uib.cipr.rs.meshgen.eclipse.keyword.Coord;
import no.uib.cipr.rs.meshgen.eclipse.keyword.Zcorn;
import no.uib.cipr.rs.meshgen.eclipse.topology.CornerPointTopology;
import no.uib.cipr.rs.meshgen.structured.HexCell;
import no.uib.cipr.rs.meshgen.structured.IJK;
import no.uib.cipr.rs.meshgen.structured.Orientation;
import no.uib.cipr.rs.meshgen.structured.HexCell.QuadFace;
import no.uib.cipr.rs.util.Configuration;

public class CornerPointGeometry {

    private CornerPointTopology topology;

    private int nx, ny, nz;

    // all (duplicated) corner point coordinates
    private Point3D[] p;

    // element center point coordinates
    private Point3D[] ec;

    private Point3D[] ic;

    // interface normal vectors
    private Vector3D[] n;

    // cell volumes
    private double[] v;

    // interface areas
    private double[] a;

    public CornerPointGeometry(Configuration config) {
        Configuration spec = config.getConfiguration("specgrid");

        nx = spec.getInt("ndivix");
        ny = spec.getInt("ndiviy");
        nz = spec.getInt("ndiviz");

        topology = new CornerPointTopology(nx, ny, nz);

        Coord coord = new Coord(config);
        Zcorn zcorn = new Zcorn(config);

        // building all corner points
        p = new Point3D[topology.getNumPoints()];

        for (IJK ijk : topology.getPointsIJK()) {

            int index = topology.getLinearPoint(ijk); // same as zcorn index

            int pillar = topology.getPointPillar(ijk);

            double depth = zcorn.getDepth(index);
            double[] xy = coord.getXY(pillar, depth);

            double x = xy[0];
            double y = xy[1];
            double z = -depth;

            p[index] = new Point3D(x, y, z);
        }

        // building all cell data
        ec = new Point3D[topology.getNumElements()];
        ic = new Point3D[topology.getNumInterfaces()];

        n = new Vector3D[topology.getNumInterfaces()];

        v = new double[topology.getNumElements()];
        a = new double[topology.getNumInterfaces()];

        for (IJK ijk : topology.getElementsIJK()) {
            int[] cornerPoints = topology.getElementPoints(ijk);

            int index = topology.getLinearElement(ijk);

            // temporary cell object
            HexCell cell = new HexCell(p[cornerPoints[0]], p[cornerPoints[1]],
                    p[cornerPoints[2]], p[cornerPoints[3]], p[cornerPoints[4]],
                    p[cornerPoints[5]], p[cornerPoints[6]], p[cornerPoints[7]]);

            v[index] = cell.getVolume();

            ec[index] = cell.getCenterPoint();

            for (Orientation orientation : getOrientations()) {

                int faceIndex = topology.getLinearInterface(ijk, orientation);

                // temporary interface object
                QuadFace face = cell.getFace(orientation);

                a[faceIndex] = face.getArea();

                n[faceIndex] = face.getNormal();

                ic[faceIndex] = face.getCenterPoint();
            }
        }
    }

    /**
     * @return an iterable object of all <code>Orientation</code> types in
     *         this geometry.
     */
    public Iterable<Orientation> getOrientations() {
        Orientation[] orientations = new Orientation[] { TOP, BOTTOM, FRONT,
                BACK, LEFT, RIGHT };

        return Arrays.asList(orientations);
    }

    public CornerPointTopology getTopology() {
        return topology;
    }

    public double getVolume(int i) {
        return v[i];
    }

    public Point3D getElementCenter(int i) {
        return ec[i];
    }

    public Point3D getInterfaceCenter(int i) {
        return ic[i];
    }

    public Vector3D getInterfaceNormal(int i) {
        return n[i];
    }

    public double getInterfaceArea(int i) {
        return a[i];
    }

    public Point3D getPoint(IJK ijk) {
        return p[topology.getLinearPoint(ijk)];
    }

    public Point3D getPoint(int i) {
        return p[i];
    }

}
