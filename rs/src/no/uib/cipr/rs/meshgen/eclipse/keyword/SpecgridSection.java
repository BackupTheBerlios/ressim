package no.uib.cipr.rs.meshgen.eclipse.keyword;

import java.io.EOFException;
import java.io.IOException;

import no.uib.cipr.rs.meshgen.eclipse.MapConfiguration;
import no.uib.cipr.rs.meshgen.eclipse.parse.DataInputParser;
import no.uib.cipr.rs.meshgen.eclipse.parse.StringInputStream;

/**
 * Specgrid section
 */
public class SpecgridSection implements DataInputParser {

    private MapConfiguration config;

    /**
     * @param input
     * @throws IOException
     * @throws EOFException
     */
    public SpecgridSection(StringInputStream input) throws EOFException,
            IOException {
        config = new MapConfiguration();

        parse(input);
    }

    /**
     * Parsing the SPECGRID items sequentially, no looping. The format is
     * described on page 3-1226 in Eclipse Reference Manual 2002a.
     */
    public void parse(StringInputStream input) throws IOException {
        config.putInt("ndivix", input.getInt());
        config.putInt("ndiviy", input.getInt());
        config.putInt("ndiviz", input.getInt());

        config.putInt("numres", input.getInt());

        config.putString("coordtype", input.getString().toLowerCase());

        // check
        if (!input.getString().toLowerCase().equals("/"))
            throw new IOException("Incorrect format of SPECGRID");

    }

    /**
     * @return A Configuration object with all relevant data.
     */
    public MapConfiguration getConfiguration() {

        return config;
    }

}