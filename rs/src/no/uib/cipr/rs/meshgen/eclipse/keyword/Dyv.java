package no.uib.cipr.rs.meshgen.eclipse.keyword;

import java.io.EOFException;
import java.io.IOException;
import java.util.ArrayList;

import no.uib.cipr.rs.meshgen.eclipse.MapConfiguration;
import no.uib.cipr.rs.meshgen.eclipse.parse.DataInputParser;
import no.uib.cipr.rs.meshgen.eclipse.parse.StringInputStream;

/**
 * For "Cartesian Block Centered Geometry": This class parses a Vector of
 * Y-direction grid block sizes or use DY. Simplified version of DY.
 * <p>
 * The keyword is followed by NDIVIY positive real numbers. The j-th number
 * specifies the size in the Y-direction of all the grid blocks with the Y axis
 * index equal to j.
 * <p>
 * (Eclipse Reference Manual 2004a, pp. 537)
 * <p>
 * TODO Must implement repeat counts using the asterisk notation.
 */

public class Dyv implements DataInputParser {

    private MapConfiguration config;

    /**
     * @param input
     * @throws IOException
     * @throws EOFException
     */
    public Dyv(StringInputStream input) throws EOFException, IOException {
        config = new MapConfiguration();

        parse(input);
    }

    public void parse(StringInputStream input) throws IOException {
        ArrayList<Double> l = new ArrayList<Double>();

        for (String v = input.getString().toLowerCase(); !v.equals("/"); v = input
                .getString().toLowerCase())
            l.add(Double.valueOf(v));

        // copy to double array
        double[] array = new double[l.size()];
        for (int i = 0; i < array.length; i++)
            array[i] = l.get(i).doubleValue();

        config.putDoubleArray("dyv", array);
    }

    public MapConfiguration getConfiguration() {
        return config;
    }

}
