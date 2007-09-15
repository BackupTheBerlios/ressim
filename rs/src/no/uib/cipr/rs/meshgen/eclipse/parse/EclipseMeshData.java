package no.uib.cipr.rs.meshgen.eclipse.parse;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import no.uib.cipr.rs.geometry.Point3D;
import no.uib.cipr.rs.geometry.Vector3D;
import no.uib.cipr.rs.meshgen.eclipse.geometry.Connection;
import no.uib.cipr.rs.meshgen.eclipse.geometry.CornerPoint3D;
import no.uib.cipr.rs.meshgen.eclipse.geometry.HexCell;
import no.uib.cipr.rs.meshgen.eclipse.geometry.Quadrilateral;
import no.uib.cipr.rs.meshgen.eclipse.keyword.Coord;
import no.uib.cipr.rs.meshgen.eclipse.keyword.Zcorn;
import no.uib.cipr.rs.util.Configuration;
import no.uib.cipr.rs.util.Tolerances;

/**
 * This class builds an Eclipse mesh. following the rs grid EclipseMesh format.
 */
public class EclipseMeshData {

    /**
     * duplicate corner points (global)
     */
    private SortedMap<Integer, CornerPoint3D> duplicate;

    /**
     * unique corner point indices (global). Must be sorted based on insertion
     * order for output files to be correct
     */
    private CornerPoint3D[] unique;

    private HexCell[] cells;

    /**
     * topological relations
     */
    Map<Integer, Integer> duplicateToUnique;

    /**
     * mapping between duplicate point index and containing cv
     */
    private Map<Integer, Integer> duplicateToCell;

    private int nz, nx, ny;

    private Connection[] connections;

    private Quadrilateral[] interfaces;

    private int numCells;

    private Map<String, double[]> rockDataMap;

    /**
     * @param config
     *            A configuration of basic eclipse format gridding data
     *            including cell parameters
     */
    public EclipseMeshData(Configuration config) {

        Configuration spec = config.getConfiguration("specgrid");

        nx = spec.getInt("ndivix");
        ny = spec.getInt("ndiviy");
        nz = spec.getInt("ndiviz");

        duplicate = new TreeMap<Integer, CornerPoint3D>();

        duplicateToUnique = new HashMap<Integer, Integer>();

        cells = new HexCell[nx * ny * nz];

        duplicateToCell = new HashMap<Integer, Integer>();

        buildMesh(config);
    }

    /**
     * Generates the mesh data structure. This includes removing duplicate
     * corner points.
     * 
     * @param config
     *            A configuration
     */
    private void buildMesh(Configuration config) {

        buildCells(config);

        buildRockData(config);

        buildUniquePoints();

        interfaces = buildInterfaces();

        connections = buildConnections();

    }

    private void buildRockData(Configuration config) {

        Configuration rockData = config.getConfiguration("RockData");

        // store rock data keys and values
        rockDataMap = new HashMap<String, double[]>(4);

        // read global rock data
        for (String key : rockData.keys()) {

            double[] data = rockData.getConfiguration(key).getDoubleArray(key);

            // if present, size must be equal to total number of cells
            if (data.length != numCells)
                throw new IllegalArgumentException(rockData.trace()
                        + "Invalid number of data elements");

            rockDataMap.put(key, data);
        }

        Configuration boxedRockData = config.getConfiguration("BoxedRockData");

        // read boxed rock data
        for (String key : boxedRockData.keys()) {
            Configuration b = boxedRockData.getConfiguration(key);

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
                    rockDataMap.put(param, new double[numCells]);

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
                            data[getLinear(i, j, k)] = boxdata[l++];

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

    /**
     * @return A list of <code>Quadrilateral</code> objects
     */
    private Quadrilateral[] buildInterfaces() {

        Quadrilateral[] l = new Quadrilateral[6 * cells.length];

        int i = 0;
        for (HexCell c : cells) {
            l[i++] = c.top();
            l[i++] = c.bottom();
            l[i++] = c.front();
            l[i++] = c.back();
            l[i++] = c.left();
            l[i++] = c.right();
        }

        return l;
    }

    /**
     * builds a list of (unique) cell-to-cell connections
     * 
     * @return A list of <code>Connection</code> objects
     */
    private Connection[] buildConnections() {

        Iterable<HexCell> candidates = null;

        List<Connection> conn = new ArrayList<Connection>();

        int ind = 0;

        for (int k = 0; k < nz; k++) {
            for (int j = 0; j < ny; j++) {
                for (int i = 0; i < nx; i++, ind++) {

                    HexCell cell = cells[ind];

                    candidates = getTopCandidates(i, j, k);
                    for (HexCell cand : candidates) {
                        if (cell.top().overlap(cand.bottom()))
                            conn.add(new Connection(cell.top(), cand.bottom()));

                    }

                    candidates = getBottomCandidates(i, j, k);
                    for (HexCell cand : candidates) {
                        if (cell.bottom().overlap(cand.top()))
                            conn.add(new Connection(cell.bottom(), cand.top()));
                    }

                    candidates = getFrontCandidates(i, j, k);
                    for (HexCell cand : candidates) {
                        if (cell.front().overlap(cand.back()))
                            conn.add(new Connection(cell.front(), cand.back()));
                    }

                    candidates = getBackCandidates(i, j, k);
                    for (HexCell cand : candidates) {
                        if (cell.back().overlap(cand.front()))
                            conn.add(new Connection(cell.back(), cand.front()));
                    }

                    candidates = getLeftCandidates(i, j, k);
                    for (HexCell cand : candidates) {
                        if (cell.left().overlap(cand.right()))
                            conn.add(new Connection(cell.left(), cand.right()));
                    }

                    candidates = getRightCandidates(i, j, k);
                    for (HexCell cand : candidates) {
                        if (cell.right().overlap(cand.left()))
                            conn.add(new Connection(cell.right(), cand.left()));
                    }

                }
            }
        }

        return conn.toArray(new Connection[conn.size()]);
    }

    /**
     * Builds all the grid cells of the Eclipse mesh including inactive.
     * 
     * Fills "duplicate" list of corner points
     * 
     * @param config
     *            A configuration containing "coord"- and "zcorn"-configurations
     * 
     */
    private void buildCells(Configuration config) {

        Coord coord = new Coord(config);
        Zcorn zcorn = new Zcorn(config);

        // linear cell index
        int index = 0;

        for (int k = 0; k < nz; k++) {
            for (int j = 0; j < ny; j++) {
                for (int i = 0; i < nx; i++, index++) {

                    int[] cvZcorn = getCellZcorn(i, j, k, nx, ny);
                    int[] cvCoord = getCellCoord(i, j, nx);

                    // local-global corner point map
                    int[] localGlobal = new int[8];

                    // 8 corner points
                    CornerPoint3D[] points = new CornerPoint3D[8];

                    for (int local = 0; local < 8; local++) {

                        int global = cvZcorn[local];

                        localGlobal[local] = global;

                        int pillar = cvCoord[local];
                        double depth = zcorn.getDepth(global);
                        double[] xy = coord.getXY(pillar, depth);

                        CornerPoint3D point = new CornerPoint3D(xy[0], xy[1],
                                -depth, global);

                        points[local] = point;

                        duplicate.put(global, point);

                        duplicateToCell.put(global, index);
                    }

                    cells[index] = new HexCell(index, points, localGlobal);
                }
            }
        }

        numCells = cells.length;

    }

    private void buildUniquePoints() {

        System.out.print("Computing duplicates: ");
        int n = duplicate.size();
        int out = n / 10;
        if (out == 0)
            out = 1;
        int index = 0;

        // assume duplicate points have their index properly set
        List<CornerPoint3D> dups = new ArrayList<CornerPoint3D>(duplicate
                .values());
        Collections.<CornerPoint3D> sort(dups);

        SortedMap<CornerPoint3D, Integer> uniqueMap = new TreeMap<CornerPoint3D, Integer>();

        for (CornerPoint3D p : dups) {

            if ((index++ % out) == 0)
                System.out.print(".");

            int d = p.getIndex();

            int u;

            if (!uniqueMap.containsKey(p)) {
                u = uniqueMap.size();
                uniqueMap.put(p, u);
            } else {
                u = uniqueMap.get(p);
            }

            duplicateToUnique.put(d, u);

        }

        System.out.println(" done");

        unique = uniqueMap.keySet()
                .toArray(new CornerPoint3D[uniqueMap.size()]);
    }

    /**
     * This routine returns an array of 8 (4 unique) COORD line indices
     * corresponding to the 8 corner points of the grid cell.
     * 
     * Input data are the x- and y-index of the grid cell and number of grid
     * cells in the x-direction.
     * 
     * @param i
     * @param j
     * @param nx
     * 
     * @return COORD indices
     * 
     * TODO put in CornerPointTopology class?
     */
    private int[] getCellCoord(int i, int j, int nx) {
        int[] ind = new int[8];

        int l = i + j * (nx + 1);

        ind[0] = l;
        ind[1] = l + 1;
        ind[2] = l + (nx + 1);
        ind[3] = l + (nx + 2);
        ind[4] = l;
        ind[5] = l + 1;
        ind[6] = l + (nx + 1);
        ind[7] = l + (nx + 2);

        return ind;
    }

    /**
     * This routine returns an int[] vector of 8 corner point indices (ZCORN)
     * that make up grid cell number (i, j, k).
     * 
     * @param i
     * @param j
     * @param k
     * @param nx
     * @param ny
     * 
     * @return ZCORN indices
     * 
     * TODO put in CornerPointTopology class?
     */
    private int[] getCellZcorn(int i, int j, int k, int nx, int ny) {
        int kk = (2 * i) + (2 * j * 2 * nx) + (2 * k * 2 * nx * 2 * ny);

        int zc1 = kk;
        int zc2 = zc1 + 1; // kk + 1;
        int zc3 = kk + 2 * nx;
        int zc4 = zc3 + 1; // kk + 2*nx + 1;
        int zc5 = kk + 2 * nx * 2 * ny;
        int zc6 = zc5 + 1; // kk + 2*nx*2*ny + 1
        int zc7 = kk + 2 * nx * 2 * ny + 2 * nx;
        int zc8 = zc7 + 1; // kk + 2*nx*2*ny + 2*nx + 1

        return new int[] { zc1, zc2, zc3, zc4, zc5, zc6, zc7, zc8 };
    }

    /**
     * @return the dimension of the mesh (always 3 for Eclipse meshes)
     */
    public int getDimension() {
        return 3;
    }

    /**
     * @return number of unique corner points
     */
    public int getNumPoints() {
        return unique.length;
    }

    /**
     * Returns array of corner point coordinates. The list numPoints*{x, y, z}
     * numbers.
     * 
     * @return An array of doubles
     */
    public double[] getPointCoordinates() {
        double[] c = new double[3 * unique.length];

        int i = 0;
        for (CornerPoint3D p : unique) {
            c[i++] = p.x();
            c[i++] = p.y();
            c[i++] = p.z();
        }
        return c;
    }

    /**
     * @return number of (duplicate) interfaces
     */
    public int getNumInterfaces() {
        return interfaces.length;
    }

    /**
     * Returns array of integers defining the interfaces (numLocalPoints, p0,
     * ..., p3). The interfaces are assumed to be quadrilaterals.
     * 
     * There are 5 ints per interface.
     * 
     * @return int array
     */
    public int[] getInterfacePoints() {

        int[] data = new int[getNumInterfaces() * 5];

        int i = 0;
        for (Quadrilateral q : interfaces) {

            int[] orientedPoints = q.getOrientedPoints();

            data[i++] = orientedPoints.length;

            for (int p = 0; p < orientedPoints.length; p++) {

                int dupl = orientedPoints[p];
                int uniq = duplicateToUnique.get(dupl);

                data[i++] = uniq;
            }
        }

        return data;
    }

    /**
     * @return double array of interface areas
     */
    public double[] getInterfaceAreas() {

        double[] data = new double[getNumInterfaces()];

        int index = 0;

        for (Quadrilateral q : interfaces)
            data[index++] = q.getArea();

        return data;
    }

    /**
     * Returns 3*numInterfaces double array containing x, y and z components
     */
    public double[] getInterfaceNormals() {

        double[] data = new double[3 * getNumInterfaces()];

        int i = 0;
        for (Quadrilateral q : interfaces) {
            Vector3D n = q.getNormal();
            data[i++] = n.x();
            data[i++] = n.y();
            data[i++] = n.z();
        }

        return data;
    }

    /**
     * Returns interface center points
     */
    public double[] getInterfaceCenters() {

        double[] data = new double[3 * getNumInterfaces()];

        int i = 0;
        for (Quadrilateral q : interfaces) {
            Point3D cp = q.getPoint(.5, .5);
            data[i++] = cp.x();
            data[i++] = cp.y();
            data[i++] = cp.z();
        }

        return data;
    }

    /**
     * Returns the total number of cells.
     */
    public int getNumElements() {
        return cells.length;
    }

    /**
     * Returns element interface indices (numInterfaces, i0, ..., i5).
     */
    public int[] getElementInterfaces() {
        int[] data = new int[(1 + 6) * getNumElements()];

        int i = 0;
        for (HexCell c : cells) {
            data[i++] = 6;
            data[i++] = c.top().getIndex();
            data[i++] = c.bottom().getIndex();
            data[i++] = c.front().getIndex();
            data[i++] = c.back().getIndex();
            data[i++] = c.left().getIndex();
            data[i++] = c.right().getIndex();
        }

        return data;
    }

    /**
     * Returns element volumes.
     */
    public double[] getElementVolumes() {
        double[] v = new double[getNumElements()];

        int i = 0;
        for (HexCell c : cells)
            v[i++] = c.getVolume();

        return v;
    }

    /**
     * Return element center coordinates.
     */
    public double[] getElementCenters() {
        double[] data = new double[3 * getNumElements()];

        int i = 0;
        for (HexCell c : cells) {
            CornerPoint3D cp = c.getCenter();
            data[i++] = cp.x();
            data[i++] = cp.y();
            data[i++] = cp.z();
        }

        return data;
    }

    /**
     * @return number of connections
     */
    public int getNumConnections() {
        return connections.length;
    }

    /**
     * Returns an array of connection interface indices.
     */
    public int[] getConnections() {
        int[] data = new int[2 * connections.length];

        int i = 0;
        for (Connection conn : connections) {
            data[i++] = conn.getHereIndex();
            data[i++] = conn.getThereIndex();
        }

        return data;
    }

    public Map<String, double[]> getRockDataMap() {
        return rockDataMap;
    }

    /**
     * Returns an array of all cell indices.
     * 
     * TODO implement Regions of Eclipse file format
     */
    public int[] getUniformRegion() {
        int[] data = new int[numCells];

        int i = 0;
        for (HexCell c : cells)
            data[i++] = c.getIndex();

        return data;
    }

    /**
     * Returns an iterable object of HexCell objects for the given i, j, and
     * k-index.
     * 
     * TODO Add possibility for refinements here
     */
    private Iterable<HexCell> getTopCandidates(int i, int j, int k) {
        List<HexCell> candidates = new ArrayList<HexCell>();
        if (k > 0)
            candidates.add(cells[getLinear(i, j, k - 1)]);
        return candidates;
    }

    /**
     * Returns an iterable object of HexCell objects for the given i, j, and
     * k-index.
     * 
     * TODO Add possibility for refinements here
     */
    private Iterable<HexCell> getBottomCandidates(int i, int j, int k) {
        List<HexCell> candidates = new ArrayList<HexCell>();
        if (k < getNz() - 1)
            candidates.add(cells[getLinear(i, j, (k + 1))]);
        return candidates;
    }

    /**
     * Returns an iterable object of HexCell objects for the given i, j, and
     * k-index.
     * 
     * TODO Add possibility for refinements here
     */
    private Iterable<HexCell> getFrontCandidates(int i, int j,
            @SuppressWarnings("unused")
            int k) {
        List<HexCell> candidates = new ArrayList<HexCell>();

        if (j > 0)
            for (int kk = 0; kk < getNz(); kk++)
                candidates.add(cells[getLinear(i, j - 1, kk)]);

        return candidates;
    }

    /**
     * Returns an iterable object of HexCell objects for the given i, j, and
     * k-index.
     * 
     * TODO Add possibility for refinements here
     */
    private Iterable<HexCell> getBackCandidates(int i, int j,
            @SuppressWarnings("unused")
            int k) {
        List<HexCell> candidates = new ArrayList<HexCell>();

        if (j < getNy() - 1)
            for (int kk = 0; kk < getNz(); kk++)
                candidates.add(cells[getLinear(i, j + 1, kk)]);

        return candidates;
    }

    /**
     * Returns an iterable object of HexCell objects for the given i, j, and
     * k-index.
     * 
     * TODO Add possibility for refinements here
     */
    private Iterable<HexCell> getLeftCandidates(int i, int j,
            @SuppressWarnings("unused")
            int k) {
        List<HexCell> candidates = new ArrayList<HexCell>();

        if (i > 0)
            for (int kk = 0; kk < getNz(); kk++)
                candidates.add(cells[getLinear(i - 1, j, kk)]);

        return candidates;
    }

    /**
     * Returns an iterable object of HexCell objects for the given i, j, and
     * k-index.
     * 
     * TODO Add possibility for refinements here
     */
    private Iterable<HexCell> getRightCandidates(int i, int j,
            @SuppressWarnings("unused")
            int k) {
        List<HexCell> candidates = new ArrayList<HexCell>();

        if (i < nx - 1)
            for (int kk = 0; kk < nz; kk++)
                candidates.add(cells[getLinear(i + 1, j, kk)]);

        return candidates;
    }

    /**
     * Returns the linear cell index for the given i, j, k indices.
     */
    public int getLinear(int i, int j, int k) {
        return i + (j * nx) + (k * nx * ny);
    }

    /**
     * Returns Nz.
     */
    public int getNz() {
        return nz;
    }

    /**
     * Returns Nx.
     */
    public final int getNx() {
        return nx;
    }

    /**
     * Returns Ny.
     */
    public final int getNy() {
        return ny;
    }

    /**
     * This routine writes the Eclipse grid to the file <caseName>.gmv .
     * 
     * TODO remove me when testing is complete.
     * 
     * @param caseName
     * @throws IOException
     */
    public void dumpDuplGMVOutput(String caseName) throws IOException {

        PrintWriter os = new PrintWriter(new BufferedWriter(new FileWriter(
                caseName + ".gmv")));

        os.write("gmvinput ascii\n");

        os.write("\nnodes " + duplicate.size() + "\n\n");

        for (CornerPoint3D p : duplicate.values())
            os.write(p.x() + " ");
        os.write("\n");

        for (CornerPoint3D p : duplicate.values())
            os.write(p.y() + " ");
        os.write("\n");

        for (CornerPoint3D p : duplicate.values())
            os.write(p.z() + " ");
        os.write("\n");

        os.write("\n");

        os.write("\ncells " + cells.length + "\n");

        for (HexCell c : cells) {

            os.write("hex 8\n");

            int cp1 = c.getGlobalCP(0) + 1;
            int cp2 = c.getGlobalCP(1) + 1;
            int cp3 = c.getGlobalCP(3) + 1;
            int cp4 = c.getGlobalCP(2) + 1;
            int cp5 = c.getGlobalCP(4) + 1;
            int cp6 = c.getGlobalCP(5) + 1;
            int cp7 = c.getGlobalCP(7) + 1;
            int cp8 = c.getGlobalCP(6) + 1;

            os.write(" " + cp1 + " " + cp2 + " " + cp3 + " " + cp4 + " " + cp5
                    + " " + cp6 + " " + cp7 + " " + cp8);

            os.write("\n");
        }
        os.write("\n");

        os.write("\ncodename geometry\n");

        os.write("\nendgmv\n");

        os.close();

    }

    /**
     * This routine writes the Eclipse grid to the file <caseName>.gmv .
     * 
     * TODO remove me when testing is complete.
     * 
     * @param caseName
     * @throws IOException
     */
    public void dumpUniqueGMVOutput(String caseName) throws IOException {

        PrintWriter os = new PrintWriter(new BufferedWriter(new FileWriter(
                caseName + ".gmv")));

        os.write("gmvinput ascii\n");

        os.write("\nnodes " + unique.length + "\n\n");

        for (CornerPoint3D p : unique)
            os.write(p.x() + " ");
        os.write("\n");

        for (CornerPoint3D p : unique)
            os.write(p.y() + " ");
        os.write("\n");

        for (CornerPoint3D p : unique)
            os.write(p.z() + " ");
        os.write("\n");

        os.write("\n");

        os.write("\ncells " + cells.length + "\n");

        for (HexCell c : cells) {

            os.write("hex 8\n");

            int cp1 = duplicateToUnique.get(c.getGlobalCP(0)).intValue() + 1;
            int cp2 = duplicateToUnique.get(c.getGlobalCP(1)).intValue() + 1;
            int cp3 = duplicateToUnique.get(c.getGlobalCP(3)).intValue() + 1;
            int cp4 = duplicateToUnique.get(c.getGlobalCP(2)).intValue() + 1;
            int cp5 = duplicateToUnique.get(c.getGlobalCP(4)).intValue() + 1;
            int cp6 = duplicateToUnique.get(c.getGlobalCP(5)).intValue() + 1;
            int cp7 = duplicateToUnique.get(c.getGlobalCP(7)).intValue() + 1;
            int cp8 = duplicateToUnique.get(c.getGlobalCP(6)).intValue() + 1;

            os.write(" " + cp1 + " " + cp2 + " " + cp3 + " " + cp4 + " " + cp5
                    + " " + cp6 + " " + cp7 + " " + cp8);

            os.write("\n");
        }
        os.write("\n");

        os.write("\ncodename geometry\n");

        os.write("\nendgmv\n");

        os.close();

    }

}
