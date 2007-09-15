package no.uib.cipr.rs.meshgen.eclipse.bsp;

import java.util.List;

/**
 * A binary space parition tree class.
 */
public class BSPTree {

    private BSPNode root;

    /**
     * Creates a binary space partition of a list of non-intersecting edges.
     * 
     * @param edges
     *            A list of objects of type <code>Edge</code>
     */
    public BSPTree(List<Edge> edges) {

        root = new BSPNode(edges);
    }

    /**
     * @return Returns the root node of the tree.
     */
    public final BSPNode getRoot() {
        return root;
    }

    /**
     * @param edge
     * @return Returns the <code>Edge</code> segment of the input edge that is
     *         inside all of the nodes of this BSPTree. If input edge is outside
     *         all edges, null is returned.
     */
    public Edge getInsideSegment(Edge edge) {
        return root.getInsideSegment(edge);
    }
}
