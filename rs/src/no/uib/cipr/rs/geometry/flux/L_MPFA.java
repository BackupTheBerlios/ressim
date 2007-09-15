package no.uib.cipr.rs.geometry.flux;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import no.uib.cipr.matrix.DenseMatrix;
import no.uib.cipr.matrix.Matrices;
import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.MatrixSingularException;
import no.uib.cipr.rs.geometry.Element;
import no.uib.cipr.rs.geometry.Interface;
import no.uib.cipr.rs.geometry.Mesh;
import no.uib.cipr.rs.geometry.NeighbourConnection;
import no.uib.cipr.rs.geometry.CornerPoint;
import no.uib.cipr.rs.geometry.Point3D;
import no.uib.cipr.rs.geometry.Tensor3D;
import no.uib.cipr.rs.geometry.Vector3D;
import no.uib.cipr.rs.util.Configuration;
import no.uib.cipr.rs.util.Tolerances;

/**
 * MPFA L-method for originally non-conforming meshes (our meshes are always
 * converted into topologically conforming meshes, i.e. no hanging nodes).
 * 
 * TODO remove and replace with L4_MPFA
 */
public class L_MPFA extends MPFA {

    private final boolean test;

    private final boolean testAreas;

    private final boolean quadArea;

    public L_MPFA(Configuration config) {
        test = config.getBoolean("Test", false);

        testAreas = config.getBoolean("TestAreas", false);

        quadArea = config.getBoolean("QuadArea", false);

        System.out.println(config.trace() + "Using the L-method");
    }

    @Override
    public List<? extends Collection<Transmissibility>> calculateTransmissibilities(
            Mesh mesh, Conductivity K) {
        this.mesh = mesh;

        if (testAreas)
            testSubfaceAreas();

        List<List<Transmissibility>> M = allocateM();

        for (CornerPoint p : mesh.points())
            if (pointOnBoundary(p))
                calculateBoundaryTransmissibilities(p, K, M);
            else
                calculateLTransmissibilities(p, K, M);

        if (test)
            testUniformFlow(M);

        return M;
    }

    /**
     * Calculates sub-face transmissibilities for connections located along the
     * boundary of the mesh. Two-point flux approximation is used.
     */
    private void calculateBoundaryTransmissibilities(CornerPoint p,
            Conductivity K, List<List<Transmissibility>> M) {
        // loop over all connections associated with this boundary point
        for (NeighbourConnection c : connections(p)) {
            Interface is = mesh.hereInterface(c);
            Interface js = mesh.thereInterface(c);

            Element ei = mesh.element(is);
            Element ej = mesh.element(js);

            Tensor3D Ki = K.getConductivity(ei);
            Tensor3D Kj = K.getConductivity(ej);

            // Compute || K*n ||
            double Kin = Ki.multNorm(is.normal);
            double Kjn = Kj.multNorm(js.normal);

            double A = computeSubFaceArea(is, p);

            // compute the transmissibility coefficient
            double t = 0;
            if (Kin != 0 && Kjn != 0)
                t = Kin * Kjn * A
                        / (Kjn * distance(ei, is) + Kjn * distance(ej, js));

            // store the coefficient
            List<Transmissibility> Mi = M.get(c.index);

            boolean hereFound = false, thereFound = false;
            Element here = mesh.here(c), there = mesh.there(c);
            for (Transmissibility tp : Mi) {
                if (mesh.element(tp) == here) {
                    hereFound = true;
                    tp.add(t);
                }
                if (mesh.element(tp) == there) {
                    thereFound = true;
                    tp.add(-t);
                }
            }

            // transmissibility not found, create it
            if (!hereFound && t != 0)
                Mi.add(new Transmissibility(here.index, t));

            if (!thereFound && t != 0)
                Mi.add(new Transmissibility(there.index, -t));
        }
    }

    /**
     * Calculate the transmissibilities for L-method interactions.
     */
    private void calculateLTransmissibilities(CornerPoint p, Conductivity K,
            List<List<Transmissibility>> M) {
        InteractionRegions regions = new InteractionRegions(p);

        for (NeighbourConnection c : connections(p)) {
            // get list of candidate L-method interaction regions for the
            // current connection
            List<LmethodInteractionRegion> candidateRegions = regions
                    .getLmethodInteractionRegions(c);

            // map relating interaction regions to computed transmissibilities.
            Map<Matrix, LmethodInteractionRegion> candidateT = new HashMap<Matrix, LmethodInteractionRegion>(
                    candidateRegions.size());

            // calculate the T-matrix of transmissibilities for each L-method
            // interaction region
            for (LmethodInteractionRegion region : candidateRegions) {
                Matrix T = region.calculateT(K);
                candidateT.put(T, region);
            }

            Entry<Matrix, LmethodInteractionRegion> chosenT = chooseTransmissibilities(
                    candidateT, c);

            // check row sums
            if (test)
                checkRowSums(chosenT.getKey());

            // Store the computed transmissibilities for the current connection
            storeLTransmissibilities(chosenT.getKey(), chosenT.getValue(), c, M
                    .get(c.index));
        }
    }

    /**
     * Chooses the transmissibility matrix from the given candidates based on
     * criteria given in paper...
     * 
     * @param c
     */
    private Entry<Matrix, LmethodInteractionRegion> chooseTransmissibilities(
            Map<Matrix, LmethodInteractionRegion> candidateT,
            NeighbourConnection c) {
        List<Entry<Matrix, LmethodInteractionRegion>> candidates = new ArrayList<Entry<Matrix, LmethodInteractionRegion>>(
                candidateT.entrySet());

        // if only one candidate, return it
        if (candidates.size() == 1)
            return candidates.get(0);

        if (candidates.size() != 2)
            throw new IllegalArgumentException(
                    "The number of candidate transmissibilities must be 1 or 2");

        // now we have two candidate transmissibility-interaction region pairs
        // and are ready to compare the elements of the two transmsissibility
        // matrices

        // extracting transmissibility coefficient associated with the primary
        // element
        Matrix T1 = candidates.get(0).getKey();
        LmethodInteractionRegion L1 = candidates.get(0).getValue();
        // pick out the current sub-face
        int i1 = L1.index(mesh.hereInterface(c));
        // pick out leading transmissibility coefficient
        int j1 = L1.index(L1.getPrimaryElement());
        double t1 = T1.get(i1, j1);

        Matrix T2 = candidates.get(1).getKey();
        LmethodInteractionRegion L2 = candidates.get(1).getValue();
        int i2 = L2.index(mesh.hereInterface(c));
        int j2 = L2.index(L2.getPrimaryElement());
        double t2 = T2.get(i2, j2);

        if (Math.abs(t1) < Math.abs(t2))
            return candidates.get(0);
        else
            return candidates.get(1);

    }

    /**
     * Stores the transmissibilities associated with the flux across the given
     * connection for the L-method
     */
    void storeLTransmissibilities(Matrix T, LmethodInteractionRegion region,
            NeighbourConnection c, List<Transmissibility> Mi) {
        int i = region.index(mesh.hereInterface(c));

        for (Element el : region.elements()) {
            int j = region.index(el);

            double tk = T.get(i, j);

            if (region.getPrimaryElement() == mesh.here(c))
                tk *= -1;

            // If the transmissibility is already in the list, just add
            boolean found = false;
            for (Transmissibility tp : Mi)
                if (mesh.element(tp) == el) {
                    found = true;
                    tp.add(tk);
                    break;
                }

            // Not there, add a new transmissiblity unless it is zero.
            // In the L-method, the transmissibility coefficients may be zero
            // for here and there elements, but these are required by Connection
            // and are thus stored.
            boolean hereThere = el == mesh.here(c) || el == mesh.there(c);
            if (!found && (tk != 0 || hereThere))
                Mi.add(new Transmissibility(el.index, tk));
        }
    }

    /**
     * Computes the subface area located in the given interface centered around
     * the given point.
     * 
     * It is assumed that the points of the interface are circularly ordered.
     */
    protected double computeSubFaceArea(Interface intf, CornerPoint p) {
        // Count the number of cornerpoints for the given interface
        int num = 0;
        for (Iterator<CornerPoint> it = mesh.points(intf).iterator(); it
                .hasNext(); it.next())
            num++;

        // Hardcoding use of bilinear face representation
        if (quadArea)
            return intf.area / num;

        // Get half-edge points
        List<Point3D> pts = getHalfEdgePoints(p, intf);

        if (pts.size() != 2)
            throw new IllegalArgumentException(
                    "Two half-edge intersection points should exist");

        Point3D a = intf.center;
        Point3D b = p.coordinate;
        Point3D c = pts.get(0);
        Point3D d = pts.get(1);

        Vector3D ab = new Vector3D(a, b);
        Vector3D ac = new Vector3D(a, c);
        Vector3D ad = new Vector3D(a, d);

        double A1 = 0.5 * ab.cross(ac).norm2();
        double A2 = 0.5 * ab.cross(ad).norm2();

        return A1 + A2;
    }

    /**
     * Test if subface areas add up to total face area.
     */
    private void testSubfaceAreas() {
        for (Interface intf : mesh.interfaces()) {
            double areaWhole = intf.area;

            // compute subface areas for the subfaces defined by the points of
            // this interface
            double areaSub = 0.0;
            for (CornerPoint p : mesh.points(intf))
                areaSub += computeSubFaceArea(intf, p);

            if (Math.abs(areaWhole - areaSub) > Tolerances.smallEps)
                throw new IllegalArgumentException(
                        "Subface areas does not add up to face area for interface "
                                + (intf.index + 1));
        }
    }

    private void checkRowSums(Matrix T) {
        for (int i = 0; i < T.numRows(); i++) {
            double t = rowSum(i, T);

            if (Math.abs(t) > Tolerances.smallEps)
                throw new IllegalArgumentException(
                        "Row sum should be zero, row " + i);
        }
    }

    private double rowSum(int i, Matrix T) {
        double t = 0.0;

        for (int j = 0; j < T.numColumns(); j++)
            t += T.get(i, j);

        return t;
    }

    /**
     * Wrapper class performing topological operations and creating the
     * candidate L-method interaction regions. The interaction regions are built
     * from the given point.
     */
    private class InteractionRegions {

        private Map<Element, List<Interface>> elementInterfaces;

        List<LmethodInteractionRegion> candidateRegions;

        // point shared by the interaction regions
        private CornerPoint p;

        public InteractionRegions(CornerPoint p) {
            this.p = p;

            // compute the element connections for the given point
            elementInterfaces = getElementInterfaces(p);

            candidateRegions = new ArrayList<LmethodInteractionRegion>();
        }

        /**
         * Computes a map containing elements associated with a point as keys.
         * The values are the list of corresponding interfaces associated with
         * each key. The interfaces are in the set of interfaces sharing the
         * point.
         */
        private Map<Element, List<Interface>> getElementInterfaces(CornerPoint p) {
            Map<Element, List<Interface>> map = new HashMap<Element, List<Interface>>();

            for (Element e : mesh.elements(p))
                map.put(e, new ArrayList<Interface>());

            for (Interface intf : mesh.interfaces(p))
                map.get(mesh.element(intf)).add(intf);

            return map;
        }

        public List<LmethodInteractionRegion> getLmethodInteractionRegions(
                NeighbourConnection c) {
            List<LmethodInteractionRegion> regions = new ArrayList<LmethodInteractionRegion>();

            // determine primary element(s) and build corresponding interaction
            // regions
            Element here = mesh.here(c);
            Element there = mesh.there(c);

            int nuHere = elementInterfaces.get(here).size();
            int nuThere = elementInterfaces.get(there).size();

            if (nuHere == 3 && nuThere == 3) {
                // both here and there elements are candidate primary elements
                regions.add(new LmethodInteractionRegion(p, here,
                        elementInterfaces));
                regions.add(new LmethodInteractionRegion(p, there,
                        elementInterfaces));
            } else if (nuHere == 3 && nuThere != 3) {
                // here element is primary element
                regions.add(new LmethodInteractionRegion(p, here,
                        elementInterfaces));
            } else if (nuHere != 3 && nuThere == 3) {
                // there element is primary element
                regions.add(new LmethodInteractionRegion(p, there,
                        elementInterfaces));
            } else
                throw new IllegalArgumentException(
                        "Illegal local vertex degree for point " + p.index);

            return regions;
        }
    }

    /**
     * Class representing a linear function in 3D. The function is on the form
     * 
     * psi(x,y,z)=c1 + c2*x + c3*y + c4*z.
     * 
     * grad(psi)=[c2, c3, c4] is also computed.
     */
    private class BasisFunction3D {

        // linear function coefficients
        private double c1, c2, c3, c4;

        public BasisFunction3D(double c1, double c2, double c3, double c4) {
            this.c1 = c1;
            this.c2 = c2;
            this.c3 = c3;
            this.c4 = c4;
        }

        /**
         * Returns the function value of this linear function evaluated in the
         * given point.
         * 
         */
        public double psi(Point3D p) {
            return psi(p.x(), p.y(), p.z());
        }

        /**
         * Returns the function value of this linear function evaluated in the
         * point with the given x-, y- and z-coordinates.
         * 
         */
        public double psi(double x, double y, double z) {
            return c1 + c2 * x + c3 * y + c4 * z;
        }

        /**
         * Returns the gradient vector of this basis function.
         */
        public Vector3D gradient() {
            return new Vector3D(c2, c3, c4);
        }

    }

    /**
     * Interaction region for the MPFA L-method in the case of points with local
     * vertex degree larger than 3 for one element of the interaction region.
     */
    private class LmethodInteractionRegion implements InteractionRegion {

        /**
         * Local element indexing
         */
        private final Map<Element, Integer> elMap;

        /**
         * Interfaces for each element
         */
        private final Map<Element, List<Interface>> elIntfMap;

        /**
         * Local interface indexing
         */
        private final Map<Interface, Integer> intfMap;

        /**
         * The number of connections for this interaction region
         */
        private int numConnections;

        /**
         * The number of elements for this interaction region
         */
        private int numElements;

        private final CornerPoint p;

        /**
         * The primary element
         */
        private final Element primary;

        /**
         * List of secondary elements. These are the elements that are coupled
         * to the primary element
         */
        private final List<Element> secondaries;

        /**
         * Constructs an interaction region for the L-method centered at the
         * given point with given map from elements to interfaces (constructed
         * based on a current connection). Not valid for points located on the
         * boundary of the domain.
         */
        public LmethodInteractionRegion(CornerPoint p, Element primary,
                Map<Element, List<Interface>> elIntfMapFull) {
            this.p = p;
            this.primary = primary;

            elIntfMap = new HashMap<Element, List<Interface>>();

            // store the primary element interfaces
            elIntfMap.put(primary, elIntfMapFull.get(primary));

            // set up secondary elements
            secondaries = new ArrayList<Element>();

            for (Interface intf : elIntfMapFull.get(primary)) {
                Interface jntf = getNeighbour(intf);
                Element secondary = mesh.element(jntf);

                if (!elIntfMapFull.containsKey(secondary))
                    throw new IllegalArgumentException(
                            "Secondary element must be in preliminary interaction region");

                secondaries.add(secondary);

                elIntfMap.put(secondary, Arrays
                        .asList(new Interface[] { jntf }));
            }

            // set up local element indexing
            numElements = 0;

            elMap = new HashMap<Element, Integer>();

            elMap.put(primary, numElements++);
            for (Element secondary : secondaries)
                elMap.put(secondary, numElements++);

            // set up local interface indexing
            numConnections = 0;

            intfMap = new HashMap<Interface, Integer>();
            for (Interface intf : elIntfMapFull.get(primary)) {
                intfMap.put(intf, numConnections);
                intfMap.put(getNeighbour(intf), numConnections);

                numConnections++;
            }

        }

        /**
         * Returns the interaction region point
         */
        public CornerPoint point() {
            return p;
        }

        /**
         * Returns a list of the secondary elements of this interaction region
         */
        public List<Element> getSecondaryElements() {
            return secondaries;
        }

        public Element getPrimaryElement() {
            return primary;
        }

        public Iterable<Element> elements() {
            return elMap.keySet();
        }

        public int index(Element el) {
            return elMap.get(el);
        }

        public int index(Interface intf) {
            return intfMap.get(intf);
        }

        public List<Interface> interfaces(Element el) {
            return elIntfMap.get(el);
        }

        public int numConnections() {
            return numConnections;
        }

        public int numElements() {
            return numElements;
        }

        /**
         * Calculates the T-matrix for an L-method interaction region.
         */
        public Matrix calculateT(Conductivity K) {
            // number of connections in the interaction region (corresponds to
            // number of flux continuity potentials).
            int numConnections = numConnections();

            // number of interacting elements in the interaction region
            // (corresponds
            // to the number of cell center potentials)
            int numElements = numElements();

            // Coefficient matrix for the midpoint potentials from the flux
            // continuity conditions.
            Matrix A = new DenseMatrix(numConnections, numConnections);

            // Coefficient matrix for the cell potentials from the flux
            // continuity
            // conditions
            Matrix B = new DenseMatrix(numConnections, numElements);

            // Coefficient matrix for the flux continuity potentials for the
            // flux
            // from here to there
            Matrix C = A.copy();

            // Coefficient matrix for the cell potentials for the flux from here
            // to
            // there
            Matrix D = B.copy();

            BasisFunction3D[] psiPrimary = null;
            Element elemPrimary = null;

            // Assemble primary element contribution
            {
                Element el = getPrimaryElement();
                List<Interface> interIntf = interfaces(el);

                // calculate basis functions for primary element
                BasisFunction3D[] psi = calculatePrimaryBasisFunctions3D(el,
                        interIntf);

                // store primary element for secondary element computations
                psiPrimary = psi;
                elemPrimary = el;

                Vector3D[] Kn = calculateLocalConductivityDirections(K
                        .getConductivity(el), p, interIntf);

                assemblePrimaryElementContribution(A, B, Kn, psi, el);
            }

            // Assemble secondary element contributions
            for (Element el : getSecondaryElements()) {
                List<Interface> interIntf = interfaces(el);
                if (interIntf.size() != 1)
                    throw new IllegalArgumentException(
                            "Only 1 interface allowed for a secondary element");

                // calculate basis functions for primary element
                BasisFunction3D[] psi = calculateSecondaryBasisFunction3D(p,
                        el, interIntf.get(0));

                Vector3D[] Kn = calculateLocalConductivityDirections(K
                        .getConductivity(el), p, interIntf);
                if (Kn.length != 1)
                    throw new IllegalArgumentException(
                            "Only 1 permeability direction allowed for a secondary element");

                assembleSecondaryElementContribution(A, B, C, D, Kn, psi,
                        psiPrimary, elemPrimary, el);
            }

            // Compute T
            return calculateTmatrix(A, B, C, D);
        }

        /**
         * Calculates the linear basis functions in 3D on a given
         * "secondary"-element relative to a point and a given primary
         * interface.
         * 
         * Every basis function is on the form psi(x,y,z) = c1 + c2*x + c3*y +
         * c4*z.
         * 
         * The method returns an array of basis functions such that psi[0] is
         * the basis function defined as 1 in the cell center, psi[1] is the
         * basis function defined as 1 in the flux continuity point, while
         * psi[2] and psi[3] represents the basis functions defined as 1 in the
         * respective full potential continuity points.
         */
        private BasisFunction3D[] calculateSecondaryBasisFunction3D(
                CornerPoint p, Element el, Interface intf) {
            int n = 4;

            Matrix A = new DenseMatrix(n, n);
            Matrix Psi = new DenseMatrix(n, n);
            Matrix I = Matrices.identity(n);

            Point3D elp = el.center;

            // single basis function defined as 1 in the element centre
            A.set(0, 0, 1);
            A.set(0, 1, elp.x());
            A.set(0, 2, elp.y());
            A.set(0, 3, elp.z());

            // single basis function defined as 1 in the flux continuity point,
            // i.e.
            // interface center.
            Point3D cp = intf.center;
            A.set(1, 0, 1);
            A.set(1, 1, cp.x());
            A.set(1, 2, cp.y());
            A.set(1, 3, cp.z());

            // single basis function defined as 1 in the given interaction
            // region
            // point
            Point3D ip = p.coordinate;
            A.set(2, 0, 1);
            A.set(2, 1, ip.x());
            A.set(2, 2, ip.y());
            A.set(2, 3, ip.z());

            // single basis function defined as 1 in one half-edge point.
            Point3D ep = getHalfEdgePoints(p, intf).get(0);
            A.set(3, 0, 1);
            A.set(3, 1, ep.x());
            A.set(3, 2, ep.y());
            A.set(3, 3, ep.z());

            // solve for the basis functions
            try {
                A.solve(I, Psi);
            } catch (MatrixSingularException e) {
                throw new IllegalArgumentException(
                        "Error computing basis functions for element "
                                + el.index);
            }

            BasisFunction3D[] psi = new BasisFunction3D[n];
            for (int i = 0; i < n; i++) {
                double c1 = Psi.get(0, i);
                double c2 = Psi.get(1, i);
                double c3 = Psi.get(2, i);
                double c4 = Psi.get(3, i);

                psi[i] = new BasisFunction3D(c1, c2, c3, c4);
            }

            return psi;
        }

        /**
         * Calculates the linear basis functions in 3D on a given
         * "primary"-element relative to a given list of interacting interfaces.
         * The method returns a vector of BasisFunction3D objects which allows
         * both evaluation of function value given an arbitrary point and
         * computation of the corresponding gradient vector.
         * 
         * Every basis function is on the form psi(x,y,z) = c1 + c2*x + c3*y +
         * c4*z.
         * 
         * The method returns an array of basis functions such that psi[0]
         * represents basis function defined as 1 in the cell center, while
         * psi[1], psi[2] and psi[3] represents the basis functions defined as 1
         * in the respective flux continuity points (face midpoints).
         */
        private BasisFunction3D[] calculatePrimaryBasisFunctions3D(Element el,
                List<Interface> interIntf) {
            int n = 4;

            Matrix A = new DenseMatrix(n, n);
            Matrix Psi = new DenseMatrix(n, n);
            Matrix I = Matrices.identity(n);

            Point3D elp = el.center;

            // single basis function defined as 1 in the element centre
            A.set(0, 0, 1);
            A.set(0, 1, elp.x());
            A.set(0, 2, elp.y());
            A.set(0, 3, elp.z());

            // three basis functions defined as 1 in the interface continuity
            // points
            for (int i = 0; i < interIntf.size(); i++) {
                Interface intf = interIntf.get(i);
                Point3D cp = intf.center;

                A.set(i + 1, 0, 1);
                A.set(i + 1, 1, cp.x());
                A.set(i + 1, 2, cp.y());
                A.set(i + 1, 3, cp.z());
            }

            // solve for the basis functions
            try {
                A.solve(I, Psi);
            } catch (MatrixSingularException e) {
                throw new IllegalArgumentException(
                        "Error computing basis functions for element "
                                + el.index);
            }

            BasisFunction3D[] psi = new BasisFunction3D[n];
            for (int i = 0; i < n; i++) {
                double c1 = Psi.get(0, i);
                double c2 = Psi.get(1, i);
                double c3 = Psi.get(2, i);
                double c4 = Psi.get(3, i);

                psi[i] = new BasisFunction3D(c1, c2, c3, c4);
            }

            return psi;
        }

        /**
         * Assembles the element contribution from the primary element. This is
         * similar to the O-method in that only cell center- and flux continuity
         * potentials are involved. The difference is that the primary element
         * only contributes to the flux continuity expression, i.e. matrices A
         * and B. That is, this element does not directly contribute to the flux
         * expression, i.e. coefficients of matrices C and D.
         */
        private void assemblePrimaryElementContribution(Matrix A, Matrix B,
                Vector3D[] Kn, BasisFunction3D[] psi, Element el) {
            List<Interface> interIntf = interfaces(el);

            if (interIntf.size() != 3)
                throw new IllegalArgumentException(
                        "Primary element contribution assembly only valid for three connections");

            for (int i = 0; i < interIntf.size(); i++) { // for each flux
                // f_j,1,
                // f_j,2, f_j,3
                // Cell centered potentials
                double f = Kn[i].dot(psi[0].gradient());
                Interface intf = interIntf.get(i);
                int is = index(intf);
                int ei = index(el);

                // add to the flux continuity expression
                B.add(is, ei, -f);

                // Midpoint potentials
                for (int j = 1; j < interIntf.size() + 1; j++) {
                    f = Kn[i].dot(psi[j].gradient());
                    Interface jntf = interIntf.get(j - 1);
                    int js = index(jntf);

                    // Add to the flux continuity expression
                    A.add(is, js, f);
                }
            }
        }

        /**
         * Assembles the element contribution from a secondary element. The
         * secondary elements contribute to all MPFA matrices, A, B, C and D.
         * The difference compared to the O-method, is that coefficients for
         * full potential continuity are represented using the potential in the
         * primary element. Only a single flux is associated with the secondary
         * elements.
         */
        private void assembleSecondaryElementContribution(Matrix A, Matrix B,
                Matrix C, Matrix D, Vector3D[] Kn, BasisFunction3D[] psi,
                BasisFunction3D[] psiPrimary, Element ep, Element el) {
            List<Interface> interIntf = interfaces(el);

            if (interIntf.size() != 1)
                throw new IllegalArgumentException(
                        "Secondary element contribution assembly only valid for one connection");

            // compute the terms in the flux expression. these are cell-center,
            // flux
            // continuity point and two full potential continuity points
            // respectively.

            Vector3D kn = Kn[0];
            Interface intf = interIntf.get(0);

            int is = index(intf);
            int ei = index(el);

            // Cell centered potential
            double f = kn.dot(psi[0].gradient());
            // add to the flux continuity expression
            B.add(is, ei, -f);
            // add to the flux expression
            D.add(is, ei, -f);

            // Mid-point potential
            f = kn.dot(psi[1].gradient());
            // add to the flux continuity expression
            A.add(is, is, f);
            // add to the flux expression
            C.add(is, is, f);

            // Full potential continuity, interaction region point
            Point3D cp = point().coordinate;
            f = kn.dot(psi[2].gradient());
            assembleFullPotentialContribution(A, B, C, D, f, cp, psiPrimary,
                    ep, is);

            // Full potential continuity, using one of the edge mid-points
            Point3D edgePoint = getHalfEdgePoints(point(), intf).get(0);
            f = kn.dot(psi[3].gradient());
            assembleFullPotentialContribution(A, B, C, D, f, edgePoint,
                    psiPrimary, ep, is);
        }

        /**
         * Creates the full potential continuity contribution for the current
         * secondary element.
         * 
         * @param ep
         * @param is
         *                row index, current flux
         */
        private void assembleFullPotentialContribution(Matrix A, Matrix B,
                Matrix C, Matrix D, double f, Point3D cp,
                BasisFunction3D[] psiPrimary, Element ep, int is) {
            // Assemble cell center
            int ei = index(ep);
            double psik = psiPrimary[0].psi(cp);

            // Add flux continuity expression
            B.add(is, ei, -f * psik);
            // Add flux expression
            D.add(is, ei, -f * psik);

            // Assemble flux continuity points
            List<Interface> interIntf = interfaces(ep);

            for (int k = 1; k < interIntf.size() + 1; k++) {
                Interface jntf = interIntf.get(k - 1);
                int js = index(jntf); // flux continuity unknown index

                psik = psiPrimary[k].psi(cp);

                // Add flux continuity expression
                A.add(is, js, f * psik);

                // Add flux expression
                C.add(is, js, f * psik);
            }
        }

        @Override
        public String toString() {
            StringBuilder string = new StringBuilder();

            string.append("\n");
            string.append("\tPoint: " + (p.index + 1) + "\n");
            string.append("\tNumber of connections: " + numConnections + "\n");
            string.append("\tElements:\n");
            for (Map.Entry<Element, Integer> e : elMap.entrySet()) {
                string.append("\t\t(global, local) : ("
                        + (e.getKey().index + 1) + ", " + e.getValue() + ")\n");
            }

            string.append("\tInterfaces:\n");
            for (Map.Entry<Interface, Integer> e : intfMap.entrySet()) {
                string.append("\t\t(global, local) : ("
                        + (e.getKey().index + 1) + ", " + e.getValue() + ")\n");
            }

            string.append("\tPrimary element interfaces:\n");
            string.append("\t\tElement (" + (primary.index + 1) + ", "
                    + elMap.get(primary) + "): ");
            for (Interface intf : elIntfMap.get(primary)) {
                string.append("(" + (intf.index + 1) + ", " + intfMap.get(intf)
                        + ") ");
            }
            string.append("\n");

            string.append("\tSecondary element interfaces:\n");
            for (Element secondary : secondaries) {
                string.append("\t\tElement (" + (secondary.index + 1) + ", "
                        + elMap.get(secondary) + "): ");
                for (Interface intf : elIntfMap.get(secondary)) {
                    string.append("(" + (intf.index + 1) + ", "
                            + intfMap.get(intf) + "), ");
                }
                string.append("\n");
            }
            string.append("\n");

            return string.toString();
        }
    }

}
