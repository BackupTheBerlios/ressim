package no.uib.cipr.rs.meshgen.eclipse.keyword;

import java.io.EOFException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import no.uib.cipr.rs.meshgen.eclipse.MapConfiguration;
import no.uib.cipr.rs.meshgen.eclipse.parse.DataInputParser;
import no.uib.cipr.rs.meshgen.eclipse.parse.StringInputStream;

/**
 * For "Corner Point3D Geometry": Defines the lines that contain all grid block
 * corner points for each (I, J) and for each reservoir in the grid.
 * <p>
 * Either COORD and ZCORN or GDFILE are required for corner point geometry.
 * <p>
 * The keyword line is followed by (NDIVIX + 1)(NDIVIY + 1)NUMRES coordinate
 * lines, each specified by 2 points, each consisting of 3 values.
 * <p>
 * (Eclipse Reference Manual 2004a, pp. 425)
 * <p>
 * TODO Implement parsing of asterisk notation
 */
public class CoordConfig implements DataInputParser {

    private MapConfiguration config;

    /**
     * @param input
     * @throws IOException
     * @throws EOFException
     */
    public CoordConfig(StringInputStream input) throws EOFException,
            IOException {
        config = new MapConfiguration();

        parse(input);
    }

    /*
     * (non-Javadoc)
     * 
     * @see rs.filter.eclipse.Data#parse(rs.util.StringInputStream)
     */
    public void parse(StringInputStream input) throws IOException {
        List<Double> l = new ArrayList<Double>();

        for (String v = input.getString().toLowerCase(); !v.equals("/"); v = input
                .getString().toLowerCase())
            l.add(Double.valueOf(v));

        // copy to double array
        double[] array = new double[l.size()];
        for (int i = 0; i < array.length; i++)
            array[i] = l.get(i);

        config.putDoubleArray("coord", array);
    }

    public MapConfiguration getConfiguration() {
        return config;
    }

}