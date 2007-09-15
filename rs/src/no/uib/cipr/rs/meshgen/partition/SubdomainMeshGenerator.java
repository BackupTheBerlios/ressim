package no.uib.cipr.rs.meshgen.partition;

import java.util.Map;

import no.uib.cipr.rs.geometry.Element;
import no.uib.cipr.rs.geometry.Geometry;
import no.uib.cipr.rs.geometry.Interface;
import no.uib.cipr.rs.geometry.Mesh;
import no.uib.cipr.rs.geometry.NeighbourConnection;
import no.uib.cipr.rs.geometry.CornerPoint;
import no.uib.cipr.rs.geometry.Point3D;
import no.uib.cipr.rs.geometry.Topology;
import no.uib.cipr.rs.geometry.Vector3D;
import no.uib.cipr.rs.meshgen.MeshGenerator;
import no.uib.cipr.rs.rock.Rock;

public class SubdomainMeshGenerator extends MeshGenerator {

    private DomainTopology topology;

    private Mesh fineMesh;

    private final int domInd;

    /**
     * Creates a subdomain mesh description from the given domain topology,
     * underlying fine mesh, and domain index
     */
    public SubdomainMeshGenerator(DomainTopology topology, Mesh fineMesh, int i) {

        this.domInd = i;

        this.topology = topology;

        this.fineMesh = fineMesh;

        Topology subTopology = new Topology();
        Geometry subGeometry = new Geometry();

        // initialize and create local geometrical objects
        buildLocalPoints(subTopology, subGeometry);
        buildLocalInterfaces(subTopology, subGeometry);
        buildLocalElements(subTopology, subGeometry);
        buildLocalConnections(subTopology, subGeometry);

        // Get the local rock types
        Rock[] rocks = buildLocalRocks();

        // Create the subdomain mesh
        mesh = new Mesh(subGeometry, subTopology, rocks);
    }

    private Rock[] buildLocalRocks() {
        Map<Integer, Integer> eMap = topology.getElementMap(domInd);

        Rock[] rocks = new Rock[eMap.size()];

        // loop over all element indices of this domain and set rock data
        for (Map.Entry<Integer, Integer> entry : eMap.entrySet()) {
            int global = entry.getKey();
            int local = entry.getValue();

            Element e = fineMesh.elements().get(global);

            rocks[local] = e.rock;
        }

        return rocks;
    }

    private void buildLocalConnections(Topology subTopology,
            Geometry subGeometry) {
        Map<Integer, Integer> iMap = topology.getInterfaceMap(domInd);
        Map<Integer, Integer> cMap = topology.getConnectionMap(domInd);

        subTopology.setNumConnections(cMap.size(), 0);
        subGeometry.setNumConnections(cMap.size(), 0);

        for (Map.Entry<Integer, Integer> entry : cMap.entrySet()) {
            int global = entry.getKey();
            int local = entry.getValue();

            NeighbourConnection c = fineMesh.neighbourConnections().get(global);

            int here = iMap.get(mesh.hereInterface(c)index);

            int there = iMap.get(mesh.thereInterface(c)index);

            double multiplier = c.getMultiplier();

            subTopology.buildNeighbourConnectionTopology(local, here, there);
            subGeometry.setNeighbourMultiplier(local, multiplier);
        }
    }

    private void buildLocalElements(Topology subTopology, Geometry subGeometry) {
        Map<Integer, Integer> iMap = topology.getInterfaceMap(domInd);
        Map<Integer, Integer> eMap = topology.getElementMap(domInd);

        subTopology.setNumElements(eMap.size());
        subGeometry.setNumElements(eMap.size());

        for (Map.Entry<Integer, Integer> entry : eMap.entrySet()) {
            int global = entry.getKey();
            int local = entry.getValue();

            Element e = fineMesh.elements().get(global);

            double volume = e.volume;

            Point3D center = e.center;

            int[] interfaces = new int[e.interfaces().size()];
            int j = 0;
            for (Interface i : e.interfaces())
                interfaces[j++] = iMap.get(iindex);

            subTopology.buildElementTopology(local, interfaces);
            subGeometry.buildElement(local, volume, center);
        }
    }

    private void buildLocalInterfaces(Topology subTopology, Geometry subGeometry) {
        // get index maps for current domain
        Map<Integer, Integer> pMap = topology.getPointMap(domInd);
        Map<Integer, Integer> iMap = topology.getInterfaceMap(domInd);

        subTopology.setNumInterfaces(iMap.size());
        subGeometry.setNumInterfaces(iMap.size());

        for (Map.Entry<Integer, Integer> entry : iMap.entrySet()) {
            int global = entry.getKey();
            int local = entry.getValue();

            Interface i = fineMesh.interfaces().get(global);

            double area = i.area;

            Point3D center = i.center;

            Vector3D normal = i.normal;

            // create array of local point indices;
            int[] points = new int[i.points().size()];
            int j = 0;
            for (CornerPoint p : i.points())
                points[j++] = pMap.get(pindex);

            subTopology.buildInterfaceTopology(local, points);
            subGeometry.buildInterface(local, area, center, normal);
        }
    }

    private void buildLocalPoints(Topology subTopology, Geometry subGeometry) {
        // get index maps for current domain
        Map<Integer, Integer> pMap = topology.getPointMap(domInd);

        subTopology.setNumPoints(pMap.size());
        subGeometry.setNumPoints(pMap.size());

        for (Map.Entry<Integer, Integer> entry : pMap.entrySet()) {
            int global = entry.getKey();
            int local = entry.getValue();

            CornerPoint p = fineMesh.points().get(global);

            subGeometry.buildPoint(local, p.coordinate);
        }
    }

}
