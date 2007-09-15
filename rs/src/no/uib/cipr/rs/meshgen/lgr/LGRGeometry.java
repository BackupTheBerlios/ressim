package no.uib.cipr.rs.meshgen.lgr;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import no.uib.cipr.rs.geometry.Geometry;
import no.uib.cipr.rs.geometry.Point3D;
import no.uib.cipr.rs.meshgen.structured.BilinearMap;
import no.uib.cipr.rs.meshgen.structured.CartesianTopology;
import no.uib.cipr.rs.meshgen.structured.IJK;
import no.uib.cipr.rs.meshgen.structured.Orientation;
import no.uib.cipr.rs.meshgen.structured.TrilinearMap;
import no.uib.cipr.rs.meshgen.transform.Transform;
import no.uib.cipr.rs.meshgen.util.IndexedPoint3D;
import no.uib.cipr.rs.rock.Rock;
import no.uib.cipr.rs.util.Configuration;

public class LGRGeometry extends Geometry {

    private static final long serialVersionUID = 1162128755049986744L;

    private LGRTopology topology;

    private CoarseGeometry coarse;

    private Map<IJK, FineGeometry> refinements;

    private IndexedPoint3D[] points;

    private Face[] faces;

    private FineElement[] elements;

    private Rock[] rocks;

    public LGRGeometry(Configuration config) {
        coarse = new CoarseGeometry(config);

        refinements = buildRefinements(config.getConfiguration("Refinements"));

        topology = new LGRTopology(coarse, refinements);

        /* add edge points of the coarse edges when creating LGR-topology */
        setCoarseEdgePoints();

        /* compute coarse-fine edge overlap */
        computeCoarseFineEdgeOverlap();

        /* build element faces */
        buildElementFaces();

        /* Computes fine-fine face overlap and returns external connections. */
        List<Connection> externalConnections = computeFineFineFaceOverlap();

        /*
         * Sets edge points for internal faces of elements that connect to other
         * refinements
         */
        // TODO not yet working!
        computeElementwiseFaceOverlap();

        /*
         * Gets the array of unique points. The element and face points have
         * indices to this array.
         */
        Point3D[] nonTransPoints = buildPointTopology();

        faces = buildFaceTopology();

        elements = buildElementTopology();

        buildConnectionTopology(externalConnections);

        setSizes(topology);

        /* Perform global transformation */
        Transform transform = Transform.create(config, nonTransPoints);

        points = new IndexedPoint3D[topology.getNumPoints()];
        for (int i = 0; i < topology.getNumPoints(); i++) {
            IndexedPoint3D p = new IndexedPoint3D(transform.getPoint(i));
            points[i] = p;
            buildPoint(i, p);
        }

        // perform geometric computations
        computeElementData();

        computeFaceData();

        // Assemble the rock data
        rocks = buildRocks();
    }

    private void computeElementwiseFaceOverlap() {
        for (IJK coarseIJK : topology.getRefinedElementsIJK()) {
            FineGeometry fine = refinements.get(coarseIJK);

            for (FineElement elem : fine.getElements())
                elem.computeFaceOverlap();

        }
    }

    /**
     * Builds the faces of each element after element edges have been augmented
     * with fine-fine points along edges that coincides with coarse element
     * edges.
     */
    private void buildElementFaces() {
        for (IJK coarseIJK : topology.getRefinedElementsIJK()) {
            FineGeometry fine = refinements.get(coarseIJK);

            for (FineElement elem : fine.getElements())
                elem.buildFaces();
        }

    }

    /**
     * Assembles the rocks list from the refinements
     */
    private Rock[] buildRocks() {
        List<Rock> rocks = new LinkedList<Rock>();

        for (IJK coarseIJK : topology.getRefinedElementsIJK()) {
            FineGeometry fine = refinements.get(coarseIJK);

            rocks.addAll(Arrays.asList(fine.getRocks()));
        }

        return rocks.toArray(new Rock[rocks.size()]);
    }

    /*
     * Computes all connections of the mesh
     */
    private void buildConnectionTopology(List<Connection> external) {
        List<Connection> list = new ArrayList<Connection>();

        // add internal connections
        for (IJK coarseIJK : topology.getRefinedElementsIJK()) {
            FineGeometry fineGeometry = refinements.get(coarseIJK);
            CartesianTopology fineTopology = fineGeometry.getTopology();

            for (IJK ijkHere : fineTopology.getElementsIJK())
                for (Orientation forward : Orientation
                        .getForwardOrientations3D()) {
                    if (fineTopology.hasElementNeighbour(ijkHere, forward)) {
                        IJK ijkThere = fineTopology.getElementNeighbour(
                                ijkHere, forward);

                        Face here = fineGeometry.getElementFace(ijkHere,
                                forward);
                        Face there = fineGeometry.getElementFace(ijkThere,
                                forward.getOpposite());

                        list.add(new Connection(here, there));
                    }
                }
        }

        // add refinement connections
        list.addAll(external);

        topology.setNumConnections(list.size(), 0);

        for (int i = 0; i < list.size(); i++) {
            Connection c = list.get(i);
            topology.buildNeighbourConnectionTopology(i, c.here(), c.there());
        }
    }

    /**
     * Creates an array of elements and sets their indices.
     */
    private FineElement[] buildElementTopology() {
        List<FineElement> list = new ArrayList<FineElement>();

        int index = 0;

        for (IJK coarseIJK : topology.getRefinedElementsIJK()) {
            FineGeometry fine = refinements.get(coarseIJK);

            for (FineElement elem : fine.getElements()) {
                elem.setIndex(index++);
                elem.setFaceIndices();

                list.add(elem);
            }
        }

        topology.setNumElements(list.size());
        for (FineElement elem : list)
            topology.buildElementTopology(elem.getIndex(), elem
                    .getFaceIndices());

        return list.toArray(new FineElement[list.size()]);
    }

    /**
     * Creates an array of all faces and sets their indices
     */
    private Face[] buildFaceTopology() {
        List<Face> list = new ArrayList<Face>();

        int index = 0;

        for (IJK coarseIJK : topology.getRefinedElementsIJK()) {
            FineGeometry fine = refinements.get(coarseIJK);

            for (FineElement elem : fine.getElements()) {
                for (Orientation orient : fine.getOrientations()) {
                    for (Face face : elem.getFaces(orient)) {
                        if (face.isRefined())
                            throw new IllegalArgumentException(
                                    "Face should not be refined in point topology setup");

                        face.setIndex(index++);
                        face.setPointIndices();

                        list.add(face);
                    }
                }
            }
        }

        topology.setNumInterfaces(list.size());
        for (Face face : list)
            topology.buildInterfaceTopology(face.getIndex(), face
                    .getPointIndices());

        return list.toArray(new Face[list.size()]);
    }

    /*
     * Sets the unique global index for all points. Returns an array of all the
     * unique points in this geometry. These are collected from the polyline
     * edges.
     */
    private IndexedPoint3D[] buildPointTopology() {
        List<IndexedPoint3D> duplicates = new ArrayList<IndexedPoint3D>();

        for (IJK coarseIJK : topology.getRefinedElementsIJK()) {
            FineGeometry fine = refinements.get(coarseIJK);

            for (FineElement elem : fine.getElements()) {
                for (Orientation orient : fine.getOrientations()) {
                    for (Face face : elem.getFaces(orient)) {
                        if (face.isRefined())
                            throw new IllegalArgumentException(
                                    "Face should not be refined in point topology setup");
                        duplicates.addAll(face.getPoints());
                    }
                }
            }
        }

        Collections.sort(duplicates);

        SortedSet<IndexedPoint3D> unique = new TreeSet<IndexedPoint3D>();

        for (IndexedPoint3D p : duplicates) {
            int u;

            if (!unique.contains(p)) {
                u = unique.size();
                p.setIndex(u);
                unique.add(p);
            } else {
                // if the current point is contained in the set, it will be the
                // last point since duplicates is sorted.
                if (unique.size() == 0)
                    throw new IllegalArgumentException(
                            "Size should not be zero here!");
                u = unique.size() - 1;
                p.setIndex(u);
            }
        }

        topology.setNumPoints(unique.size());

        return unique.toArray(new IndexedPoint3D[unique.size()]);
    }

    /*
     * Computes the face overlap between neighbouring refinements. The result is
     * a partitioning of faces for each refinement. Returns a list of external
     * connections, i.e. connections between refinements.
     */
    private List<Connection> computeFineFineFaceOverlap() {
        List<Connection> externalConnections = new ArrayList<Connection>();

        for (IJK coarseIJKHere : topology.getRefinedElementsIJK())
            for (Orientation forward : Orientation.getForwardOrientations3D())

                if (topology.hasRefinedElementNeighbour(coarseIJKHere, forward)) {
                    IJK coarseIJKThere = topology.getCoarseElementNeighbour(
                            coarseIJKHere, forward);

                    FineGeometry geomHere = refinements.get(coarseIJKHere);
                    FineGeometry geomThere = refinements.get(coarseIJKThere);

                    // TODO implement bounding box
                    for (IJK fineIJKHere : topology.getFineFaceElements(
                            coarseIJKHere, forward))
                        for (IJK fineIJKThere : topology.getFineFaceElements(
                                coarseIJKThere, forward.getOpposite())) {
                            Face here = geomHere.getElementFace(fineIJKHere,
                                    forward);
                            Face there = geomThere.getElementFace(fineIJKThere,
                                    forward.getOpposite());

                            Face overlapHere = here.getIntersection(there);

                            if (overlapHere != null) {
                                // copy to make connection store two unique
                                // faces
                                Face overlapThere = there.getIntersection(here);

                                // true: hereOverlap.equals(thereOverlap)
                                here.addSubface(overlapHere);
                                there.addSubface(overlapThere);

                                externalConnections.add(new Connection(
                                        overlapHere, overlapThere));
                            }
                        }
                }

        return externalConnections;
    }

    /**
     * For all coarse edges, find refinements and compute overlap with fine
     * edges.
     */
    private void computeCoarseFineEdgeOverlap() {
        for (CoarseEdge coarseEdge : topology.getCoarseEdges()) {
            for (Map.Entry<IJK, EdgeLocation> e : coarseEdge.getElements()
                    .entrySet()) {
                IJK coarseIJK = e.getKey();

                if (topology.isRefinedElement(coarseIJK)) {
                    EdgeLocation loc = e.getValue();
                    for (FineEdge fineEdge : getFineEdges(coarseIJK, loc))
                        fineEdge.computeEdgeOverlap(coarseEdge);
                }
            }
        }
    }

    /**
     * Adds all fine edge points to the coarse edges
     */
    private void setCoarseEdgePoints() {
        for (CoarseEdge coarseEdge : topology.getCoarseEdges()) {
            SortedSet<IndexedPoint3D> pts = new TreeSet<IndexedPoint3D>();

            for (Map.Entry<IJK, EdgeLocation> e : coarseEdge.getElements()
                    .entrySet()) {
                IJK coarseIJK = e.getKey();

                if (topology.isRefinedElement(coarseIJK)) {
                    EdgeLocation loc = e.getValue();
                    pts.addAll(getFineEdgePoints(coarseIJK, loc));
                }
            }
            coarseEdge.setPoints(pts);
        }
    }

    /**
     * Returns an iterable object of the fine edges in the given refinement at
     * the given edge location.
     */
    private Iterable<FineEdge> getFineEdges(IJK coarseIJK, EdgeLocation loc) {
        List<FineEdge> l = new ArrayList<FineEdge>();
        FineGeometry fine = refinements.get(coarseIJK);

        for (IJK ijk : topology.getFineEdgeElements(coarseIJK, loc))
            l.add(fine.getElementEdge(ijk, loc));

        return l;
    }

    /**
     * Returns a sorted set of edge points for the refinement within the given
     * coarse element at the given edge location.
     */
    private List<IndexedPoint3D> getFineEdgePoints(IJK coarseIJK,
            EdgeLocation loc) {
        SortedSet<IndexedPoint3D> finePoints = new TreeSet<IndexedPoint3D>();

        FineGeometry fine = refinements.get(coarseIJK);

        for (IJK ijk : topology.getFineEdgeElements(coarseIJK, loc))
            finePoints.addAll(fine.getElementEdge(ijk, loc).vertices());

        return new ArrayList<IndexedPoint3D>(finePoints);
    }

    private Map<IJK, FineGeometry> buildRefinements(Configuration config) {
        Map<IJK, FineGeometry> map = new HashMap<IJK, FineGeometry>();

        for (String key : config.keys()) {
            Configuration sub = config.getConfiguration(key);

            FineGeometry fine = new FineGeometry(sub, coarse);
            IJK coarseIJK = fine.getCoarseElementIJK();

            if (map.containsKey(coarseIJK))
                throw new IllegalArgumentException(sub.trace()
                        + "Target coarse element already refined");

            map.put(coarseIJK, fine);
        }

        return map;
    }

    public LGRTopology getTopology() {
        return topology;
    }

    /*
     * Computes interface centers, areas and normals
     */
    private void computeFaceData() {
        int n = topology.getNumInterfaces();

        for (int i = 0; i < n; i++) {
            IndexedPoint3D[] pts = getFacePoints(faces[i].getCornerIndices());
            BilinearMap bilinear = new BilinearMap(pts[0], pts[1], pts[3],
                    pts[2]);

            buildInterface(i, bilinear.getArea(), bilinear.getCenterPoint(),
                    bilinear.getNormal());
        }
    }

    /*
     * Computes element centers and volumes
     */
    private void computeElementData() {
        int n = topology.getNumElements();

        for (int i = 0; i < n; i++) {
            IndexedPoint3D[] pts = getElementPoints(elements[i]
                    .getCornerIndices());
            TrilinearMap trilinear = new TrilinearMap(pts[0], pts[1], pts[3],
                    pts[2], pts[4], pts[5], pts[7], pts[6]);

            buildElement(i, trilinear.getVolume(), trilinear.getCenterPoint());
        }
    }

    /*
     * Computes face areas, normals and centers
     */
    private IndexedPoint3D[] getFacePoints(List<Integer> indices) {
        int n = indices.size();
        if (n != 4)
            throw new IllegalArgumentException("Only 4 vertices are allowed");

        IndexedPoint3D[] l = new IndexedPoint3D[n];

        for (int i = 0; i < n; i++)
            l[i] = points[indices.get(i)];

        return l;
    }

    private IndexedPoint3D[] getElementPoints(List<Integer> indices) {
        int n = indices.size();
        if (n != 8)
            throw new IllegalArgumentException("Only 8 vertices are allowed");

        IndexedPoint3D[] l = new IndexedPoint3D[n];

        for (int i = 0; i < n; i++)
            l[i] = points[indices.get(i)];

        return l;
    }

    public Rock getRock(int i) {
        return rocks[i];
    }
}
