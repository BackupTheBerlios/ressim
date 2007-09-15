package no.uib.cipr.rs.meshgen.eclipse.keyword;

import java.io.EOFException;
import java.io.IOException;

import no.uib.cipr.rs.meshgen.eclipse.MapConfiguration;
import no.uib.cipr.rs.meshgen.eclipse.parse.DataInputParser;
import no.uib.cipr.rs.meshgen.eclipse.parse.StringInputStream;

/**
 * Y-direction transmissibility values.
 * 
 * This keyword specifies explicit transmissibility values in the Y-direction.
 * It is followed by one non-negative real number for every grid block. A value
 * specified for block (I,J,K) is the transmissibility between blocks (I,J,K)
 * and (I, J+1, K).
 * 
 * Grid blocks are ordered with the X axis index cycling fastest, followed by
 * the Y and Z axis indices.
 */
public class Trany implements DataInputParser {

    public Trany(@SuppressWarnings("unused")
    StringInputStream input) {
        // TODO Auto-generated constructor stub
    }

    public void parse(StringInputStream input) throws EOFException, IOException {
        // TODO Auto-generated method stub

    }

    public MapConfiguration getConfiguration() {
        // TODO Auto-generated method stub
        return null;
    }

}
