package no.uib.cipr.rs.meshgen.eclipse.keyword;

import java.io.EOFException;
import java.io.IOException;
import java.util.ArrayList;

import no.uib.cipr.rs.meshgen.eclipse.MapConfiguration;
import no.uib.cipr.rs.meshgen.eclipse.parse.DataInputParser;
import no.uib.cipr.rs.meshgen.eclipse.parse.StringInputStream;

/**
 * This class parses DX for "Cartesian Block Centered Geometry": X-direction
 * grid block sizes for the current input box (or use DXV). The keyword is
 * followed by one positive real number for every grid block.
 * <p>
 * (Eclipse Reference Manual 2004a, pp. 534)
 * <p>
 * TODO We have not yet implemented defaulting DX to corresponding cells below
 * the top plane as described in the Eclipse Reference Manual.
 */
public class Dx implements DataInputParser {

    private MapConfiguration config;

    /**
     * @param input
     * @throws IOException
     * @throws EOFException
     */
    public Dx(StringInputStream input) throws EOFException, IOException {
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

        config.putDoubleArray("dx", array);
    }

    public MapConfiguration getConfiguration() {
        return config;
    }

}