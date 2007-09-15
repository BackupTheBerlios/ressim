package no.uib.cipr.rs.meshgen.triangle;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import no.uib.cipr.rs.geometry.Point3D;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * Base class for reading XML-based vector graphics formats. These format
 * combine points and lines into one and must thus store all lines in memory
 * after one pass through the input and send them piece-wise to the sink
 * afterwards. The concrete parsing is deferred to subclasses.
 * 
 * @author roland.kaufmann@cipr.uib.no
 */
public abstract class XmlVectorParser implements Source {
    /**
     * Helper method to setup a parser for a given input stream, sending
     * events to the specified handler. 
     */
    protected void parseInputStream(InputStream stream, ContentHandler handler)
            throws TriExc {
        try {
            // create a new parser for this file
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();
            XMLReader reader = parser.getXMLReader();

            // don't read the DTD; instead of getting them from the
            // file system, then just return an empty file
            reader.setEntityResolver(new EntityResolver() {
                public InputSource resolveEntity(String publicId,
                        String systemId) {
                    return new InputSource(
                            new ByteArrayInputStream(new byte[0]));
                }
            });

            // we don't need validation for this document (assume that
            // the schema is fixed and hardcoded for the handler)
            reader.setFeature("http://xml.org/sax/features/validation", false);

            // send events to this sink
            reader.setContentHandler(handler);

            // start parsing!
            reader.parse(new InputSource(stream));
        } catch (Exception e) {
            if (e instanceof SAXException) {
                SAXException se = (SAXException) e;                
                if (se.getException() instanceof TriExc) {
                    throw (TriExc) se.getException();
                }
            }
            throw TriExc.CANNOT_PARSE.create(e);
        }
    }
    
    /**
     * Helper method that reads a named attribute from a tag and return its
     * value interpreted as a double-precision number.
     * 
     * @param attr
     *            Set of attributes that is passed to the handler
     * @param name
     *            Name of the attribute
     * @return Value within the attribute. An exception will be thrown if the
     *         attribute is not a double value, or if it is missing from the
     *         set.
     */
    protected double getDouble(Attributes attr, String name) {
        int index = attr.getIndex(name);
        String text = attr.getValue(index);
        if (text.endsWith("cm")) {
            text = text.substring(0, text.length() - "cm".length());
        }
        double value = Double.parseDouble(text);
        return value;
    }

    /**
     * Helper method that reads a named attribute from a tag and return the
     * value interpreted as a string.
     * 
     * @param attr
     *            Set of attributes that is passed to the handler
     * @param name
     *            Name of the attribute
     * @return Value within the attribute. Null is returned if the attribute is
     *         not set.
     */
    protected String getString(Attributes attr, String name) {
        int index = attr.getIndex(name);
        String str = attr.getValue(index);
        return str;
    }

    // list of lines discovered in the file. we'll have to keep them in
    // memory since the further processing demands that we pass all the
    // points first and then the composition into lines (whereas we have
    // both coded in one object).
    final private ArrayList<Line> lines = new ArrayList<Line>();
    
    /**
     * Helper method to create a line from four coordinates (which seems
     * to be the standard way of specifying a line).  
     */
    protected void makeLine(double x1, double y1, double x2, double y2, Kind c) {
        Line line = new Line(new Point3D(x1, y1, 0d), 
                new Point3D(x2, y2, 0d), c);
        // raise this line object to the outer kind
        lines.add(line);
        //System.err.printf("Line: %s%n", line);
    }    

    /**
     * Helper method to create a proper bounding box from the height and
     * the width of the drawing specified. Origo is implicitly one of the
     * corners of the bounding box.
     * 
     * @param width
     * @param height
     */
    protected void makeBoundingBox(double width, double height) {
        // create each of the corners based on these sizes;
        // all boxen start at origo (0, 0)
        Point3D lowerLeft = new Point3D(0d, 0d, 0d);
        Point3D lowerRight = new Point3D(width, 0d, 0d);
        Point3D upperLeft = new Point3D(0d, height, 0d);
        Point3D upperRight = new Point3D(width, height, 0d);

        // compose each of the four lines that make out the
        // box for these points
        boundingBox = new Line[] {
                new Line(lowerLeft, upperLeft, Kind.BOUNDARY),
                new Line(upperLeft, upperRight, Kind.BOUNDARY),
                new Line(upperRight, lowerRight, Kind.BOUNDARY),
                new Line(lowerRight, lowerLeft, Kind.BOUNDARY) };
        //for(int i = 0; i < boundingBox.length; i++)
        //    System.err.printf("Boundary: %s%n", boundingBox[i]);
    }
        
    // bounding box that was finally selected as the master layout
    protected Line[] boundingBox = null;
    
    /**
     * One pass through the input file. This method is expected to call
     * the parseInputStream() helper method to launch the parsing, and
     * fill the lines and boundingBox members with appropriate values. 
     */
    protected abstract void readInput() throws TriExc;
    
    public void readAll(PointHandler pointHandler,
            FractureHandler fractureHandler) throws TriExc {
        // read the bounding box and the drawing of fractures
        readInput();

        // merge the two lists since the handlers view the bounding box
        // as lines as well, although with a flag
        lines.addAll(Arrays.asList(boundingBox));

        // map which contains the indices of each point (since only the
        // index, not the point itself can be sent to the tringulation)
        Map<Point3D, Integer> map = new HashMap<Point3D, Integer>(2 * lines
                .size());

        // iterate through the list, raising all the points. each line
        // have two points associated with them
        pointHandler.prepareForPoints(2 * lines.size());
        try {
            for (Line line : lines) {
                for (Point3D p : line.points) {
                    int i = pointHandler.onPoint(p.x(), p.y(), p.z());
                    map.put(p, i);
                }
            }
        } finally {
            pointHandler.closePoints();
        }

        // iterate through all the lines a second time, this time
        // generating lines for each of them
        fractureHandler.prepareForFractures(lines.size());
        try {
            for (Line line : lines) {
                // get the indices for each of the points on the line
                // (which will be returned and put in the map by the
                // loop above
                Point3D p1 = line.points[0];
                Point3D p2 = line.points[1];
                int i1 = map.get(p1);
                int i2 = map.get(p2);

                // raise the event, telling the triangulation that we
                // have a constraint along this line
                fractureHandler.onFracture(i1, i2, line.kind);
            }
        } finally {
            fractureHandler.closeFractures();
        }
    }    
}
