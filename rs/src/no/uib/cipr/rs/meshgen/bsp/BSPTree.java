package no.uib.cipr.rs.meshgen.bsp;

import java.util.List;

/**
 * A binary space parition tree class.
 */
public class BSPTree {

    private BSPNode root;

    /**
     * Creates a binary space partition from a given list of non-intersecting
     * edges.
     */
    public BSPTree(List<Edge3D> edges) {
        root = new BSPNode(edges);
    }

    /**
     * Returns the root node of the tree.
     */
    public final BSPNode getRoot() {
        return root;
    }

    /**
     * Returns the edge segment of the given edge that is inside all of the
     * nodes of this BSPTree. If input edge is outside all edges, null is
     * returned.
     */
    public Edge3D getInsideSegment(Edge3D edge) {
        return root.getInsideSegment(edge);
    }
}
