package no.uib.cipr.rs.meshgen.triangle;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.zip.ZipFile;

import no.uib.cipr.rs.util.Pair;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Reads lines representing fractures from an OpenOffice Draw file; kind of a
 * poor man's CAD approach. Parsing is not as robust as it could be; the
 * namespace alias of the elements are hardcoded in the kind, which could
 * potentially be a problem if a source other than OpenOffice writes the files
 * (however, it makes the program easier to grasp).
 * 
 * @author roland.kaufmann@cipr.uib.no
 */
class OpenOfficeParser extends XmlVectorParser implements Source {
    // OpenOffice files are really zip files containing text-encoded data
    ZipFile zip;

    // open the file and make it ready for parsing
    public OpenOfficeParser(String fileName) throws TriExc {
        // open the file and have it ready for parsing
        try {
            this.zip = new ZipFile(new File(fileName), ZipFile.OPEN_READ);
        } catch (IOException ioe) {
            throw TriExc.CANNOT_READ_INPUT.create(ioe, fileName);
        }
    }

    // make sure that all handles are freed when we're done using the filter
    public void close() {
        try {
            zip.close();
        } catch (IOException ioe) {
            // what to do? we're trying to recover here!
        }
    }

    /**
     * Parse a given stream, handing events to the designated handler.
     * 
     * @param name
     *            Name of the substream that is to be read within the archive,
     *            e.g. "styles.xml", "content.xml"
     * @param handler
     *            Callback that will receive the events from the parsing; each
     *            handler is a composite callback that processes the data within
     *            the file.
     */
    private void read(String name, ContentHandler handler) throws TriExc {
        try {
            // open the stream from the archive
            InputStream stream = zip.getInputStream(zip.getEntry(name));
            try {
                parseInputStream(stream, handler);
            } finally {
                stream.close();
            }
        } catch (TriExc te) {
            throw te;
        } catch (Exception e) {
            // transform all other to our application-defined exception type
            throw TriExc.CANNOT_READ_SUBSTREAM.create(e, name, zip.getName());
        }
    }

    /**
     * Read contents of a drawing file, i.e. fractures.
     */
    private void readContent() throws TriExc {
        // start reading the content file, doing the kind below with each
        // of the discovered elements within that file
        read("content.xml", new DefaultHandler() {
            // name of the style and parent currently being parsed
            private String currentStyle;
            private String currentParent;
            
            // map from the name of the style to the ARGB value of the color
            // that is defined for it
            private HashMap<String, Integer> styleColors =
                new HashMap<String, Integer>();
            
            @Override
            final public void startElement(String uri, String localName,
                    String qName, Attributes attributes) throws SAXException {
                try {
                    if (qName.equals("style:style")) {
                        
                        // change the name of the current style
                        currentStyle = getString(attributes, "style:name");
                        currentParent = getString(attributes, "style:parent-name");
                    }
                    // style section introduces new styles and put them in scope
                    // whereas the grunt work is done by a sub-tag
                    else if(qName.equals("style:graphic-properties")) {                                                    
                        // read the textual description of the color
                        String color = getString(attributes, "svg:stroke-color");
                        
                        // if no color is defined for this style, then use the color
                        // of the parent
                        if(color != null) {
                            // strip away the initial hash and convert the numerical
                            // hash kind into a value variable
                            if(color.startsWith("#")) {
                                color = color.substring(1, color.length());
                                try {
                                    // ARGB value of the color for the style; 
                                    // this is our target
                                    Integer argb = Integer.parseInt(color, 16);
                                    
                                    // associate the style name with the color
                                    styleColors.put(currentStyle, argb);
                                }
                                catch(NumberFormatException nfe) {
                                    // note that we wrap in SAX exception to be able
                                    // to throw it from this method
                                    throw new SAXException(
                                            TriExc.INVALID_ARGB_VALUE.create(nfe, color));
                                }
                            }
                            else {
                                throw new SAXException(
                                        TriExc.UNKNOWN_COLOR_NAME.create(color));
                            }
                        }
                        
                    } 
                    // we are only interested in lines that are drawn
                    else if (qName.equals("draw:line")) {
                        // read each of the coordinates of the line
                        double x1 = getDouble(attributes, "svg:x1");
                        double y1 = getDouble(attributes, "svg:y1");
                        double x2 = getDouble(attributes, "svg:x2");
                        double y2 = getDouble(attributes, "svg:y2");
                        
                        // read the style of the line, and map it to the color
                        String style = getString(attributes, "draw:style-name");
                        Integer color = styleColors.get(style);
                        Kind kind = Kind.fromColor(color);
    
                        // add this line to the collection of existing lines
                        makeLine(x1, y1, x2, y2, kind);
                    }
                }
                catch(TriExc te) {
                    throw new SAXException(te);
                }                
            }
            
            @Override
            final public void endElement(String uri, String localName,
                    String qName) throws SAXException {
                if (qName.equals("style:style")) {
                    // if no color was assigned to this style, then use the
                    // color of the parent style
                    if(!styleColors.containsKey(currentStyle) && currentParent != null) {
                        Integer argb = styleColors.get(currentParent);
                        styleColors.put(currentStyle, argb);
                    }
                    
                    // when we see the end element for this name, then the style
                    // has gone out of scope and is no longer assignable
                    currentStyle = null;
                    currentParent = null;
                }                
            }
        });
    }

    /**
     * Read the page format to figure out the bounding box of the problem.
     */
    private void readStyles() throws TriExc {
        read("styles.xml", new DefaultHandler() {
            // name of the current layout that we are parsing
            private String currentLayout;
            
            // various styles that are possible to select, mapped to their name
            // each style contain a set of lines that make out the bounding box
            private HashMap<String, Pair<Double, Double>> layouts = 
                new HashMap<String, Pair<Double, Double>>();
            
            @Override
            final public void startElement(String uri, String localName,
                    String qName, Attributes attributes) throws SAXException {
                
                if (qName.equals("style:page-layout") || // 2.0
                        qName.equals("style:page-master")) { // 1.0
                    // when the page layout tag is opened, note the name
                    // of the layout so that we can attach the child
                    // properties to this name when we encounter them
                    currentLayout = getString(attributes, "style:name");
                } else if (qName.equals("style:page-layout-properties") || // 2.0
                        qName.equals("style:properties")) { // 1.0
                    // only bother to read the format if we are currently
                    // parsing a page -- don't bother to parse properties
                    // for other elements
                    if (currentLayout != null) {
                        // read the format of the box
                        double width = getDouble(attributes, "fo:page-width");
                        double height = getDouble(attributes, "fo:page-height");

                        // give a name to this box; there is supposed to be
                        // only one properties tag within a style; if there
                        // are more, then only the last will be found
                        Pair<Double, Double> extent = new Pair<Double, Double>(
                                width, height);
                        layouts.put(currentLayout, extent);
                    }
                } else if (qName.equals("style:master-page")) {
                    // when the master-layout is specified, then get the
                    // name of the layout that is the default (i.e. for
                    // the entire page, and set that as our bounding box
                    String layout = getString(attributes, "style:name");
                    if (layout.equals("Default")) {
                        String name = attributes.getValue(attributes
                                .getIndex("style:page-layout-name")); // 2.0
                        if (name == null) {
                            name = attributes.getValue(attributes
                                    .getIndex("style:page-master-name")); // 1.0
                        }
                        // get the extent of the style chosen and create
                        // a bounding box for that information
                        Pair<Double, Double> extent = layouts.get(name);
                        makeBoundingBox(extent.x(), extent.y());
                    }
                }
            }

            @Override
            final public void endElement(String uri, String localName,
                    String qName) throws SAXException {
                // when the section that specifies the page master ends,
                // then stop parsing properties and creating boxen for them
                if (qName.equals("style:page-layout")
                        || qName.equals("style:page-master")) {
                    currentLayout = null;
                }
            }
        });
    }

    @Override
    protected void readInput() throws TriExc {
        // OpenOffice drawings keep the information in two different
        // substreams, both of which should be parsed
        readStyles();
        readContent();
    }
}
