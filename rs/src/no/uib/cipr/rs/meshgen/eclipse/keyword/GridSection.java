package no.uib.cipr.rs.meshgen.eclipse.keyword;

import java.io.EOFException;
import java.io.IOException;

import no.uib.cipr.rs.meshgen.eclipse.MapConfiguration;
import no.uib.cipr.rs.meshgen.eclipse.parse.DataInputParser;
import no.uib.cipr.rs.meshgen.eclipse.parse.StringInputStream;

/**
 * This class parses the GRID section of Eclipse input files.
 * <p>
 * The GRID section determines the basic geometry of the simulation grid and
 * various rock properties (porosity, absolute permeability, net-to-gross
 * ratios) in each grid cell.
 * <p>
 * Note that the number of grid blocks in each direction in the model (NX, NY,
 * NZ) is set by the keyword DIMENS in the RUNSPEC section. This might be
 * relevant for the implementation/use of this class.
 * <p>
 * See Eclipse Reference Manual 2004a, pp. 70.
 */
public class GridSection implements DataInputParser {

    private MapConfiguration config;

    /**
     * The constructor is responsible for calling the private parse method. This
     * builds the Configuration member.
     * 
     * @param input
     * @throws IOException
     * @throws EOFException
     *             TODO Recognize either corner point or structured Eclipse grid
     *             format
     */
    public GridSection(StringInputStream input) throws EOFException,
            IOException {

        config = new MapConfiguration();

        // TODO Recognize either corner point or structured Eclipse grid format
        config.putString("type", "EclipseMesh");

        parse(input);

    }

    /*
     * (non-Javadoc)
     * 
     * @see rs.filter.eclipse.Section#parse(rs.util.StringInputStream)
     */
    public void parse(StringInputStream input) throws IOException, EOFException {

        DataInputParser c = null;

        // stores rock data, added to config after the parse
        MapConfiguration rockData = new MapConfiguration();

        // stores boxed data, added to config after parse
        int boxIndex = 0;
        MapConfiguration boxData = new MapConfiguration();

        for (String keyword = input.getString().toLowerCase(); !keyword
                .equals("/"); keyword = input.getString().toLowerCase()) {

            if (keyword.equals("specgrid")) {
                c = new SpecgridSection(input);
                config.putConfiguration(keyword, c.getConfiguration());
            }

            else if (keyword.equals("debug")) {
                c = new Debug(keyword);
                config.putConfiguration(keyword, c.getConfiguration());
            }

            else if (keyword.equals("pseudo")) {
                c = new Pseudo(keyword);
                config.putConfiguration(keyword, c.getConfiguration());
            }

            else if (keyword.equals("dxv")) {
                c = new Dxv(input);
                config.putConfiguration(keyword, c.getConfiguration());
            }

            else if (keyword.equals("dyv")) {
                c = new Dyv(input);
                config.putConfiguration(keyword, c.getConfiguration());
            }

            else if (keyword.equals("dx")) {
                c = new Dx(input);
                config.putConfiguration(keyword, c.getConfiguration());
            }

            else if (keyword.equals("dy")) {
                c = new Dy(input);
                config.putConfiguration(keyword, c.getConfiguration());
            }

            else if (keyword.equals("dz")) {
                c = new Dz(input);
                config.putConfiguration(keyword, c.getConfiguration());
            }

            else if (keyword.equals("tops")) {
                c = new Tops(input);
                config.putConfiguration(keyword, c.getConfiguration());
            }

            else if (keyword.equals("coord")) {
                c = new CoordConfig(input);
                config.putConfiguration(keyword, c.getConfiguration());
            }

            else if (keyword.equals("zcorn")) {
                c = new ZcornConfig(input);
                config.putConfiguration(keyword, c.getConfiguration());
            }

            else if (keyword.equals("poro")) {
                c = new Poro(input);
                rockData.putConfiguration(keyword, c.getConfiguration());
            }

            else if (keyword.equals("permx")) {
                c = new Permx(input);
                rockData.putConfiguration(keyword, c.getConfiguration());
            }

            else if (keyword.equals("permy")) {
                c = new Permy(input);
                rockData.putConfiguration(keyword, c.getConfiguration());
            }

            else if (keyword.equals("box")) {
                c = new BoxSection(input);
                boxData.putConfiguration(keyword + boxIndex++, c
                        .getConfiguration());
            }

            else if (keyword.equals("permz")) {
                c = new Permz(input);
                rockData.putConfiguration(keyword, c.getConfiguration());
            }

            else if (keyword.equals("actnum")) {
                c = new Actnum(input, keyword);
                config.putConfiguration(keyword, c.getConfiguration());
            }

            else if (keyword.equals("tranx")) {
                System.out.println("found tranx");
                c = new Tranx(input);
                config.putConfiguration(keyword, c.getConfiguration());

            }

            else if (keyword.equals("trany")) {
                System.out.println("found trany");
                c = new Trany(input);
                config.putConfiguration(keyword, c.getConfiguration());
            }

            else if (keyword.equals("tranz")) {
                System.out.println("found tranz");
                c = new Tranz(input);
                config.putConfiguration(keyword, c.getConfiguration());
            }

            else if (keyword.equals("nnc")) {
                c = new Nnc(input);
                config.putConfiguration(keyword, c.getConfiguration());
            }

            else if (keyword.equals("carfin")) {
                c = new Carfin(input);
                config.putConfiguration(keyword, c.getConfiguration());
            }

            else if (keyword.equals("rptgrid")) {
                c = new RptgridSection(input, keyword);
                config.putConfiguration(keyword, c.getConfiguration());
            }

            else
                throw new IllegalArgumentException(String.format(config.trace()
                        + "Illegal keyword for import filter: \"%s\"", keyword));

        }

        // add boxed rock data to configuration
        config.putConfiguration("RockDataBoxed", boxData);

        // add rock data to configuration
        config.putConfiguration("RockData", rockData);
    }

    /**
     * @return This method returns a Configuration containing the data of the
     *         GRID section.
     */
    public MapConfiguration getConfiguration() {

        return config;

    }

}