package no.uib.cipr.rs.meshgen.eclipse.keyword;

import java.io.IOException;

import no.uib.cipr.rs.meshgen.eclipse.MapConfiguration;
import no.uib.cipr.rs.meshgen.eclipse.parse.DataInputParser;
import no.uib.cipr.rs.meshgen.eclipse.parse.StringInputStream;

/**
 * TODO implement me
 */
public class Debug implements DataInputParser {

    private MapConfiguration config;

    /**
     * @param key
     */
    public Debug(String key) {
        config = new MapConfiguration();

        config.putString(key, "true");
    }

    /*
     * (non-Javadoc)
     * 
     * @see rs.filter.eclipse.Data#parse(rs.util.StringInputStream)
     */
    public void parse(StringInputStream input) throws IOException {
        System.out.println("\tParsing not yet implemented in Debug.java");

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