package no.uib.cipr.rs.meshgen.eclipse.keyword;

import java.io.EOFException;
import java.io.IOException;
import java.util.ArrayList;

import no.uib.cipr.rs.meshgen.eclipse.MapConfiguration;
import no.uib.cipr.rs.meshgen.eclipse.parse.DataInputParser;
import no.uib.cipr.rs.meshgen.eclipse.parse.StringInputStream;

/**
 * For "Block Centered Geometry": Depths of top faces of grid blocks for the
 * current input box. (Eclipse Reference Manual 2004a, pp. 77.)
 * <p>
 * The keyword is followed by one number for every grid block, specifying the
 * depth at the top of each grid block.
 * <p>
 * Data is terminated by slash (/). Grid blocks are ordered with the X axis
 * index cycling fastest, folled by the Y and Z axis indices.
 * <p>
 * (Eclipse Reference Manual 2004a, pp. 1660).
 * <p>
 * TODO Repeat counts for repeated values using asterisk is not implemented yet.
 */
public class Tops implements DataInputParser {

    private MapConfiguration config;

    /**
     * @param input
     * @throws IOException
     * @throws EOFException
     */
    public Tops(StringInputStream input) throws EOFException, IOException {
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

        config.putDoubleArray("tops", array);
    }

    public MapConfiguration getConfiguration() {
        return config;
    }

}