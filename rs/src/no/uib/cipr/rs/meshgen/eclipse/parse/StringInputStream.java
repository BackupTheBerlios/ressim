package no.uib.cipr.rs.meshgen.eclipse.parse;

import java.io.EOFException;
import java.io.IOException;
import java.io.Reader;
import java.io.StreamTokenizer;

/**
 * Input stream wrapper for extracting strings and numbers like a tokenizer.
 * Useful for reading the RS input files in a robust and easy manner
 */
public class StringInputStream {

    /**
     * This one performs the reading. It will be configured to return only
     * strings or EOF's
     */
    private StreamTokenizer st;

    /**
     * Current comment character
     */
    private char comment;

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
        st.commentChar(comment = '%');

        // Skip whitespace and newlines
        st.whitespaceChars(' ', ' ');
        st.whitespaceChars('\u0009', '\u000e');
    }

    /**
     * Sets a new comment character. The previous is set to be an ordinary
     * character again
     */
    public void setCommentChar(char c) {
        st.ordinaryChar(comment);
        st.commentChar(comment = c);
    }

    /**
     * Checks that the next string in the stream matches the given. If there is
     * no match, an IOException is thrown
     * 
     * @throws IOException
     *             An internal I/O failure or if a mismatch was detected
     */
    public void checkNextString(String name) throws IOException {
        st.nextToken();

        if (st.ttype == StreamTokenizer.TT_WORD) {
            if (!st.sval.equalsIgnoreCase(name))
                throw new IllegalArgumentException("Line " + st.lineno()
                        + ": Expected \"" + name + "\", got \"" + st.sval
                        + "\"");
            else if (st.ttype == StreamTokenizer.TT_EOF)
                throw new EOFException();
        } else
            throw new RuntimeException("Internal parser error");
    }

    /**
     * Returns the next string from the stream
     * 
     * @return String read. It will be a whitespace separated word
     * @throws IOException
     *             An internal I/O failure
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
     * Returns the next double from the stream
     * 
     * @return Double read. It will be a whitespace separated double
     * @throws IOException
     *             An internal I/O failure
     */
    public double getDouble() throws IOException {
        st.nextToken();

        if (st.ttype == StreamTokenizer.TT_WORD)
            try {
                return Double.parseDouble(st.sval);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Line " + st.lineno()
                        + " Expected a numerical value, but got \"" + st.sval
                        + "\"");
            }
        else if (st.ttype == StreamTokenizer.TT_EOF)
            throw new EOFException();
        else
            throw new RuntimeException("Internal parser error");
    }

    /**
     * Returns the next integer from the stream
     * 
     * @return Integer read. It will be a whitespace separated integer
     * @throws IOException
     *             An internal I/O failure
     */
    public int getInt() throws IOException {
        st.nextToken();

        if (st.ttype == StreamTokenizer.TT_WORD)
            try {
                return Integer.parseInt(st.sval);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Line " + st.lineno()
                        + " Expected an integer value, but got \"" + st.sval
                        + "\"");
            }
        else if (st.ttype == StreamTokenizer.TT_EOF)
            throw new EOFException();
        else
            throw new RuntimeException("Internal parser error");
    }

    /**
     * Checks for the given keyword, and returns the following integer
     */
    public int getInt(String key) throws IOException {
        checkNextString(key);
        return getInt();
    }

    /**
     * Checks for the given keyword, and returns the following double
     */
    public double getDouble(String key) throws IOException {
        checkNextString(key);
        return getDouble();
    }

    /**
     * Checks for the given keyword, and returns the following string
     */
    public String getString(String key) throws IOException {
        checkNextString(key);
        return getString();
    }

    /**
     * Reads in <code>n</code> doubles
     */
    public double[] getDoubleVector(int n) throws IOException {
        double[] value = new double[n];
        for (int i = 0; i < n; ++i)
            value[i] = getDouble();
        return value;
    }

    /**
     * Reads in <code>n</code> doubles after checking for a given keyword
     */
    public double[] getDoubleVector(String key, int n) throws IOException {
        checkNextString(key);
        return getDoubleVector(n);
    }

    /**
     * Reads in <code>n</code> integers
     */
    public int[] getIntVector(int n) throws IOException {
        int[] value = new int[n];
        for (int i = 0; i < n; ++i)
            value[i] = getInt();
        return value;
    }

    /**
     * Reads in <code>n</code> integers after checking for a given keyword
     */
    public int[] getIntVector(String key, int n) throws IOException {
        checkNextString(key);
        return getIntVector(n);
    }

    /**
     * Reads in <code>n</code> strings after checking for a given keyword
     */
    public String[] getStringVector(String key, int n) throws IOException {
        checkNextString(key);
        return getStringVector(n);
    }

    /**
     * Reads in <code>n</code> strings
     */
    public String[] getStringVector(int n) throws IOException {
        String[] value = new String[n];
        for (int i = 0; i < n; ++i)
            value[i] = getString();
        return value;
    }

    /**
     * Returns the current line number
     */
    public int getLineNumber() {
        return st.lineno();
    }
}