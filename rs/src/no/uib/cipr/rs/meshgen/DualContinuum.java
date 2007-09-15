package no.uib.cipr.rs.meshgen;

import java.util.Arrays;
import java.util.List;

import no.uib.cipr.rs.geometry.Connection;
import no.uib.cipr.rs.geometry.CornerPoint;
import no.uib.cipr.rs.geometry.Element;
import no.uib.cipr.rs.geometry.Geometry;
import no.uib.cipr.rs.geometry.Interface;
import no.uib.cipr.rs.geometry.Mesh;
import no.uib.cipr.rs.geometry.NeighbourConnection;
import no.uib.cipr.rs.geometry.Tensor3D;
import no.uib.cipr.rs.geometry.Topology;
import no.uib.cipr.rs.geometry.flux.AbsolutePermeability;
import no.uib.cipr.rs.geometry.flux.Conductivity;
import no.uib.cipr.rs.geometry.flux.RockHeatConductivity;
import no.uib.cipr.rs.geometry.flux.Transmissibility;
import no.uib.cipr.rs.geometry.flux.TransmissibilityComputer;
import no.uib.cipr.rs.rock.Rock;
import no.uib.cipr.rs.util.Configuration;

/**
 * Creates a dual continuum from a given mesh
 */
public class DualContinuum {

    private Mesh mesh;

    public DualContinuum(Configuration config, Mesh mesh) {
        Configuration dual = config.getConfiguration("DualContinuum");

        boolean use = dual.getBoolean("use", false);

        if (use)
            this.mesh = createDualContinuum(dual, mesh);
        else
            this.mesh = mesh;
    }

    private Mesh createDualContinuum(Configuration config, Mesh mesh) {

        System.out.println(config.trace() + "Creating a dual continuum");

        // Make sure there's no non-neighbour connections present
        if (mesh.nonNeighbourConnections().size() > 0)
            throw new IllegalArgumentException(config.trace()
                    + "Grid contains non-neighbour connections");

        DualTopology topology = new DualTopology(mesh);

        DualGeometry geometry = new DualGeometry(mesh, topology);

        double scale = config.getDouble("scale");

        Rock[] rocks = buildRocks(mesh, scale);

        Mesh dual = new Mesh(geometry, topology, rocks);

        calculateNonNeighbourTransmissibilities(dual, scale);

        return dual;
    }

    private void calculateNonNeighbourTransmissibilities(Mesh dual, double scale) {
        // Darcy transmissibilities
        Conductivity K = new AbsolutePermeability();
        for (Connection c : dual.nonNeighbourConnections()) {
            List<Transmissibility> M = calculateTransmissibility(c, K, scale);
            c.setDarcyTransmissibilities(M);
        }

        // Fourier transmissibilities
        Conductivity k = new RockHeatConductivity();
        if (TransmissibilityComputer.nonZeroTensor(dual, k))
            for (Connection c : dual.nonNeighbourConnections()) {
                List<Transmissibility> M = calculateTransmissibility(c, k,
                        scale);
                c.setFourierTransmissibilities(M);
            }
    }

    private List<Transmissibility> calculateTransmissibility(Connection c,
            Conductivity K, double scale) {
        double Kh = K.getConductivity(mesh.here(c)).horizontal();

        double t = Kh * scale;

        Transmissibility ti = new Transmissibility(c.hereElement, t);
        Transmissibility tj = new Transmissibility(c.thereElement, -t);

        return Arrays.asList(ti, tj);
    }

    private Rock[] buildRocks(Mesh mesh, double scale) {
        int origNumElements = mesh.elements().size();
        int numElements = mesh.elements().size() * 2;

        Rock[] rocks = new Rock[numElements];

        for (int i = 0; i < origNumElements; ++i) {
            rocks[i] = mesh.elements().get(i).rock;

            // Scale the capacities
            double phi = rocks[i].getInitialPorosity() * scale;
            double c = rocks[i].getRockHeatCapacity() * scale;

            // Scale the conductivities
            Tensor3D K = rocks[i].getAbsolutePermeability().scale(scale);
            Tensor3D k = rocks[i].getRockHeatConductivity().scale(scale);

            // Same compaction as before
            double cr = rocks[i].getRockCompaction();

            // Append '_dual' to the region names
            String region = new StringBuilder().append(rocks[i].getRegion())
                    .append("_dual").toString();

            rocks[i + origNumElements] = new Rock(phi, cr, K, k, c, region);
        }

        return rocks;
    }

    public Mesh getMesh() {
        return mesh;
    }

    /**
     * Geometry of a dual-continuum mesh
     */
    private static class DualGeometry extends Geometry {

        private static final long serialVersionUID = -5243096748060799070L;

        public DualGeometry(Mesh mesh, DualTopology topology) {
            setSizes(topology);

            int origNumPoints = mesh.points().size();
            int origNumInterfaces = mesh.interfaces().size();
            int origNumElements = mesh.elements().size();
            int origNumNeighbourConnections = mesh.neighbourConnections()
                    .size();

            /*
             * Build point geometry
             */

            for (int i = 0; i < origNumPoints; ++i) {
                CornerPoint p = mesh.points().get(i);

                buildPoint(i, p.coordinate);
                buildPoint(i + origNumPoints, p.coordinate);
            }

            /*
             * Build element geometry
             */

            for (int i = 0; i < origNumElements; ++i) {
                Element el = mesh.elements().get(i);

                buildElement(i, el.volume, el.center);
                buildElement(i + origNumElements, el.volume, el.center);
            }

            /*
             * Build interface geometry
             */

            for (int i = 0; i < origNumInterfaces; ++i) {
                Interface intf = mesh.interfaces().get(i);

                buildInterface(i, intf.area, intf.center, intf.normal);
                buildInterface(i + origNumInterfaces, intf.area, intf.center,
                        intf.normal);
            }

            /*
             * Set transmissibility multipliers
             */

            for (int i = 0; i < origNumNeighbourConnections; ++i) {
                Connection c = mesh.neighbourConnections().get(i);

                double multiplier = c.multiplier;

                setNeighbourMultiplier(i, multiplier);
                setNeighbourMultiplier(i + origNumNeighbourConnections,
                        multiplier);
            }
        }

    }

    /**
     * Topology of a dual-continuum mesh
     */
    private static class DualTopology extends Topology {

        private static final long serialVersionUID = -6903884522074904738L;

        public DualTopology(Mesh mesh) {
            int origNumPoints = mesh.points().size();
            int origNumInterfaces = mesh.interfaces().size();
            int origNumElements = mesh.elements().size();
            int origNumNeighbourConnections = mesh.neighbourConnections()
                    .size();

            int numPoints = origNumPoints * 2;
            int numInterfaces = origNumInterfaces * 2;
            int numElements = origNumElements * 2;
            int numNeighbourConnections = origNumNeighbourConnections * 2;
            int numNonNeighbourConnections = mesh.elements().size();

            setSizes(numPoints, numInterfaces, numElements,
                    numNeighbourConnections, numNonNeighbourConnections);

            /*
             * Build the interface topology
             */

            for (int i = 0; i < origNumInterfaces; ++i) {
                List<CornerPoint> points = mesh
                        .points(mesh.interfaces().get(i));

                int[] primPoint = new int[points.size()];
                for (int j = 0; j < points.size(); ++j)
                    primPoint[j] = points.get(j).index;
                int[] dualPoint = createOffsetList(primPoint, origNumPoints);

                buildInterfaceTopology(i, primPoint);
                buildInterfaceTopology(i + origNumInterfaces, dualPoint);
            }

            /*
             * Build the element topology
             */

            for (int i = 0; i < origNumElements; ++i) {
                List<Interface> interfaces = mesh.interfaces(mesh.elements()
                        .get(i));

                int[] primInterface = new int[interfaces.size()];
                for (int j = 0; j < interfaces.size(); ++j)
                    primInterface[j] = interfaces.get(j).index;
                int[] dualInterface = createOffsetList(primInterface,
                        origNumInterfaces);

                buildElementTopology(i, primInterface);
                buildElementTopology(i + origNumElements, dualInterface);
            }

            /*
             * Build the neighbour connection topology
             */

            for (int i = 0; i < origNumNeighbourConnections; ++i) {
                NeighbourConnection c = mesh.neighbourConnections().get(i);

                int here = mesh.hereInterface(c).index;
                int there = mesh.thereInterface(c).index;

                buildNeighbourConnectionTopology(i, here, there);
                buildNeighbourConnectionTopology(i
                        + origNumNeighbourConnections,
                        here + origNumInterfaces, there + origNumInterfaces);
            }

            /*
             * Build the non-neighbour connection topology
             */

            for (int i = 0; i < origNumElements; ++i) {
                int here = i, there = i + origNumElements;
                buildNonNeighbourConnectionTopology(i, here, there);
            }
        }

        private int[] createOffsetList(int[] list, int offset) {
            int[] copy = list.clone();
            for (int i = 0; i < list.length; ++i)
                copy[i] += offset;
            return copy;
        }
    }
}
