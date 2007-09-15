package no.uib.cipr.rs.meshgen.chevron;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.zip.GZIPInputStream;

import no.uib.cipr.rs.geometry.Geometry;
import no.uib.cipr.rs.geometry.Mesh;
import no.uib.cipr.rs.geometry.Point3D;
import no.uib.cipr.rs.geometry.Tensor3D;
import no.uib.cipr.rs.geometry.Topology;
import no.uib.cipr.rs.meshgen.structured.HexCell;
import no.uib.cipr.rs.meshgen.structured.Orientation;
import no.uib.cipr.rs.rock.Rock;
import no.uib.cipr.rs.util.Configuration;
import no.uib.cipr.rs.util.Conversions;

public class Grid {
    // data for entire grid supplied through Matlab .mat file
    Data d;

    // starting i, j, k for subset. defaults at 0
    int si, sj, sk;

    // target i, j, k for subset, i.e. one higher than the last index.
    // defaults at the length of the dataset
    int ti, tj, tk;

    // size (delta) of subset; calculated once and for all and cached
    int di, dj, dk;

    // porosity for the entire field
    double phi;

    public Grid(Configuration config) throws Exception {
        // get the filename from the configuration file; check that we are
        // supplied with a sensible type of file
        String fileName = config.getString("name");
        if (!fileName.toLowerCase().endsWith(".dat.gz")) {
            throw new Exception(String.format("Unknown file format: %s",
                    fileName));
        }

        // read dataset from the specified file. we need the contents of this
        // file to set some of the defaults later
        FileInputStream fis = new FileInputStream(fileName);
        try {
            GZIPInputStream gis = new GZIPInputStream(fis);
            try {
                ObjectInputStream ois = new ObjectInputStream(gis);
                try {
                    this.d = (Data) ois.readObject();
                } finally {
                    ois.close();
                }
            } finally {
                gis.close();
            }
        } finally {
            fis.close();
        }

        // create a subset, using the entire dataset as default
        int[] subset;
        if (config.containsKey("subset")) {
            subset = config.getIntArray("subset");
        } else {
            subset = new int[] { 1, 1, 1, this.d.ni, this.d.nj, this.d.nk };
        }
        // we need two times three
        if (subset.length != 6) {
            throw new Exception(
                    String
                            .format("Subset must be specified as 2*3 coordinate numbers"));
        }

        // in the file it is specified with inclusive indices starting at one;
        // we want one inclusive and one exclusive, both starting at zero
        this.si = subset[0] - 1;
        this.sj = subset[1] - 1;
        this.sk = subset[2] - 1;
        this.ti = subset[3];
        this.tj = subset[4];
        this.tk = subset[5];

        // validate subset specification
        if (!(0 <= this.si && this.ti <= this.d.ni)
                || !(0 <= this.sj && this.tj <= this.d.nj)
                || !(0 <= this.sk && this.tk <= this.d.nk)) {
            throw new Exception(String.format(
                    "Subset must be within (%d, %d, %d)-(%d, %d, %d)", 1, 1, 1,
                    this.d.ni, this.d.nj, this.d.nk));
        }

        // instead of calculating this for every index lookup, we do it once
        // and store the result as a member. these variables replace d.nX from
        // the previous version.
        this.di = this.ti - this.si;
        this.dj = this.tj - this.sj;
        this.dk = this.tk - this.sk;

        // porosity is missing from input data
        this.phi = config.getDouble("Porosity");
    }

    // returns the total number of points
    int numOfPts() {
        return (di + 1) * (dj + 1) * (dk + 1);
    }

    // returns the total number of cells
    int numOfElems() {
        return di * dj * dk;
    }

    // each element has its own set of interfaces
    int numOfIntf() {
        return numOfElems() * sidePoints.length;
    }

    // there are ni-1 planes, and nj*nk connections on each plane, that goes
    // vertically. same for the two other dimensions. total number of
    // connections are the sum of these
    int numOfConns() {
        return di * dj * (dk - 1) + di * (dj - 1) * dk + (di - 1) * dj * dk;
    }

    // returns the index of the point that is located at logical (i,j,k)
    int pointIndex(int i, int j, int k) {
        // indices relative to start of subset
        int ri = i - si;
        int rj = j - sj;
        int rk = k - sk;
        int ndx = (rk * (dj + 1) + rj) * (di + 1) + ri;
        return ndx;
    }

    // returns the index of the element that is located at logical (i,j,k)
    int elementIndex(int i, int j, int k) {
        // indices relative to start of subset
        int ri = i - si;
        int rj = j - sj;
        int rk = k - sk;
        return (rk * dj + rj) * di + ri;
    }

    // returns the index of the interface in the given orientation for an elem.
    int interfaceIndex(int i, int j, int k, Orientation o) {
        return elementIndex(i, j, k) * sides.length + o.ordinal();
    }

    // eight points, with three values specifying i, j, and k for each of
    // the points relative to the index of the cell. the points are in the
    // order that they should be specified to the HexCell constructor.
    // note that the original file specifies depth with a positive z-value,
    // whereas we want depth (below sealevel) as a negative z-value. thus, we
    // use their {left,front,upper} for our {left,front,lower}; all lower values
    // become upper and vice versa, which is why the third element in this table
    // starts at one and decreases, opposite of the two other. (if you are
    // getting negative coordinates, swap the first four and the last four
    // entries in this table).
    static int[][] cornerPoints = new int[][] { { 0, 0, 1 }, // left, front,
            // upper
            { 1, 0, 1 }, // right, front, upper
            { 1, 1, 1 }, // right, back, upper
            { 0, 1, 1 }, // left, back, upper
            { 0, 0, 0 }, // left, front, lower
            { 1, 0, 0 }, // right, front, lower
            { 1, 1, 0 }, // right, back, lower
            { 0, 1, 0 }, // left, back, lower
    };

    // each cell has six sides; this is the orientation of the various sides
    // in the order that they are constructed
    static Orientation[] sides = new Orientation[] { Orientation.TOP,
            Orientation.BOTTOM, Orientation.FRONT, Orientation.BACK,
            Orientation.LEFT, Orientation.RIGHT };

    // outer-level is the array of sides, then the various points for each of
    // the sides, then the index in the cornerPoints table that says where which
    // point is relative to the element index
    static int[][] sidePoints = new int[][] { { 0, 1, 2, 3 }, // top
            { 7, 6, 5, 4 }, // bottom
            { 4, 5, 1, 0 }, // front
            { 6, 7, 3, 2 }, // back
            { 0, 3, 7, 4 }, // left
            { 5, 6, 2, 1 } // right
    };

    public Mesh build() {
        System.out.format("Chevron: (%d, %d, %d) - (%d, %d, %d)", si + 1,
                sj + 1, sk + 1, ti, tj, tk);
        Geometry g = new Geometry();
        Topology t = new Topology();

        // calculate the geometry of the grid; replacing the functions with a
        // variable that contains the number it calculated
        int numOfPts = numOfPts();
        int numOfElems = numOfElems();
        int numOfIntf = numOfIntf();
        int numOfConns = numOfConns();

        // allocate memory to hold all the information
        t.setNumPoints(numOfPts);
        t.setNumElements(numOfElems);
        t.setNumInterfaces(numOfIntf);
        t.setNumConnections(numOfConns, 0);
        g.setSizes(t);
        Rock[] r = new Rock[numOfElems];
        int numOfRock = 0;

        // write all the points. note that the loop includes the target index
        for (int i = si; i <= ti; i++) {
            for (int j = sj; j <= tj; j++) {
                for (int k = sk; k <= tk; k++) {
                    // note the mirroring of the Z-axis to get negative values
                    Point3D p = new Point3D(d.x[i][j][k], d.y[i][j][k],
                            -d.z[i][j][k]);
                    int ndx = pointIndex(i, j, k);
                    g.buildPoint(ndx, p);
                }
            }
        }

        // cache of various rock types
        /*
         * / Map<Rock, Rock> canonical = new HashMap<Rock, Rock>(); //
         */

        // write all the elements
        for (int i = si; i < ti; i++) {
            for (int j = sj; j < tj; j++) {
                for (int k = sk; k < tk; k++) {
                    // set of points for this cell
                    int[] pi = new int[cornerPoints.length];
                    for (int m = 0; m < pi.length; m++) {
                        pi[m] = pointIndex(i + cornerPoints[m][0], j
                                + cornerPoints[m][1], k + cornerPoints[m][2]);
                    }

                    // find the center and volume from the point cloud
                    HexCell h = new HexCell(g, pi);
                    int en = elementIndex(i, j, k);
                    g.buildElement(en, h.getVolume(), h.getCenterPoint());

                    // interfaces for this cell; we already have the points;
                    // only extract the necessary subset
                    int[] ii = new int[sidePoints.length];
                    for (int m = 0; m < ii.length; m++) {
                        // find the index for this interface
                        ii[m] = interfaceIndex(i, j, k, sides[m]);

                        // get the statistics for this interface; this is
                        // already calculated in the helper class
                        HexCell.QuadFace q = h.getFace(sides[m]);
                        g.buildInterface(ii[m], q.getArea(),
                                q.getCenterPoint(), q.getNormal());

                        // points for each interface (fi = face index)
                        int[] fi = new int[sidePoints[m].length];
                        for (int n = 0; n < fi.length; n++) {
                            fi[n] = pi[sidePoints[m][n]];
                        }
                        t.buildInterfaceTopology(ii[m], fi);
                    }
                    // after we have created all the interfaces, then put the
                    // list of interface indices into the element
                    t.buildElementTopology(en, ii);

                    // create a rock object for this element. if the case had
                    // been very homogeneous, then we could have compacted the
                    // set of different rock types
                    double cr = 0; // no compaction
                    Tensor3D K = new Tensor3D(d.kx[i][j][k]
                            * Conversions.mDarcyInSquareMeter, d.ky[i][j][k]
                            * Conversions.mDarcyInSquareMeter, d.kz[i][j][k]
                            * Conversions.mDarcyInSquareMeter, d.kxy[i][j][k]
                            * Conversions.mDarcyInSquareMeter, d.kxz[i][j][k]
                            * Conversions.mDarcyInSquareMeter, d.kyz[i][j][k]
                            * Conversions.mDarcyInSquareMeter);
                    /*
                     * / String region = String.format("rock%d", numOfRock); /
                     */
                    String region = "rock";
                    // */
                    r[en] = new Rock(this.phi, cr, K, region);

                    // replace with canonical representation
                    /*
                     * / if(canonical.containsKey(r[en])) { r[en] =
                     * canonical.get(r[en]); } else { canonical.put(r[en],
                     * r[en]); numOfRock++; } /
                     */
                    numOfRock++;
                    // */
                }
            }
        }
        System.out.format("Rock count: %d", numOfRock);

        // running connection index counter
        int cn = 0;

        // connections in the XY-plane
        for (int i = si; i < ti; i++) {
            for (int j = sj; j < tj; j++) {
                for (int k = sk; k < tk - 1; k++) {
                    // normally we use the secondary position along the axis for
                    // the 'here' position, but since we have mirrored all
                    // points along the XY-plane, we do the opposite here. (if
                    // you get negative coordinates in the input file, then do
                    // this the other way).
                    int here = interfaceIndex(i, j, k, Orientation.TOP);
                    int there = interfaceIndex(i, j, k + 1, Orientation.BOTTOM);
                    t.buildNeighbourConnectionTopology(cn++, here, there);
                }
            }
        }
        // connections in the XZ-plane
        for (int i = si; i < ti; i++) {
            for (int j = sj; j < tj - 1; j++) {
                for (int k = sk; k < tk; k++) {
                    int here = interfaceIndex(i, j, k, Orientation.BACK);
                    int there = interfaceIndex(i, j + 1, k, Orientation.FRONT);
                    t.buildNeighbourConnectionTopology(cn++, here, there);
                }
            }
        }
        // connections in the YZ-plane
        for (int i = si; i < ti - 1; i++) {
            for (int j = sj; j < tj; j++) {
                for (int k = sk; k < tk; k++) {
                    int here = interfaceIndex(i, j, k, Orientation.RIGHT);
                    int there = interfaceIndex(i + 1, j, k, Orientation.LEFT);
                    t.buildNeighbourConnectionTopology(cn++, here, there);
                }
            }
        }
        assert cn == numOfConns;

        // collect all the data into a mesh and return
        Mesh m = new Mesh(g, t, r);
        return m;
    }
}
