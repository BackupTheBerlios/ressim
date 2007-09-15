package no.uib.cipr.rs.meshgen.dfn;

import java.util.ArrayList;
import java.util.List;

import no.uib.cipr.rs.geometry.Topology;
import no.uib.cipr.rs.meshgen.util.ArrayData;

public class DFNTopology extends Topology {

    private static final long serialVersionUID = 9202193632394396538L;

    public DFNTopology(Parser fileData) {
        setNumPoints(fileData.getNumNodes());
        setNumElements(fileData.getNumActiveCVs());

        setNumInterfaces(fileData.getNumInterfaces());
        buildInterfaceElementTopology(fileData);

        setNumConnections(0, fileData.getNumConnections());
        buildConnectionInterfaceTopology(fileData);
    }

    private void buildInterfaceElementTopology(Parser fileData) {
        List<CV> active = fileData.getActiveCVs();

        for (int elem = 0, interf = 0; elem < active.size(); elem++) {
            CV cv = active.get(elem);

            List<Integer> elementInterfaces = new ArrayList<Integer>();

            if (cv instanceof Node)
                throw new IllegalArgumentException("Node CVs not implemented");
            else if (cv instanceof Segment) {
                Segment e = (Segment) cv;

                int[] l = e.getPointIndices();

                int ni = l[0];
                int nj = l[1];

                // intf 1
                buildInterfaceTopology(interf, new int[] { ni, nj });
                elementInterfaces.add(interf++);

                // intf 2
                buildInterfaceTopology(interf, new int[] { nj });
                elementInterfaces.add(interf++);

                // intf 3
                buildInterfaceTopology(interf, new int[] { nj, ni });
                elementInterfaces.add(interf++);

                // intf 4
                buildInterfaceTopology(interf, new int[] { ni });
                elementInterfaces.add(interf++);

            } else if (cv instanceof Polygon) {
                Polygon e = (Polygon) cv;

                int[] segments = e.getSegmentIndices();

                for (int s : segments) {
                    Segment seg = fileData.getSegment(s);

                    buildInterfaceTopology(interf, seg.getPointIndices());
                    elementInterfaces.add(interf++);
                }

            } else if (cv instanceof Polyhedron)
                throw new IllegalArgumentException(
                        "Polyhedron CVs not implemented");
            else
                throw new IllegalArgumentException("Unknown CV type");

            buildElementTopology(elem, ArrayData
                    .integerListToArray(elementInterfaces));
        }

    }

    private void buildConnectionInterfaceTopology(Parser fileData) {
        for (int i = 0; i < fileData.getNumConnections(); i++) {
            Connection conn = fileData.getConnection(i);

            int cv0 = conn.getCV0();
            int cv1 = conn.getCV1();

            buildNonNeighbourConnectionTopology(i, cv0, cv1);
        }
    }
}
