package no.uib.cipr.rs.meshgen.eclipse.keyword;

import java.io.EOFException;
import java.io.IOException;
import java.util.ArrayList;

import no.uib.cipr.rs.meshgen.eclipse.MapConfiguration;
import no.uib.cipr.rs.meshgen.eclipse.parse.DataInputParser;
import no.uib.cipr.rs.meshgen.eclipse.parse.StringInputStream;

/**
 * @author $Author: boh $
 */
public class Actnum implements DataInputParser {

    private MapConfiguration config;

    private String key;

    /**
     * @param input
     * @param key
     * @throws IOException
     * @throws EOFException
     */
    public Actnum(StringInputStream input, String key) throws EOFException,
            IOException {

        this.key = key;

        config = new MapConfiguration();

        parse(input);
    }

    /**
     * TODO Class visibility should be changed to public.
     * 
     * @param input
     * @throws IOException
     */
    public void parse(StringInputStream input) throws IOException {

        ArrayList<Integer> l = new ArrayList<Integer>();

        for (String v = input.getString().toLowerCase(); !v.equals("/"); v = input
                .getString().toLowerCase())
            l.add(Integer.valueOf(v));

        // copy to int array
        int[] array = new int[l.size()];
        for (int i = 0; i < array.length; i++)
            array[i] = l.get(i).intValue();

        config.putIntArray(key, array);

    }

    public MapConfiguration getConfiguration() {
        return config;
    }

}