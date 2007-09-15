package no.uib.cipr.rs.meshgen.eclipse.keyword;

import java.io.EOFException;
import java.io.IOException;

import no.uib.cipr.rs.meshgen.eclipse.MapConfiguration;
import no.uib.cipr.rs.meshgen.eclipse.parse.DataInputParser;
import no.uib.cipr.rs.meshgen.eclipse.parse.StringInputStream;

/**
 * X-direction transmissibility values.
 * 
 * This keyword specifies explicit transmissibility values in the X-direction.
 * It is followed by one non-negative real number for every grid block. A value
 * specified for block (I,J,K) is the transmissibility between blocks (I,J,K)
 * and (I+1, J, K).
 * 
 * Grid blocks are ordered with the X axis index cycling fastest, followed by
 * the Y and Z axis indices.
 */
public class Tranx implements DataInputParser {

    public Tranx(StringInputStream input) throws IOException {
        // TODO Auto-generated constructor stub
        for (String v = input.getString().toLowerCase(); !v.equals("/"); v = input
                .getString().toLowerCase()) {
            // TODO
        }
    }

    public void parse(StringInputStream input) throws EOFException, IOException {
        // TODO Auto-generated method stub

    }

    public MapConfiguration getConfiguration() {
        // TODO Auto-generated method stub
        return null;
    }

}
