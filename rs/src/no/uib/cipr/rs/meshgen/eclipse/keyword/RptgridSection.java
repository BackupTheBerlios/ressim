package no.uib.cipr.rs.meshgen.eclipse.keyword;

import java.io.EOFException;
import java.io.IOException;

import no.uib.cipr.rs.meshgen.eclipse.MapConfiguration;
import no.uib.cipr.rs.meshgen.eclipse.parse.DataInputParser;
import no.uib.cipr.rs.meshgen.eclipse.parse.StringInputStream;

/**
 * Rptgrid section
 */
public class RptgridSection implements DataInputParser {

    private MapConfiguration config;

    /**
     * @param input
     * @param key
     * @throws IOException
     * @throws EOFException
     */
    public RptgridSection(StringInputStream input, String key)
            throws EOFException, IOException {
        config = new MapConfiguration();

        config.putString(key, "true");

        parse(input);
    }

    public void parse(StringInputStream input) throws IOException {
        System.out
                .println("\tParsing not yet implemented in RptgridSection.java");

        for (String key = input.getString().toLowerCase(); !key.equals("/"); key = input
                .getString().toLowerCase()) {

            // add keyword recognition here

        }
    }

    public MapConfiguration getConfiguration() {
        return config;
    }

}
