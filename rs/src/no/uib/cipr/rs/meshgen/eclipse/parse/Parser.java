package no.uib.cipr.rs.meshgen.eclipse.parse;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import no.uib.cipr.rs.meshgen.eclipse.MapConfiguration;
import no.uib.cipr.rs.meshgen.eclipse.keyword.GridSection;
import no.uib.cipr.rs.meshgen.eclipse.keyword.PropsSection;
import no.uib.cipr.rs.meshgen.eclipse.keyword.RegionsSection;
import no.uib.cipr.rs.meshgen.eclipse.keyword.RunspecSection;
import no.uib.cipr.rs.meshgen.eclipse.keyword.ScheduleSection;
import no.uib.cipr.rs.meshgen.eclipse.keyword.SolutionSection;
import no.uib.cipr.rs.meshgen.eclipse.keyword.SummarySection;
import no.uib.cipr.rs.util.Configuration;

/**
 * This class parses an Eclipse Data file
 * 
 * All keyword-values in the input file must be terminated with a '/' character.
 * This is especially important for all flags, i.e. keywords without data. This
 * could be made more flexible in the future.
 * 
 * TODO This class is just for testing purposes. The functionality will be
 * relocated in a restructured Eclipse import filter.
 */
public class Parser implements DataInputParser {

    @SuppressWarnings("unused")
    private Map<String, Object> sections;

    private MapConfiguration config;

    /**
     * The constructor is responsible for the main functionality of the class.
     * 
     * @param input
     * @throws Exception
     */
    public Parser(File input) throws Exception {

        sections = new HashMap<String, Object>();

        config = new MapConfiguration();

        try {
            parse(new StringInputStream(new RepeatCountReader(
                    new IncludeReader(new NoCommentReader(new BufferedReader(
                            new FileReader(input)))))));

        } catch (Exception e) {
            System.out.println("Parser error: " + e.getMessage());
            throw e;
        }

        // TODO remove this
        config.outputConfiguration(new FileOutputStream(new File("test.out")));
        System.out.println("temporary output: \"test.out\"");

    }

    /**
     * Parse the input file and fill the sections map with specific data
     * sections.
     * <p>
     * The method will fail if the input file does not end with the keyword
     * "END"
     * 
     * @param input
     * @throws EOFException
     * @throws IOException
     */
    public void parse(StringInputStream input) throws EOFException, IOException {

        DataInputParser sec = null;

        for (String key = input.getString().toLowerCase(); !key.equals("end"); key = input
                .getString().toLowerCase()) {

            // the logics: check for Eclipse keyword, but add configuration with
            // RS keywords, e.g. "grid" identifies Eclipse GRID section
            // which is added to the configuration with RS "Mesh" keyword.
            if (key.equals("runspec")) {
                sec = new RunspecSection(input);
                config.putConfiguration(key, sec.getConfiguration());
            }

            else if (key.equals("grid")) {
                sec = new GridSection(input);
                config.putConfiguration("Mesh", sec.getConfiguration());
            }

            else if (key.equals("props")) {
                sec = new PropsSection(input);
                config.putConfiguration(key, sec.getConfiguration());
            }

            else if (key.equals("regions")) {
                sec = new RegionsSection(input);
                config.putConfiguration(key, sec.getConfiguration());
            }

            else if (key.equals("summary")) {
                sec = new SummarySection(input);
                config.putConfiguration(key, sec.getConfiguration());
            }

            else if (key.equals("solution")) {
                sec = new SolutionSection(input);
                config.putConfiguration(key, sec.getConfiguration());
            }

            else if (key.equals("schedule")) {
                sec = new ScheduleSection(input);
                config.putConfiguration(key, sec.getConfiguration());
            }

        }
    }

    /**
     * @return The complete Eclipse <code>Configuration</code>
     */
    public Configuration getConfiguration() {
        return config;
    }

    /**
     * @param key
     *            Configuration name
     * @return A <code>Configuration</code> with the given key
     */
    public Configuration getConfiguration(String key) {
        return config.getConfiguration(key);
    }

}