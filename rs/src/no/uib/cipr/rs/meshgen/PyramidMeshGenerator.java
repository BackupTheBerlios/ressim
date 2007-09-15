package no.uib.cipr.rs.meshgen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import no.uib.cipr.rs.geometry.Connection;
import no.uib.cipr.rs.geometry.Element;
import no.uib.cipr.rs.geometry.Geometry;
import no.uib.cipr.rs.geometry.Interface;
import no.uib.cipr.rs.geometry.Mesh;
import no.uib.cipr.rs.geometry.NeighbourConnection;
import no.uib.cipr.rs.geometry.CornerPoint;
import no.uib.cipr.rs.geometry.Point3D;
import no.uib.cipr.rs.geometry.Tetrahedra;
import no.uib.cipr.rs.geometry.Topology;
import no.uib.cipr.rs.geometry.Triangle;
import no.uib.cipr.rs.rock.Rock;
import no.uib.cipr.rs.util.Configuration;
import no.uib.cipr.rs.util.OrderedPair;
import no.uib.cipr.rs.util.Tolerances;

/**
 * Refine a hexahedral mesh into pyramids, using the center of the cell as the
 * apex for each of the cells in the fine grid.
 * 
 * @author roland.kaufmann@cipr.uib.no
 */
public class PyramidMeshGenerator extends MeshGenerator {
    public PyramidMeshGenerator(Configuration config) {
        // read the coarse mesh recursively from a sub-configuration
        Mesh coarse = MeshGenerator.generate(config);

        // get key data from the coarse mesh
        int numCoarseElems = coarse.elements().size();
        int numCoarseIntfs = coarse.interfaces().size();
        int numCoarsePts = coarse.points().size();
        int numCoarseConns = coarse.neighbourConnections().size();

        // we are going to add the center point of every cell as a new corner
        int numFinePts = numCoarsePts + numCoarseElems;

        // each side of the hexahedra is converted into its own pyramid, which
        // means that we'll have six times (the number of sides in the old mesh)
        int numFineElems = 0;
        for (Element e : coarse.elements())
            numFineElems += mesh.interfaces(e).size();

        // each new element have four new sides
        int numNewFineIntfs = 0;
        for (Interface i : coarse.interfaces()) {
            // there is one ridge between each pair of points, and then one back
            // to the beginning again, so there is as many as there are points
            numNewFineIntfs += mesh.points(i).size();
        }
        // they inherit the 'floor' from the coarse mesh
        int numFineIntfs = numCoarseIntfs + numNewFineIntfs;

        // for each new interface we need half a connection, thus we add half
        // the number of new interfaces (remember that we're multiplying with
        // the number of *new* elements here).
        int numFineConns = numCoarseConns + numNewFineIntfs / 2;

        // we are going to expand every element of the coarse mesh to its number
        // of interfaces; count the number of new non-neighbour connections
        // we'll need for that
        int numFineNonNeighbours = 0;
        for (Connection c : coarse.nonNeighbourConnections()) {
            int hereCnt = mesh.interfaces(mesh.here(c)).size();
            int thereCnt = mesh.interfaces(mesh.there(c)).size();

            numFineNonNeighbours += hereCnt * thereCnt;
        }

        // create containers to hold the new mesh
        Geometry geometry = new Geometry();
        Topology topology = new Topology();

        // declare the size of the mesh
        topology.setSizes(numFinePts, numFineIntfs, numFineElems, numFineConns,
                numFineNonNeighbours);
        geometry.setSizes(topology);
        Rock[] rocks = new Rock[numFineElems];

        // include all the old points verbatim
        for (int i = 0; i < numCoarsePts; i++) {
            CornerPoint p = coarse.points().get(i);
            assert (p.index == i);
            Point3D coord = p.coordinate;
            geometry.buildPoint(i, coord);
        }

        // append all the old cell centers afterwards
        for (int i = 0; i < numCoarseElems; i++) {
            Element e = coarse.elements().get(i);
            Point3D coord = e.center;
            int ndx = numCoarsePts + i;
            geometry.buildPoint(ndx, coord);
        }

        // running counter of interfaces and elements created
        int fineElem = 0;
        int fineIntf = 0;
        int fineConn = 0;

        // map of ridges for which we have created internal interfaces; the two
        // interfaces that shares the same ridge should
        Map<OrderedPair<Integer>, List<Integer>> ridges = new HashMap<OrderedPair<Integer>, List<Integer>>();

        // list of subelements for each element, stored as the first index of
        // the subelements for this coarse element (thus we need one more at the
        // end to terminate the sequence properly)
        int[/* coarse */] subElems = new int[numCoarseElems + 1];

        // map between old interfaces (sides of the cube) and new interfaces
        // (floors of the pyramids)
        int[/* coarse */] coarseToFineIntf = new int[numCoarseIntfs];

        // refine each coarse element one-by-one
        for (int i = 0; i < numCoarseElems; i++) {
            // bind the index into the element set to an object reference
            Element coarseElem = coarse.elements().get(i);

            // each pyramid inherits the rock from the old coarse hexahedra
            // (the petrophysic doesn't change for the hexahedra over-all)
            Rock rock = coarseElem.rock;

            // find the center of the element (see the code above on how its
            // index into the new point collection is calculated
            int apex = numCoarsePts + i;
            Point3D coarseCenter = coarseElem.center;

            // we don't need all the ridges from the other elements, nor do
            // we want to connect internal interfaces from one pyramid to
            // the interfaces which shares the ridge, but which are in other
            // elements
            ridges.clear();

            // mark the number of elements written so far; this is the starting
            // point for this subsequence
            subElems[i] = fineElem;

            // double check the sum of fine element volumes
            double residualVolume = coarseElem.volume;

            // each side of the coarse element will form the basis for a new
            // element in the fine mesh
            for (int j = 0; j < mesh.interfaces(coarseElem).size(); j++) {
                Interface coarseIntf = mesh.interfaces(coarseElem).get(j);

                // indices of the interfaces generated by this
                int[] intfNdx = new int[mesh.points(coarseIntf).size() + 1];

                // collect indices of each point in the floor interface (the old
                // side); we'll use these later for building the last interface
                int[] ptNdx = new int[mesh.points(coarseIntf).size()];

                // collect all the points of the floor of the pyramid; together
                // with the apex this will be the point cloud of this new
                // element
                Point3D[] fineCoords = new Point3D[mesh.points(coarseIntf).size() + 1];

                // each ridge in a coarse side will form basis line of a new
                // interface
                for (int k = 0; k < mesh.points(coarseIntf).size(); k++) {
                    // bind a reference to this point
                    CornerPoint p1 = mesh.points(coarseIntf).get(k);

                    // we need this to rebuild the floor interface afterwards
                    ptNdx[k] = p1.index;
                    fineCoords[k] = p1.coordinate;

                    // assuming that the points in the interface is specified in
                    // "counter-clockwise" order to generate outward normals in
                    // a right-hand coordinate system, we need to reverse the
                    // ridge going from this point to the next to have the
                    // normal point in the correct direction for the triangle
                    // side
                    int end = (k + 1) % mesh.points(coarseIntf).size();
                    CornerPoint p2 = mesh.points(coarseIntf).get(end);

                    // add the interface we are creating for the ridge that
                    // formed its basis
                    OrderedPair<Integer> ridge = new OrderedPair<Integer>(
                            p1.index, p2.index);
                    List<Integer> sharers;
                    if (!ridges.containsKey(ridge)) {
                        sharers = new ArrayList<Integer>(2);
                        ridges.put(ridge, sharers);
                    } else {
                        sharers = ridges.get(ridge);
                    }
                    sharers.add(fineIntf);

                    // build a triangle for this particular ridge
                    Triangle t = new Triangle(p2.coordinate, p1.coordinate,
                            coarseCenter);
                    geometry.buildInterface(fineIntf, t.getArea(), t
                            .getCenter(), t.getNormal());
                    topology.buildInterfaceTopology(fineIntf, new int[] {
                            p2.index, p1.index, apex });
                    intfNdx[k] = fineIntf;
                    fineIntf++;

                }

                // generate the bottom floor of the pyramid based on the
                // interface we copy the information verbatim from the old
                // interface, though we'll have to loop through the point
                // collection to create an array instead of a list as which it
                // is provided
                geometry.buildInterface(fineIntf, coarseIntf.area,
                        coarseIntf.center, coarseIntf.normal);
                topology.buildInterfaceTopology(fineIntf, ptNdx);
                intfNdx[intfNdx.length - 1] = fineIntf;
                coarseToFineIntf[coarseIntf.index] = fineIntf;
                fineIntf++;

                // add the center as the apex of the new pyramid
                fineCoords[fineCoords.length - 1] = coarseCenter;

                // assume that we are building pyramids; otherwise the code
                // below turns out wrong
                assert (4 <= fineCoords.length && fineCoords.length <= 5);

                // helper class to calculate the volume of the two tetrahedras
                // which is the same as the pyramid's
                Point3D[] p = fineCoords;
                double fineVolume;
                // TODO: Implement more generic tetrahedralization
                if (fineCoords.length == 4) {
                    Tetrahedra t = new Tetrahedra(p[0], p[1], p[2], p[3]);
                    fineVolume = t.getVolume();
                } else {
                    Tetrahedra t1 = new Tetrahedra(p[0], p[1], p[2], p[4]);
                    Tetrahedra t2 = new Tetrahedra(p[0], p[2], p[3], p[4]);
                    fineVolume = t1.getVolume() + t2.getVolume();
                }
                Point3D fineCenter = Point3D.center(p);

                // subtract the volume from the coarse element; we are now left
                // with the residual of all the remaining pyramids
                residualVolume -= fineVolume;

                // build the fine element itself, now that we've created all its
                // constituents
                geometry.buildElement(fineElem, fineVolume, fineCenter);
                topology.buildElementTopology(fineElem, intfNdx);
                rocks[fineElem] = rock;
                fineElem++;
            }

            // we don't want any volume to go missing after refinement
            assert (residualVolume < Tolerances.smallEps);

            // generate connections between all the internal interfaces;
            // each ridge will be the origin of a connection
            for (List<Integer> sharers : ridges.values()) {
                // there should only be two interfaces that shares a ridge
                // (within one element), as there can only be two interfaces
                // in a connection
                assert (sharers.size() == 2);

                // get the two parties in the connection and register them
                // in the topology (using a first-come, first-serve basis
                // for the ordering between the two).
                int here = sharers.get(0);
                int there = sharers.get(1);

                topology
                        .buildNeighbourConnectionTopology(fineConn, here, there);
                fineConn++;
            }
        }

        // finalize the element index list be writing the total count at the
        // last position of the array
        subElems[subElems.length - 1] = fineElem;

        // restore all old connections that was between neighbouring coarse
        // elements; topology between coarse elements doesn't change
        for (NeighbourConnection c : coarse.neighbourConnections()) {
            // get the indices in the old mesh
            int here = mesh.hereInterface(c).index;
            int there = mesh.thereInterface(c).index;

            // translate to indices in the fine mesh
            here = coarseToFineIntf[here];
            there = coarseToFineIntf[there];

            // include this neighbour connection in the new, fine mesh
            topology.buildNeighbourConnectionTopology(fineConn, here, there);
            fineConn++;
        }

        // running index of the non-neighbour connections in the fine mesh
        int nonNeighbour = 0;

        // expand each non-neighbour connection into the complete graph between
        // subelements in the fine mesh
        for (Connection c : coarse.nonNeighbourConnections()) {
            int here = mesh.here(c).index;
            int there = mesh.there(c).index;

            // loop through the indices for all of the subelements; since we
            // have expanded each coarse element in sequence, the element
            // indices in the fine mesh will also be located next to eachother
            for (int i = subElems[here]; i < subElems[here + 1]; i++) {
                for (int j = subElems[there]; j < subElems[there + 1]; j++) {
                    topology.buildNonNeighbourConnectionTopology(nonNeighbour,
                            subElems[i], subElems[j]);
                    nonNeighbour++;
                }
            }
        }

        // make sure that we wrote the number of elements that we expected
        assert (fineIntf == numFineIntfs);
        assert (fineElem == numFineElems);
        assert (fineConn == numFineConns);
        assert (nonNeighbour == numFineNonNeighbours);

        // return the new fine mesh
        mesh = new Mesh(geometry, topology, rocks);
    }
}
