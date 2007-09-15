package no.uib.cipr.rs.meshgen.lgr;

import static no.uib.cipr.rs.meshgen.structured.Direction.I;
import static no.uib.cipr.rs.meshgen.structured.Direction.J;
import static no.uib.cipr.rs.meshgen.structured.Direction.K;
import static no.uib.cipr.rs.meshgen.structured.Orientation.BACK;
import static no.uib.cipr.rs.meshgen.structured.Orientation.BOTTOM;
import static no.uib.cipr.rs.meshgen.structured.Orientation.FRONT;
import static no.uib.cipr.rs.meshgen.structured.Orientation.LEFT;
import static no.uib.cipr.rs.meshgen.structured.Orientation.RIGHT;
import static no.uib.cipr.rs.meshgen.structured.Orientation.TOP;

import java.util.Arrays;
import java.util.Map;

import no.uib.cipr.rs.geometry.Point3D;
import no.uib.cipr.rs.meshgen.structured.Box;
import no.uib.cipr.rs.meshgen.structured.BoxParser;
import no.uib.cipr.rs.meshgen.structured.CartesianTopology;
import no.uib.cipr.rs.meshgen.structured.CartesianTopology3D;
import no.uib.cipr.rs.meshgen.structured.IJK;
import no.uib.cipr.rs.meshgen.structured.Orientation;
import no.uib.cipr.rs.meshgen.structured.RegionGeometry;
import no.uib.cipr.rs.meshgen.structured.RegionMapParser;
import no.uib.cipr.rs.meshgen.structured.RockDataParser;
import no.uib.cipr.rs.meshgen.structured.RockRegionParser;
import no.uib.cipr.rs.meshgen.transform.Transform;
import no.uib.cipr.rs.meshgen.transform.TranslateDilateTransform;
import no.uib.cipr.rs.meshgen.util.ArrayData;
import no.uib.cipr.rs.meshgen.util.IndexedPoint3D;
import no.uib.cipr.rs.rock.Rock;
import no.uib.cipr.rs.util.Configuration;

class FineGeometry extends RegionGeometry {

    private static final long serialVersionUID = -4809384574459277086L;

    private CartesianTopology topology;

    private double x0, y0, z0;

    private IndexedPoint3D[] p;

    private IJK coarseIJK;

    /**
     * Array of edge-representation elements in this geometry
     */
    private transient FineElement[] elements;

    private Rock[] rocks;

    /**
     * Creates a refinement mesh geometry from the given fine geometry
     * configuration. The mesh lies within a specified coarse element.
     */
    public FineGeometry(Configuration config, CoarseGeometry coarseGeometry) {
        int[] coarse = config.getIntArray("CoarseElementIJK");
        if (coarse.length != 3)
            throw new IllegalArgumentException(config.trace()
                    + "3 indices must be given");

        // coarse element ijk, converting from 1-indexing to 0-indexing
        coarseIJK = new IJK(coarse[I.dir()] - 1, coarse[J.dir()] - 1, coarse[K
                .dir()] - 1);

        Configuration sub = config.getConfiguration("FineGeometry");

        // refinement begins at origin
        x0 = 0.0;
        y0 = 0.0;
        z0 = 0.0;

        int[] nx = sub.getIntArray("Nx");
        int[] ny = sub.getIntArray("Ny");
        int[] nz = sub.getIntArray("Nz");

        double[] dx = sub.getDoubleArray("Dx");
        double[] dy = sub.getDoubleArray("Dy");
        double[] dz = sub.getDoubleArray("Dz");

        checkParts(nx, ny, nz, dx, dy, dz);

        mappingType = sub.getString("RegionMappingType");

        checkMappingType();

        // find total number of cells in each direction
        int numX = ArrayData.getTotal(nx);
        int numY = ArrayData.getTotal(ny);
        int numZ = ArrayData.getTotal(nz);

        topology = new CartesianTopology3D(sub, numX, numY, numZ);

        // expand deltas
        double[] deltaX = ArrayData.getExpanded(nx, dx);
        double[] deltaY = ArrayData.getExpanded(ny, dy);
        double[] deltaZ = ArrayData.getExpanded(nz, dz);

        partZ = ArrayData.getExpanded(nz);

        // expand point coordinates
        double[] x = ArrayData.getExpandedPoints(x0, deltaX);
        double[] y = ArrayData.getExpandedPoints(y0, deltaY);
        double[] z = ArrayData.getExpandedPoints(z0, deltaZ);

        p = new IndexedPoint3D[topology.getNumPoints()];
        for (IJK ijk : topology.getPointsIJK()) {
            p[topology.getLinearPoint(ijk)] = new IndexedPoint3D(x[ijk.i()],
                    y[ijk.j()], z[ijk.k()]);
        }

        translateDilate(coarseGeometry.getElementOrigin(coarseIJK),
                coarseGeometry.getElementLength(coarseIJK));

        // build elements
        elements = new FineElement[topology.getNumElements()];

        for (IJK ijk : topology.getElementsIJK()) {
            int[] cp = topology.getElementPoints(ijk);

            int i = topology.getLinearElement(ijk);

            elements[i] = new FineElement(new IndexedPoint3D[] { p[cp[0]],
                    p[cp[1]], p[cp[2]], p[cp[3]], p[cp[4]], p[cp[5]], p[cp[6]],
                    p[cp[7]] });
        }

        // build rock region and rock data maps
        Map<String, Box> boxes = new BoxParser(config, topology).getBoxes();

        // map region indices to rock names
        Map<Integer, String> regions = new RegionMapParser(config, topology,
                this).getRegionMap();

        // map rock region names to element indices
        Map<String, int[]> rockRegionMap = new RockRegionParser(config, boxes,
                regions, topology, this).getRockRegionMap();

        // Rock data for each cell in the fine geometry
        RockDataParser rockDataParser = new RockDataParser(config, boxes,
                regions, topology, this, rockRegionMap);
        rocks = new Rock[topology.getNumElements()];
        for (int i = 0; i < rocks.length; ++i)
            rocks[i] = rockDataParser.getRock(i);
    }

    /**
     * Checks parts for consistent values.
     * 
     * Number of parts must be equal for nx-dx, ny-dy and nz-dz. nx, ny and nz
     * must all be positive. dx and dy must be positive and dz must be negative
     */
    private void checkParts(int[] nx, int[] ny, int[] nz, double[] dx,
            double[] dy, double[] dz) {
        if (nx.length != dx.length)
            throw new IllegalArgumentException(
                    "the number of nx and dx values are not equal");
        if (ny.length != dy.length)
            throw new IllegalArgumentException(
                    "the number of ny and dy values are not equal");
        if (nz.length != dz.length)
            throw new IllegalArgumentException(
                    "the number of nz and dz values are not equal");

        for (int val : nx)
            if (!(val > 0))
                throw new IllegalArgumentException("nx must be positive: nx = "
                        + val);
        for (int val : ny)
            if (!(val > 0))
                throw new IllegalArgumentException("ny must be positive: ny = "
                        + val);
        for (int val : nz)
            if (!(val > 0))
                throw new IllegalArgumentException("nz must be positive: nz = "
                        + val);

        for (double val : dz)
            if (!(val < 0.0))
                throw new IllegalArgumentException("dz must be�negative: dz = "
                        + val);
        for (double val : dy)
            if (!(val > 0.0))
                throw new IllegalArgumentException("dy must be positive: dy = "
                        + val);
        for (double val : dx)
            if (!(val > 0.0))
                throw new IllegalArgumentException("dx must be positive: dx = "
                        + val);
    }

    /**
     * Returns the forward orientations in this geometry.
     */
    public Iterable<Orientation> getForwardOrientations() {
        Orientation[] orientations = new Orientation[] { BOTTOM, BACK, RIGHT };

        return Arrays.asList(orientations);
    }

    /**
     * Returns all the orientations in this geometry.
     */
    public Iterable<Orientation> getOrientations() {
        Orientation[] orientations = new Orientation[] { TOP, BOTTOM, FRONT,
                BACK, LEFT, RIGHT };

        return Arrays.asList(orientations);
    }

    public CartesianTopology getTopology() {
        return topology;
    }

    /**
     * Translates and dilates the geometry to fit within the box defined by the
     * given origin and length.
     */
    private void translateDilate(double[] origin, double[] length) {
        Transform transform = new TranslateDilateTransform(origin, length,
                topology, p);

        IndexedPoint3D[] pts = new IndexedPoint3D[topology.getNumPoints()];

        for (IJK ijk : topology.getPointsIJK()) {
            Point3D tp = transform.getPoint(topology.getLinearPoint(ijk));
            pts[topology.getLinearPoint(ijk)] = new IndexedPoint3D(tp.x(), tp
                    .y(), tp.z());
        }
        p = pts;
    }

    /**
     * Returns the ijk-index of the coarse element that embeds this fine
     * geometry.
     */
    public IJK getCoarseElementIJK() {
        return coarseIJK;
    }

    /**
     * Returns the fine edge at the given location of the element with the given
     * ijk-index.
     */
    public FineEdge getElementEdge(IJK ijk, EdgeLocation edgeLoc) {
        return elements[topology.getLinearElement(ijk)].getFineEdge(edgeLoc);
    }

    /**
     * Returns the face of the given element in the given orientation.
     */
    public Face getElementFace(IJK ijk, Orientation orientation) {
        return elements[topology.getLinearElement(ijk)].getFace(orientation);
    }

    /**
     * Returns an iterable object of all the elements in this geometry.
     */
    public Iterable<FineElement> getElements() {
        return Arrays.asList(elements);
    }

    /**
     * Returns the rocks
     */
    public Rock[] getRocks() {
        return rocks;
    }

    /**
     * Returns the element with the given (local) linear index.
     */
    public FineElement getElement(int i) {
        return elements[i];
    }

}
