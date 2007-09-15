package no.uib.cipr.rs.meshgen.eclipse.parse;

import java.io.EOFException;
import java.io.IOException;

import no.uib.cipr.rs.util.Configuration;

/**
 * This interface is enforcing a parse() and getConfiguration() implementation.
 */
public interface DataInputParser {

    /**
     * Parse the stream
     * 
     * @param input
     * @throws EOFException
     * @throws IOException
     */
    public void parse(StringInputStream input) throws EOFException, IOException;

    /**
     * @return A Configuration object
     */
    public Configuration getConfiguration();

}
