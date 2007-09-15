package no.uib.cipr.rs.meshgen.eclipse.parse;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.IllegalFormatFlagsException;

/**
 * This class provides a stream that interprets and includes files specified
 * with the Eclipse INCLUDE keyword.
 */
public class IncludeReader extends BufferedReader {

    private BufferedReader in;

    // this stores the file that is read
    private IncludeReader include = null;

    // flag that determines what stream to read from.
    private boolean useinc;

    private StringBuilder buf = null;

    private int next;

    // end of stream flag
    private boolean eos;

    /**
     * Creates the IncludeReader
     */
    public IncludeReader(BufferedReader in) throws IOException {

        super(in);

        this.in = in;

        this.useinc = false;
        this.eos = false;

        this.buf = new StringBuilder(80);

        this.readStringLine();

    }

    /**
     * This routine reads next line from the underlying BufferedReader stream.
     * If the end of the stream is reached, i.e. BufferedReader.readLine()
     * returns null, the StringBuilder buffer is set to length zero. This is
     * used as a flag by this class' read routines to return either null or -1.
     * 
     * @throws IOException
     */
    private void readStringLine() throws IOException {

        next = 0;

        // read next line of underlying buffer.
        String input = this.readIncludeLine();

        if (input != null) {
            buf.replace(0, buf.length(), input);
            eos = false;
        }

        else
            eos = true;

    }

    /**
     * This method returns a String containing the next data line of the reader.
     * It returns null if the end of the stream is reached.
     * 
     */
    @Override
    public String readLine() throws IOException {

        String s = null;

        if (!eos) {
            s = buf.substring(next);
            readStringLine();
        }

        return s;
    }

    /**
     * Get next character from stream
     * 
     * In this implementation, we return the next character from the internal
     * StringBuilder object.
     * 
     * The function returns -1 if the end of the stream is reached
     * 
     * TODO in order to work properly with e.g. StringTokenizer, the routine
     * returns a '\n' character when the next character is beyond the
     * end-of-line. This could perhaps be implemented in a more elegant fashion.
     * 
     * @return character code
     * @throws IOException
     * 
     */
    @Override
    public int read() throws IOException {

        // check if next character read is beyond the current line
        if ((next > buf.length() - 1) && !eos) {

            readStringLine();

            return '\n';
        }

        if (!eos) {

            int c = buf.codePointAt(next);
            ++next;

            return c;
        }

        return -1; // end of stream indicator

    }

    /**
     * TODO Implemented, but not tested
     * 
     * @param cbuf
     * @param off
     * @param len
     * 
     * @return The number of characters read before end-of-stream, or -1 if the
     *         end of stream has been reached.
     * @throws IOException
     */
    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {

        int data = 0;
        int nread = 0;

        for (int i = off; i < off + len && data != -1; i++) {
            data = this.read();

            // hvis ikke end of stream
            if (data != -1) {
                cbuf[i] = (char) this.read();
                nread++;
            }
        }

        return (data == -1 ? data : nread);

    }

    /**
     * 
     * This function checks if the input keyword is found. in that case a new
     * include reader is created and subsequent readLine() calls are made from
     * that object.
     * 
     * @return A string containing the next line of the stream or null if the
     *         end is reached.
     * 
     * @throws IOException
     */
    private String readIncludeLine() throws IOException {

        String line = null;

        if (useinc) {
            line = include.readLine();

            // check if end of child-stream. Also check for ENDINC
            if (line == null) {
                useinc = false;
                line = "\n";
            }

        } else {

            line = in.readLine();

            // if not end-of-stream, check for keyword INCLUDE
            if (line != null) {

                // check only first string, since keywords must be placed alone
                // according to Eclipse format specification.
                if (line.toLowerCase().equals("include")) {

                    // find data item
                    String fileName = getFileName();

                    // create child stream to read from file
                    include = new IncludeReader(new NoCommentReader(
                            new BufferedReader(new FileReader(
                                    new File(fileName)))));

                    // read next line
                    line = include.readLine();

                    // set flag
                    useinc = true;
                }
            }
        }

        return line;

    }

    /**
     * This method is responsible for extracting the filename following an
     * INCLUDE keyword.
     * 
     * @return A string containing the file name specified after the INCLUDE
     *         keyword.
     * 
     * @throws IOException
     */
    private String getFileName() throws IOException {

        // read line
        StringInputStream s;
        String result = null;

        boolean findData = true;
        boolean findEnd = true;

        while (findData || findEnd) {

            String line = in.readLine();

            if (line == null) {
                // an exception should perhaps be thrown here.
                findData = false;
                findEnd = false;
            }

            // check for empty line
            else if (line.trim().equals("")) {
                // continue
            }

            // non-empty line, read data + end-of-field character
            else {
                s = new StringInputStream(new StringReader(line));

                String data = null;

                boolean eof = false;

                while (!eof) {
                    try {

                        // this function either returns a non-whitespace
                        // string or throws an EOFException. thus, we do not
                        // need to check for empty strings.
                        data = s.getString();

                        // if the execution continues here, no EOF was thrown
                        // now data can be stored
                        if (findData && !data.equals("/")) {
                            result = data; // store output.
                            findData = false;

                        } else if (findData && data.equals("/")) {
                            throw new IllegalFormatFlagsException(
                                    "Expected <data>, read \"/\"");
                        }

                        // we should now have found the end-of-field character
                        else if (findEnd && data.equals("/")) {
                            findEnd = false;
                            eof = true;
                        }

                        else {
                            throw new IllegalFormatFlagsException(
                                    "Expected \"/\", read \"" + data + "\"");
                        }

                    } catch (EOFException e) {
                        eof = true;
                    }
                } // end while (!eof)
            } // end else

        } // end while(findData || findEnd)

        return result;

    }

}