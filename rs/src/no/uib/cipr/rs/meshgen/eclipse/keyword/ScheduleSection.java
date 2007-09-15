package no.uib.cipr.rs.meshgen.eclipse.keyword;

import no.uib.cipr.rs.meshgen.eclipse.MapConfiguration;
import no.uib.cipr.rs.meshgen.eclipse.parse.DataInputParser;
import no.uib.cipr.rs.meshgen.eclipse.parse.StringInputStream;

/**
 * Schedule section
 */
public class ScheduleSection implements DataInputParser {

    private MapConfiguration config;

    /**
     * @param input
     */
    public ScheduleSection(@SuppressWarnings("unused")
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
