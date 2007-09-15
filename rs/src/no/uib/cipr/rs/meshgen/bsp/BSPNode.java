package no.uib.cipr.rs.meshgen.bsp;

import static no.uib.cipr.rs.meshgen.bsp.Partition.COINCIDENT;
import static no.uib.cipr.rs.meshgen.bsp.Partition.INSIDE;
import static no.uib.cipr.rs.meshgen.bsp.Partition.INTERSECTING;
import static no.uib.cipr.rs.meshgen.bsp.Partition.OUTSIDE;

import java.util.ArrayList;
import java.util.List;

import no.uib.cipr.rs.geometry.Plane3D;
import no.uib.cipr.rs.geometry.Vector3D;
import no.uib.cipr.rs.meshgen.util.IndexedPoint3D;
import no.uib.cipr.rs.util.Tolerances;

/**
 * Binary Space Partition node.
 */
public class BSPNode {

    private BSPTree inside;

    private BSPTree outside;

    private Edge3D edge;

    private Plane3D partitionPlane;

    /**
     * Creates a BSPNode from a given a list of edges. The edge list must
     * contain non-overlapping edges. This is not checked.
     */
    public BSPNode(List<Edge3D> name) {
        if (name.isEmpty())
            throw new IllegalArgumentException(
                    "BSPNode input edge list must have at least one element.");

        // store first edge in list
        edge = name.remove(0);

        try {
            partitionPlane = new Plane3D(edge.getNormal(), edge.getBeginPoint());
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException(
                    "Could not create partition plane: " + e.getMessage());
        }

        // if more edges, continue build
        if (!name.isEmpty()) {
            List<Edge3D> insideList = new ArrayList<Edge3D>();
            List<Edge3D> outsideList = new ArrayList<Edge3D>();

            // loop through edges and determine inside/outside relationship
            for (Edge3D e : name) {
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
     * Returns the edge segment of the given edge that is inside this node's
     * edge. If input edge is outside, null is returned.
     */
    public Edge3D getInsideSegment(Edge3D e) {
        Partition type = e.classifyEdge(partitionPlane);

        if (type == INSIDE || type == COINCIDENT) {
            if (inside == null) {
                return e;
            }

            return inside.getInsideSegment(e);
        }

        // segment the edge
        else if (type == INTERSECTING) {
            IndexedPoint3D a = e.getBeginPoint();
            IndexedPoint3D b = e.getEndPoint();

            double da = partitionPlane.getPointDistance(a);
            double db = partitionPlane.getPointDistance(b);

            // compute intersection point
            Vector3D v = new Vector3D(a, b);
            double s = -da / (partitionPlane.getNormal().dot(v));
            IndexedPoint3D intersect = new IndexedPoint3D(a, v.mult(s));

            Edge3D segment = null;
            if (db > Tolerances.smallEps) { // point B outside
                if (da < Tolerances.smallEps) { // point A inside
                    if (a.equals(intersect))
                        return null;

                    segment = e.getSegment(a, intersect);
                    try {
                        segment.setNormal(e.getNormal());
                    } catch (IllegalAccessException e1) {
                        e1.printStackTrace();
                    }
                }
            } else if (db < Tolerances.smallEps) { // point B inside
                if (da > Tolerances.smallEps) { // point A outside
                    if (intersect.equals(b))
                        return null;

                    segment = e.getSegment(intersect, b);
                    try {
                        segment.setNormal(e.getNormal());
                    } catch (IllegalAccessException e1) {
                        e1.printStackTrace();
                    }
                }
            }

            if (inside == null) {
                return segment;
            }

            return inside.getInsideSegment(segment);
        }

        else {
            // type == OUTSIDE
            return null;
        }
    }
}
