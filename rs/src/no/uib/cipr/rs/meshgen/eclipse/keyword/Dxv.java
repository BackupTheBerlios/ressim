package no.uib.cipr.rs.meshgen.eclipse.keyword;

import java.io.EOFException;
import java.io.IOException;
import java.util.ArrayList;

import no.uib.cipr.rs.meshgen.eclipse.MapConfiguration;
import no.uib.cipr.rs.meshgen.eclipse.parse.DataInputParser;
import no.uib.cipr.rs.meshgen.eclipse.parse.StringInputStream;

/**
 * For "Cartesian Block Centered Geometry": This class parses a Vector of
 * X-direction grid block sizes or use DX. Simplified version of DX.
 * <p>
 * The keyword is followed by NDIVIX positive real numbers. The i-th number
 * specifies the size in the X-direction of all the grid blocks with the X axis
 * index equal to i.
 * <p>
 * (Eclipse Reference Manual 2004a, pp. 535)
 * <p>
 */
public class Dxv implements DataInputParser {

    private MapConfiguration config;

    /**
     * @param input
     * @throws IOException
     * @throws EOFException
     */
    public Dxv(StringInputStream input) throws EOFException, IOException {
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

        config.putDoubleArray("dxv", array);
    }

    public MapConfiguration getConfiguration() {
        return config;
    }

}