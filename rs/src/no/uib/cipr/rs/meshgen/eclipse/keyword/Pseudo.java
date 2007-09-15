package no.uib.cipr.rs.meshgen.eclipse.keyword;

import java.io.EOFException;
import java.io.IOException;

import no.uib.cipr.rs.meshgen.eclipse.MapConfiguration;
import no.uib.cipr.rs.meshgen.eclipse.parse.DataInputParser;
import no.uib.cipr.rs.meshgen.eclipse.parse.StringInputStream;

/**
 * Pseudo section
 */
public class Pseudo implements DataInputParser {

    private MapConfiguration config;

    /**
     * @param key
     */
    public Pseudo(String key) {
        config = new MapConfiguration();

        config.putString(key, "true");
    }

    /*
     * (non-Javadoc)
     * 
     * @see rs.filter.eclipse.DataInputParser#parse(rs.util.StringInputStream)
     */
    public void parse(StringInputStream input) throws EOFException, IOException {
        System.out.println("\tParsing not yet implemented in Pseudo.java");

        for (String key = input.getString().toLowerCase(); !key.equals("/"); key = input
                .getString().toLowerCase()) {

            // System.out.println(key);
            // add keyword recognition here

        }
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
