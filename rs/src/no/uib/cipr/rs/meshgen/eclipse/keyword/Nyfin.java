package no.uib.cipr.rs.meshgen.eclipse.keyword;

import java.io.EOFException;
import java.io.IOException;
import java.util.ArrayList;

import no.uib.cipr.rs.meshgen.eclipse.MapConfiguration;
import no.uib.cipr.rs.meshgen.eclipse.parse.DataInputParser;
import no.uib.cipr.rs.meshgen.eclipse.parse.StringInputStream;

/**
 * This class parses the NYFIN keyword, see Eclipse Reference Manual 2004a, pp.
 * 1130.
 */
public class Nyfin implements DataInputParser {

    private MapConfiguration config;

    /**
     * @param input
     * @throws EOFException
     * @throws IOException
     */
    public Nyfin(StringInputStream input) throws EOFException, IOException {
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

        config.putDoubleArray("nyfin", array);
    }

    public MapConfiguration getConfiguration() {
        return config;
    }

}