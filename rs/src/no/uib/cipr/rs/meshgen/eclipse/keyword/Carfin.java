package no.uib.cipr.rs.meshgen.eclipse.keyword;

import java.io.EOFException;
import java.io.IOException;

import no.uib.cipr.rs.meshgen.eclipse.MapConfiguration;
import no.uib.cipr.rs.meshgen.eclipse.parse.DataInputParser;
import no.uib.cipr.rs.meshgen.eclipse.parse.StringInputStream;

/**
 */
public class Carfin implements DataInputParser {

    private MapConfiguration config;

    /**
     * This class reads the CARFIN keyword which is used to set up a Cartesian
     * local grid refinement. It specifies a cell or a box of cells identified
     * by its global grid coordinates I1-I2, J1-J2, K1-K2 to be replaced by
     * refined cells. The dimensions of the refined grid within this box are
     * specified as NX, NY, NZ. (Eclipse Reference Manual 2004a, pp. 328)
     * <p>
     * TODO Decide if 10 or 12 data items should follow, ref. Reference Manual.
     * 
     * @param input
     * @throws IOException
     * @throws EOFException
     */
    public Carfin(StringInputStream input) throws EOFException, IOException {

        config = new MapConfiguration();

        parse(input);

    }

    /*
     * (non-Javadoc)
     * 
     * @see rs.filter.eclipse.Data#parse(rs.util.StringInputStream)
     */
    public void parse(StringInputStream input) throws IOException {

        config.putString("lgrname", input.getString());
        config.putInt("i1", input.getInt());
        config.putInt("i2", input.getInt());
        config.putInt("j1", input.getInt());
        config.putInt("j2", input.getInt());
        config.putInt("k1", input.getInt());
        config.putInt("k2", input.getInt());

        config.putDouble("nx", input.getDouble());
        config.putDouble("ny", input.getDouble());
        config.putDouble("nz", input.getDouble());

        //
        if (!input.getString().toLowerCase().equals("/"))
            throw new IOException("Incorrect format of CARFIN");

        // now read all parameters that apply to this refinement
        for (String key = input.getString().toLowerCase(); !key
                .equals("endfin"); key = input.getString().toLowerCase()) {

            DataInputParser c = null;

            if (key.equals("nxfin")) {
                c = new Nxfin(input);
                config.putConfiguration(key, c.getConfiguration());
            }

            else if (key.equals("nyfin")) {
                c = new Nyfin(input);
                config.putConfiguration(key, c.getConfiguration());
            }

            else if (key.equals("nzfin")) {
                c = new Nzfin(input);
                config.putConfiguration(key, c.getConfiguration());
            }

            else if (key.equals("hxfin")) {
                c = new Hxfin(input);
                config.putConfiguration(key, c.getConfiguration());
            }

            else if (key.equals("hyfin")) {
                c = new Hyfin(input);
                config.putConfiguration(key, c.getConfiguration());
            }

            else if (key.equals("hzfin")) {
                c = new Hzfin(input);
                config.putConfiguration(key, c.getConfiguration());
            }

            else if (key.equals("permx")) {
                c = new Permx(input);
                config.putConfiguration(key, c.getConfiguration());
            }

            else if (key.equals("permy")) {
                c = new Permy(input);
                config.putConfiguration(key, c.getConfiguration());
            }

            else if (key.equals("permz")) {
                c = new Permy(input);
                config.putConfiguration(key, c.getConfiguration());
            }

            else if (key.equals("poro")) {
                c = new Poro(input);
                config.putConfiguration(key, c.getConfiguration());
            }

            else if (key.equals("actnum")) {
                c = new Actnum(input, key);
                config.putConfiguration(key, c.getConfiguration());
            }

        }

    }

    public MapConfiguration getConfiguration() {
        return config;
    }
}