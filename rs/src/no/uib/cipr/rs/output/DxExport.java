package no.uib.cipr.rs.output;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import no.uib.cipr.rs.Paths;
import no.uib.cipr.rs.field.Field;
import no.uib.cipr.rs.fluid.Phase;
import no.uib.cipr.rs.geometry.Element;
import no.uib.cipr.rs.geometry.Interface;
import no.uib.cipr.rs.geometry.Mesh;
import no.uib.cipr.rs.geometry.CornerPoint;
import no.uib.cipr.rs.geometry.Point3D;

/**
 * Export mesh and fields to an OpenDX data file for visualization.
 * 
 * This version exports a simulation grid as a set of hexahedra cells. Cells
 * with a different geometry are expanded to hexahedra, using collapsed sides.
 * The data series are associated with the cells themselves (cell-centered data)
 * 
 * This version is only developed as a proof-of-concept to attempt to export the
 * data to OpenDX. It does not posess production quality!
 * 
 * Run the visualization by copying program.* to the output directory and then
 * execute the command:
 * 
 * dx -data output -edit output/program.net &
 * 
 * @author roland.kaufmann@cipr.uib.no
 */
public class DxExport {
    /**
     * We define a cube from the top and the bottom sides, each specified using
     * a right-hand rule (the normal/thumb pointing out of the cube). Data
     * Explorer has its own definition, which is specified at page 21 in the
     * User's Guide. The following table describes which indices in the dx
     * specification that correspond to which indices in an rs (our)
     * specification. To setup an export of a cube, read points indexed
     * indirectly through this table. It should be safe to assume that this
     * table is a bijection, i.e. that each source point refers to target and
     * that all targets are referred to.
     */
    private final static int DX_CUBE_MAPPING[] = new int[] {
    // DX RS Description
            /* 0 */7, // lower left front
            /* 1 */0, // upper left front
            /* 2 */6, // lower right front
            /* 3 */1, // upper right front
            /* 4 */4, // lower left back
            /* 5 */3, // upper left back
            /* 6 */5, // lower right back
            /* 7 */2 // upper right back
    };

    /**
     * Obvious geometrical definitions included to spare the code for 'magic'
     * constants. Note that we need only the points from two quad planes to make
     * up the definition of a cube.
     */
    private static final int TETRA_SIDES = 4; // tetrahedra

    private static final int HEXA_SIDES = 6; // hexahedra

    private static final int TRIANGLE_POINTS = 3; // triangle

    private static final int QUAD_POINTS = 4; // square

    private static final int QUAD_PLANES_IN_CUBE = 2; // 2*4 = 8

    /**
     * When visualizing cubes we make them slightly transparent so that we can
     * easier see the interior lines.
     */
    private static final double DEFAULT_OPACITY = 0.7;

    /**
     * Entry point for the driver when run in stand-alone mode.
     * 
     * The driver is currently implemented as a procedural program that is
     * called as a stand-alone program from the command-line. In the future,
     * export filter drivers may be run as a biproduct of a successful
     * simulation.
     * 
     * @param args
     *                Command-line arguments for the filter driver. This version
     *                of the driver only understands one argument, namely the
     *                directory of the simulation. The names of the files are
     *                hardcoded relative to this directory. If no directory is
     *                specified, then the current directory is used.
     * @throws IOException
     * @throws FileNotFoundException
     * @throws ClassNotFoundException
     */
    public static void main(String[] args) throws FileNotFoundException,
            IOException, ClassNotFoundException {

        // construct a mesh from the array included in configuration, and other
        // parameters that regulates the construction of the grid.
        ObjectInput in = new ObjectInputStream(new BufferedInputStream(
                new FileInputStream(Paths.GRIDDING_OUTPUT + "/"
                        + Paths.MESH_FILE)));
        Mesh mesh = (Mesh) in.readObject();
        in.close();

        // setup a data file in the same output directory as the rest of the
        // scratch files. if this file already exists, it will be overwritten.
        // in this version, we use a text printer since we are going to write
        // everything in text format anyhow.
        File exportFile = new File(Paths.VISUALIZATION_OUTPUT, "data.dx");
        PrintWriter out = null;
        try {
            out = new PrintWriter(exportFile);
        } catch (FileNotFoundException fnfe) {
            System.err.printf("dx: Could not write to file '%s'%n", exportFile);
            System.exit(6);
        }

        // object counter. whenever we allocate new arrays in the dx file, we
        // increment this value, creating a new identifier. this variable is a
        // property of the output file
        int heapPtr = 0;

        // universe of points. all vertices in the grid will refer to one or
        // more of these points.
        List<CornerPoint> points = mesh.points();

        // header of the points array in the dx file. each point is a tensor of
        // rank one, i.e. a vector, having three dimensions. Cartesian points
        // are
        // always expressed in real numbers.
        int pointsId = ++heapPtr;
        out
                .printf(
                        "# points%n"
                                + "object %d class array type float rank 1 shape 3 items %d data follows%n",
                        pointsId, points.size());

        // dump point universe to stream. coordinates are always specified as a
        // (x,y,z)-tuple. additionally, we dump the index of the point (zero-
        // based) as a comment after each three data elements, for easier
        // debugging
        int pointIndex = 0;
        for (CornerPoint p : points) {
            Point3D coords = p.coordinate;
            out.printf("\t%f\t%f\t%f\t# [%d]%n", coords.x(), coords.y(), coords
                    .z(), p.index);

            // check that the numbering of the points in the export is
            // consistent with the numbering in the file. the visualization
            // engine only cares about the position in the file -- it does not
            // read the comment to figure out the logical number that we have
            // assigned to the node
            if (p.index != pointIndex) {
                System.err.printf("dx: Out-of-sequence point detected! "
                        + "Expected: %d, but got: %d", pointIndex, p.index);
                System.exit(5);
            }
            ++pointIndex;
        }

        // spacing between sections in the file
        out.printf("%n");

        // load the elements. each element represents one grid cell.
        List<Element> elements = mesh.elements();

        // header of the elements array in the dx file. each element is given as
        // a set of points that constitutes the figures. all elements in the
        // same array must be of the same type. in this version, they are all
        // hexahedra which are defined using eight points. the content of the
        // array is just indices into the above array of points (zero-based).
        int elementsId = ++heapPtr;
        out
                .printf(
                        "# elements%n"
                                + "object %d class array type int rank 1 shape 8 items %d data follows%n",
                        elementsId, elements.size());

        // write each interface as its own row in the data file (that it is a
        // row is only a formatting trick. dx doesn't care about that, it reads
        // the tuple size from the header).
        for (Element el : elements) {
            // corners of the cubes are stored in the interfaces
            List<Interface> interfaces = mesh.interfaces(el);

            // volume figures should have at least 4 sides, i.e. the simplex is
            // a tetrahedra, or we have non-linear edges in the figure. we don't
            // handle such a situation.
            if (interfaces.size() < TETRA_SIDES) {
                System.err
                        .printf(
                                "dx: Volume object must have at least %d sides, "
                                        + "which is not the case for element %d (it has %d)%n",
                                TETRA_SIDES, el.index, interfaces.size());
                System.exit(3);
            }
            if (interfaces.size() > HEXA_SIDES) {
                System.err
                        .printf(
                                "dx: Volume object for element %d is more complex "
                                        + "than a hexahedra (it has %d sides). This is not "
                                        + "supported by the driver%n",
                                el.index, interfaces.size());
                System.exit(7);
            }

            // there are always eight points in a hexahedra; the tuples that we
            // are exporting always have this dimension.
            int corners[] = new int[QUAD_PLANES_IN_CUBE * QUAD_POINTS];

            // interfaces are stored as two and two sides that are opposite to
            // eachother. the two first are the top and the bottom, and we use
            // those to get our eight points to define the cube. if we have
            // less that six sides, then two points must be collapsed in one or
            // two of the sides; making it a prism or a tetrahedra. these cases
            // are handled in the point enumeration below.
            Interface top = interfaces.get(0);
            Interface bottom = interfaces.get(1);
            Interface[] sides = new Interface[] { top, bottom };

            // get four points from each of the opposite sides. we could have
            // used two different sides, but that would have given us the same
            // results (we would have to perform a slightly different mapping
            // from our coordinates to the one expected by dx, though).
            for (int i = 0; i < sides.length; ++i) {
                List<CornerPoint> polygon = mesh.points(sides[i]);
                // range check; only triangles and quads are allowed for each
                // side
                if (polygon.size() < TRIANGLE_POINTS) {
                    System.err.printf(
                            "dx: Curvilinear sides not allowed for side %d "
                                    + "in element %d%n", i, el.index);
                    System.exit(4);
                }
                if (polygon.size() > QUAD_POINTS) {
                    System.err
                            .printf(
                                    "dx: Polygon more complex that quad not "
                                            + "allowed for side %d in element %d (it has %d)%n",
                                    i, el.index, polygon.size());
                }

                // get four points from this side. the array is organized such
                // that the first four points stem from the top and the second
                // four points from the bottom. thus, we may use the index of
                // the
                // side iteration, i (which is either 0 or 1) to determine if we
                // are in the range 0..3 or 4..7. The offset to this segment is
                // determined by our inner counter. if we have too few points in
                // the side (i.e. three), then we pad using the last point of
                // the polygon.
                for (int j = 0; j < QUAD_POINTS; ++j) {
                    CornerPoint p = polygon
                            .get(Math.min(j, polygon.size() - 1));
                    // since we made sure that we stored all the points in the
                    // same sequence in the output file as they were stored in
                    // the simulator, then we can use the simulator index
                    // directly here to tell the output file which point we
                    // should use
                    corners[i * 4 + j] = p.index;
                }
            }

            // write out each of the eight points, remapping the index to suit
            // Data Explorer's definition of a cube
            for (int i = 0; i < corners.length; ++i) {
                out.printf("\t%d", corners[DX_CUBE_MAPPING[i]]);
                // out.printf("\t%d", corners[i]);
            }

            // write the index of the element as a debugging comment at the end
            // of each line (to easier find flaws)
            out.printf("\t# [%d]%n", el.index);
        }

        // the logical type of the element is set in an attribute (metadata) to
        // the array. here we tell the visualizer that our eight-tuples are
        // really cube definitions, and that the points refer to indices in the
        // data array that are set as points for the parent element (we could in
        // theory keep the element definitions and changed the points from one
        // timestep to another, if we wanted to have grid changes.
        out.printf("\tattribute\t\"element type\"\tstring\t\"cubes\"%n");
        out.printf("\tattribute\t\"ref\"\t\tstring\t\"positions\"%n");

        // spacing between sections in the file
        out.printf("%n");

        // write opacities to better see the interior of the grid. we use a
        // fixed
        // opacity for every element
        int opacitiesId = ++heapPtr;
        out.printf("# opacities%n"
                + "object %d class array type float items %d data follows%n",
                opacitiesId, elements.size());
        for (int i = 0; i < elements.size(); ++i) {
            out.printf("\t%f\t# [%d]%n", DEFAULT_OPACITY, i);
        }
        out.printf("\tattribute\t\"dep\"\t\tstring\t\"connections\"%n");

        // spacing between sections in the file
        out.printf("%n");

        // lists of the timesteps that written from the simulator and the
        // corresponding identifier of the field that describes data for this
        // timestep. this should really be a list of pairs, which unbelievably
        // is still not a part of the core classes! we may have to sort the list
        // on the timestep value since the file enumeration is not necessarily
        // in lexiographical order
        ArrayList<Double> timeSteps = new ArrayList<Double>();
        ArrayList<Integer> timeFields = new ArrayList<Integer>();

        // enumerate checkpoints from the timestepper that has been exported
        // to the output directory. list them in lexicographical order so that
        // we get them in the order of the timesteps.
        File outputDir = new File(Paths.SIMULATION_OUTPUT);
        List<File> fileList = Arrays.asList(outputDir
                .listFiles(new Paths.OutputDirectoryFileFilter()));
        // TODO This doesn't work, esp. not for parallel runs. As this filter
        // isn't being used, this shouldn't be a cause for concern
        Collections.sort(fileList);

        // dump each checkpoint as an array of data
        for (File checkpoint : fileList) {
            System.err.printf("dx: Field checkpoint: '%s'%n", checkpoint);

            // load the field from this checkpoint
            ObjectInputStream ois = null;
            try {
                ois = new ObjectInputStream(new BufferedInputStream(
                        new FileInputStream(checkpoint)));
            } catch (IOException ioe) {
                System.err.printf("dx: Could not load checkpoint '%s': %s%n",
                        checkpoint, ioe.getMessage());
                System.exit(5);
            }
            Field field = null;
            try {
                field = (Field) ois.readObject();
            } catch (ClassNotFoundException cnfe) {
                System.err
                        .printf(
                                "dx: Error loading field class from checkpoint: '%s'%n",
                                checkpoint);
                System.exit(6);
            } catch (IOException ioe) {
                System.err
                        .printf(
                                "dx: Error reading all field data from file '%s': %s%n",
                                checkpoint, ioe.getMessage());
            }

            // for which point in time was this field created?
            double time = field.getTime();

            // write the header for the data array. we are going to write one
            // scalar (i.e. a tensor of rank 0, containing real numbers) for
            // each
            // element in the grid
            int saturationId = ++heapPtr;
            out
                    .printf(
                            "# saturation for time %f%n"
                                    + "object %d class array type float rank 0 items %d data follows%n",
                            time, saturationId, elements.size());

            // loop through each element (from the one and only grid!) and find
            // the saturation of water for each element in this field. as before
            // we write the element index in a comment behind the real data.
            for (Element el : elements) {
                double saturation = field.getControlVolume(el).getSaturation(
                        Phase.WATER);
                out.printf("\t%f\t# [%d]%n", saturation, el.index);
            }

            // write a metadata attribute for this data array that attaches it
            // to the cells, not the points (i.e. we have cell-centered data).
            out.printf("\tattribute\t\"dep\"\tstring\t\"connections\"%n");

            // spacing between sections
            out.printf("%n");

            // write the header for another data array; this time we want to
            // know
            // the pressure in each of the cells
            int pressureId = ++heapPtr;
            out
                    .printf(
                            "# pressure for time %f%n"
                                    + "object %d class array type float rank 0 items %d data follows%n",
                            time, pressureId, elements.size());

            // same iteration loop as above. the best thing would be to collect
            // all data for the same element and write down to each its section;
            // we could for instance write each section to a temporary file and
            // then paste them together afterwards (or perhaps a memory-mapped
            // section, since we know the number of bytes required for each
            // block when using binary encoding).
            for (Element el : elements) {
                double pressure = field.getControlVolume(el).getPressure();
                out.printf("\t%f\t# [%d]%n", pressure, el.index);
            }

            // pressure is also cell-centered data
            out.printf("\tattribute\t\"dep\"\tstring\t\"connections\"%n");

            // spacing between sections
            out.printf("%n");

            // write a field connecting the data series together with the
            // spatial grid, making it possible to visualize the entire thing.
            // all the components are made up of object identifiers written
            // earlier
            int fieldId = ++heapPtr;
            out.printf("# field for time %f%n" + "object %d class field%n"
                    + "\tcomponent\t\"positions\"\tvalue\t%d%n"
                    + "\tcomponent\t\"connections\"\tvalue\t%d%n"
                    + "\tcomponent\t\"opacities\"\tvalue\t%d%n"
                    + "\tcomponent\t\"data.saturation\"\tvalue\t%d%n"
                    + "\tcomponent\t\"data.pressure\"\tvalue\t%d%n", time,
                    fieldId, pointsId, elementsId, opacitiesId, saturationId,
                    pressureId);

            // spacing between sections
            out.printf("%n");

            // get the timestep index associated with this field, and store the
            // data for the time step in our intermediate list
            timeSteps.add(time);
            timeFields.add(fieldId);
        }

        // output the timeseries consisting of all the fields; note that there
        // may be no fields in the time series. the first item in each row here
        // is just the consecutive index, which has to be a natural running
        // counter from zero. the second item is the identifier of the field to
        // use for this timestep and the third and last item is the timestep
        // itself, which may be a float.
        int seriesId = ++heapPtr;
        out.printf("# timeseries for entire simulation%n"
                + "object %d class series%n", seriesId);
        for (int i = 0; i < timeSteps.size(); ++i) {
            out.printf("\tmember\t%d\tvalue\t%d\tposition\t%f%n", i, timeFields
                    .get(i), timeSteps.get(i));
        }

        // spacing between sections
        out.printf("%n");

        // we are done writing any data to the file
        out.printf("end%n");

        // close the data file, committing changes to disk
        out.close();

        // return with a successful exit code if we were able to export all the
        // information from the reservoir. note that this makes the driver
        // unsuitable to run collocated with other Java programs.
        System.exit(0);
    }
}
