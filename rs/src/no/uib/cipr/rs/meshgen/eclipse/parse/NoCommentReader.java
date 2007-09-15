package no.uib.cipr.rs.meshgen.eclipse.parse;

import java.io.BufferedReader;
import java.io.IOException;

/**
 * This class provides a stream which removes the text behind an Eclipse comment
 * (including the comment). The commment string is "--".
 */
public class NoCommentReader extends BufferedReader {

    private String comment = "--";

    private BufferedReader in;

    private StringBuilder buf;

    private int next;

    private boolean eos; // end of stream flag

    /**
     * 
     * @param in
     *            A BufferedReader object
     * @throws IOException
     */
    NoCommentReader(BufferedReader in) throws IOException {

        super(in);

        this.in = in;

        this.buf = new StringBuilder(80);

        readStringLine();

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
        String input = in.readLine();

        if (input != null) {
            String[] s = input.split(comment);

            // deal with a line consisting only of a comment string
            if (s.length == 0)
                input = "";
            else
                input = s[0];

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
            this.readStringLine();
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
     * @return The number of characters read or -1 if the end of stream has been
     *         reached.
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

}