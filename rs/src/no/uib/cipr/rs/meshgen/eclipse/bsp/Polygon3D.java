package no.uib.cipr.rs.meshgen.eclipse.bsp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import no.uib.cipr.rs.geometry.Point3D;
import no.uib.cipr.rs.geometry.Vector3D;
import no.uib.cipr.rs.meshgen.eclipse.geometry.CornerPoint3D;

/**
 * A 3D Polygon class that uses a BSP tree representation. This provides a
 * simple implementation of the <code>getPartition()</code> method.
 */
public class Polygon3D implements Polygon {

    private List<Edge> edges;

    private int numVertices;

    BSPTree bspTree;

    private double area;

    private Point3D[] vertices;

    /**
     * Builds a polygon given an array of vertex points. The last point is
     * connected to the first point.
     * 
     * @param vertices
     *            An array of vertices. Assumed to be counterclockwise ordered
     *            and containing at least three elements.
     * @throws IllegalArgumentException
     */
    public Polygon3D(CornerPoint3D[] vertices) throws IllegalArgumentException {

        if (vertices.length < 3)
            throw new IllegalArgumentException(
                    "At least three vertices are required");

        this.vertices = vertices;

        numVertices = vertices.length;

        edges = buildEdgeList(vertices);

        checkEdgeList();

        // compute and set the normals of each edge
        computeNormals();

        area = calculateArea();

        bspTree = new BSPTree(new ArrayList<Edge>(edges));

    }

    /**
     * Creates a polygon from a list of edges. There must be at least three
     * edges given in counterclockwise order. If the edges are not connected an
     * exception is thrown.
     * 
     * @param edges
     *            A list of edges that defines the polygon.
     * @throws IllegalArgumentException
     */
    public Polygon3D(List<Edge> edges) throws IllegalArgumentException {

        if (edges.size() < 3)
            throw new IllegalArgumentException(
                    "Polygon must have at least three edges");

        this.edges = edges;

        checkEdgeList();

        vertices = buildVertices(edges);

        numVertices = vertices.length;

        computeNormals();

        area = calculateArea();

        bspTree = new BSPTree(new ArrayList<Edge>(edges));
    }

    private Point3D[] buildVertices(List<Edge> el) {
        Point3D[] v = new Point3D[el.size()];
        int i = 0;

        for (Edge e : el)
            v[i++] = e.getBeginPoint();

        return v;
    }

    /**
     * @param vl
     *            Array of edge vertices
     * @return Returns a list of edges.
     */
    private List<Edge> buildEdgeList(CornerPoint3D[] vl) {

        List<Edge> edgeList = new ArrayList<Edge>();

        for (int i = 0; i < numVertices; i++) {
            CornerPoint3D p1 = vl[i];
            CornerPoint3D p2 = vl[(i + 1) % numVertices];

            Edge3D nextEdge = new Edge3D(p1, p2);
            edgeList.add(nextEdge);

        }

        return edgeList;

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
        Edge e1 = edges.get(0);
        Vector3D u = new Vector3D(e1.getBeginPoint(), e1.getEndPoint());

        Edge e2 = edges.get(1);
        Vector3D v = new Vector3D(e2.getBeginPoint(), e2.getEndPoint());

        Vector3D normal = u.cross(v);

        // TODO implement check that all edges are coplanar

        for (Edge e : edges) {
            Vector3D p1p2 = new Vector3D(e.getBeginPoint(), e.getEndPoint());
            Vector3D temp = p1p2.cross(normal);
            e.setNormal(temp.mult(1 / temp.norm2()));
        }
    }

    /**
     * Computes the area of this simple convex polygon by summing contributions
     * from sub-triangles.
     * 
     * @return The area of the polygon
     */
    private double calculateArea() {

        double a = 0.0;

        CornerPoint3D p0 = edges.get(0).getBeginPoint();

        for (int i = 1; i < edges.size() - 1; i++) {
            CornerPoint3D p1 = edges.get(i).getBeginPoint();
            CornerPoint3D p2 = edges.get(i + 1).getBeginPoint();

            Vector3D v1 = new Vector3D(p0, p1);
            Vector3D v2 = new Vector3D(p0, p2);

            a += 0.5 * v1.cross(v2).norm2();
        }

        return a;
    }

    private List<Edge> getSortedEdges(List<Edge> unsorted) {
        List<Edge> sorted = new ArrayList<Edge>();
    
        boolean connected;
    
        int i = 0;
    
        Edge current = unsorted.remove(i);
        sorted.add(current);
    
        while (unsorted.size() > 0) {
            Edge next = unsorted.get(i);
    
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

    /*
     * (non-Javadoc)
     * 
     * @see bsp.Polygon#getArea()
     */
    public double getArea() {
        return area;
    }

    /*
     * (non-Javadoc)
     * 
     * @see bsp.Polygon#getEdges()
     */
    public Iterable<Edge> getEdges() {
        return Collections.unmodifiableList(edges);
    }

    /*
     * (non-Javadoc)
     * 
     * @see bsp.Polygon#getIntersection(bsp.Polygon)
     */
    public Polygon getIntersection(Polygon q) {

        List<Edge> intersectEdges = new ArrayList<Edge>();

        for (Edge e : this.getEdges()) {

            Edge segment = q.getInsideSegment(e);

            if (segment != null && !intersectEdges.contains(segment)) {
                intersectEdges.add(segment);
            }
        }

        for (Edge e : q.getEdges()) {
            Edge segment = this.getInsideSegment(e);

            if (segment != null && !intersectEdges.contains(segment)) {
                intersectEdges.add(segment);
            }
        }

        // check if too few edges where found
        if (intersectEdges.size() < 3) {
            return null;
        }

        List<Edge> sortedEdges = getSortedEdges(intersectEdges);

        Polygon intersectPQ = new Polygon3D(sortedEdges);

        return intersectPQ;
    }

    public Edge getInsideSegment(Edge e) {

        Edge segment = bspTree.getInsideSegment(e);

        return segment;
    }

    public List<Point3D> vertices() {
        return Arrays.asList(vertices);
    }

    @Override
    public String toString() {

        String s = new String("Polygon: " + edges.size() + " edges, area = "
                + getArea());

        for (Edge e : edges) {
            s += "\n\t" + e.toString();
        }

        return s;
    }
}
