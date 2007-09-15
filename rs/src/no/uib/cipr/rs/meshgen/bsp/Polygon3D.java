package no.uib.cipr.rs.meshgen.bsp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import no.uib.cipr.rs.geometry.Vector3D;
import no.uib.cipr.rs.meshgen.util.IndexedPoint3D;

/**
 * A 3D Polygon class that uses a BSP tree representation. This provides a
 * simple implementation of the <code>getPartition()</code> method.
 */
public class Polygon3D {

    private List<Edge3D> edges;

    BSPTree bspTree;

    private boolean isReversed;

    /**
     * Creates a polygon given a list of edges. There must be at least three
     * edges given in counterclockwise order. If the edges are not connected an
     * exception is thrown.
     * 
     * @param edges
     *            A list of edges that defines the polygon.
     * @throws IllegalArgumentException
     */
    public Polygon3D(List<Edge3D> edges) throws IllegalArgumentException {
        if (edges.size() < 3)
            throw new IllegalArgumentException(
                    "Polygon must have at least three edges");

        // Create new edges but with a copy of the given point references
        this.edges = new ArrayList<Edge3D>(edges.size());
        for (Edge3D e : edges) {
            this.edges.add(new Edge3D(e.vertices()));
        }

        checkEdgeList();

        isReversed = false;

        computeNormals();

        bspTree = new BSPTree(new ArrayList<Edge3D>(this.edges));
    }

    /**
     * Check proper ordering and connectedness of edge list.
     */
    private void checkEdgeList() {
        for (int i = 0, numEdges = edges.size(); i < numEdges; i++) {
            if (!edges.get(i).getEndPoint().equals(
                    edges.get((i + 1) % numEdges).getBeginPoint())) {
                throw new IllegalArgumentException(
                        "Polygon edges must be ordered and connected");
            }
        }
    }

    /**
     * compute the polygon normal and normals of each edge
     */
    private void computeNormals() {
        // compute polygon normal, assuming counterclockwise ordered edges.
        Edge3D e1 = edges.get(0);
        Vector3D u = new Vector3D(e1.getBeginPoint(), e1.getEndPoint());

        Edge3D e2 = edges.get(1);
        Vector3D v = new Vector3D(e2.getBeginPoint(), e2.getEndPoint());

        Vector3D normal = u.cross(v);

        // TODO implement check that all edges are coplanar

        for (Edge3D e : edges) {
            Vector3D p1p2 = new Vector3D(e.getBeginPoint(), e.getEndPoint());
            Vector3D temp = p1p2.cross(normal);
            e.setNormal(temp.mult(1 / temp.norm2()));
        }
    }

    private List<Edge3D> getSortedEdges(List<Edge3D> unsorted) {
        List<Edge3D> sorted = new ArrayList<Edge3D>();

        boolean connected;

        int i = 0;

        Edge3D current = unsorted.remove(i);
        sorted.add(current);

        // TODO the loop might need an abort-value to avoid infinite loops.
        while (unsorted.size() > 0) {
            Edge3D next = unsorted.get(i);

            connected = current.getEndPoint().equals(next.getBeginPoint());

            if (connected) {
                current = unsorted.remove(i);
                sorted.add(current);
            }

            if (unsorted.size() == 1 && !connected)
                throw new IllegalArgumentException(
                        "Edge list must form a closed polygon");

            // increment counter
            if (unsorted.size() > 0)
                i = (i + 1) % unsorted.size();
        }

        int first = 0;
        int last = sorted.size() - 1;

        connected = sorted.get(first).getBeginPoint().equals(
                sorted.get(last).getEndPoint());

        if (!connected)
            throw new IllegalArgumentException(
                    "Edge list must form a closed polygon.");

        return sorted;
    }

    /**
     * Returns an iterable object of the edges of this polygon.
     */
    public Iterable<Edge3D> getEdges() {
        return Collections.unmodifiableList(edges);
    }

    /**
     * Returns the segment of the given edge that is inside this polygon.
     * 
     * Null is returned if no part of the edge is inside.
     */
    public Edge3D getInsideSegment(Edge3D e) {
        return bspTree.getInsideSegment(e);
    }

    /**
     * Returns the resulting polygon from intersecting this polygon with the
     * given input polygon.
     * 
     * Null is returned if the intersection contains less than three edges and
     * consequently represents a degenerate polygon.
     */
    public Polygon3D getIntersection(Polygon3D q) {
        List<Edge3D> intersectEdges = new ArrayList<Edge3D>();

        for (Edge3D e : this.getEdges()) {
            Edge3D segment = q.getInsideSegment(e);

            if (segment != null && !intersectEdges.contains(segment))
                intersectEdges.add(segment);
        }

        for (Edge3D e : q.getEdges()) {
            Edge3D segment = this.getInsideSegment(e);

            if (segment != null && !intersectEdges.contains(segment))
                intersectEdges.add(segment);
        }

        // check if too few edges where found
        if (intersectEdges.size() < 3) {
            if (intersectEdges.size() == 2) { // store point of intersection
                // for each of the two segments
                for (Edge3D ei : intersectEdges)
                    System.out.println("\tEdge: " + ei.toString());
            }

            return null;
        }

        List<Edge3D> sortedEdges = getSortedEdges(intersectEdges);

        Polygon3D intersectPQ = new Polygon3D(sortedEdges);

        return intersectPQ;
    }

    /**
     * Returns a list of the points of this polygon. This does not duplicate
     * edge begin and end points.
     */
    public List<IndexedPoint3D> getPoints() {
        List<IndexedPoint3D> points = new ArrayList<IndexedPoint3D>();
        for (Edge3D e : edges) {
            points.addAll(e.getPoints());
        }
        return points;
    }

    /**
     * Returns the begin points of the edges of this polygon.
     */
    public List<IndexedPoint3D> getBeginPoints() {
        List<IndexedPoint3D> list = new ArrayList<IndexedPoint3D>(4);

        for (Edge3D e : edges)
            list.add(e.getBeginPoint());

        return list;
    }

    /**
     * Reverses the edges and edge points.
     */
    public void reverse() {
        isReversed = isReversed ? false : true;

        // reverse edge order
        Collections.reverse(edges);

        // reverse edge point order
        for (Edge3D e : edges)
            e.reverse();
    }

    /**
     * Returns true if the polygon has been reversed.
     */
    public boolean isReversed() {
        return isReversed;
    }

    @Override
    public String toString() {
        String s = "Polygon: " + edges.size() + " edges";

        for (Edge3D e : edges)
            s += "\n\t" + e.toString();

        return s;
    }
}
