package no.uib.cipr.rs.meshgen.triangle;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import no.uib.cipr.rs.geometry.Point3D;
import no.uib.cipr.rs.meshgen.TwoDimensionalPartitionDescription;
import no.uib.cipr.rs.meshgen.partition.Partition2D;
import no.uib.cipr.rs.meshgen.partition.Segment3D;
import no.uib.cipr.rs.util.Configuration;

/**
 * Read triangularization constraints from a set of segments that make out the
 * partition boundaries.
 * 
 * @author roland.kaufmann@cipr.uib.no
 */
class PartitionParser implements Source {
    /**
     * This class contains lines and points that are created from a structural
     * description that is not designed to yield them incrementally but rather
     * build a complete model at once. Thus we keep all the information in
     * arrays that are sent incrementally
     */
    Set<Point3D> points;

    Set<Segment3D> segments;

    /**
     * Create constraints from a partition description. All elements must be
     * fully within one and only one partition.
     * 
     * @param mesh
     *  Configuration section that describes the partitioning of the fine mesh
     *  into coarse cells.
     */
    PartitionParser(Configuration mesh) {
        // verify that we indeed got a partition mesh description
        String type = mesh.getString("type");
        if (!type.equalsIgnoreCase(TwoDimensionalPartitionDescription.class
                .getSimpleName()))
            throw new IllegalArgumentException(mesh.trace()
                    + "Unknown type of partition mesh: '" + type + "'\n");

        // don't create the entire mesh, just the partitioning will
        // suffice. (however, the entire configuration will be read)
        Partition2D partition = new Partition2D(mesh);

        // enumerate the boundaries of the partition which we are
        // going to use as constraints for the triangularization
        this.segments = partition.boundaries(new HashSet<Segment3D>());

        // create a new set of points
        this.points = new HashSet<Point3D>();

        // create set of unique points which are found in the segments
        for (Segment3D segment : segments) {
            // find the start and the end points of this segment
            Point3D p0 = segment.getFirst();
            Point3D p1 = segment.getSecond();

            // add them to the set; it may not be that the set grows
            // for each points -- some lines may have common points
            points.add(p0);
            points.add(p1);
        }

        // list the points that should be added to the problem. for
        // a 10x10 partitioning, there should be 121 points; each
        // domain has a unique upper, left point (which make out 100
        // points). in addition there is the lower left point for the
        // first column and the upper right point for the first row
        // which make out 2 * 10 points) and the ultimately lower
        // right point, totalling 121 points alltogether
        /*
         * for(Point3D point : points) { System.out.println(point); } /
         */// */
        // list the constraints to the console for debugging. for a
        // 10x10 partitioning, there should be 220 segments (each
        // domain has a unique left and upper side, which is 2 * 100
        // lines. The adjacent neighbours make out the right and lower
        // lines except for the last row and column, which make out
        // 2 * 10 lines, which makes it total 220 lines.
        /*
         * for(Segment3D segment : segments) { System.out.println(segment); } /
         */// */
    }

    private Map<Point3D, Integer> readPoints(PointHandler pointHandler)
            throws TriExc {
        // create the map that will receive the index (or handle or whatever
        // for each point, used to write the segments. we know that there
        // is going to be one entry for each point, not more not less
        int numOfPoints = points.size();
        Map<Point3D, Integer> map = new HashMap<Point3D, Integer>(numOfPoints,
                1f);
        pointHandler.prepareForPoints(numOfPoints);

        // pump all the points from the source to this sink
        for (Point3D point : points) {
            // deliver the point to the sink (which will write it to file)
            int handle = pointHandler.onPoint(point.x(), point.y(), point.z());

            // get the handle which we will use to identify the point later
            map.put(point, handle);
        }

        // allow the sink to finalize the poins
        pointHandler.closePoints();

        return map;
    }

    private void readFractures(FractureHandler fractureHandler,
            Map<Point3D, Integer> map) throws TriExc {
        // allow the sink to allocate necessary resources
        int numOfFractures = segments.size();
        fractureHandler.prepareForFractures(numOfFractures);

        for (Segment3D segment : segments) {
            // get each of the ends of the line
            Point3D p0 = segment.getFirst();
            Point3D p1 = segment.getSecond();

            // translate the points to their handle understood by the sink
            int i0 = map.get(p0);
            int i1 = map.get(p1);

            // pass the fracture to the sink for writing. partitions are
            // not true fractures, but rather artifical edges inserted to
            // guide the triangularization
            fractureHandler.onFracture(i0, i1, Kind.PARTITION);
        }

        // note the end of fracture reading from our part
        fractureHandler.closeFractures();
    }

    /**
     * Enumerate over the set of lines that was passed to the constructor.
     * 
     * @param pointHandler
     *            Callback that will receive all the vertices of the constraints
     * @param fractureHandler
     *            Callback that will receive all the edges of the constraints
     */
    public void readAll(PointHandler pointHandler,
            FractureHandler fractureHandler) throws TriExc {
        Map<Point3D, Integer> map = readPoints(pointHandler);
        readFractures(fractureHandler, map);
    }

    public void close() {
        // only here to fit the general signature of a parser; doesn't really
        // do anything
    }
}
