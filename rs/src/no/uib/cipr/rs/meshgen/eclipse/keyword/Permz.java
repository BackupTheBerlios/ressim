package no.uib.cipr.rs.meshgen.eclipse.keyword;

import java.io.EOFException;
import java.io.IOException;
import java.util.ArrayList;

import no.uib.cipr.rs.meshgen.eclipse.MapConfiguration;
import no.uib.cipr.rs.meshgen.eclipse.parse.DataInputParser;
import no.uib.cipr.rs.meshgen.eclipse.parse.StringInputStream;
import no.uib.cipr.rs.util.Conversions;

/**
 * This class parses the PERMZ data. A unit-conversion is performed from mD to
 * m^2 (SI).
 * <p>
 * See Eclipse Reference Manual 2004a, pp. 1241.
 */
public class Permz implements DataInputParser {

    private MapConfiguration config;

    /**
     * @param input
     * @throws IOException
     * @throws EOFException
     */
    public Permz(StringInputStream input) throws EOFException, IOException {
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
            array[i] = l.get(i).doubleValue() * Conversions.mDarcyInSquareMeter;

        config.putDoubleArray("permz", array);
    }

    public MapConfiguration getConfiguration() {
        return config;
    }

}
