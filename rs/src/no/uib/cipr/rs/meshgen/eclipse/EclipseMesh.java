package no.uib.cipr.rs.meshgen.eclipse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import no.uib.cipr.rs.geometry.Point3D;
import no.uib.cipr.rs.geometry.Vector3D;
import no.uib.cipr.rs.meshgen.eclipse.geometry.CornerPoint3D;
import no.uib.cipr.rs.meshgen.eclipse.geometry.CornerPointGeometry;
import no.uib.cipr.rs.meshgen.eclipse.parse.Parser;
import no.uib.cipr.rs.meshgen.eclipse.topology.CornerPointTopology;
import no.uib.cipr.rs.meshgen.structured.HexCell;
import no.uib.cipr.rs.meshgen.structured.IJK;
import no.uib.cipr.rs.meshgen.structured.Orientation;
import no.uib.cipr.rs.meshgen.structured.HexCell.QuadFace;
import no.uib.cipr.rs.util.Configuration;
import no.uib.cipr.rs.util.Tolerances;

public class EclipseMesh implements OutputMesh {

    private CornerPoint3D[] points;

    private CornerPointInterface[] interfaces;

    private CornerPointCell[] elements;

    private CornerPointConnection[] connections;

    CornerPointTopology topology;

    CornerPointGeometry geometry;

    private Map<String, double[]> rockDataMap;

    public EclipseMesh(Parser parser) {

        Configuration config = parser.getConfiguration("Mesh");

        geometry = new CornerPointGeometry(config);
        topology = geometry.getTopology();

        buildMesh();

        buildRockData(config);
    }

    private void buildMesh() {

        points = buildPoints();

        interfaces = buildInterfaces();

        elements = buildElements();

        connections = buildConnections();

    }

    private CornerPointConnection[] buildConnections() {

        List<CornerPointConnection> conn = new ArrayList<CornerPointConnection>();

        for (IJK ijk : topology.getElementsIJK()) {

            int index = topology.getLinearElement(ijk);

            HexCell cell = elements[index].getHexCell();

            for (Orientation orient : geometry.getOrientations()) {

                Iterable<HexCell> candidates = getOverlapCandidates(ijk, orient);

                for (HexCell cand : candidates) {

                    @SuppressWarnings("unused")
                    QuadFace here = cell.getFace(orient);
                    @SuppressWarnings("unused")
                    QuadFace there = cand.getFace(orient.getOpposite());

                    // TODO overlap computations might go here
                }
            }

        }

        return conn.toArray(new CornerPointConnection[conn.size()]);
    }

    private Iterable<HexCell> getOverlapCandidates(IJK ijk,
            Orientation orientation) {
        List<HexCell> candidates = new ArrayList<HexCell>();

        switch (orientation) {
        case TOP:
            if (ijk.k() > 0) {
                int index = topology.getLinearElement(ijk.i(), ijk.j(),
                        ijk.k() - 1);
                candidates.add(elements[index].getHexCell());
            }
            return candidates;
        case BOTTOM:
            if (ijk.k() < topology.getNumElementsK() - 1) {
                int index = topology.getLinearElement(ijk.i(), ijk.j(),
                        ijk.k() + 1);
                candidates.add(elements[index].getHexCell());
            }
            return candidates;
        case FRONT:
            if (ijk.j() > 0) {
                for (int kk = 0; kk < topology.getNumElementsK(); kk++) {
                    int index = topology.getLinearElement(ijk.i(), ijk.j() - 1,
                            kk);
                    candidates.add(elements[index].getHexCell());
                }
            }
            return candidates;
        case BACK:
            if (ijk.j() < topology.getNumElementsJ() - 1) {
                for (int kk = 0; kk < topology.getNumElementsK(); kk++) {
                    int index = topology.getLinearElement(ijk.i(), ijk.j() + 1,
                            kk);
                    candidates.add(elements[index].getHexCell());
                }
            }
            return candidates;
        case LEFT:
            if (ijk.i() > 0) {
                for (int kk = 0; kk < topology.getNumElementsK(); kk++) {
                    int index = topology.getLinearElement(ijk.i() - 1, ijk.j(),
                            ijk.k());
                    candidates.add(elements[index].getHexCell());
                }
            }
            return candidates;
        case RIGHT:
            if (ijk.i() < topology.getNumElementsI() - 1) {
                for (int kk = 0; kk < topology.getNumElementsK(); kk++) {
                    int index = topology.getLinearElement(ijk.i() + 1, ijk.j(),
                            ijk.k());
                    candidates.add(elements[index].getHexCell());
                }
            }
            return candidates;
        default:
            break;
        }

        return candidates;
    }

    private CornerPoint3D[] buildPoints() {
        CornerPoint3D[] p = new CornerPoint3D[topology.getNumPoints()];

        for (IJK ijk : topology.getPointsIJK()) {
            int index = topology.getLinearPoint(ijk);
            Point3D point = geometry.getPoint(ijk);
            p[index] = new CornerPoint3D(point, index);
        }
        return p;
    }

    private CornerPointInterface[] buildInterfaces() {
        CornerPointInterface[] i = new CornerPointInterface[topology
                .getNumInterfaces()];

        for (IJK ijk : topology.getElementsIJK()) {

            for (Orientation orientation : geometry.getOrientations()) {

                int index = topology.getLinearInterface(ijk, orientation);

                i[index] = new CornerPointInterface(ijk, index, orientation);
            }
        }

        return i;
    }

    // -------------------------------------------------------------

    private CornerPointCell[] buildElements() {
        CornerPointCell[] e = new CornerPointCell[topology.getNumElements()];

        for (IJK ijk : topology.getElementsIJK()) {

            int index = topology.getLinearElement(ijk);

            e[index] = new CornerPointCell(ijk, index);
        }
        return e;
    }

    private void buildRockData(Configuration config) {

        Configuration rockData = config.getConfiguration("RockData");

        // store rock data keys and values
        rockDataMap = new HashMap<String, double[]>(4);

        // read global rock data
        for (String key : rockData.keys()) {

            double[] data = rockData.getConfiguration(key).getDoubleArray(key);

            // if present, size must be equal to total number of cells
            if (data.length != topology.getNumElements())
                throw new IllegalArgumentException(rockData.trace()
                        + "Invalid number of data elements");

            rockDataMap.put(key, data);
        }

        Configuration rockDataBoxed = config.getConfiguration("RockDataBoxed");

        // read boxed rock data
        for (String key : rockDataBoxed.keys()) {
            Configuration b = rockDataBoxed.getConfiguration(key);

            int[] dim = b.getIntArray("boxlimits");

            if (dim.length != 6)
                throw new IllegalArgumentException(config.trace()
                        + "Box dimensions must be specified by 6 integers");

            int i1 = dim[0], i2 = dim[1], j1 = dim[2], j2 = dim[3], k1 = dim[4], k2 = dim[5];
            int ni = i2 - i1 + 1, nj = j2 - j1 + 1, nk = k2 - k1 + 1;

            // loop through parameters of this box
            for (String param : b.keys()) {

                if (param.equals("boxlimits"))
                    continue;

                if (!rockDataMap.containsKey(param))
                    rockDataMap.put(param,
                            new double[topology.getNumElements()]);

                double[] data = rockDataMap.get(param);

                double[] boxdata = b.getConfiguration(param).getDoubleArray(
                        param);

                if (boxdata.length != ni * nj * nk)
                    throw new IllegalArgumentException(b.trace()
                            + "Invalid number of data elements");

                // copy values of boxdata into data
                int l = 0;
                for (int k = k1 - 1; k < k2; k++)
                    for (int j = j1 - 1; j < j2; j++)
                        for (int i = i1 - 1; i < i2; i++)
                            data[topology.getLinearElement(i, j, k)] = boxdata[l++];

                // put adjusted values back into map
                rockDataMap.put(param, data);

            }
        }

        // TODO check that all necessary keywords are read
        // TODO check when no global data that all boxes fill the global domain

        // set zero diagonal permeabilities to Constants.smallEps
        for (String key : rockData.keys()) {
            if (key.equals("permx") || key.equals("permy")
                    || key.equals("permz")) {

                double[] val = rockDataMap.remove(key);

                for (int i = 0; i < val.length; i++) {
                    if (val[i] == 0.0) {
                        val[i] = Tolerances.smallEps;
                    }
                }

                rockDataMap.put(key, val);
            }
        }

    }

    public int getDimension() {
        return 3;
    }

    public int getNumPoints() {
        return points.length;
    }

    public double[] getPointCoordinates() {
        double[] val = new double[3 * points.length];

        int i = 0;
        for (CornerPoint3D p : points) {
            val[i++] = p.x();
            val[i++] = p.y();
            val[i++] = p.z();
        }

        return val;
    }

    public int getNumInterfaces() {
        return topology.getNumInterfaces();
    }

    public int[] getInterfacePoints() {
        int[] val = new int[getNumInterfaces()
                * (topology.getNumInterfacePoints() + 1)];

        int i = 0;
        for (CornerPointInterface intf : interfaces) {
            int[] pts = intf.points();

            val[i++] = pts.length;

            for (int p : pts) {
                val[i++] = p;
            }
        }

        return val;
    }

    public double[] getInterfaceAreas() {
        double[] val = new double[getNumInterfaces()];

        int i = 0;
        for (CornerPointInterface intf : interfaces) {
            val[i++] = intf.getArea();
        }

        return val;
    }

    public double[] getInterfaceNormals() {

        double val[] = new double[3 * getNumInterfaces()];

        int i = 0;

        for (CornerPointInterface intf : interfaces) {
            Vector3D n = intf.getNormal();

            val[i++] = n.x();
            val[i++] = n.y();
            val[i++] = n.z();
        }

        return val;
    }

    public double[] getInterfaceCenters() {

        double val[] = new double[3 * getNumInterfaces()];

        int i = 0;

        for (CornerPointInterface intf : interfaces) {
            Point3D c = intf.getCenter();

            val[i++] = c.x();
            val[i++] = c.y();
            val[i++] = c.z();
        }

        return val;
    }

    public int getNumElements() {
        return topology.getNumElements();
    }

    public int[] getElementInterfaces() {

        int numElIntf = topology.getNumElementInterfaces();

        int[] val = new int[(1 + numElIntf) * getNumElements()];

        int i = 0;
        for (CornerPointCell el : elements) {
            val[i++] = numElIntf;

            int[] intf = el.interfaces();

            for (int index : intf) {
                val[i++] = index;
            }
        }

        return val;
    }

    public double[] getElementVolumes() {
        double[] val = new double[getNumElements()];

        int i = 0;
        for (CornerPointCell el : elements) {
            val[i++] = el.getVolume();
        }

        return val;
    }

    public double[] getElementCenters() {

        double val[] = new double[3 * getNumElements()];

        int i = 0;

        for (CornerPointCell el : elements) {
            Point3D c = el.getCenter();

            val[i++] = c.x();
            val[i++] = c.y();
            val[i++] = c.z();
        }

        return val;

    }

    public int getNumConnections() {
        return connections.length;
    }

    public int[] getConnections() {
        int[] val = new int[2 * getNumConnections()];

        int i = 0;
        for (CornerPointConnection conn : connections) {
            val[i++] = conn.getHere();
            val[i++] = conn.getThere();
        }

        return val;
    }

    public Map<String, double[]> getRockDataMap() {
        return rockDataMap;
    }

    public int[] getUniformRegion() {
        int[] val = new int[getNumElements()];

        int i = 0;
        for (CornerPointCell el : elements) {
            val[i++] = el.getIndex();
        }
        return val;
    }

    // -------------------------------------------------------------

    public class CornerPointCell {

        private int i;

        private IJK ijk;

        public CornerPointCell(IJK ijk, int i) {
            this.ijk = ijk;
            this.i = i;
        }

        public HexCell getHexCell() {
            int[] p = topology.getElementPoints(ijk);
            Point3D p0 = geometry.getPoint(p[0]);
            Point3D p1 = geometry.getPoint(p[1]);
            Point3D p2 = geometry.getPoint(p[2]);
            Point3D p3 = geometry.getPoint(p[3]);
            Point3D p4 = geometry.getPoint(p[4]);
            Point3D p5 = geometry.getPoint(p[5]);
            Point3D p6 = geometry.getPoint(p[6]);
            Point3D p7 = geometry.getPoint(p[7]);
            return new HexCell(p0, p1, p2, p3, p4, p5, p6, p7);
        }

        public int[] interfaces() {
            return topology.getElementInterfaces(ijk);
        }

        public int getIndex() {
            return i;
        }

        public Point3D getCenter() {
            return geometry.getElementCenter(i);
        }

        public double getVolume() {
            return geometry.getVolume(i);
        }

    }

    public class CornerPointConnection {

        private int here, there;

        public int getHere() {
            return here;
        }

        public int getThere() {
            return there;
        }

    }

    public class CornerPointInterface {

        private IJK ijk;

        private int i;

        private Orientation orientation;

        public CornerPointInterface(IJK ijk, int i, Orientation orientation) {
            this.ijk = ijk;
            this.i = i;
            this.orientation = orientation;
        }

        public int[] points() {
            return topology.getInterfacePoints(ijk, orientation);
        }

        public Point3D getCenter() {
            return geometry.getInterfaceCenter(i);
        }

        public Vector3D getNormal() {
            return geometry.getInterfaceNormal(i);
        }

        public double getArea() {
            return geometry.getInterfaceArea(i);
        }

    }

}
