package no.uib.cipr.rs.meshgen.grdecl;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StreamTokenizer;

/**
 * Transform a character stream into keyword tokens that can be used for lexical
 * analyses of the file. You are not expected to use this class yourself, it is
 * used by the Parser.
 * 
 * @author roland.kaufmann@cipr.uib.no
 */
class Lexer {
    // lexer that does the heavy lifting of reading from the stream
    StreamTokenizer tok;
    
    Lexer(InputStream is) {
        tok = new StreamTokenizer(new InputStreamReader(is));
        tok.resetSyntax();
        tok.whitespaceChars(0, ' ');
        
        // double hyphens indicate that the rest of the line should be skipped
        tok.slashSlashComments(true);
        tok.commentChar('-');
        
        // treat numeric symbols as part of words; we want the string
        // representation of the numbers, in order to retrieve the type
        // that we want (including the repeat count). (note that the slash
        // is a separate char
        tok.wordChars('-', '-');
        tok.wordChars('+', '+');
        tok.wordChars('*', '*');
        tok.wordChars('0', '9');
        tok.wordChars('.', '.');
        tok.wordChars('a', 'z');
        tok.wordChars('A', 'Z');
        
        // keywords should be considered case-insensitive
        tok.lowerCaseMode(true);
        
        // we don't care about line-break formatting
        tok.eolIsSignificant(false);
        
        // slashes are used to indicate the end of a section
        tok.ordinaryChar('/');
    }
    
    // last token read, or null if we were unable to pull more tokens out of
    // the stream.
    private String token;
 
    /**
     * Advance the lexer one token. Either a new token is available through a
     * call to get(), or done() will return true.
     * 
     * @return
     *  A reference to itself so that further methods can be called in a pipe.
     * @throws Exception
     */
    Lexer next() throws Exception {
        switch(tok.nextToken()) {
            case StreamTokenizer.TT_EOF:
                token = null;
                break;
            case StreamTokenizer.TT_WORD:
                token = tok.sval;
                break;
            default:
                token = Character.toString((char) tok.ttype);
                break;
        }
        return this;
    }
    
    /**
     * Inspect the lexer for an end of stream. Note that this call is only valid
     * after a call to next().
     * 
     * @return
     *  True if the end of the stream is encountered, false if there are still
     *  available tokens.
     */
    boolean done() {
        boolean done = (token == null);
        return done;
    }
    
    /**
     * Inspect the contents of the current lexer token. This may be either a
     * keyword or a data item.
     * 
     * @return
     *  Textual representation of the next token. Consider opening a substream
     *  if you need a typed token.
     */
    String get() {
        String result = token;
        return result;
    }
    
    /**
     * Start reading an array of typed tokens from the stream. This class supports
     * the asterisk syntax of the Eclipse input files, where a repeat count may be
     * specified to reduce the length of the file. Current limitations are that we
     * are not able to switch the type of the reader (e.g. from int to double)
     * while in the middle of a repeated number (but there is no use case for that
     * anyway). The client is assumed to know how many elements to read, so there
     * is no termination check(!).
     * 
     * The parameterization of the class is just to get the concrete type of the
     * subclass. This is necessary in order to return a reference handle which
     * does not erase the type information of the get() method. Since Java5 does
     * not allow generics of basic types, we cannot simply parameterize on the
     * returned type (which admittedly would have been much more elegant). Also,
     * since generics are implemented through erasure, we have to provide a method
     * that does the cast for us.
     */
    abstract class Substream<T extends Substream<?>> {
        // number of elements left containing the same value that we have in store
        int repeatCount = 0;

        // outsource the reading and seting of the value itself to a subclass; we
        // don't know exactly what kind of token is being read; we just handle the
        // advance and repeat logic.
        protected abstract void setValue(String representation);
        
        @SuppressWarnings("unchecked")
        T next() throws Exception {
            // if we have exhausted the internal list, then we must start pulling
            // new tokens from the underlaying stream. use while instead of if in
            // case anyone specifies zero as the repeat count :-)
            while(repeatCount == 0) {
                // read one more token from the stream
                String s = Lexer.this.next().get();
                // if the token contained an asterisk, then everyhing before it is
                // considered the repeat count and everything after the value
                int pos = s.indexOf('*');
                if(pos != -1) {
                    repeatCount = Integer.parseInt(s.substring(0, pos));
                    s = s.substring(pos+1, s.length());
                }
                else {
                    repeatCount = 1;
                }
                // convert the string into a value
                setValue(s);
            }
            
            // use one more of our "cached" array 
            repeatCount--;
            
            // return a reference to ourself, typesafe
            return (T) this;
        }
        
    }

    /**
     * Subclasses for each typed value we want to read. Since the basic types
     * don't implement a common interface for their parse() methods, we need a
     * subclass that can perform that task (and of course there is the odd type
     * that is represented in a way that the standard libraries don't understand)
     * Anyways, we cannot parameterize the base class since the basic types cannot
     * be used as a generic argument. 
     */
    class IntSubstream extends Substream<IntSubstream> {
        // current instance of the type at which we are looking
        int lastValue;
        
        // convert the string from the file into the value type
        @Override
        protected void setValue(String representation) {
            lastValue = Integer.parseInt(representation);
        }
        
        // observe the value in a type-safe manner
        int get() {
            return lastValue;
        }
    }

    class DoubleSubstream extends Substream<DoubleSubstream> {
        double lastValue;
        
        @Override
        protected void setValue(String representation) {
            lastValue = Double.parseDouble(representation);
        }
        
        double get() {
            return lastValue;
        }
    }

    class BooleanSubstream extends Substream<BooleanSubstream> {
        boolean lastValue;
        
        @Override
        protected void setValue(String representation) {
            char ch = representation.trim().toUpperCase().charAt(0);
            switch(ch) {
            case 'T':
            case '1':
                lastValue = true;
                break;
            case 'F':
            case '0':
                lastValue = false;
                break;
            }
        }
        
        boolean get() {
            return lastValue;
        }
    }    
}
