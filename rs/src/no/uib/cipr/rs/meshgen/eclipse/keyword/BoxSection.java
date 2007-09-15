package no.uib.cipr.rs.meshgen.eclipse.keyword;

import java.io.EOFException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import no.uib.cipr.rs.meshgen.eclipse.MapConfiguration;
import no.uib.cipr.rs.meshgen.eclipse.parse.DataInputParser;
import no.uib.cipr.rs.meshgen.eclipse.parse.StringInputStream;

/**
 * Stores boxed data. Six integers are read that redefine the current input box.
 * Data read into an array (for example, using PERMX) are assigned to the grid
 * blocks in the current input box.
 * <p>
 * Currently, only the following data items are read: PORO, PERMX, PERMY, PERMZ.
 * TODO: TRANX, TRANY, TRANZ
 */
public class BoxSection implements DataInputParser {

    private MapConfiguration config;

    /**
     * Creates an input box section
     * 
     * @param input
     * @throws EOFException
     * @throws IOException
     */
    public BoxSection(StringInputStream input) throws EOFException, IOException {
        config = new MapConfiguration();

        parse(input);
    }

    public void parse(StringInputStream input) throws EOFException, IOException {

        DataInputParser c = null;

        // read box dimensions
        List<Integer> l = new ArrayList<Integer>(6);

        for (String v = input.getString().toLowerCase(); !v.equals("/"); v = input
                .getString().toLowerCase())
            l.add(Integer.valueOf(v));

        int[] array = new int[l.size()];
        for (int i = 0; i < array.length; i++)
            array[i] = l.get(i);

        config.putIntArray("boxlimits", array);

        // read keywords until ENDBOX
        for (String keyword = input.getString().toLowerCase(); !keyword
                .equalsIgnoreCase("endbox"); keyword = input.getString()
                .toLowerCase()) {

            if (keyword.equals("poro")) {
                c = new Poro(input);
                config.putConfiguration(keyword, c.getConfiguration());
            }

            else if (keyword.equals("permx")) {
                c = new Permx(input);
                config.putConfiguration(keyword, c.getConfiguration());
            }

            else if (keyword.equals("permy")) {
                c = new Permy(input);
                config.putConfiguration(keyword, c.getConfiguration());
            }

            else if (keyword.equals("permz")) {
                c = new Permz(input);
                config.putConfiguration(keyword, c.getConfiguration());
            }

        }

    }

    public MapConfiguration getConfiguration() {
        return config;
    }

}
