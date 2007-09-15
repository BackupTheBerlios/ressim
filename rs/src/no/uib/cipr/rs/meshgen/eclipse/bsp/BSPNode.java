package no.uib.cipr.rs.meshgen.eclipse.bsp;

import static no.uib.cipr.rs.meshgen.eclipse.bsp.Partition.COINCIDENT;
import static no.uib.cipr.rs.meshgen.eclipse.bsp.Partition.INSIDE;
import static no.uib.cipr.rs.meshgen.eclipse.bsp.Partition.INTERSECTING;
import static no.uib.cipr.rs.meshgen.eclipse.bsp.Partition.OUTSIDE;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import no.uib.cipr.rs.geometry.Vector3D;
import no.uib.cipr.rs.meshgen.eclipse.geometry.CornerPoint3D;
import no.uib.cipr.rs.meshgen.eclipse.geometry.IJ;
import no.uib.cipr.rs.util.Tolerances;

/**
 * Binary Space Partition node.
 */
public class BSPNode {

    /**
     * Holds all log-info
     */
    private final static Logger log = Logger.getLogger(BSPNode.class.getName());

    private BSPTree inside;

    private BSPTree outside;

    private Edge edge;

    private Plane3D partitionPlane;

    /**
     * Creates a <code>BSPNode</code> given a list of <code>Edge</code>s.
     * The edge list must contain non-overlapping edges. This is not checked.
     * 
     * @param edges
     */
    public BSPNode(List<Edge> edges) {

        if (edges.isEmpty())
            throw new IllegalArgumentException(
                    "BSPNode input edge list must have at least one element.");

        // store first edge in list
        edge = edges.remove(0);

        try {
            partitionPlane = new Plane3D(edge.getNormal(), edge.getBeginPoint());
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException(
                    "Could not create partition plane: " + e.getMessage());
        }

        // if more edges, continue build
        if (!edges.isEmpty()) {

            List<Edge> insideList = new ArrayList<Edge>();
            List<Edge> outsideList = new ArrayList<Edge>();

            // loop through edges and determine inside/outside relationship
            for (Edge e : edges) {
                Partition type = e.classifyEdge(partitionPlane);

                if (type == INTERSECTING) {
                    throw new IllegalArgumentException(
                            "Edges must be non-intersecting." + "\n\tEdge: "
                                    + e.toString() + "\n\tPartition plane: "
                                    + partitionPlane.toString());
                }

                else if (type == INSIDE || type == COINCIDENT) {
                    insideList.add(e);
                }

                else if (type == OUTSIDE) {
                    outsideList.add(e);
                }

            }

            if (!insideList.isEmpty()) {
                inside = new BSPTree(insideList);
            }

            if (!outsideList.isEmpty()) {
                outside = new BSPTree(outsideList);
            }
        }
    }

    public BSPTree getInside() {
        return inside;
    }

    public BSPTree getOutside() {
        return outside;
    }

    /**
     * @param e
     *            An <code>Edge</code>
     * @return Returns the <code>Edge</code> segment of the input edge that is
     *         inside this node's edge. If input edge is outside, null is
     *         returned.
     */
    public Edge getInsideSegment(Edge e) {
        Partition type = e.classifyEdge(partitionPlane);

        if (type == INSIDE || type == COINCIDENT) {
            if (inside == null) {
                return e;
            }

            return inside.getInsideSegment(e);
        }

        // segment the edge
        else if (type == INTERSECTING) {
            CornerPoint3D a = e.getBeginPoint();
            CornerPoint3D b = e.getEndPoint();

            double da = partitionPlane.getPointDistance(a);
            double db = partitionPlane.getPointDistance(b);

            // compute intersection point
            Vector3D v = new Vector3D(a, b);
            double s = -da / (partitionPlane.normal.dot(v));
            CornerPoint3D intersect = new CornerPoint3D(a, v.mult(s), new IJ(
                    edge.getIndex(), e.getIndex()));

            Edge segment = null;
            if (db > Tolerances.smallEps) { // point B outside
                if (da < Tolerances.smallEps) { // point A inside
                    segment = new Edge3D(a, intersect, e.getIndex());
                    try {
                        segment.setNormal(e.getNormal());
                    } catch (IllegalAccessException e1) {
                        log.severe(e1.getMessage());
                        // e1.printStackTrace();
                    }
                }
            } else if (db < Tolerances.smallEps) { // point B inside
                if (da > Tolerances.smallEps) { // point A outside
                    segment = new Edge3D(intersect, b, e.getIndex());
                    try {
                        segment.setNormal(e.getNormal());
                    } catch (IllegalAccessException e1) {
                        log.severe(e1.getMessage());
                    }
                }
            }

            if (inside == null) {
                return segment;
            }

            return inside.getInsideSegment(segment);
        }

        else { // type == OUTSIDE
            return null;
        }
    }
}
