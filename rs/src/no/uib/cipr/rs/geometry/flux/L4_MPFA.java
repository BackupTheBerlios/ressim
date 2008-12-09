package no.uib.cipr.rs.geometry.flux;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import no.uib.cipr.matrix.DenseMatrix;
import no.uib.cipr.matrix.Matrices;
import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.MatrixSingularException;
import no.uib.cipr.rs.geometry.Connection;
import no.uib.cipr.rs.geometry.Element;
import no.uib.cipr.rs.geometry.Interface;
import no.uib.cipr.rs.geometry.Mesh;
import no.uib.cipr.rs.geometry.NeighbourConnection;
import no.uib.cipr.rs.geometry.CornerPoint;
import no.uib.cipr.rs.geometry.Point3D;
import no.uib.cipr.rs.geometry.Tensor3D;
import no.uib.cipr.rs.geometry.Vector3D;
import no.uib.cipr.rs.util.Configuration;

/**
 * L-method in 3D with four possible configurations
 */
public class L4_MPFA extends MPFA {

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

        private final NeighbourConnection c;

        /**
         * Constructs an interaction region for the L-method centered at the
         * given point with given map from elements to interfaces (constructed
         * based on a current connection). Not valid for points located on the
         * boundary of the domain.
         */
        public LmethodInteractionRegion(CornerPoint p, Element primary,
                Map<Element, List<Interface>> elIntfMapFull,
                NeighbourConnection c) {
            this.p = p;
            this.primary = primary;
            this.c = c;

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
            // (corresponds to the number of cell center potentials)
            int numElements = numElements();

            // Coefficient matrix for the midpoint potentials from the flux
            // continuity conditions.
            Matrix A = new DenseMatrix(numConnections, numConnections);

            // Coefficient matrix for the cell potentials from the flux
            // continuity conditions
            Matrix B = new DenseMatrix(numConnections, numElements);

            // Coefficient matrix for the flux continuity potentials for the
            // flux from here to there
            Matrix C = A.copy();

            // Coefficient matrix for the cell potentials for the flux from here
            // to there
            Matrix D = B.copy();

            BasisFunction3D[] psiPrimary = null;
            Element elemPrimary = null;

            // Assemble primary element contribution
            {
                Element el = getPrimaryElement();
                List<Interface> interIntf = interfaces(el);

                // calculate basis functions for primary element
                BasisFunction3D[] psi = calculatePrimaryBasisFunctions(el,
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
                BasisFunction3D[] psi = calculateSecondaryBasisFunction(p, el,
                        interIntf.get(0));

                Vector3D[] Kn = calculateLocalConductivityDirections(K
                        .getConductivity(el), p, interIntf);
                if (Kn.length != 1)
                    throw new IllegalArgumentException(
                            "Only 1 permeability direction allowed for a secondary element");

                assembleSecondaryElementContribution(A, B, C, D, Kn, psi,
                        psiPrimary, elemPrimary, el);
            }

            // Compute T
            Matrix T = calculateTmatrix(A, B, C, D);

            // Extract one row of T
            return extractConnectionRow(T);
        }

        /**
         * Extracts the row of the T-matrix corresponding to the flux connection
         */
        private Matrix extractConnectionRow(Matrix T) {
            Matrix Ti = new DenseMatrix(1, numElements());

            int i = index(mesh.hereInterface(c));
            for (Element el : elements()) {
                int j = index(el);

                double tk = T.get(i, j);

                if (getPrimaryElement() == mesh.here(c))
                    tk *= -1;

                Ti.add(0, j, tk);
            }

            return Ti;
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
        private BasisFunction3D[] calculateSecondaryBasisFunction(
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
        private BasisFunction3D[] calculatePrimaryBasisFunctions(Element el,
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
         *            row index, current flux
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

    /**
     * Skewed L-method interaction region
     */
    private class L4methodInteractionRegion implements InteractionRegion {

        private final CornerPoint p;

        // (Sorted) list of elements in interaction region. The two first
        // elements are the middle cells.
        private final List<Element> elements;

        /**
         * For an element, yields the interfaces it has as part of this
         * interaction region
         */
        private final Map<Element, List<Interface>> elIntfMap;

        private final Map<Interface, Integer> intfMap;

        private Interface fluxInterface;

        private int fluxInterfaceLocalIndex;

        private final Map<Element, Point3D[]> coordinate;

        private final Map<Element, int[]> coefficientIndex;

        /**
         * Constructs an L-method interaction region from the given list of
         * interacting elements and a map between element and list of
         * corresponding interfaces associated with the central point in
         * question.
         * 
         * @param p
         *            Centerpoint this interaction region is constructed around
         * @param elements
         *            First element is here, second is there, then a neighbour
         *            of here, and finally a neighbour of there which isn't
         *            connected to the previous element (yep)
         * @param elIntfMap
         *            For each element in the region, this yields the interfaces
         *            of that element which also are in the region
         */
        public L4methodInteractionRegion(CornerPoint p, List<Element> elements,
                Map<Element, List<Interface>> elIntfMap) {
            this.p = p;
            this.elements = elements;
            this.elIntfMap = elIntfMap;

            if (elements.size() != 4)
                throw new IllegalArgumentException(
                        "Interaction region must contain exactly four elements");

            // construct mapping between interaction region interfaces and local
            // flux-face indices
            intfMap = new HashMap<Interface, Integer>();

            Element here = elements.get(0);
            Element there = elements.get(1);
            Element hereNeighbour = elements.get(2);
            Element thereNeighbour = elements.get(3);

            /*
             * This assigns indices to interfaces. The interfaces between here
             * and there are 0, there and outer is 1, and here and outer is 2.
             * 
             * Also, the interface for which the flux is calculated is stored
             * (between here and there, from here), and its local index is kept.
             */

            int i = 0;
            for (Interface intf : elIntfMap.get(here)) {
                Interface jntf = getNeighbour(intf);
                if (mesh.element(jntf) == there) {
                    fluxInterface = intf;
                    fluxInterfaceLocalIndex = i;

                    intfMap.put(intf, 0);
                    intfMap.put(jntf, 0);
                } else {
                    intfMap.put(intf, 2);
                    intfMap.put(jntf, 2);
                }

                i++;
            }

            for (Interface intf : elIntfMap.get(there)) {
                Interface jntf = getNeighbour(intf);
                if (mesh.element(jntf) != here) {
                    intfMap.put(intf, 1);
                    intfMap.put(jntf, 1);
                }
            }

            /*
             * Set potential coefficient indices.
             */

            coefficientIndex = new HashMap<Element, int[]>();

            coefficientIndex.put(here, new int[] { 0, 2, 3 });
            coefficientIndex.put(there, new int[] { 0, 1, 5 });
            coefficientIndex.put(thereNeighbour, new int[] { 1, 3, 4 });
            coefficientIndex.put(hereNeighbour, new int[] { 2, 5, 6 });

            /*
             * Set potential coefficient coordinates.
             */

            coordinate = new HashMap<Element, Point3D[]>();

            Interface thereNeighbourIntf = elIntfMap.get(thereNeighbour).get(0);
            Interface hereNeighbourIntf = elIntfMap.get(hereNeighbour).get(0);

            // Interface center points
            Point3D x1t = fluxInterface.center;
            Point3D x2t = thereNeighbourIntf.center;
            Point3D x3t = hereNeighbourIntf.center;

            // Half-edge points
            List<Point3D> hereNeighbourPoints = getHalfEdgePoints(p,
                    hereNeighbourIntf);
            List<Point3D> thereNeighbourPoints = getHalfEdgePoints(p,
                    thereNeighbourIntf);

            Point3D x4h = thereNeighbourPoints.get(0);
            Point3D x5h = thereNeighbourPoints.get(1);
            Point3D x6h = hereNeighbourPoints.get(1);
            Point3D x7h = hereNeighbourPoints.get(0);

            coordinate.put(here, new Point3D[] { x1t, x3t, x4h });
            coordinate.put(there, new Point3D[] { x1t, x2t, x6h });
            coordinate.put(thereNeighbour, new Point3D[] { x2t, x4h, x5h });
            coordinate.put(hereNeighbour, new Point3D[] { x3t, x6h, x7h });
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder("\n\n\tElements: ");
            for (Element e : elements)
                sb.append((e.index + 1) + " ");

            return sb.toString();
        }

        public Iterable<Element> elements() {
            return elements;
        }

        /**
         * Element indices are locally sorted according to insertion order here
         */
        public int index(Element el) {
            int i = 0;
            for (Element e : elements) {
                if (e == el)
                    return i;
                else
                    i++;
            }

            throw new RuntimeException("Element not found");
        }

        public int index(Interface intf) {
            return intfMap.get(intf);
        }

        public List<Interface> interfaces(Element el) {
            return elIntfMap.get(el);
        }

        public int numConnections() {
            return 3;
        }

        public int numElements() {
            return 4;
        }

        public int numFullPotentials() {
            return 7;
        }

        public Point3D getCoordinate(Element el, int i) {
            return coordinate.get(el)[i];
        }

        public int getCoefficientIndex(Element el, int i) {
            return coefficientIndex.get(el)[i];
        }

        public Interface getFluxInterface() {
            return fluxInterface;
        }

        public int getFluxInterfaceLocalIndex() {
            return fluxInterfaceLocalIndex;
        }

        public Element getThereElement() {
            return elements.get(1);
        }

        public Element getHereElement() {
            return elements.get(0);
        }

        /**
         * Outer element connected to there element is fourth element.
         */
        public Element getThereOuterNeighbour() {
            return elements.get(3);
        }

        /**
         * Outer element connected to here elment is third element.
         */
        public Element getHereOuterNeighbour() {
            return elements.get(2);
        }

        /**
         * Calculates the matrix of transmissibility coefficients. This is for
         * the case with a skew last
         */
        public Matrix calculateT(Conductivity K) {
            // number of connections in the interaction region (corresponds to
            // number of flux continuity potentials).
            int numConnections = numConnections();

            // number of interacting elements in the interaction region
            // (corresponds to the number of cell center potentials)
            int numElements = numElements();

            // number of all continuity points
            int numAllContinuities = numFullPotentials();

            // expanded matrices for easy assembly
            Matrix Ae = new DenseMatrix(numConnections, numAllContinuities);
            Matrix Ce = new DenseMatrix(1, numAllContinuities);

            // Coefficient matrix for the midpoint potentials from the flux
            // continuity conditions.
            Matrix A = new DenseMatrix(numConnections, numConnections);

            // Coefficient matrix for the cell potentials from the flux
            // continuity conditions
            Matrix B = new DenseMatrix(numConnections, numElements);

            // Coefficient matrix for the flux continuity potentials for the
            // flux from here to there
            Matrix C = new DenseMatrix(1, numConnections);

            // Coefficient matrix for the cell potentials for the flux from here
            // to there
            Matrix D = new DenseMatrix(1, numElements);

            // Assemble expanded A and B matrices
            for (Element el : elements()) {

                // get flux interfaces
                List<Interface> interIntf = interfaces(el);

                // calculate basis functions for primary element
                BasisFunction3D[] psi = calculateBasisFunctions(el);

                // calculate directional conductivities
                Vector3D[] Kn = calculateLocalConductivityDirections(K
                        .getConductivity(el), p, interIntf);

                // assembles expanded flux continuity
                assembleFluxContinuity(Ae, B, Kn, psi, el);

                // Assemble flux
                if (index(el) == 0)
                    assembleFlux(Ce, D, Kn, psi, el);
            }

            // Split the system into reduced A and C
            splitSystem(A, B, C, D, Ae, Ce);

            // Compute T = C*A^{-1}*B - D
            return calculateTmatrix(A, B, C, D);
        }

        /**
         * Basically, this method reduces the overdetermined system by enforcing
         * full potential continuity. The potential unknowns u4, u5, u6, and u7
         * are expressed in terms of known cell and face potentials
         */
        private void splitSystem(Matrix A, Matrix B, Matrix C, Matrix D,
                Matrix Ae, Matrix Ce) {

            // u4, u6
            Element here = getHereElement();
            Element there = getThereElement();

            BasisFunction3D[] psiHere = calculateBasisFunctions(here);
            BasisFunction3D[] psiThere = calculateBasisFunctions(there);

            double[] u4h_cell = new double[numElements()];
            double[] u6h_cell = new double[numElements()];

            double[] u4h_face = new double[numConnections()];
            double[] u6h_face = new double[numConnections()];

            {
                double[] psiHereX6 = computePsi(getCoordinate(there, 2),
                        psiHere);
                double[] psiThereX4 = computePsi(getCoordinate(here, 2),
                        psiThere);

                double denominator = 1 / (1 - psiThereX4[3] * psiHereX6[3]);

                // a
                u4h_cell[1] += denominator * psiThereX4[0];
                u4h_face[0] += denominator * psiThereX4[1];
                u4h_face[1] += denominator * psiThereX4[2];
                // b
                u4h_cell[0] += denominator * psiHereX6[0] * psiThereX4[3];
                u4h_face[0] += denominator * psiHereX6[1] * psiThereX4[3];
                u4h_face[2] += denominator * psiHereX6[2] * psiThereX4[3];

                // b
                u6h_cell[0] += denominator * psiHereX6[0];
                u6h_face[0] += denominator * psiHereX6[1];
                u6h_face[2] += denominator * psiHereX6[2];
                // a
                u6h_cell[1] += denominator * psiThereX4[0] * psiHereX6[3];
                u6h_face[0] += denominator * psiThereX4[1] * psiHereX6[3];
                u6h_face[1] += denominator * psiThereX4[2] * psiHereX6[3];
            }

            // u5
            double[] u5h_cell = new double[numElements()];
            double[] u5h_face = new double[numConnections()];

            {
                Element et = getThereOuterNeighbour();
                double[] psiThereX5 = computePsi(getCoordinate(et, 2), psiThere);

                u5h_cell[1] += psiThereX5[0];
                u5h_face[0] += psiThereX5[1];
                u5h_face[1] += psiThereX5[2];

                for (int i = 0; i < numElements(); i++)
                    u5h_cell[i] += u6h_cell[i] * psiThereX5[3];

                for (int i = 0; i < numConnections(); i++)
                    u5h_face[i] += u6h_face[i] * psiThereX5[3];
            }

            // u7
            double[] u7h_cell = new double[numElements()];
            double[] u7h_face = new double[numConnections()];

            {
                Element eh = getHereOuterNeighbour();
                double[] psiHereX7 = computePsi(getCoordinate(eh, 2), psiHere);

                u7h_cell[0] += psiHereX7[0];
                u7h_face[0] += psiHereX7[1];
                u7h_face[2] += psiHereX7[2];

                for (int i = 0; i < numElements(); i++)
                    u7h_cell[i] += u4h_cell[i] * psiHereX7[3];

                for (int i = 0; i < numConnections(); i++)
                    u7h_face[i] += u4h_face[i] * psiHereX7[3];
            }

            // Insert primary block of Ae into A
            for (int i = 0; i < numConnections(); i++)
                for (int j = 0; j < numConnections(); j++)
                    A.set(i, j, Ae.get(i, j));

            // Insert primary block of Ce into C
            for (int i = 0; i < numConnections(); i++)
                C.set(0, i, Ce.get(0, i));

            // cell values
            for (int i = 0; i < numConnections(); i++)
                for (int j = 0; j < numElements(); j++) {
                    B.add(i, j, -Ae.get(i, 3) * u4h_cell[j]);
                    B.add(i, j, -Ae.get(i, 4) * u5h_cell[j]);
                    B.add(i, j, -Ae.get(i, 5) * u6h_cell[j]);
                    B.add(i, j, -Ae.get(i, 6) * u7h_cell[j]);
                }

            // Flux continuity expression
            for (int i = 0; i < numConnections(); i++)
                for (int j = 0; j < numConnections(); j++) {
                    A.add(i, j, Ae.get(i, 3) * u4h_face[j]);
                    A.add(i, j, Ae.get(i, 4) * u5h_face[j]);
                    A.add(i, j, Ae.get(i, 5) * u6h_face[j]);
                    A.add(i, j, Ae.get(i, 6) * u7h_face[j]);
                }

            // Reduced flux expression
            for (int i = 0; i < numConnections(); i++) {
                D.add(0, i, -Ce.get(0, 3) * u4h_cell[i]);
                D.add(0, i, -Ce.get(0, 4) * u5h_cell[i]);
                D.add(0, i, -Ce.get(0, 5) * u6h_cell[i]);
                D.add(0, i, -Ce.get(0, 6) * u7h_cell[i]);

                C.add(0, i, Ce.get(0, 3) * u4h_face[i]);
                C.add(0, i, Ce.get(0, 4) * u5h_face[i]);
                C.add(0, i, Ce.get(0, 5) * u6h_face[i]);
                C.add(0, i, Ce.get(0, 6) * u7h_face[i]);
            }

        }

        private double[] computePsi(Point3D coord, BasisFunction3D[] psi) {
            double psiVal[] = new double[4];

            for (int i = 0; i < 4; i++)
                psiVal[i] = psi[i].psi(coord);

            return psiVal;
        }

        private void assembleFlux(Matrix Ce, Matrix D, Vector3D[] Kn,
                BasisFunction3D[] psi, Element el) {
            int ei = index(el);

            Vector3D kn = Kn[getFluxInterfaceLocalIndex()];

            // Cell centered potential
            double f = kn.dot(psi[0].gradient());

            // add to the flux expression
            D.add(0, ei, -f);

            for (int i = 0; i < 3; i++) {
                f = kn.dot(psi[i + 1].gradient());

                // add to the flux expression
                int is = getCoefficientIndex(el, i);
                Ce.add(0, is, f);
            }
        }

        private void assembleFluxContinuity(Matrix Ae, Matrix B, Vector3D[] Kn,
                BasisFunction3D[] psi, Element el) {

            List<Interface> interIntf = interfaces(el);

            // for each flux interface
            for (int i = 0; i < interIntf.size(); i++) {
                Interface intf = interIntf.get(i);
                int is = index(intf);
                int ei = index(el);

                double f = Kn[i].dot(psi[0].gradient());

                // add to the flux continuity expression
                B.add(is, ei, -f);

                // potentials to be eliminated
                for (int j = 0; j < 3; j++) {
                    f = Kn[i].dot(psi[j + 1].gradient());

                    int js = getCoefficientIndex(el, j);

                    // Add to the flux continuity expression
                    Ae.add(is, js, f);
                }
            }

        }

        private BasisFunction3D[] calculateBasisFunctions(Element el) {
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

            // three basis functions defined as 1 in the continuity points
            for (int i = 0; i < 3; i++) {
                Point3D cp = getCoordinate(el, i);

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
    }

    /**
     * Contains the four different cases of L-methods. For topological pyramids,
     * only a single interaction region will be used
     */
    private class InteractionRegions {

        // center point for the interaction regions
        private final CornerPoint p;

        private final Map<Element, List<Interface>> elementInterfaces;

        public InteractionRegions(CornerPoint p) {

            this.p = p;

            // compute the element connections for the given point
            elementInterfaces = getElementInterfaces(p);
        }

        public List<? extends InteractionRegion> getInteractionRegions(
                NeighbourConnection c) {
            List<InteractionRegion> regions = new ArrayList<InteractionRegion>();

            // central cells are here/there cells
            Element here = mesh.here(c);
            Element there = mesh.there(c);

            // compute number of possible neighbours
            int nHere = elementInterfaces.get(here).size();
            int nThere = elementInterfaces.get(there).size();

            // Neighbours of here or there, excluding there or here (...)
            List<Element> hereNeighbours = getElementNeighbours(here, there);
            List<Element> thereNeighbours = getElementNeighbours(there, here);

            // Conforming standard case. Each element is trivalent, and
            // neighbours aren't shared. Shared neighbours occur at LGRs, where
            // a big cell can be a neighbour of two smaller cells
            if (nHere == 3 && nThere == 3
                    && Collections.disjoint(hereNeighbours, thereNeighbours)) {

                // First pick out standard last

                // standard last with here as centre cell
                regions.add(new LmethodInteractionRegion(p, here,
                        elementInterfaces, c));

                // standard last with there as centre cell
                regions.add(new LmethodInteractionRegion(p, there,
                        elementInterfaces, c));

                // skew lasts
                for (Element eh : hereNeighbours) {
                    List<Element> l = new ArrayList<Element>();

                    // add central cells
                    l.add(here);
                    l.add(there);

                    // add outer cell connected to here element
                    l.add(eh);

                    // find there neighbour cell not connected to e.
                    boolean found = false;
                    for (Element et : thereNeighbours) {

                        // Skip if interfaces of 'et' is connected to 'eh'
                        if (connected(et, eh))
                            continue;

                        // Now 'et' is not connected to 'eh'.

                        // If this has occured before, we throw an exception
                        if (found)
                            throw new IllegalArgumentException(
                                    "Element already connected in skew interaction region");

                        // Now, 'et' is not connected to 'eh', and never before.
                        l.add(et);
                        found = true;
                    }

                    // now list is complete and we can construct a new
                    // interaction region.
                    regions.add(new L4methodInteractionRegion(p, l,
                            createElementInterfaceMapRegion(l,
                                    elementInterfaces)));
                }

            } else if (nHere == 3)
                // here element is primary element
                regions.add(new LmethodInteractionRegion(p, here,
                        elementInterfaces, c));
            else if (nThere == 3)
                // there element is primary element
                regions.add(new LmethodInteractionRegion(p, there,
                        elementInterfaces, c));
            else
                throw new IllegalArgumentException(
                        "Illegal local vertex degree for point " + p.index);

            return regions;
        }

        /**
         * Checks if the given elements are connected via the element interfaces
         * as given in the element interface map for this set of interaction
         * regions.
         */
        private boolean connected(Element et, Element eh) {
            boolean connected = false;
            for (Interface intf : elementInterfaces.get(et)) {
                Interface jntf = getNeighbour(intf);
                Element etNeighbour = mesh.element(jntf);

                if (etNeighbour.equals(eh))
                    return true;

            }
            return connected;
        }

        /**
         * Returns a list of elements that are neighbours to the given element
         * through the element's interfaces. If neighbouring elements equal the
         * second given element, these are not added to the list. The method
         * uses elementInterfaces.
         */
        private List<Element> getElementNeighbours(Element e, Element exempt) {
            List<Element> neighbours = new ArrayList<Element>();

            for (Interface intf : elementInterfaces.get(e)) {
                Interface jntf = getNeighbour(intf);
                Element en = mesh.element(jntf);
                if (en != exempt)
                    neighbours.add(en);
            }

            return neighbours;
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

        /*
         * This method is not specific to the L-method ...
         */
        private Map<Element, List<Interface>> createElementInterfaceMapRegion(
                List<Element> elements,
                Map<Element, List<Interface>> elementInterfaces) {
            Map<Element, List<Interface>> elIntfMap = new HashMap<Element, List<Interface>>();

            for (Element el : elements) {
                List<Interface> iList = new ArrayList<Interface>();

                for (Interface intf : elementInterfaces.get(el)) {
                    Connection c = mesh.connection(intf);
                    if (elements.contains(mesh.here(c))
                            && elements.contains(mesh.there(c)))
                        iList.add(intf);
                }
                elIntfMap.put(el, iList);
            }

            return elIntfMap;
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

    public L4_MPFA(Configuration config) {
        System.out.println(config.trace() + "Using the L4-method");
    }

    @Override
    public List<? extends Collection<Transmissibility>> calculateTransmissibilities(
            Mesh mesh, Conductivity K) {
        this.mesh = mesh;

        List<List<Transmissibility>> M = allocateM();

        for (CornerPoint p : mesh.points())
            if (pointOnBoundary(p))
                calculateBoundaryTransmissibilities(p, K, M);
            else
                calculateTransmissibilities(p, K, M);

        testUniformFlow(M);

        return M;
    }

    /**
     * Calculate the transmissibilities for internal connections around a corner
     * point using a variety of different L-configurations. The best one for
     * each connection is chosen
     */
    private void calculateTransmissibilities(CornerPoint p, Conductivity K,
            List<List<Transmissibility>> M) {
        // construct the container of all possible interaction regions
        InteractionRegions regions = new InteractionRegions(p);

        for (NeighbourConnection c : connections(p)) {
            // get list of candidate L-method interaction regions for the
            // current connection
            List<? extends InteractionRegion> candidateRegions = regions
                    .getInteractionRegions(c);

            // Map relating interaction regions to computed transmissibilities.
            Map<Matrix, InteractionRegion> candidateT = new HashMap<Matrix, InteractionRegion>();

            // calculate the T-matrix of transmissibilities for each L-method
            // interaction region
            for (InteractionRegion region : candidateRegions) {
                Matrix T = region.calculateT(K);
                candidateT.put(T, region);
            }

            Entry<Matrix, InteractionRegion> chosenT = chooseTransmissibilities(
                    candidateT, c);

            // Store the computed transmissibilities for the current connection
            storeTransmissibilities(chosenT.getKey(), chosenT.getValue(), M
                    .get(c.index));
        }
    }

    /**
     * Selects the interaction region whose transmissibilities minimize the
     * absolute value of the difference T_here - T_there. This ensures that the
     * additional cells in the interaction region are important (large
     * transmissibilities)
     */
    private Entry<Matrix, InteractionRegion> chooseTransmissibilities(
            Map<Matrix, InteractionRegion> candidateT, NeighbourConnection c) {
        double[] difference = new double[candidateT.size()];

        Element hereElement = mesh.here(c);
        Element thereElement = mesh.there(c);

        int i = 0;
        for (Entry<Matrix, InteractionRegion> e : candidateT.entrySet()) {
            Matrix T = e.getKey();
            InteractionRegion region = e.getValue();

            int here = region.index(hereElement);
            int there = region.index(thereElement);

            difference[i] = Math.abs(T.get(0, here) - T.get(0, there));

            i++;
        }

        // Which was the smallest?
        int smallest = 0;
        for (i = 0; i < difference.length; ++i)
            if (difference[i] < difference[smallest])
                smallest = i;

        // Return it
        i = 0;
        for (Entry<Matrix, InteractionRegion> e : candidateT.entrySet())
            if (i++ == smallest)
                return e;

        throw new RuntimeException();
    }

    /**
     * Stores the calculated transmissibilities into the given set
     */
    private void storeTransmissibilities(Matrix T, InteractionRegion region,
            List<Transmissibility> Mi) {
        for (Element el : region.elements()) {
            int i = region.index(el);

            double tk = T.get(0, i);

            // If the transmissibility is already in the list, just add
            boolean found = false;
            for (Transmissibility tp : Mi)
                if (mesh.element(tp) == el) {
                    found = true;
                    tp.add(tk);
                    break;
                }

            // Not there, add a new transmissiblity
            if (!found)
                Mi.add(new Transmissibility(el.index, tk));
        }
    }

    /**
     * Calculates transmissibilities for connections at the boundary of the
     * domain. Currently only two-point flux approximation is used.
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

}
