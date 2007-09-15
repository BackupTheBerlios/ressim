package no.uib.cipr.rs.meshgen.eclipse.keyword;

import no.uib.cipr.rs.meshgen.eclipse.MapConfiguration;
import no.uib.cipr.rs.meshgen.eclipse.parse.DataInputParser;
import no.uib.cipr.rs.meshgen.eclipse.parse.StringInputStream;

/**
 * Solution section
 */
public class SolutionSection implements DataInputParser {

    private MapConfiguration config;

    /**
     * @param input
     */
    public SolutionSection(@SuppressWarnings("unused")
    StringInputStream input) {
        config = new MapConfiguration();

        // System.out.println("\tNot yet implemented. Skipping.");

    }

    /*
     * (non-Javadoc)
     * 
     * @see rs.filter.eclipse.Section#parse(rs.util.StringInputStream)
     */
    public void parse(StringInputStream input) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see rs.filter.eclipse.Section#getConfiguration()
     */
    public MapConfiguration getConfiguration() {

        return config;
    }

}
