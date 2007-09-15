package no.uib.cipr.rs.meshgen.eclipse.keyword;

import java.io.EOFException;
import java.io.IOException;
import java.util.ArrayList;

import no.uib.cipr.rs.meshgen.eclipse.MapConfiguration;
import no.uib.cipr.rs.meshgen.eclipse.parse.DataInputParser;
import no.uib.cipr.rs.meshgen.eclipse.parse.StringInputStream;

/**
 * This class parses the NNC keyword. See Eclipse Reference Manual.
 */
public class Nnc implements DataInputParser {

    private MapConfiguration config;

    /**
     * @param input
     * @throws IOException
     * @throws EOFException
     */
    public Nnc(StringInputStream input) throws EOFException, IOException {
        config = new MapConfiguration();

        parse(input);
    }

    /*
     * (non-Javadoc)
     * 
     * @see rs.filter.eclipse.DataInputParser#parse(rs.util.StringInputStream)
     */
    public void parse(StringInputStream input) throws EOFException, IOException {
        ArrayList<Integer> nnc = new ArrayList<Integer>();
        ArrayList<Double> trans = new ArrayList<Double>();

        for (String v = input.getString().toLowerCase(); !v.equals("/"); v = input
                .getString().toLowerCase()) {

            // read 2 (i, j, k) sets
            for (int i = 0; i < 6; i++, v = input.getString().toLowerCase())
                nnc.add(Integer.valueOf(v));

            // next is transmissibility read from for loop above
            trans.add(Double.valueOf(v));

            // read until end of line indicated by "/"
            while (true)
                if ((input.getString().toLowerCase()).equals("/"))
                    break;

        }

        // copy to int array
        int[] nncArray = new int[nnc.size()];
        for (int i = 0; i < nncArray.length; i++)
            nncArray[i] = nnc.get(i).intValue();
        config.putIntArray("nnc", nncArray);

        double[] transArray = new double[trans.size()];
        for (int i = 0; i < transArray.length; i++)
            transArray[i] = trans.get(i).doubleValue();
        config.putDoubleArray("nnctrans", transArray);

    }

    /*
     * (non-Javadoc)
     * 
     * @see rs.filter.eclipse.DataInputParser#getConfiguration()
     */
    public MapConfiguration getConfiguration() {
        return config;
    }

}
