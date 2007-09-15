package no.uib.cipr.rs.meshgen.eclipse.keyword;

import java.io.EOFException;
import java.io.IOException;
import java.util.ArrayList;

import no.uib.cipr.rs.meshgen.eclipse.MapConfiguration;
import no.uib.cipr.rs.meshgen.eclipse.parse.DataInputParser;
import no.uib.cipr.rs.meshgen.eclipse.parse.StringInputStream;

/**
 * For "Corner Point3D Geometry": Point3D depths of grid block corners. Either
 * COORD and ZCORN or GDFILE are required for corner point geometry.
 * <p>
 * This class parses the ZCORN keyword. ZCORN enables the depths of each cell
 * corner to be specified separately. The keyword line is followed by 2*NDIVIX *
 * 2*NDIVIY * 2*NDIVIZ values.
 * <p>
 * (Eclipse Reference Manual 2004a, pp. 2061).
 * <p>
 * TODO Implement parsing of asterisk notation.
 */

public class ZcornConfig implements DataInputParser {

    private MapConfiguration config;

    /**
     * @param input
     * @throws IOException
     * @throws EOFException
     */
    public ZcornConfig(StringInputStream input) throws EOFException,
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
        ArrayList<Double> l = new ArrayList<Double>();

        for (String v = input.getString().toLowerCase(); !v.equals("/"); v = input
                .getString().toLowerCase())
            l.add(Double.valueOf(v));

        // copy to double array
        double[] array = new double[l.size()];
        for (int i = 0; i < array.length; i++)
            array[i] = l.get(i).doubleValue();

        config.putDoubleArray("zcorn", array);
    }

    public MapConfiguration getConfiguration() {
        return config;
    }

}