package no.uib.cipr.rs.meshgen.lgr;

import static no.uib.cipr.rs.meshgen.lgr.EdgeLocation.BACKLEFT;
import static no.uib.cipr.rs.meshgen.lgr.EdgeLocation.BACKRIGHT;
import static no.uib.cipr.rs.meshgen.lgr.EdgeLocation.BOTTOMBACK;
import static no.uib.cipr.rs.meshgen.lgr.EdgeLocation.BOTTOMFRONT;
import static no.uib.cipr.rs.meshgen.lgr.EdgeLocation.BOTTOMLEFT;
import static no.uib.cipr.rs.meshgen.lgr.EdgeLocation.BOTTOMRIGHT;
import static no.uib.cipr.rs.meshgen.lgr.EdgeLocation.FRONTLEFT;
import static no.uib.cipr.rs.meshgen.lgr.EdgeLocation.FRONTRIGHT;
import static no.uib.cipr.rs.meshgen.lgr.EdgeLocation.TOPBACK;
import static no.uib.cipr.rs.meshgen.lgr.EdgeLocation.TOPFRONT;
import static no.uib.cipr.rs.meshgen.lgr.EdgeLocation.TOPLEFT;
import static no.uib.cipr.rs.meshgen.lgr.EdgeLocation.TOPRIGHT;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import no.uib.cipr.rs.meshgen.bsp.Edge3D;
import no.uib.cipr.rs.meshgen.structured.Orientation;
import no.uib.cipr.rs.meshgen.util.ArrayData;
import no.uib.cipr.rs.meshgen.util.IndexedPoint3D;

/**
 * Edge-representation based element class to be used for refinements.
 */
class FineElement {

    private Map<EdgeLocation, FineEdge> edges;

    private Map<Orientation, Face> faces;

    private boolean facesBuilt;

    private int i;

    /**
     * Stores the global face indices of this element
     */
    private List<Integer> faceIndices;

    /**
     * Creates a fine element from the given array of counter-clockwise ordered
     * corner points (top-first).
     */
    public FineElement(IndexedPoint3D[] points) {
        edges = new HashMap<EdgeLocation, FineEdge>(11);

        edges.put(TOPFRONT, new FineEdge(points[0], points[1]));
        edges.put(TOPBACK, new FineEdge(points[3], points[2]));
        edges.put(TOPLEFT, new FineEdge(points[0], points[3]));
        edges.put(TOPRIGHT, new FineEdge(points[1], points[2]));
        edges.put(BOTTOMFRONT, new FineEdge(points[4], points[5]));
        edges.put(BOTTOMBACK, new FineEdge(points[7], points[6]));
        edges.put(BOTTOMLEFT, new FineEdge(points[4], points[7]));
        edges.put(BOTTOMRIGHT, new FineEdge(points[5], points[6]));
        edges.put(FRONTLEFT, new FineEdge(points[0], points[4]));
        edges.put(FRONTRIGHT, new FineEdge(points[1], points[5]));
        edges.put(BACKLEFT, new FineEdge(points[3], points[7]));
        edges.put(BACKRIGHT, new FineEdge(points[2], points[6]));

        facesBuilt = false;
    }

    /**
     * Returns the edge of this element at the given edge location.
     */
    public FineEdge getFineEdge(EdgeLocation edgeLoc) {
        return edges.get(edgeLoc);
    }

    /**
     * Returns the face of this element with the given orientation.
     */
    public Face getFace(Orientation orientation) {
        if (!facesBuilt)
            throw new IllegalArgumentException("Element faces are not built.");

        return faces.get(orientation);
    }

    /**
     * Build the (non-refined) polygonal faces of the mesh. This must be done
     * after element edges has been augmented with fine-fine connection corner
     * points.
     * 
     * The face edges are all oriented in global counter-clockwise order, i.e.
     * bottom, back and right faces must have their edges reversed in order to
     * produce an outward pointing normal relative to the element.
     * 
     */
    public void buildFaces() {
        faces = new HashMap<Orientation, Face>(9);

        for (Orientation orient : Orientation.getOrientations3D())
            faces.put(orient, new Face(getFaceEdges(orient), orient));

        facesBuilt = true;
    }

    /**
     * Returns an iterable object of the faces including subfaces of this
     * element in the given orientation.
     */
    public Iterable<Face> getFaces(Orientation orientation) {
        if (!facesBuilt)
            throw new IllegalArgumentException("Faces must be built");

        Face face = faces.get(orientation);

        List<Face> l = new ArrayList<Face>();

        if (face.isRefined())
            l = face.getSubfaces();
        else
            l.add(face);

        return l;
    }

    /**
     * Sets the linear index of this element to the given value.
     */
    public void setIndex(int i) {
        this.i = i;
    }

    /**
     * Returns the linear index of this element.
     */
    public int getIndex() {
        return i;
    }

    /**
     * Returns the faces (subfaces if refined face) building up this element.
     */
    public Iterable<Face> getFaces() {
        List<Face> l = new ArrayList<Face>();

        for (Orientation orient : Orientation.getOrientations3D()) {
            Face face = faces.get(orient);
            if (face.isRefined())
                l.addAll(face.getSubfaces());
            else
                l.add(face);
        }

        return l;
    }

    /**
     * Returns an array containing the global indices of the primary corner
     * points of this element.
     */
    public List<Integer> getCornerIndices() {
        List<Integer> indices = new ArrayList<Integer>(8);

        indices.add(edges.get(TOPFRONT).getBeginPoint().getIndex());
        indices.add(edges.get(TOPRIGHT).getBeginPoint().getIndex());
        indices.add(edges.get(TOPBACK).getEndPoint().getIndex());
        indices.add(edges.get(TOPLEFT).getEndPoint().getIndex());
        indices.add(edges.get(BOTTOMFRONT).getBeginPoint().getIndex());
        indices.add(edges.get(BOTTOMRIGHT).getBeginPoint().getIndex());
        indices.add(edges.get(BOTTOMBACK).getEndPoint().getIndex());
        indices.add(edges.get(BOTTOMLEFT).getEndPoint().getIndex());

        return indices;
    }

    /**
     * Returns a list of edges for the face with the given orientation. This
     * list can be used to construct or update a face's edges or points.
     */
    private List<Edge3D> getFaceEdges(Orientation orient) {
        List<Edge3D> e = new ArrayList<Edge3D>(4);
        switch (orient) {
        case TOP:
            e.add(new Edge3D(edges.get(TOPFRONT).vertices()));
            e.add(new Edge3D(edges.get(TOPRIGHT).vertices()));
            e.add(new Edge3D(ArrayData.reverse(edges.get(TOPBACK).vertices())));
            e.add(new Edge3D(ArrayData.reverse(edges.get(TOPLEFT).vertices())));
            break;
        case BOTTOM:
            e.add(new Edge3D(edges.get(BOTTOMFRONT).vertices()));
            e.add(new Edge3D(edges.get(BOTTOMRIGHT).vertices()));
            e.add(new Edge3D(ArrayData
                    .reverse(edges.get(BOTTOMBACK).vertices())));
            e.add(new Edge3D(ArrayData
                    .reverse(edges.get(BOTTOMLEFT).vertices())));
            break;
        case FRONT:
            e
                    .add(new Edge3D(ArrayData.reverse(edges.get(TOPFRONT)
                            .vertices())));
            e
                    .add(new Edge3D(ArrayData.reverse(edges.get(FRONTLEFT)
                            .vertices())));
            e.add(new Edge3D(edges.get(BOTTOMFRONT).vertices()));
            e.add(new Edge3D(edges.get(FRONTRIGHT).vertices()));
            break;
        case BACK:
            e.add(new Edge3D(ArrayData.reverse(edges.get(TOPBACK).vertices())));
            e
                    .add(new Edge3D(ArrayData.reverse(edges.get(BACKLEFT)
                            .vertices())));
            e.add(new Edge3D(edges.get(BOTTOMBACK).vertices()));
            e.add(new Edge3D(edges.get(BACKRIGHT).vertices()));
            break;
        case LEFT:
            e.add(new Edge3D(edges.get(TOPLEFT).vertices()));
            e
                    .add(new Edge3D(ArrayData.reverse(edges.get(BACKLEFT)
                            .vertices())));
            e.add(new Edge3D(ArrayData
                    .reverse(edges.get(BOTTOMLEFT).vertices())));
            e.add(new Edge3D(edges.get(FRONTLEFT).vertices()));
            break;
        case RIGHT:
            e.add(new Edge3D(edges.get(TOPRIGHT).vertices()));
            e
                    .add(new Edge3D(ArrayData.reverse(edges.get(BACKRIGHT)
                            .vertices())));
            e.add(new Edge3D(ArrayData.reverse(edges.get(BOTTOMRIGHT)
                    .vertices())));
            e.add(new Edge3D(edges.get(FRONTRIGHT).vertices()));
            break;
        default:
            throw new IllegalArgumentException("Illegal orientation " + orient);
        }
        return e;
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder("Element:\n");

        s.append("\tEdges:\n");
        for (Map.Entry<EdgeLocation, FineEdge> e : edges.entrySet()) {
            String loc = e.getKey().toString();
            FineEdge edge = e.getValue();
            s.append("\t\t" + loc + ": " + edge.toString() + "\n");
        }

        if (!facesBuilt)
            throw new IllegalArgumentException("Faces not built.");

        s.append("\n\tFaces:\n");
        for (Map.Entry<Orientation, Face> e : faces.entrySet()) {
            String orient = e.getKey().toString();
            Face face = e.getValue();
            s.append("\t\t" + orient + ": " + face.toString() + "\n");
        }

        return s.toString();
    }

    public void setFaceIndices() {
        faceIndices = new ArrayList<Integer>();

        for (Face face : getFaces())
            faceIndices.add(face.getIndex());
    }

    /**
     * Returns the indices of the faces contributing to this element.
     */
    public int[] getFaceIndices() {
        return ArrayData.integerListToArray(faceIndices);
    }

    /**
     * Computes additional face points due to refinements that are not taken
     * care of during refinement-refinement overlap computations.
     */
    public void computeFaceOverlap() {
        for (Orientation orient : Orientation.getOrientations3D()) {
            Face face = getFace(orient);
            if (!face.isRefined()) {
                // for each edge of face, check with other orientations
                for (EdgeLocation el : getEdgeLocations(orient)) {
                    FineEdge edge = getFineEdge(el);

                    // pick an aribtrary point of the edge to determine the
                    // plane coordinates. Here we use the begin point.
                    IndexedPoint3D begin = edge.getBeginPoint();
                    double coord = getPlaneCoordinate(orient, begin);

                    Face otherFace = getFace(EdgeLocation
                            .getConnectedOrientation(orient, el));

                    if (otherFace.isRefined()) {
                        for (Face refinedFace : otherFace.getSubfaces()) {
                            // check if the refined face's points are part
                            // of the candidate face point list. If not check if
                            // the point in question lies along an edge of the
                            // face.
                            for (IndexedPoint3D p : refinedFace.getPoints()) {
                                if (getPlaneCoordinate(orient, p) == coord) {
                                    // Now we know the refined face point is
                                    // along the non-refined edge and we ask the
                                    // edge to add it.
                                    // It is left to the edge to determine if
                                    // the point is already in the list
                                    // of edge points. If not, it is added to
                                    // the point list in a consistent location.
                                    edge.addPoint(p);
                                }
                            }
                        }
                    }
                }
                // all additional points have been added to FineElement edges.
                // now update the edges of the face
                face.updateEdges(getFaceEdges(orient));
            }
        }
    }

    /**
     * Returns a list of edge locations corresponding to the given face
     * orientation
     */
    private List<EdgeLocation> getEdgeLocations(Orientation orient) {
        switch (orient) {
        case TOP:
            return Arrays.asList(new EdgeLocation[] { TOPFRONT, TOPBACK,
                    TOPLEFT, TOPRIGHT });
        case BOTTOM:
            return Arrays.asList(new EdgeLocation[] { BOTTOMFRONT, BOTTOMBACK,
                    BOTTOMLEFT, BOTTOMRIGHT });
        case FRONT:
            return Arrays.asList(new EdgeLocation[] { TOPFRONT, BOTTOMFRONT,
                    FRONTLEFT, FRONTRIGHT });
        case BACK:
            return Arrays.asList(new EdgeLocation[] { TOPBACK, BOTTOMBACK,
                    BACKLEFT, BACKRIGHT });
        case LEFT:
            return Arrays.asList(new EdgeLocation[] { TOPLEFT, BOTTOMLEFT,
                    FRONTLEFT, BACKLEFT });
        case RIGHT:
            return Arrays.asList(new EdgeLocation[] { TOPRIGHT, BOTTOMRIGHT,
                    FRONTRIGHT, BACKRIGHT });
        default:
            throw new IllegalArgumentException("Illegal orientation");
        }
    }

    /**
     * Returns the coordinate of the plane defined by the orientation
     * intersecting the given point
     */
    private double getPlaneCoordinate(Orientation orientation,
            IndexedPoint3D point) {
        switch (orientation) {
        case TOP:
            return point.z();
        case BOTTOM:
            return point.z();
        case FRONT:
            return point.y();
        case BACK:
            return point.y();
        case LEFT:
            return point.x();
        case RIGHT:
            return point.x();
        default:
            throw new IllegalArgumentException("Invalid orientation");
        }
    }
}
