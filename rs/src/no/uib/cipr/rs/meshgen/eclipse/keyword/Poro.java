package no.uib.cipr.rs.meshgen.eclipse.keyword;

import java.io.EOFException;
import java.io.IOException;
import java.util.ArrayList;

import no.uib.cipr.rs.meshgen.eclipse.MapConfiguration;
import no.uib.cipr.rs.meshgen.eclipse.parse.DataInputParser;
import no.uib.cipr.rs.meshgen.eclipse.parse.StringInputStream;

/**
 */
public class Poro implements DataInputParser {

    MapConfiguration config;

    /**
     * @param input
     * @throws IOException
     * @throws EOFException
     */
    public Poro(StringInputStream input) throws EOFException, IOException {
        config = new MapConfiguration();

        this.parse(input);
    }

    /**
     * This class parses the PORO data. the format of the input is specified on
     * page 1268 in the Eclipse Reference Manual 2004a.
     * <p>
     * TODO currently the parsing does not use any information on how many data
     * items to treat. Consequently, a Vector is used for storage while parsing.
     * This might be inefficient and should be revised.
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

        config.putDoubleArray("poro", array);
    }

    public MapConfiguration getConfiguration() {
        return config;
    }

}
