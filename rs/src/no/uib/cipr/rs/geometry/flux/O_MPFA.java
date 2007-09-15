package no.uib.cipr.rs.geometry.flux;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import no.uib.cipr.rs.geometry.Vector3D;
import no.uib.cipr.rs.util.Configuration;

/**
 * The generalised MPFA O-method for calculating transmissibilities
 */
public class O_MPFA extends MPFA {

    private final boolean test;

    /**
     * Placement of the continuity point. 1 (inclusive) is at the midpoint,
     * while 0 (exclusive) is at the corner
     */
    final double continuity;

    /**
     * Sets up the flux discretisation
     * 
     * @param config
     *                Configuration
     */
    public O_MPFA(Configuration config) {
        test = config.getBoolean("Test", false);

        continuity = config.getDouble("Continuity", 1);

        if (continuity > 1 || continuity <= 0)
            throw new IllegalArgumentException(config.trace()
                    + "continuity must be in (0,1]");

        System.out.println(config.trace()
                + "Using multi-point flux approximation with continuity="
                + continuity);
    }

    @Override
    public List<List<Transmissibility>> calculateTransmissibilities(Mesh mesh,
            Conductivity K) {
        this.mesh = mesh;

        List<List<Transmissibility>> M = allocateM();

        for (CornerPoint p : mesh.points())
            calculateTransmissibilities(p, K, M);

        if (test)
            testUniformFlow(M);

        return M;
    }

    private void calculateTransmissibilities(CornerPoint p, Conductivity K,
            List<List<Transmissibility>> M) {

        // Construct the O-method interaction region
        InteractionRegion region = new OmethodInteractionRegion(p);

        // Calculate the T-matrix of transmissibilities
        Matrix T = region.calculateT(K);

        // Store the computed transmissibilities for all the connections
        for (NeighbourConnection c : connections(p))
            storeTransmissibilities(T, region, c, M.get(c.index));
    }

    /**
     * Stores the transmissibilities associated with the flux across the given
     * connection
     */
    private void storeTransmissibilities(Matrix T, InteractionRegion region,
            NeighbourConnection c, List<Transmissibility> Mi) {

        int i = region.index(mesh.hereInterface(c));

        for (Element el : region.elements()) {

            int j = region.index(el);

            double tk = T.get(i, j);

            // If the transmissibility is already in the list, just add
            boolean found = false;
            for (Transmissibility tp : Mi)
                if (mesh.element(tp) == el) {
                    found = true;
                    tp.add(tk);
                    break;
                }

            // Not there, add a new transmissiblity
            if (!found && tk != 0)
                Mi.add(new Transmissibility(el.index, tk));
        }
    }

    /**
     * Interaction region for the MPFA O-method
     */
    private class OmethodInteractionRegion implements InteractionRegion {

        private CornerPoint p;

        private Map<Element, Integer> elMap;

        private Map<Interface, Integer> intfMap;

        private int numConnections;

        /**
         * Constructs an interaction region for the O-method centered at a given
         * grid point
         */
        public OmethodInteractionRegion(CornerPoint p) {
            this.p = p;
            elMap = createLocalElementMap(p);
            intfMap = createLocalInterfaceMap(p);
        }

        /**
         * Creates local indexing for the interfaces associated with the given
         * point. The numbering is based on the connections, so that interfaces
         * which share a connection get the same index
         */
        private Map<Interface, Integer> createLocalInterfaceMap(CornerPoint p) {
            Map<Interface, Integer> map = new HashMap<Interface, Integer>();

            numConnections = 0;
            for (Interface intf : mesh.interfaces(p))
                if (!intf.boundary && map.containsKey(getNeighbour(intf)))
                    map.put(intf, map.get(getNeighbour(intf)));
                else
                    map.put(intf, numConnections++);

            return map;
        }

        /**
         * Creates local indexing for the elements associated with the given
         * point
         */
        private Map<Element, Integer> createLocalElementMap(CornerPoint p) {
            Map<Element, Integer> map = new HashMap<Element, Integer>();

            int index = 0;
            for (Element el : mesh.elements(p))
                map.put(el, index++);

            return map;
        }

        public Iterable<Element> elements() {
            return mesh.elements(p);
        }

        public List<Interface> interfaces(Element el) {
            return getCommonInterfaces(el);
        }

        public int index(Element el) {
            return elMap.get(el);
        }

        public int index(Interface intf) {
            return intfMap.get(intf);
        }

        public int numElements() {
            return elMap.size();
        }

        public int numConnections() {
            return numConnections;
        }

        public Matrix calculateT(Conductivity K) {

            int numConnections = numConnections();
            int numElements = numElements();

            // Coefficient matrix for the midpoint potentials from the flux
            // continuity conditions
            Matrix A = new DenseMatrix(numConnections, numConnections);

            // Coefficient matrix for the cell potentials from the flux
            // continuity conditions
            Matrix B = new DenseMatrix(numConnections, numElements);

            // Coefficient matrix for the midpoint potentials for the flux from
            // here to there
            Matrix C = A.copy();

            // Coefficient matrix for the cell potentials for the flux from here
            // to there
            Matrix D = B.copy();

            for (Element el : elements()) {

                List<Interface> interIntf = interfaces(el);

                // Calculate basis function gradients on current element
                Vector3D[] gradpsi = null;
                if (interIntf.size() == 2)
                    gradpsi = calculateBasisFunctions2D(p, el, interIntf);
                else if (interIntf.size() == 3)
                    gradpsi = calculateBasisFunctions3D(p, el, interIntf);
                else
                    throw new IllegalArgumentException(
                            "Interaction region has too many interfaces in element "
                                    + (el.index + 1));

                // Calculate the outward normal conductivity on each interface
                Vector3D[] Kn = calculateLocalConductivityDirections(K
                        .getConductivity(el), p, interIntf);

                // Assemble local contributions
                assembleElementContribution(A, B, C, D, Kn, gradpsi, el);
            }

            // T = C * (A \ B) - D
            return calculateTmatrix(A, B, C, D);
        }

        /**
         * Get the interfaces which the point and the element has in common
         */
        private List<Interface> getCommonInterfaces(Element el) {
            // Create an array to hold the intersection
            List<Interface> commonIntf = new ArrayList<Interface>();

            // Traverse both sets of interfaces, and add those in both
            for (Interface intf : mesh.interfaces(p))
                for (Interface jntf : mesh.interfaces(el))
                    if (intf == jntf)
                        commonIntf.add(intf);

            return commonIntf;
        }

        /**
         * Calculates the linear basis functions on a given element relative to
         * a point. This is for the 2D case
         * 
         * @param p
         *                Point
         * @param el
         *                Element containing the point
         * @param interIntf
         *                The interfaces of the element as part of the
         *                interaction region
         * @return Calculated gradients. The first has support in the element
         *         centre, the other have support on the interfaces
         */
        private Vector3D[] calculateBasisFunctions2D(CornerPoint p, Element el,
                List<Interface> interIntf) {

            // Number of basis functions. One in the element center, and one for
            // each interface
            int n = 3;

            Matrix A = new DenseMatrix(n, n);
            Matrix Psi = new DenseMatrix(n, n);
            Matrix I = Matrices.identity(n);

            /*
             * Assemble system: psi_i(x_j,y_j,z_j) = delta_ij
             * 
             * The basis functions have the form:
             * 
             * psi(x,y,z) = C1 + C2 * x + C3 * y
             */

            Point3D elp = el.center;

            // The single basis function defined as 1 in the element centre
            A.set(0, 0, 1);
            A.set(0, 1, elp.x());
            A.set(0, 2, elp.y());

            // The two basis functions which are equal to 1 on the interfaces
            for (int i = 0; i < interIntf.size(); ++i) {
                Interface intf = interIntf.get(i);
                Point3D cp = getInterfaceContinuity(p, intf);

                A.set(i + 1, 0, 1);
                A.set(i + 1, 1, cp.x());
                A.set(i + 1, 2, cp.y());
            }

            /*
             * Solve for the basis functions
             */

            try {
                A.solve(I, Psi);
            } catch (MatrixSingularException e) {
                throw new IllegalArgumentException(
                        "Severe grid distortion at element " + (el.index + 1));
            }

            // Extract the gradients
            Vector3D[] gradpsi = new Vector3D[n];
            for (int i = 0; i < n; ++i) {
                double psix = Psi.get(1, i), psiy = Psi.get(2, i);

                gradpsi[i] = new Vector3D(psix, psiy, 0);
            }

            return gradpsi;
        }

        /**
         * Calculates the linear basis functions on a given element relative to
         * a point. This is for the 3D case
         * 
         * @param p
         *                Point
         * @param el
         *                Element containing the point
         * @param interIntf
         *                The interfaces of the element as part of the
         *                interaction region
         * @return Calculated gradients. The first has support in the element
         *         centre, the other have support on the interfaces
         */
        private Vector3D[] calculateBasisFunctions3D(CornerPoint p, Element el,
                List<Interface> interIntf) {

            int n = 4;

            Matrix A = new DenseMatrix(n, n);
            Matrix Psi = new DenseMatrix(n, n);
            Matrix I = Matrices.identity(n);

            /*
             * Assemble system: psi_i(x_j,y_j,z_j) = delta_ij
             * 
             * The basis functions have the form:
             * 
             * psi(x,y,z) = C1 + C2 * x + C3 * y + C4 * z
             */

            Point3D elp = el.center;

            // The single basis function defined as 1 in the element centre
            A.set(0, 0, 1);
            A.set(0, 1, elp.x());
            A.set(0, 2, elp.y());
            A.set(0, 3, elp.z());

            // The three basis functions which are equal to 1 on the interfaces
            for (int i = 0; i < interIntf.size(); ++i) {
                Interface intf = interIntf.get(i);
                Point3D cp = getInterfaceContinuity(p, intf);

                A.set(i + 1, 0, 1);
                A.set(i + 1, 1, cp.x());
                A.set(i + 1, 2, cp.y());
                A.set(i + 1, 3, cp.z());
            }

            /*
             * Solve for the basis functions
             */

            try {
                A.solve(I, Psi);
            } catch (MatrixSingularException e) {
                throw new IllegalArgumentException(
                        "Severe grid distortion at element " + (el.index + 1));
            }

            // Extract the gradients
            Vector3D[] gradpsi = new Vector3D[n];
            for (int i = 0; i < n; ++i) {
                double psix = Psi.get(1, i);
                double psiy = Psi.get(2, i);
                double psiz = Psi.get(3, i);

                gradpsi[i] = new Vector3D(psix, psiy, psiz);
            }

            return gradpsi;
        }

        private Point3D getInterfaceContinuity(CornerPoint p, Interface intf) {
            Point3D center = intf.center;

            // This is the usual case
            if (continuity == 1)
                return center;

            Point3D corner = p.coordinate;

            double x = (center.x() - corner.x()) * continuity + corner.x();
            double y = (center.y() - corner.y()) * continuity + corner.y();
            double z = (center.z() - corner.z()) * continuity + corner.z();

            return new Point3D(x, y, z);
        }

        /**
         * Assembles local contributions
         */
        private void assembleElementContribution(Matrix A, Matrix B, Matrix C,
                Matrix D, Vector3D[] Kn, Vector3D[] gradpsi, Element el) {
            List<Interface> interIntf = interfaces(el);

            for (int i = 0; i < interIntf.size(); i++) {

                // Cell centered potential
                double f = Kn[i].dot(gradpsi[0]);
                Interface intf = interIntf.get(i);
                int is = index(intf);
                int ei = index(mesh.element(intf));

                boolean isHere = isHere(intf);

                // Add to the flux continuity expression
                B.add(is, ei, -f);

                // Add to the flux expression, if on a 'here' side
                if (isHere)
                    D.add(is, ei, -f);

                // Midpoint potentials
                for (int j = 1; j < interIntf.size() + 1; j++) {
                    f = Kn[i].dot(gradpsi[j]);
                    Interface jntf = interIntf.get(j - 1);
                    int js = index(jntf);

                    // Add to the flux continuity expression
                    A.add(is, js, f);

                    // Add to the flux expression, if on a 'here' side
                    if (isHere)
                        C.add(is, js, f);
                }
            }
        }

        @Override
        public String toString() {
            StringBuilder string = new StringBuilder();

            string.append("Point: " + (p.index + 1) + "\n");
            string.append("Number of connections: " + numConnections + "\n");
            string.append("Elements:\n");
            for (Map.Entry<Element, Integer> e : elMap.entrySet()) {
                string.append("\tGlobal index: " + (e.getKey().index + 1)
                        + "\n");
                string.append("\tLocal index: " + e.getValue() + "\n");
            }
            string.append("Interfaces:\n");
            for (Map.Entry<Interface, Integer> e : intfMap.entrySet()) {
                string.append("\tGlobal index: " + (e.getKey().index + 1)
                        + "\n");
                string.append("\tLocal index: " + e.getValue() + "\n");
            }

            return string.toString();
        }

    }
}
