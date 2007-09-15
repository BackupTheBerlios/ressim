package no.uib.cipr.rs.meshgen.eclipse;

import java.io.EOFException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.lang.reflect.Array;

import no.uib.cipr.rs.geometry.Tensor3D;
import no.uib.cipr.rs.util.Configuration;

/**
 * A configuration which is stored in a memory map
 */
public class MapConfiguration extends Configuration {
    /**
     * Creates an empty sub-configuration in the tree
     */
    MapConfiguration(MapConfiguration parent, String name) {
        super(parent, name);
    }

    /**
     * Creates an empty configuration
     */
    public MapConfiguration() {
        super(null, null);
    }

    /**
     * Reads configuration information from the given filename
     */
    public MapConfiguration(String fileName) throws IOException,
            FileNotFoundException {
        super(fileName);
    }

    /**
     * Adds the given value into the given map, using the given key. Does some
     * duplication error checking
     */
    void add(StringInputStream input, String key, Object value) {
        if (config.put(key.toLowerCase(), value) != null) {
            System.out.println(trace() + "At " + input.getLocation()
                    + ": Duplicate keyword \"" + key + "\" (new=" + value
                    + ", old=" + config.get(key.toLowerCase()) + ")");
            System.exit(1);
        }
    }

    /**
     * Adds a string value
     * 
     * @param key
     *                Configuration key for the value
     * @param value
     *                Corresponding value
     */
    public void putString(String key, String value) {
        config.put(key.toLowerCase(), value);
    }

    /**
     * Adds a string array
     * 
     * @param key
     *                Configuration key for the value
     * @param value
     *                Corresponding string array. A defensive copy is made
     */
    public void putStringArray(String key, String[] value) {
        config.put(key.toLowerCase(), value.clone());
    }

    /**
     * Adds a double
     * 
     * @param key
     *                Configuration key for the double
     * @param value
     *                Corresponding double value
     */
    public void putDouble(String key, double value) {
        config.put(key.toLowerCase(), Double.toString(value));
    }

    /**
     * Adds a double array
     * 
     * @param key
     *                Configuration key for the array
     * @param value
     *                Corresponding double array
     */
    public void putDoubleArray(String key, double[] value) {
        String[] array = new String[value.length];
        for (int i = 0; i < value.length; ++i)
            array[i] = Double.toString(value[i]);

        config.put(key.toLowerCase(), array);
    }

    /**
     * Adds an integer
     * 
     * @param key
     *                Configuration key for the integer
     * @param value
     *                Corresponding integer value
     */
    public void putInt(String key, int value) {
        /*
         * / config.put(key.toLowerCase(), Integer.toString(value)); /
         */
        config.put(key.toLowerCase(), new Integer(value));
        // */
    }

    /**
     * Adds an integer array
     * 
     * @param key
     *                Configuration key for the array
     * @param value
     *                Corresponding integer array
     */
    public void putIntArray(String key, int[] value) {
        String[] array = new String[value.length];
        for (int i = 0; i < value.length; ++i)
            array[i] = Integer.toString(value[i]);

        config.put(key.toLowerCase(), array);
    }

    /**
     * Adds a sub-configuration
     * 
     * @param key
     *                Configuration key for the sub-configuration. Also used as
     *                the name of the subconfiguration
     * @param value
     *                Configuration to add. Its name and parent fields are
     *                modified
     */
    public void putConfiguration(String key, Configuration value) {
        setName(key.toLowerCase());
        setParent(this);
        config.put(key.toLowerCase(), value);
    }

    /**
     * Parts of the tensor; the first three components are the diagonal
     * elements, the last three components are the off-diagonal components (and
     * the tensor is symmetric, so xy == yx). The order of these components are
     * important! They must be listed here in the same order as the parameters
     * to the constructor of Tensor3D. The string values are the suffices that
     * are added to the configuration values, e.g. if the tensor is named
     * "perm", then "permx" and "permxy" will be the key names for two of its
     * (six) values.
     */
    static final String[] TENSOR_PARTS = new String[] { "x", "y", "z", "xy",
            "xz", "yz" };

    /**
     * A thread can only be in readTensor one time simultaneously (it is not
     * recursive); instead of allocating the same local value every time, use a
     * common (thread-local) storage.
     */
    private static final ThreadLocal<double[]> tensorValues = new ThreadLocal<double[]>() {
        @Override
        protected synchronized double[] initialValue() {
            return new double[TENSOR_PARTS.length];
        }
    };

    /**
     * Reads in a tensor
     */
    public Tensor3D readTensor(String prefix) {
        // prepare a buffer that will contain the configuration value for this
        // tensor component. this code is called a lot of times; to avoid over-
        // loading the garbage collector, we reuse the string buffer that
        // represents the keyname of each component.

        // we'll need to store the entire prefix and then two characters to hold
        // each of the suffices. initialize the first part of the buffer with
        // the prefix itself and the last part of the buffer with dummy suffix
        int start = prefix.length();
        int end = start + 2; // maximum size of part suffix
        StringBuilder key = new StringBuilder(end);
        key.replace(0, start, prefix);

        // read each tensor component into a correspending array slot
        double[] t = tensorValues.get(); // new double[TENSOR_PARTS.length];
        for (int i = 0; i < t.length; ++i) {
            // replace the suffix of the buffer with the one that we are looking
            // for in this iteration
            key.replace(start, end, TENSOR_PARTS[i]);

            // get the value from the configuration; if it isn't found, then
            // use 0.0 as the value for this component
            String s = key.toString();
            t[i] = containsKey(s) ? getDouble(s) : 0.;
        }

        // create the tensor from the array of values
        return new Tensor3D(t[0], t[1], t[2], t[3], t[4], t[5]);
    }

    /**
     * Writes the configuration to the given stream, flushing it at the end
     * 
     * @param out
     *                Output destination
     * @param comments
     *                Comments which are added to the beginning of the stream.
     *                One comment is written on each line, and they are all
     *                prefixed with the comment character
     */
    public void outputConfiguration(OutputStream out, String... comments)
            throws IOException {
        PrintWriter w = new PrintWriter(out);

        if (comments.length > 0) {
            for (String comment : comments)
                w.append("% " + comment + "\n");
            w.append("\n");
        }

        outputConfiguration(w, "");

        w.flush();
    }

    private static final int LINE_LENGTH = 80;

    private static final ThreadLocal<StringBuilder> output = new ThreadLocal<StringBuilder>() {
        @Override
        protected StringBuilder initialValue() {
            return new StringBuilder(LINE_LENGTH);
        }
    };

    private void outputConfiguration(Appendable str, String pre)
            throws IOException {
        StringBuilder output = MapConfiguration.output.get();
        for (String key : config.keySet()) {
            Object obj = config.get(key);

            if (obj instanceof String)
                str.append(pre).append(key).append(' ').append((String) obj)
                        .append('\n');

            else if (obj instanceof Array) {
                // write the type and size hints to output if the array is of a
                // native type (will optimize reading the array back in)
                String length = Integer.toString(Array.getLength(obj));
                if (obj instanceof double[]) {
                    str.append(pre).append("double ").append(length)
                            .append(' ');
                } else if (obj instanceof int[]) {
                    str.append(pre).append("int ").append(length).append(' ');
                }

                // all arrays have this prefix, regardless of type
                str.append(pre).append("array ").append(key).append('\n');

                if (obj instanceof double[]) {
                    double[] array = (double[]) obj;
                    for (int i = 0; i < array.length;) {
                        output.setLength(0);
                        output.append(pre).append('\t');
                        for (; i < array.length
                                && output.length() < LINE_LENGTH; i++) {
                            output.append(array[i]).append(' ');
                        }
                        output.append('\n');
                        str.append(output);
                    }
                } else if (obj instanceof int[]) {
                    int[] array = (int[]) obj;
                    for (int i = 0; i < array.length;) {
                        output.setLength(0);
                        output.append(pre).append('\t');
                        for (; i < array.length
                                && output.length() < LINE_LENGTH; i++) {
                            output.append(array[i]).append(' ');
                        }
                        output.append('\n');
                        str.append(output);
                    }
                } else { // if (obj instanceof String[]) {
                    String[] array = (String[]) obj;
                    for (int i = 0; i < array.length;) {
                        output.setLength(0);
                        output.append(pre).append('\t');
                        for (; i < array.length
                                && output.length() < LINE_LENGTH; i++) {
                            output.append(array[i]).append(' ');
                        }
                        output.append('\n');
                        str.append(output);
                    }
                }

                str.append(pre).append("end").append('\n');
            }

            else if (obj instanceof Configuration) {
                str.append(pre).append("begin ").append(key).append('\n');
                MapConfiguration sub = (MapConfiguration) obj;
                sub.outputConfiguration(str, pre + '\t');
                str.append(pre).append("end").append('\n');
            }

        }
    }

    /**
     * Input stream wrapper for extracting strings like a tokenizer. Recognises
     * '%' as a comment character.
     */
    private static class StringInputStream {

        /**
         * This one performs the reading. It will be configured to return only
         * strings or EOF's
         */
        private StreamTokenizer st;

        /**
         * Creates a new input stream for the given reader
         */
        public StringInputStream(Reader r) {
            st = new StreamTokenizer(r);

            st.resetSyntax();
            st.eolIsSignificant(false);

            // Parse numbers as words
            st.wordChars('0', '9');
            st.wordChars('-', '.');

            // Characters as words
            st.wordChars('\u0000', '\u00FF');

            // Skip everything after '%' on the same line
            st.commentChar('%');

            // Skip whitespace and newlines
            st.whitespaceChars(' ', ' ');
            st.whitespaceChars('\u0009', '\u000e');
        }

        /**
         * Returns the next string from the stream
         * 
         * @return String read. It will be a whitespace separated word
         * @throws IOException
         *                 An internal I/O failure
         */
        public String getString() throws IOException {
            st.nextToken();

            if (st.ttype == StreamTokenizer.TT_WORD)
                return st.sval;
            else if (st.ttype == StreamTokenizer.TT_EOF)
                throw new EOFException();
            else
                throw new RuntimeException("Internal parser error");
        }

        /**
         * Returns the current file name and line number. Useful for
         * error-reporting
         */
        public String getLocation() {
            // we report line number anyway
            int lineNumber = st.lineno();

            String location = String.format("line %d", lineNumber);
            return location;
        }
    }
}
