package no.uib.cipr.rs.meshgen.eclipse.keyword;

import java.io.EOFException;
import java.io.IOException;
import java.util.ArrayList;

import no.uib.cipr.rs.meshgen.eclipse.MapConfiguration;
import no.uib.cipr.rs.meshgen.eclipse.parse.DataInputParser;
import no.uib.cipr.rs.meshgen.eclipse.parse.StringInputStream;

/**
 * For "Block Centered Geometry": Z-direction (vertical) grid block sizes for
 * the current input box. (Eclipse Reference Manual 2004a, pp. 538.)
 * <p>
 * TODO Implement parsing of asterisk notation.
 */
public class Dz implements DataInputParser {

    private MapConfiguration config;

    /**
     * @param input
     * @throws IOException
     * @throws EOFException
     */
    public Dz(StringInputStream input) throws EOFException, IOException {
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

        config.putDoubleArray("dy", array);
    }

    public MapConfiguration getConfiguration() {
        return config;
    }

}