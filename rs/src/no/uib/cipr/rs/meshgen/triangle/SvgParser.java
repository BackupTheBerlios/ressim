package no.uib.cipr.rs.meshgen.triangle;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.Queue;
import java.util.StringTokenizer;

import no.uib.cipr.rs.util.Pair;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Read lines from a Scalable Vector Graphics (.svg) file.
 * 
 * @author roland.kaufmann@cipr.uib.no
 */
class SvgParser extends XmlVectorParser {
    InputStream stream;

    public SvgParser(String fileName) throws FileNotFoundException {
        // open the stream, prepare it for reading
        stream = new BufferedInputStream(
                new FileInputStream(new File(fileName)));
    }

    public void close() throws IOException {
        // make sure that we return the file handle to the OS when done
        stream.close();
    }

    @Override
    protected void readInput() throws TriExc {
        parseInputStream(stream, new DefaultHandler() {
            // keep the format of the drawing since we need to adjust the
            // coordinates (they start from the bottom of the screen)
            double width;

            double height;

            @Override
            final public void startElement(String uri, String localName,
                    String qName, Attributes attributes) throws SAXException {
                if (qName.equals("svg")) {
                    // main tag specifies the format of the drawing
                    width = getDouble(attributes, "width");
                    height = getDouble(attributes, "height");
                    makeBoundingBox(width, height);
                } else if (qName.equals("line")) {
                    // a single line element. note that we adjust the
                    // height coordinate to fit with our model
                    double x1 = getDouble(attributes, "x1");
                    double y1 = getDouble(attributes, "y1");
                    double x2 = getDouble(attributes, "x2");
                    double y2 = getDouble(attributes, "y2");
                    makeLine(x1, height - y1, x2, height - y2, Kind.REGULAR);
                } else if (qName.equals("polyline")) {
                    // we must specify the list of separators here, if we
                    // pass them in the method call, then the next token
                    // will contain the delimiter as well
                    String points = getString(attributes, "points");
                    StringTokenizer tokenizer = new StringTokenizer(points,
                            ", ", false);

                    // read the list of points from the attribute, building
                    // a queue of tuples. each point is specified with its
                    // x- and y-coordinate separated by comma. each tuple is
                    // separated with space
                    Queue<Pair<Double, Double>> q = new LinkedList<Pair<Double, Double>>();
                    try {
                        while (tokenizer.hasMoreTokens()) {
                            double x = Double
                                    .parseDouble(tokenizer.nextToken());
                            double y = Double
                                    .parseDouble(tokenizer.nextToken());
                            q.add(new Pair<Double, Double>(x, y));
                        }
                    } catch (NumberFormatException nfe) {
                        throw new SAXException(nfe);
                    }

                    // transform two and two points pair-wise into lines
                    // until we have only one point left (which has a start
                    // but no end of the line). this loop will terminate
                    // since we remove one element for each iteration.
                    Pair<Double, Double> p1;
                    Pair<Double, Double> p2;
                    for (;;) {
                        p1 = q.poll();
                        p2 = q.peek();
                        if (p2 == null) {
                            break;
                        }
                        // if are looking at two point (one that we removed
                        // and another in look-ahead), then create a line
                        // for them.
                        makeLine(p1.x(), height - p1.y(), 
                                 p2.x(), height - p2.y(),
                                 Kind.REGULAR);
                    }
                } else {
                    // ignore all other elements
                    // System.err.printf("%s%n", qName);
                }
            }
        });
    }
}
