package no.uib.cipr.rs.meshgen.lgr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import no.uib.cipr.rs.geometry.Topology;
import no.uib.cipr.rs.meshgen.structured.CartesianTopology;
import no.uib.cipr.rs.meshgen.structured.IJK;
import no.uib.cipr.rs.meshgen.structured.Orientation;

/**
 * This class stores the lgr-topology information
 */
public class LGRTopology extends Topology {

    private static final long serialVersionUID = -7798931812549812094L;

    /**
     * Coarse element topology
     */
    private CartesianTopology coarse;

    /**
     * Map from coarse element ijk-index to refinement topology
     */
    private Map<IJK, CartesianTopology> refinements;

    /**
     * List of refined coarse elements
     */
    private List<IJK> refinedCoarse;

    /**
     * List of coarse edges
     */
    private List<CoarseEdge> coarseEdges;

    /**
     * Creates an LGR topology
     */
    public LGRTopology(CoarseGeometry coarse, Map<IJK, FineGeometry> refinements) {

        this.coarse = coarse.getTopology();

        this.refinements = new HashMap<IJK, CartesianTopology>();

        for (Map.Entry<IJK, FineGeometry> entry : refinements.entrySet()) {
            IJK ijk = entry.getKey();
            FineGeometry fine = entry.getValue();
            this.refinements.put(ijk, fine.getTopology());
        }

        refinedCoarse = new ArrayList<IJK>();
        refinedCoarse.addAll(refinements.keySet());

        coarseEdges = buildInnerCoarseEdges();
    }

    /**
     * Returns true if the coarse element ijk is refined
     */
    boolean isRefinedElement(IJK ijk) {
        return refinedCoarse.contains(ijk);
    }

    /**
     * Returns an iterable object of ijk-indices of the coarse elements that are
     * refined
     */
    Iterable<IJK> getRefinedElementsIJK() {
        return refinedCoarse;
    }

    /**
     * Returns true if the given coarse element has a refined coarse neighbour
     * in the given direction.
     */
    boolean hasRefinedElementNeighbour(IJK ijk, Orientation orientation) {
        if (coarse.hasElementNeighbour(ijk, orientation))
            return isRefinedElement(coarse
                    .getElementNeighbour(ijk, orientation));
        else
            return false;
    }

    IJK getCoarseElementNeighbour(IJK ijk, Orientation orientation) {
        return coarse.getElementNeighbour(ijk, orientation);
    }

    /**
     * Returns an iterable object of ijk-indices of the fine elements in the
     * given coarse element along the given edge location (edge type).
     */
    Iterable<IJK> getFineEdgeElements(IJK ref, EdgeLocation edgeLoc) {
        CartesianTopology fine = refinements.get(ref);

        List<IJK> edgeElements = new ArrayList<IJK>();

        int ni = fine.getNumElementI();
        int nj = fine.getNumElementJ();
        int nk = fine.getNumElementK();

        switch (edgeLoc) {
        case TOPFRONT:
            for (int i = 0; i < ni; i++)
                edgeElements.add(new IJK(i, 0, 0));
            break;
        case TOPBACK:
            for (int i = 0; i < ni; i++)
                edgeElements.add(new IJK(i, nj - 1, 0));
            break;
        case TOPLEFT:
            for (int j = 0; j < nj; j++)
                edgeElements.add(new IJK(0, j, 0));
            break;
        case TOPRIGHT:
            for (int j = 0; j < nj; j++)
                edgeElements.add(new IJK(ni - 1, j, 0));
            break;
        case BOTTOMFRONT:
            for (int i = 0; i < ni; i++)
                edgeElements.add(new IJK(i, 0, nk - 1));
            break;
        case BOTTOMBACK:
            for (int i = 0; i < ni; i++)
                edgeElements.add(new IJK(i, nj - 1, nk - 1));
            break;
        case BOTTOMLEFT:
            for (int j = 0; j < nj; j++)
                edgeElements.add(new IJK(0, j, nk - 1));
            break;
        case BOTTOMRIGHT:
            for (int j = 0; j < nj; j++)
                edgeElements.add(new IJK(ni - 1, j, nk - 1));
            break;
        case FRONTLEFT:
            for (int k = 0; k < nk; k++)
                edgeElements.add(new IJK(0, 0, k));
            break;
        case FRONTRIGHT:
            for (int k = 0; k < nk; k++)
                edgeElements.add(new IJK(ni - 1, 0, k));
            break;
        case BACKLEFT:
            for (int k = 0; k < nk; k++)
                edgeElements.add(new IJK(0, nj - 1, k));
            break;
        case BACKRIGHT:
            for (int k = 0; k < nk; k++)
                edgeElements.add(new IJK(ni - 1, nj - 1, k));
            break;

        default:
            throw new IllegalArgumentException("Invalid edge location type");
        }

        return edgeElements;
    }

    /**
     * Returns an iterable object of element ijk-indices along the given
     * boundary face for the given refinement.
     */
    Iterable<IJK> getFineFaceElements(IJK ref, Orientation orientation) {
        CartesianTopology fine = refinements.get(ref);

        List<IJK> elem = new ArrayList<IJK>();

        int ni = fine.getNumElementI();
        int nj = fine.getNumElementJ();
        int nk = fine.getNumElementK();

        switch (orientation) {
        case TOP:
            for (int i = 0; i < ni; i++)
                for (int j = 0; j < nj; j++)
                    elem.add(new IJK(i, j, 0));
            return elem;
        case BOTTOM:
            for (int i = 0; i < ni; i++)
                for (int j = 0; j < nj; j++)
                    elem.add(new IJK(i, j, nk - 1));
            return elem;
        case FRONT:
            for (int i = 0; i < ni; i++)
                for (int k = 0; k < nk; k++)
                    elem.add(new IJK(i, 0, k));
            return elem;
        case BACK:
            for (int i = 0; i < ni; i++)
                for (int k = 0; k < nk; k++)
                    elem.add(new IJK(i, nj - 1, k));
            return elem;
        case LEFT:
            for (int j = 0; j < nj; j++)
                for (int k = 0; k < nk; k++)
                    elem.add(new IJK(0, j, k));
            return elem;
        case RIGHT:
            for (int j = 0; j < nj; j++)
                for (int k = 0; k < nk; k++)
                    elem.add(new IJK(ni - 1, j, k));
            return elem;
        default:
            throw new IllegalArgumentException("Invalid orientation");
        }
    }

    /**
     * Returns the coarse edges of the lgr mesh
     */
    List<CoarseEdge> getCoarseEdges() {
        return coarseEdges;
    }

    /*
     * Builds the list of inner coarse edges
     */
    private List<CoarseEdge> buildInnerCoarseEdges() {
        List<CoarseEdge> list = new ArrayList<CoarseEdge>();

        int ni = coarse.getNumElementI();
        int nj = coarse.getNumElementJ();
        int nk = coarse.getNumElementK();

        // build i-edges
        for (int i = 0; i < ni; i++)
            for (int j = 0; j < nj + 1; j++)
                for (int k = 0; k < nk + 1; k++)
                    list.add(new CoarseEdge(getCoarseEdgeMapI(i, j, k)));

        // build j-edges
        for (int j = 0; j < nj; j++)
            for (int i = 0; i < ni + 1; i++)
                for (int k = 0; k < nk + 1; k++)
                    list.add(new CoarseEdge(getCoarseEdgeMapJ(i, j, k)));

        // build k-edges
        for (int k = 0; k < nk; k++)
            for (int j = 0; j < nj + 1; j++)
                for (int i = 0; i < ni + 1; i++)
                    list.add(new CoarseEdge(getCoarseEdgeMapK(i, j, k)));

        return list;
    }

    private EdgeMap getCoarseEdgeMapI(int i, int j, int k) {
        EdgeMap map = new EdgeMap(4, coarse);

        map.put(new IJK(i, j - 1, k - 1), EdgeLocation.BOTTOMBACK);
        map.put(new IJK(i, j - 1, k), EdgeLocation.TOPBACK);
        map.put(new IJK(i, j, k - 1), EdgeLocation.BOTTOMFRONT);
        map.put(new IJK(i, j, k), EdgeLocation.TOPFRONT);

        return map;
    }

    private EdgeMap getCoarseEdgeMapJ(int i, int j, int k) {
        EdgeMap map = new EdgeMap(4, coarse);

        map.put(new IJK(i - 1, j, k - 1), EdgeLocation.BOTTOMRIGHT);
        map.put(new IJK(i - 1, j, k), EdgeLocation.TOPRIGHT);
        map.put(new IJK(i, j, k - 1), EdgeLocation.BOTTOMLEFT);
        map.put(new IJK(i, j, k), EdgeLocation.TOPLEFT);

        return map;
    }

    private EdgeMap getCoarseEdgeMapK(int i, int j, int k) {
        EdgeMap map = new EdgeMap(4, coarse);

        map.put(new IJK(i - 1, j - 1, k), EdgeLocation.BACKRIGHT);
        map.put(new IJK(i - 1, j, k), EdgeLocation.FRONTRIGHT);
        map.put(new IJK(i, j - 1, k), EdgeLocation.BACKLEFT);
        map.put(new IJK(i, j, k), EdgeLocation.FRONTLEFT);

        return map;
    }
}
