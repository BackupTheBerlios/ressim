package no.uib.cipr.rs.meshgen.triangle;

import java.util.HashMap;

import no.uib.cipr.rs.util.Configuration;

/**
 * 
 * @author roland.kaufmann@cipr.uib.no
 */
class Kind {
    /**
     * List of named colors that this component understand. This should really
     * be handled by a system call.
     */
    @SuppressWarnings("serial")
    private static HashMap<String, Integer> colorMap = 
        new HashMap<String, Integer>() {{
            put("red",      0x00FF0000);
            put("green",    0x0000FF00);
            put("blue",     0x000000FF);
            put("purple",   0x00993366);
        }};
     
    /*
     * Translate a color name into a value. Piping the color name through this
     * function let us specify colors through friendly names instead of hex
     * values.
     */
    private static int colorFromName(String name) throws TriExc {
        // don't care about the case of the name that was specified
        name = name.toLowerCase().trim();
        
        // first see if the color is a recognized symbolic name
        if(colorMap.containsKey(name)) {
            Integer color = colorMap.get(name);
            return color.intValue();
        }
        
        // if the name is a valid hex kind, then use this as ARGB value
        String hexPrefix = "0x";
        if(name.startsWith(hexPrefix)) {
            String hex = name.substring(hexPrefix.length(), name.length());
            try {
                Integer color = Integer.parseInt(hex, 16);
                return color.intValue();
            }
            catch(NumberFormatException nfe) {
                throw TriExc.INVALID_ARGB_VALUE.create(nfe, hex);
            }
            
        }
        
        // otherwise return "bottom" -- throw an error
        throw TriExc.UNKNOWN_COLOR_NAME.create(name);
    }
    
    // integer kind used to mark the edge in the triangle program
    private int marker;
    
    // multiplier that is associated with this particular kind
    private double multiplier;

    /**
     * Construct codes from a tuple of its constituencies; this operation is
     * reserved internal for the algebra. 
     */
    private Kind(int marker, double multiplier) {
        this.marker = marker;
        this.multiplier = multiplier;
    }
    
    // type 1 is hardcoded as a border marker
    // see http://www.cs.cmu.edu/~quake/triangle.markers.html
    static final Kind PARTITION = new Kind(0, 1.0);
    static final Kind BOUNDARY  = new Kind(1, 1.0);
    
    // in case no colors are specified in the file, then use this
    static final Kind REGULAR   = PARTITION;
    
    // find codes for a particular color; used when we create the input files to
    // the triangularizer. there is no predefined colors.
    @SuppressWarnings("serial")
    private static final HashMap<Integer, Kind> colorToCode =
        new HashMap<Integer, Kind>();
        
    // find codes for a particular marker; used when we read the output files
    // from the triangularizer. make sure that the predefined types are always
    // present in this table.
    @SuppressWarnings("serial")
    private static final HashMap<Integer, Kind> markerToCode =
        new HashMap<Integer, Kind>() {{
            put(PARTITION.marker, PARTITION);
            put(BOUNDARY.marker,  BOUNDARY);
        }};
    
    static void setupFractures(Configuration generator) throws TriExc {
        // fractures are identified in a subsection of mesh generation
        Configuration fractures = generator.getConfiguration("Fractures");
        
        // each fracture type will be assigned a running number, starting from
        // the next available in the list (all positive).
        int runningNumber = markerToCode.size();

        // read the list of fracture types
        for(String name : fractures.keys()) {
            Configuration fractureDescr = fractures.getConfiguration(name);
            
            // read the properties associated with this type of fractures
            Integer color = colorFromName(fractureDescr.getString("Color"));
            double multiplier = fractureDescr.getDouble("FracMult");
            
            // create a fracture kind for this fracture
            Kind c = new Kind(runningNumber++, multiplier);
            
            // create associations for this kind so we can locate it again later
            colorToCode.put(color, c);
            markerToCode.put(c.marker, c);
        }
    }
    
    /**
     * Retrieve the fracture kind that has been assigned to this marker. Enable
     * us to relocate fracture types from a triangularizer that only understands
     * integer codes (for example requiring that it is written to an external
     * file).
     * 
     * @param marker
     *  Marker kind; this should be a value returned from the toMarker() method.
     * @return
     *  Kind object that corresponds to the marker. Markers are unique 
     *  identifiers (within a run).
     */
    static Kind fromMarker(int marker) throws TriExc {
        Kind c = markerToCode.get(marker);
        if(c == null) {
            throw TriExc.INCOHERENT_MARKER.create(marker);
        }
        else {
            return c;
        }
    }
    
    /**
     * Map fractures to their transmissibility multiplier based on the color
     * value read from the CAD input file.
     * 
     * @param color
     *  Integer value that represents an ARGB color. If you have a symbolic name
     *  for the color, then use the colorFromName() function to find the value.
     *  If you don't support any colors, then pass null for the color to get the
     *  default value.
     * @return
     *  A kind that represents the fractures drawn in this color.
     * @throws TriExc
     *  If it was not possible to map the color into a fracture type.
     */
    static Kind fromColor(Integer color) throws TriExc {
        if(color == null) {
            return Kind.REGULAR;
        }
        else {
            Kind c = colorToCode.get(color);
            if(c != null) {
                return c;
            }
            else {
                throw TriExc.UNDEFINED_COLOR.create(color);
            }
        }
    }
    
    int toMarker() {
        return marker;
    }
    
    boolean isBoundary() {
        return this.marker == BOUNDARY.marker;
    }
    
    // returns the multiplier assigned to this kind of fracture 
    double multiplier() {
        return multiplier;
    }
}
