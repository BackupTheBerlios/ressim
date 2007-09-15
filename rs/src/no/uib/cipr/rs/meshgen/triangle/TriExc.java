package no.uib.cipr.rs.meshgen.triangle;

import java.lang.Throwable;

/**
 * Exceptions specific for reading input from the triangle program. The name of
 * the class has been shortened since it is mostly referred to by throwing a
 * prototype, e.g.
 * 
 * throw TriExc.INVALID_COUNT(numOfPoints);
 * 
 * The arguments to each exception is different, and only a runtime check of the
 * argument list is performed. Please try to avoid having errors in the error
 * reporting :-)
 * 
 * @author roland.kaufmann@cipr.uib.no
 */
public class TriExc extends Exception {

    private static final long serialVersionUID = -4240342063282800321L;

    // private constructor so that only prototypes (which are "inside"
    // us) can access it and create new objects
    TriExc(String message, Throwable cause) {
        super(message, cause);
    }

    // helper class that represents prototypes for various failures that
    // may happen during the import from the triangle program. the
    // prototypes contain the message template and an error kind for each
    // kind of different exception.
    public static class TriExcProto {

        // template message that will be written to each of the concrete
        // exceptions that are thrown.
        private String message;

        // private constructor so that we are restricted to the predefined
        // set of errors; only a finite set of errors are possible
        TriExcProto(@SuppressWarnings("unused")
        int code, String message) {
            this.message = message;
        }

        // create a new exception that can be thrown; we do not throw
        // the exception for you (and pollute your callstack and call
        // analysis for the compiler)
        public TriExc create(Throwable cause, Object... args) {
            return new TriExc(String.format(message, args), cause);
        }

        // convenience method in case you are the originator of the error
        public TriExc create(Object... args) {
            return new TriExc(String.format(message, args), null);
        }
    }

    // invalid number of triangles, e.g. negative. parameter is the count
    // that was reported in the header.
    public final static TriExcProto INVALID_COUNT_POINTS = new TriExcProto(
            0x0001, "Invalid number %d of points");

    // invalid number of dimensions for each triangle; parameter is the
    // actual number of dimensions reported
    public final static TriExcProto NOT_TWO_DIM = new TriExcProto(0x0002,
            "Expected two dimensions, not %d, for each point");

    // error locating the specified input file name. the full name is
    // given as an argument to the error
    public final static TriExcProto FILE_NOT_FOUND = new TriExcProto(0x0003,
            "Input file '%s' not found");

    // an edge can only be the border between two triangles (otherwise
    // an extra point should be introduced and it should really be two
    // edges instead of one). the arguments to the error is the indices
    // of the points (unfortunately, the triangle number is unknown at
    // the point we detect this inconsistency).
    public final static TriExcProto MORE_THAN_TWO_NEIGHBOURS = new TriExcProto(
            0x0004, "Edge between points %d and %d occurs more than twice");

    // a triangle cannot be its own neighbour. the arguments to the
    // exception are the indices of the edges.
    public final static TriExcProto OWN_NEIGHBOUR = new TriExcProto(0x0005,
            "Triangle is its own neighbour through edges %d and %d");

    // similar error as the number of points read from file, but this
    // time for triangles. the argument is the number the file claims
    // to have.
    public final static TriExcProto INVALID_COUNT_TRIANGLES = new TriExcProto(
            0x0005, "Invalid number %d of triangles");

    // being a triangle involves having three points, not more not less.
    // (though later we may expand the file to reading simplices in three
    // dimensions having four points).
    public final static TriExcProto NOT_THREE_POINTS = new TriExcProto(0x0006,
            "Expected three points, not %d, for each point");

    // a boundary was detected that was not in the edge collection and
    // therefore cannot be part of any triangle. argument is a pair of
    // point indices to the edge.
    public final static TriExcProto BOUNDARY_NOT_EDGE = new TriExcProto(0x0007,
            "Boundary %s is not part of any triangle");

    // if we were unable to figure out the connection for the fracture,
    // it must be because the triangularizer has not managed to create
    // triangles adjacent to it.
    public final static TriExcProto FRACTURE_NOT_EDGE = new TriExcProto(0x0008,
            "Fracture %s is not part of any triangle");

    // fracture file contains a line for the number of points, but it is
    // supposed to be empty. argument is the declared number of points,
    // but this may be due to an error in the header; data contents are
    // never checked.
    public final static TriExcProto UNEXPECTED_POINTS = new TriExcProto(0x0009,
            "Number of points in fracture file should be 0, not %d");

    // same as the points above but for holes (that come after the
    // fractures)
    public final static TriExcProto UNEXPECTED_HOLES = new TriExcProto(0x000A,
            "Number of holes in fracture file should be 0, not %d");

    // we can only read a positive number of fractures
    public final static TriExcProto INVALID_COUNT_FRACTURES = new TriExcProto(
            0x000B, "Invalid number %d of fractures");

    // boundary flag was not present in the output file
    public final static TriExcProto MUST_HAVE_BOUNDARIES = new TriExcProto(
            0x000C, "Fractures must be declared with a boundary flag");

    // different number of points were sent to the sink than was
    // declared by the source at the beginning of the file.
    public final static TriExcProto POINT_MISMATCH = new TriExcProto(0x000D,
            "Expected %d points but got %d instead");

    // different number of edges were sent to the sink than was
    // declared by the source at the beginning of the file
    public final static TriExcProto FRACTURE_MISMATCH = new TriExcProto(0x000E,
            "Expected %d fractures but got %d instead");

    // the output file name that was generated was disapproved by some
    // reason. the inner exception will contain more detail.
    public final static TriExcProto CANNOT_OPEN_OUTPUT = new TriExcProto(
            0x000F, "Cannot open output file '%s' for writing");

    // if we are getting input of points that are not in a plane, then
    // we cannot handle them yet
    public final static TriExcProto POINTS_NOT_IN_PLANE = new TriExcProto(
            0x0010, "Only two dimensions are supported in current version");

    // if another node to which we are logically linked because we are
    // both working on the same problem fails, then this error will be thrown
    public final static TriExcProto CLUSTER_FAILURE = new TriExcProto(0x0011,
            "Another node in the computational cluster failed");

    // input file is blocked for some reason; the inner exception will
    // contained the detailed cause of the problem
    public final static TriExcProto CANNOT_READ_INPUT = new TriExcProto(0x0012,
            "Cannot open input file '%s' for reading");

    // some input formats have a hierarchial structure; this exception is
    // thrown if the root file/directory exists, but the hierarchy is not
    // as expected.
    public final static TriExcProto CANNOT_READ_SUBSTREAM = new TriExcProto(
            0x0013, "Cannot open stream '%s' in file '%s'");

    // we are passed a file with an extension that we don't understand
    public final static TriExcProto UNKNOWN_FILE_FORMAT = new TriExcProto(
            0x0014, "Unable to infer file format for file '%s'");

    // wrapper class for XML parser errors. the inner exception should
    // contain more information
    public final static TriExcProto CANNOT_PARSE = new TriExcProto(0x0015,
            "Parser error");
    
    // if a color kind is specified but it is not a hexadecimal number
    public final static TriExcProto INVALID_ARGB_VALUE = new TriExcProto(0x0016,
            "'%s' does not represent a value ARGB value");
    
    // a symbolic color name was given, but it does not match the list of
    // pre-registered colors
    public final static TriExcProto UNKNOWN_COLOR_NAME = new TriExcProto(0x0017,
            "Color with name '%s' is unknown");
 
    // we read a marker that we had not written previously
    public final static TriExcProto INCOHERENT_MARKER = new TriExcProto(0x0018,
            "Marker %d is not associated with any type of fracture");
    
    // no fracture type has been mapped for this color
    public final static TriExcProto UNDEFINED_COLOR = new TriExcProto(0x0019,
            "Color kind %d does not map to any fracture type");
}
