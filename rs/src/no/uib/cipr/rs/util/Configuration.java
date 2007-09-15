package no.uib.cipr.rs.util;

import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

/**
 * Holds a hierachical configuration
 */
public class Configuration {

    /**
     * Holds the configuration mapping
     */
    protected Map<String, Object> config;

    /**
     * The set of keys which have been requested. Used to ensure that all keys
     * in the configuration have been requested
     */
    private Set<String> requested;

    /**
     * Pointer to parent configuration. It is null for the root
     */
    private Configuration parent;

    /**
     * Name of this configuration. It is null for the root
     */
    private String name;

    /**
     * Compatibility methods for MapConfiguration. Remove these as soon as
     * possible!
     */
    protected void setParent(Configuration parent) {
        this.parent = parent;
    }

    protected void setName(String name) {
        this.name = name;
    }

    /**
     * Stack of the files from which input are being read. New inputs should
     * always be added to the top of the stack. The stack is initialized with a
     * dummy entry that represents the current directory so that all references
     * will be relative to that one.
     */
    private Stack<File> fileStack;

    /**
     * If we have no parent map, then start out with this stack
     */
    private static final Stack<File> initFileStack() {
        Stack<File> fileStack = new Stack<File>();

        // name of the current directory
        String currentDir = System.getProperty("user.dir");

        // file object that represents the same directory (this is
        // necessary since we will attempt to get the parent of this
        // object (which will be ourselves)
        File fileForCurrDir = new File(currentDir, ".");

        // always leave something on the stack so that the relative
        // lookup algorithm doesn't have to worry about special cases
        fileStack.push(fileForCurrDir);

        return fileStack;
    }

    /**
     * Delay-create the file stack; only those sections that include another
     * file need the file stack of itself and the parent. This function creates
     * the stack upon the first invocation (recursively). Use this instead of
     * accessing the fileStack member directly.
     */
    Stack<File> getFileStack() {
        if (this.fileStack == null) {
            // use the same stack as the parent, or the root stack if no
            // parent is specified. since the entire child is parsed before
            // the parent is resumed, this should be safe
            if (this.parent == null) {
                this.fileStack = initFileStack();
            } else {
                this.fileStack = this.parent.getFileStack();
            }
        }
        return this.fileStack;
    }

    /**
     * Creates an empty sub-configuration in the tree
     */
    protected Configuration(Configuration parent, String name) {
        this.parent = parent;
        this.name = name;
        config = new HashMap<String, Object>();
        requested = new HashSet<String>();
    }

    /**
     * Creates a nested configuration
     */
    Configuration(Configuration parent, String name, StringInputStream input)
            throws IOException {
        this.parent = parent;
        this.name = name;
        config = new HashMap<String, Object>();
        requested = new HashSet<String>();
        new ParseAction().doForInput(input);
    }

    /**
     * Reads configuration information from the given filename
     */
    public Configuration(String fileName) throws IOException,
            FileNotFoundException {
        config = new HashMap<String, Object>();
        requested = new HashSet<String>();
        doForFile(fileName, new ParseAction(), new RelativeToCurrentDirectory());
    }

    /**
     * Signature of a function that locates a file based on a relative path
     * specification.
     */
    private interface FileLookup {
        File lookup(File fileName) throws IOException;
    }

    /**
     * Locate relative files based on the current directory of the program
     * (usual semantics)
     */
    private static class RelativeToCurrentDirectory implements FileLookup {
        public File lookup(File fileName) throws IOException {
            return fileName.getCanonicalFile();
        }
    }

    /**
     * Locate relative files based on the directory of the file from which we
     * are currently reading. This is useful for files that include other files
     * again.
     */
    private class RelativeToCurrentFile implements FileLookup {
        public File lookup(File fileName) throws IOException {
            // if the filename is relative then resolve it against the
            // directory name that is at the top of the stack
            if (!fileName.isAbsolute()) {
                File currentFile = getFileStack().peek();
                File currentDir = currentFile.getParentFile();
                fileName = new File(currentDir, fileName.toString());
                fileName = fileName.getCanonicalFile();
            }

            // at this point the filename is the absolute name of the
            // file. note that we blalantly overwrote the reference on
            // the stack that was passed us as a parameter, but that
            // this change won't propagate to our caller.
            return fileName;
        }
    }

    /**
     * Signature for an action that can be performed when reading from the
     * input. It is parameterized on the resulting type that is read.
     */
    private static interface Action<T> {
        T doForInput(StringInputStream input) throws IOException;
    }

    /**
     * Continue parsing configuration from a specific file, using a specified
     * strategy for locating the file from the relative name
     */
    <T> T doForFile(String fileName, Action<T> action, FileLookup strategy)
            throws IOException {
        // open the file for reading
        File relativeName = new File(fileName);
        File absoluteName = strategy.lookup(relativeName);
        FileReader fr = new FileReader(absoluteName);
        try {
            // if the file was found, note that we are currently reading
            // from another file than before
            getFileStack().push(absoluteName);

            // read the content into the configuration
            String name = absoluteName.toString();
            StringInputStream input = new StringInputStream(fr, name);

            // do common error handling when exiting from the file here
            // (if we do it in doForInput, then we'll get two of them;
            // one in the source file and one in the file that included it
            try {
                return action.doForInput(input);
            } catch (IOException e) {
                IOException ioe = new IOException(trace() + "At "
                        + input.getLocation());
                ioe.initCause(e);
                throw ioe;
            }
        } finally {
            // make sure that we don't leak file handles in case of an
            // error of any kind
            fr.close();

            // we are no longer reading from this file
            getFileStack().pop();
        }
    }

    /**
     * Recursive-decent parsing of include statements.
     * 
     * @param <T>
     *                Datatype of the file that could be included; top-level
     *                includes must be other configuration files, arrays must
     *                include lists.
     * @param key
     *                One keyword look-ahead into the input stream, which is
     *                used to determine if this is an include statement.
     * @param input
     *                Stream from which further keywords should be read
     * @param action
     *                Parser that will handle the reading of the included file
     * @return Object found through an include file, or null if the statement is
     *         not an include statement.
     */
    <T> T parseInclude(String key, StringInputStream input, Action<T> action)
            throws IOException {
        // kind of include
        FileLookup strategy;

        // determine if this is an include statement; the strategy
        // variable serves as a flag (non-null or null)
        if (key.equals("include")) {
            strategy = new RelativeToCurrentDirectory();
        } else if (key.equals("rinclude")) {
            strategy = new RelativeToCurrentFile();
        } else {
            strategy = null;
        }

        // if we found an include keyword, then delegate the parsing to
        // the appropriate action after getting the filename; otherwise
        // just propagate the bottom value (i.e. null) to the outer layer
        // to resume parsing at the look-ahead token (no extra keywords
        // have been read from the stream)
        T result;
        if (strategy != null) {
            String fileName = input.getString();
            // if the filename has been enclosed with apostrophs, then read new
            // tokens until we find the one with a closing apostroph, and remove
            // the prefix and suffix afterwards. note however, that filenames
            // with spaces will still not work correctly due to the implicit
            // stripping of whitespace that is done by the parser.
            String quote = fileName.substring(0, 1);
            if (quote.equals("\'") || quote.equals("\"")) {
                while (!fileName.endsWith(quote)) {
                    fileName = fileName + input.getString();
                }
                fileName = fileName.substring(quote.length(), fileName.length()
                        - quote.length());
            }
            result = doForFile(fileName, action, strategy);
        } else {
            result = null;
        }

        // single return-point for the routine
        return result;
    }

    /**
     * Reads configuration information from the given stream, inserting it into
     * the passed configuration map
     */
    private class ParseAction implements Action<Configuration> {
        public Configuration doForInput(StringInputStream input)
                throws IOException {
            try {
                for (String key = input.getString().toLowerCase(); !key
                        .equals("end"); key = input.getString().toLowerCase()) {

                    // A subsection? Parse it recursively
                    if (key.equals("begin")) {
                        key = input.getString();
                        Configuration sub = new Configuration(
                                Configuration.this, key, input);
                        add(input, key.toLowerCase(), sub);
                    }

                    // A file to be included?
                    else if (parseInclude(key, input, this) != null) {
                        // Action done inside parseInclude
                    }

                    // Hinted number of arrays; we get to know both the size and
                    // the type of the elements and are thus able to optimize
                    // storage of the data that is read. the type keyword
                    // introduces the hint since there are only two of them (and
                    // they can be considered to be "reserved" in the same way
                    // that 'array' already is.
                    else if (key.equals("int")) {
                        // next token after the type hint is the size hint; this
                        // must be present if there is any point in doing more
                        // efficient reading (a dynamic array of Integer does
                        // not save much space compared to one of String, and we
                        // must parse the contents sometime -- doesn't matter
                        // much where)
                        int count = Integer.parseInt(input.getString());

                        // after the size comes the 'array' keyword; it has no
                        // function than to make the input file more readable
                        if (!input.getString().equals("array"))
                            throw new IOException("Got hint, but not array");

                        // 'array' keyword introduces us to the name of the key
                        // this must of course be read before all the data and
                        // kept until we can write it together with the array
                        String name = input.getString();

                        // read using the specialized array class for this type;
                        // notice that it is passed the size of the array to the
                        // constructor
                        int[] a = new IntArrayAction(count).doForInput(input);

                        // add the specialized array to the map; even if the
                        // code that reads this item is not capable of digesting
                        // this particular type, it is no problem converting it
                        // to string
                        add(input, name, a);
                    } else if (key.equals("double")) {
                        int count = Integer.parseInt(input.getString());
                        if (!input.getString().equals("array"))
                            throw new IOException("Got hint, but not array");
                        String name = input.getString();
                        double[] a = new DoubleArrayAction(count)
                                .doForInput(input);
                        add(input, name, a);
                    }

                    // An array of numbers. We store the array as an array of
                    // strings, which is the most general approach
                    else if (key.equals("array")) {
                        key = input.getString().toLowerCase();
                        String[] a = new StringArrayAction().doForInput(input);
                        add(input, key, a);
                    }

                    else if (isNumber(key))
                        throw new IOException(
                                "Expected a string, but got the number " + key);

                    // Otherwise, this is a standard "keyword value" map
                    else
                        add(input, key, input.getString());
                }
            } catch (EOFException e) {
                // ignored -- implicit end of section
            }

            return Configuration.this;
        }

        /**
         * Reads in an array of strings, stopping if the "end" keyword is found
         */
        private abstract class ArrayAction<T> implements Action<T> {
            public T doForInput(StringInputStream input) throws IOException {
                try {
                    for (String key = input.getString().toLowerCase(); !key
                            .equals("end"); key = input.getString()
                            .toLowerCase()) {
                        // includes are now allowed in the middle (!) of an
                        // array section; check each token for such. if the
                        // token is an array, then it will have been recursively
                        // parsed since we pass ourself down the chain (and
                        // consequently all the elements already added to our
                        // list); if the parseInclude method didn't take the
                        // token (signaled by returning null), then we are in
                        // the base case (single element, and add the token
                        // directly to the list).
                        if (parseInclude(key, input, this) == null) {
                            shift(key);
                        }
                    }
                } catch (EOFException e) {
                    // ignored -- implicit end of array
                }

                // return the list so that the outer client can get hold of the
                // memory that was created. if we get to this function
                // recursively, then we're adding directly to the parent
                // caller's list, and we don't need the actual reference for
                // anything.
                return reduce();
            }

            // convert the token (one element in the array) to the type expected
            // for this array, and merge it with the internal list
            // representation
            protected abstract void shift(String token);

            // observe the internal list (return its semantic object). this
            // method is called for each recursive level, so return a type that
            // is mergeable with the internal list without performance penalties
            protected abstract T reduce();
        }

        /**
         * A dynamic array is an input array which is not hinted with size; the
         * elements are read into a structure that expands easily.
         * 
         * @param <T>
         *                Basic type of the array elements, e.g. String
         */
        private abstract class DynamicArrayAction<T> extends
                ArrayAction<List<T>> {
            // use a dynamic data structure since we don't know how many
            // elements we are going to read
            List<T> list = new ArrayList<T>();

            @Override
            protected void shift(String token) {
                this.list.add(fromString(token));
            }

            @Override
            protected List<T> reduce() {
                return this.list;
            }

            // keep the type of the elements away from the type of the list (the
            // list is a monad); this method help us glue the string reading
            // with the abstract list representation.
            protected abstract T fromString(String token);
        }

        /**
         * Dynamic array of string elements. If an array is untyped, the string
         * representation of its elements will be retained in memory until a
         * getXxx call specify which type that is expected.
         * 
         * Notice how we could also create dynamic arrays of other types easily
         * by simply writing a glue class like this that describes the
         * conversion from String (unfortunately, there is no common interface
         * between parseInt() and parseDouble(), forcing us to write a
         * conversion class like this.
         */
        private class DynamicStringArrayAction extends
                DynamicArrayAction<String> {
            @Override
            protected String fromString(String token) {
                return token;
            }
        }

        /**
         * Wrapper class that converts a dynamic list to a fixed-size array that
         * can be put into the configuration. Having a different class to read
         * the items into a dynamic list is needed since it is passed
         * recursively down the descent chain, but we want to have a fixed-size
         * array when reading it back from the configuration tree.
         */
        private class StringArrayAction implements Action<String[]> {
            public String[] doForInput(StringInputStream input)
                    throws IOException {
                List<String> list = new DynamicStringArrayAction()
                        .doForInput(input);
                String[] a = list.toArray(new String[list.size()]);
                return a;
            }
        }

        /**
         * A fixed array keeps preallocated storage of the correct array type
         * and simply assign the elements directly into the next slot as they
         * are read, giving us the most efficient implementation (the array is
         * returned immediately with no intermediate copying).
         */
        private class IntArrayAction extends ArrayAction<int[]> {
            // storage for the elements that we are reading. the constructor
            // will setup the array to contain the exact number of elements
            // expected.
            private int[] array;

            // number of items read so far; whenever we read another token and
            // put it into the array, we
            private int items = 0;

            public IntArrayAction(int count) {
                this.array = new int[count];
                this.items = 0;
            }

            @Override
            protected void shift(String token) {
                this.array[this.items++] = Integer.parseInt(token);
            }

            @Override
            protected int[] reduce() {
                return this.array;
            }
        }

        /**
         * Basically the same as IntArrayAction but for doubles. We need an
         * entire copy of this class since native types cannot be used for
         * generic parameters.
         */
        private class DoubleArrayAction extends ArrayAction<double[]> {
            // storage for the elements that we are reading. the constructor
            // will setup the array to contain the exact number of elements
            // expected.
            private double[] array;

            // number of items read so far; whenever we read another token and
            // put it into the array, we
            private int items = 0;

            public DoubleArrayAction(int count) {
                this.array = new double[count];
                this.items = 0;
            }

            @Override
            protected void shift(String token) {
                this.array[this.items++] = Double.parseDouble(token);
            }

            @Override
            protected double[] reduce() {
                return this.array;
            }
        }

        /**
         * Checks if the given key is a number
         */
        private boolean isNumber(String key) {
            try {
                Double.valueOf(key);
            } catch (NumberFormatException e) {
                return false;
            }
            return true;
        }

        /**
         * Adds the given value into the given map, using the given key. Does
         * some duplication error checking, in which case an exception is thrown
         */
        private void add(StringInputStream input, String key, Object value) {
            if (config.put(key, value) != null)
                throw new IllegalArgumentException(trace() + "At "
                        + input.getLocation() + ": Duplicate keyword '" + key
                        + "'");
        }
    }

    /**
     * Returns the string value for the given key, using the provided default if
     * no value exists
     * 
     * @param key
     *                The configuration key to look up
     * @param def
     *                Default value to use
     */
    public String getString(String key, String def) {
        requested.add(key.toLowerCase());

        Object obj = config.get(key.toLowerCase());
        if (obj != null)
            if (obj instanceof String)
                return (String) obj;
            else
                errorType(key, obj);

        if (def != null)
            return def;

        throw new IllegalArgumentException(trace() + "The required key '" + key
                + "' was not found");
    }

    /**
     * Returns the string value for the given key
     * 
     * @param key
     *                The configuration key to look up
     * @throws IllegalArgumentException
     *                 If the key was not found
     */
    public String getString(String key) {
        return getString(key, null);
    }

    /**
     * Mark a certain key as read (even if the value has not been returned). If
     * the key is a section, then all keys within that section will also be
     * regarded as read. Use this method if you want to ignore a certain section
     * without having to read through its contents.
     * 
     * @param key
     *                Name of the key that should be regarded as read.
     */
    public void touch(String key) {
        // mark the parent key itself
        requested.add(key.toLowerCase());

        // recursively touch everything that was in this key
        Object obj = config.get(key.toLowerCase());
        if (obj != null && obj instanceof Configuration) {
            Configuration config = (Configuration) obj;
            for (String subkey : config.keys()) {
                config.touch(subkey);
            }
        }
    }

    /**
     * Gets the boolean value for the given key, returning the default value if
     * it was not found. It is true for the string values "yes" or "true", false
     * for the string values "no" or "false" (case insensitive)
     * 
     * @param key
     *                The configuration key to look up
     * @param def
     *                Default value to use
     */
    public boolean getBoolean(String key, boolean def) {
        requested.add(key.toLowerCase());

        Object obj = config.get(key.toLowerCase());
        if (obj != null)
            if (obj instanceof String) {
                String s = (String) obj;
                if (s.equalsIgnoreCase("yes") || s.equalsIgnoreCase("true"))
                    return true;
                else if (s.equalsIgnoreCase("no")
                        || s.equalsIgnoreCase("false"))
                    return false;

                throw new IllegalArgumentException(trace()
                        + "The value for the key '" + key
                        + "' must be boolean (yes/no)");
            } else
                errorType(key, obj);

        return def;
    }

    /**
     * Gets the boolean value for the given key. It is true for the string
     * values "yes" or "true", false for the string values "no" or "false" (case
     * insensitive)
     * 
     * @param key
     *                The configuration key to look up
     * @throws IllegalArgumentException
     *                 If the key was not found
     * @throws NumberFormatException
     *                 For conversion errors
     */
    public boolean getBoolean(String key) throws IllegalArgumentException,
            NumberFormatException {
        requested.add(key.toLowerCase());

        Object obj = config.get(key.toLowerCase());
        if (obj != null)
            if (obj instanceof String) {
                String s = (String) obj;
                if (s.equalsIgnoreCase("yes") || s.equalsIgnoreCase("true"))
                    return true;
                else if (s.equalsIgnoreCase("no")
                        || s.equalsIgnoreCase("false"))
                    return false;

                throw new IllegalArgumentException(trace()
                        + "The value for the key '" + key
                        + "' must be boolean (yes/no)");
            } else
                errorType(key, obj);

        throw new IllegalArgumentException(trace() + "The required key '" + key
                + "' was not found");
    }

    /**
     * Gets the integer for the given key, returning the default value if it was
     * not found
     * 
     * @param key
     *                The configuration key to look up
     * @param def
     *                Default value to use
     */
    public int getInt(String key, int def) {
        requested.add(key.toLowerCase());

        Object obj = config.get(key.toLowerCase());
        if (obj != null)
            if (obj instanceof String)
                try {
                    return Integer.parseInt((String) obj);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException(trace()
                            + "Expected an integer value for the key '" + key
                            + "', got '" + obj + "'");
                }
            else
                errorType(key, obj);

        return def;
    }

    /**
     * Gets the integer for the given key
     * 
     * @param key
     *                The configuration key to look up
     * @throws IllegalArgumentException
     *                 If the key was not found
     * @throws NumberFormatException
     *                 For conversion errors
     */
    public int getInt(String key) {
        requested.add(key.toLowerCase());

        Object obj = config.get(key.toLowerCase());
        if (obj != null)
            if (obj instanceof String)
                try {
                    return Integer.parseInt((String) obj);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException(trace()
                            + "Expected an integer value for the key '" + key
                            + "', got '" + obj + "'");
                }
            else
                errorType(key, obj);

        throw new IllegalArgumentException(trace() + "The required key '" + key
                + "' was not found");
    }

    /**
     * Gets the double for the given key, returning the default value if it was
     * not found
     * 
     * @param key
     *                The configuration key to look up
     * @param def
     *                Default value to use
     */
    public double getDouble(String key, double def) {
        requested.add(key.toLowerCase());

        Object obj = config.get(key.toLowerCase());
        if (obj != null)
            if (obj instanceof String)
                try {
                    return Double.parseDouble((String) obj);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException(trace()
                            + "Expected a numerical value for the key '" + key
                            + "', got '" + obj + "'");
                }
            else
                errorType(key, obj);

        return def;
    }

    /**
     * Gets the double for the given key
     * 
     * @param key
     *                The configuration key to look up
     * @throws IllegalArgumentException
     *                 If the key was not found
     * @throws NumberFormatException
     *                 For conversion errors
     */
    public double getDouble(String key) {
        requested.add(key.toLowerCase());

        Object obj = config.get(key.toLowerCase());
        if (obj != null)
            if (obj instanceof String)
                try {
                    return Double.parseDouble((String) obj);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException(trace()
                            + "Expected a numerical value for the key '" + key
                            + "', got '" + obj + "'");
                }
            else
                errorType(key, obj);

        throw new IllegalArgumentException(trace() + "The required key '" + key
                + "' was not found");
    }

    /**
     * Returns the integer array for the given key
     * 
     * @param key
     *                Configuration key to look up
     * @return The array for the given key. If the key was not found, an empty
     *         array is returned instead
     * @throws NumberFormatException
     *                 For conversion errors
     */
    public int[] getIntArray(String key) {
        requested.add(key.toLowerCase());

        Object obj = config.get(key.toLowerCase());
        if (obj != null) {
            if (obj instanceof String[]) {
                String[] data = (String[]) obj;
                int[] ret = new int[data.length];
                for (int i = 0; i < ret.length; ++i)
                    ret[i] = Integer.parseInt(data[i]);
                return ret;
            } else if (obj instanceof int[]) {
                int[] ret = ((int[]) obj);
                return ret/* .clone() */;
            } else
                errorType(key, obj);
        }

        return new int[0];
    }

    /**
     * Returns the double array for the given key
     * 
     * @param key
     *                Configuration key to look up
     * @return The double-array for the given key. If the key was not found, an
     *         empty array is returned instead
     * @throws NumberFormatException
     *                 For conversion errors
     */
    public double[] getDoubleArray(String key) {
        requested.add(key.toLowerCase());

        Object obj = config.get(key.toLowerCase());
        if (obj != null) {
            if (obj instanceof double[]) {
                double[] ret = (double[]) obj;
                return ret/* .clone() */;
            } else if (obj instanceof String[]) {
                String[] data = (String[]) obj;
                double[] ret = new double[data.length];
                for (int i = 0; i < ret.length; ++i)
                    ret[i] = Double.parseDouble(data[i]);
                return ret;
            } else
                errorType(key, obj);
        }

        return new double[0];
    }

    /**
     * Returns the string array for the given key
     * 
     * @param key
     *                Configuration key to look up
     * @return The array for the given key. If the key was not found, an empty
     *         array is returned instead
     */
    public String[] getStringArray(String key) {
        requested.add(key.toLowerCase());

        Object obj = config.get(key.toLowerCase());
        if (obj != null) {
            if (obj instanceof String[]) {
                String[] data = (String[]) obj;

                // This copying is done so that the user cannot change the
                // underlying datastructure
                String[] ret = new String[data.length];
                for (int i = 0; i < ret.length; ++i)
                    ret[i] = data[i];
                return ret;
            } else
                errorType(key, obj);
        }

        return new String[0];
    }

    /**
     * Returns the sub-configuration for the given key
     * 
     * @param key
     *                Key of the sub-configuration to look up
     * @return The requested configuration. It will be an empty configuration if
     *         the key was not found
     */
    public Configuration getConfiguration(String key) {
        requested.add(key.toLowerCase());

        Object obj = config.get(key.toLowerCase());
        if (obj != null) {
            if (obj instanceof Configuration)
                return (Configuration) obj;
            errorType(key, obj);
        }

        return new Configuration(this, key);
    }

    /**
     * Returns an iterator over the configuration keys
     */
    public Collection<String> keys() {
        return Collections.unmodifiableCollection(config.keySet());
    }

    /**
     * Gets the number of keys
     */
    public int numKeys() {
        return config.size();
    }

    /**
     * Gives an error that the type was of incorrect type
     */
    private void errorType(String key, Object obj) {
        if (obj instanceof Configuration)
            throw new IllegalArgumentException(trace() + "The key \"" + key
                    + "\" has the incorrect type: configuration");
        else if (obj instanceof int[])
            throw new IllegalArgumentException(trace() + "The key \"" + key
                    + "\" has the incorrect type: integer array");
        else if (obj instanceof double[])
            throw new IllegalArgumentException(trace() + "The key \"" + key
                    + "\" has the incorrect type: double array");
        else if (obj instanceof String[])
            throw new IllegalArgumentException(trace() + "The key \"" + key
                    + "\" has the incorrect type: string array");
        else if (obj instanceof Integer)
            throw new IllegalArgumentException(trace() + "The key \"" + key
                    + "\" has the incorrect type: integer");
        else if (obj instanceof Double)
            throw new IllegalArgumentException(trace() + "The key \"" + key
                    + "\" has the incorrect type: double");
        else if (obj instanceof String)
            throw new IllegalArgumentException(trace() + "The key \"" + key
                    + "\" has the incorrect type: string");
        else
            throw new IllegalArgumentException(trace() + "The key \"" + key
                    + "\" has an unknown type");
    }

    /**
     * Gives a configuration "stack" trace for diagnostics
     */
    public String trace() {
        Configuration config = this;
        String trace = " ";
        while (config != null) {
            String name = config.name;
            if (name != null)
                trace = config.name + ":" + trace;
            config = config.parent;
        }
        return trace;
    }

    /**
     * Checks that all configuration keys have been requested
     * 
     * @throws IllegalStateException
     *                 If there are any unrequested keys
     */
    public void ensureEmpty() {
        for (String key : config.keySet()) {
            if (!requested.contains(key))
                throw new IllegalStateException(trace() + "The key '" + key
                        + "' was never requested");

            // Recurse into sub configurations
            Object value = config.get(key);
            if (value instanceof Configuration)
                ((Configuration) value).ensureEmpty();
        }
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
        StringBuilder output = Configuration.output.get();
        for (String key : config.keySet()) {
            Object obj = config.get(key);

            if (obj instanceof String || obj instanceof Integer
                    || obj instanceof Double) {
                output.append(pre).append(key).append(' ').append(
                        obj.toString()).append('\n');
            }

            // funnily, 'obj instanceof Array' does not work as one should
            // expect intuitively
            else if (obj instanceof int[] || obj instanceof double[]
                    || obj instanceof String[]) {
                // indentation
                output.append(pre);

                // write the type and size hints to output if the array is of a
                // native type (will optimize reading the array back in)
                String length = Integer.toString(Array.getLength(obj));
                if (obj instanceof double[]) {
                    output.append("double ").append(length).append(' ');
                } else if (obj instanceof int[]) {
                    output.append("int ").append(length).append(' ');
                }

                // all arrays have this prefix, regardless of type
                output.append("array ").append(key).append('\n');

                // code for each array type is duplicated because we want to
                // call the right version of append()
                if (obj instanceof double[]) {
                    double[] array = (double[]) obj;
                    int width = LINE_LENGTH - pre.length();
                    for (int i = 0; i < array.length;) {
                        output.append(pre).append('\t');
                        for (; i < array.length && output.length() < width; i++) {
                            output.append(array[i]).append(' ');
                        }
                        output.append('\n');
                        // flush after each line in an array since the array may
                        // be very long (try to keep the buffer short)
                        str.append(output);
                        output.setLength(0);
                    }
                } else if (obj instanceof int[]) {
                    int[] array = (int[]) obj;
                    int width = LINE_LENGTH - pre.length();
                    for (int i = 0; i < array.length;) {
                        output.append(pre).append('\t');
                        for (; i < array.length && output.length() < width; i++) {
                            output.append(array[i]).append(' ');
                        }
                        output.append('\n');
                        str.append(output);
                        output.setLength(0);
                    }
                } else { // if (obj instanceof String[]) {
                    String[] array = (String[]) obj;
                    int width = LINE_LENGTH - pre.length();
                    for (int i = 0; i < array.length;) {
                        output.append(pre).append('\t');
                        for (; i < array.length && output.length() < width; i++) {
                            output.append(array[i]).append(' ');
                        }
                        output.append('\n');
                        str.append(output);
                        output.setLength(0);
                    }
                }

                output.append(pre).append("end").append('\n');
            }

            else if (obj instanceof Configuration) {
                output.append(pre).append("begin ").append(key).append('\n');
                Configuration sub = (Configuration) obj;
                // notice that the output buffer is reused since it is for the
                // same thread; only if the subconfiguration contains arrays
                // will it be flushed
                sub.outputConfiguration(str, pre + '\t');
                output.append(pre).append("end").append('\n');
            } else
                errorType(key, obj);

            // flush current output (at this time it is converted to a string)
            str.append(output);
            output.setLength(0);
        }
    }

    /**
     * Returns true if this configurations contains a mapping for the specified
     * key.
     */
    public boolean containsKey(String key) {
        return config.containsKey(key.toLowerCase());
    }

    /**
     * Gets a function
     * 
     * @param name
     *                The configuration key to look up
     * @param d
     *                Required dimension
     * @throws IllegalArgumentException
     *                 If the key was not found, if the type is incorrect, or if
     *                 the dimension is wrong
     */
    public Function getFunction(String name, int d) {
        // Check for a constant function first (shortcut)
        Function c = getConstantValue(name);
        if (c != null)
            return c;

        Function f = getObject(name, Function.class, name);

        if (!f.isDimension(d))
            throw new IllegalArgumentException(trace() + "The function '" + f
                    + "' must be " + (d + 1) + "-dimensional");
        else
            return f;
    }

    /**
     * Gets a function, falling back on a default if it isn't found
     * 
     * @param name
     *                The configuration key to look up
     * @param d
     *                Required dimension
     * @param def
     *                Default function to be created if the key was not found
     * @param args
     *                Arguments to pass to the default function
     * @throws IllegalArgumentException
     *                 If the type is incorrect, or if the dimension is wrong
     */
    public Function getFunction(String name, int d,
            Class<? extends Function> def, Object... args) {

        // Check for a constant function first (shortcut)
        Function c = getConstantValue(name);
        if (c != null)
            return c;

        /*
         * We're not calling getObject, since the arguments given here are only
         * to be passed to the default function, and not to the function we're
         * seeking (ie. the one specified by the "type" field)
         */

        Configuration sub = getConfiguration(name);
        String className = sub.getString("type", def.getSimpleName());

        Function f;
        if (!sub.containsKey("type"))
            f = createObject(className, Function.class, fuse(sub, args));
        else
            f = createObject(className, Function.class, sub, name);

        if (!f.isDimension(d))
            throw new IllegalArgumentException(trace() + "The function '" + f
                    + "' must be " + (d + 1) + "-dimensional");
        else
            return f;
    }

    /**
     * Check if a constant value has been given as a shortcut for a function
     * 
     * @param name
     *                Name of the function to seek
     * @return A constant value function if a double value was present, else
     *         null
     */
    private ConstantValue getConstantValue(String name) {
        requested.add(name.toLowerCase());
        Object obj = config.get(name.toLowerCase());

        if (obj != null && obj instanceof String)
            try {
                return new ConstantValue(name, Double.parseDouble((String) obj));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(trace()
                        + "Expected a numerical value for the key '" + name
                        + "', got '" + obj + "'");
            }

        return null;
    }

    /**
     * Creates a class instance
     * 
     * @param type
     *                Type of the object. The actual class may be a subclass
     * @param args
     *                Optional arguments to the constructor. These follow the
     *                config
     */
    public <T> T getObject(String name, Class<T> type, Object... args) {
        Configuration sub = getConfiguration(name);
        String className = sub.getString("type");

        return createObject(className, type, fuse(sub, args));
    }

    /**
     * Creates a class instance, falling back to a default if necessary
     * 
     * @param type
     *                Type of the object. The actual class may be a subclass
     * @param def
     *                Default class if a type was not given in the configuration
     * @param args
     *                Optional arguments to the constructor. These follow the
     *                config
     */
    public <T> T getObject(String name, Class<T> type, Class<?> def,
            Object... args) {
        Configuration sub = getConfiguration(name);
        String className = sub.getString("type", def.getSimpleName());

        return createObject(className, type, fuse(sub, args));
    }

    /**
     * Prepends the first argument to the second argument array. Returns a new
     * array large enough to accomodate this
     */
    private Object[] fuse(Object add, Object... array) {
        Object[] newArray = new Object[array.length + 1];
        newArray[0] = add;
        for (int i = 0; i < array.length; ++i)
            newArray[i + 1] = array[i];
        return newArray;
    }

    /**
     * Creates an object of the given name, passing the given arguments to it.
     * The name must is just a class-name, the fully qualified name is made by
     * prepending the package name of the desired class type
     */
    private <T> T createObject(String name, Class<T> type, Object... args) {

        // Get the class, checking that it exists and is of the right type
        Class<T> clazz = getClass(name, type);

        // Then get a constructor accepting the desired arguments
        Constructor<T> constructor = getConstructor(clazz, args);

        // Then create an instance
        return createInstance(constructor, args);
    }

    /**
     * Creates an instance of an object, passing the given arguments to its
     * constructor
     * 
     * @param constructor
     *                Class constructor to use
     * @param args
     *                Arguments to pass to the constructor
     * @return Instance of the given class
     */
    private <T> T createInstance(Constructor<T> constructor, Object... args) {
        try {
            return constructor.newInstance(args);
        } catch (InvocationTargetException e) {

            // Check if this is a parser exception
            Throwable t = e.getTargetException();
            if (t instanceof IllegalArgumentException)
                throw (IllegalArgumentException) t;

            throw new IllegalArgumentException(String.format(
                    "Error creating '%s'", constructor.getDeclaringClass()
                            .getName()), t);
        } catch (Exception e) {
            throw new IllegalArgumentException(String.format(
                    "Error creating '%s'", constructor.getDeclaringClass()
                            .getName()), e);
        }
    }

    /**
     * Gets a constructor for the given class accepting the given arguments
     */
    @SuppressWarnings("unchecked")
    private <T> Constructor<T> getConstructor(Class<T> clazz, Object... args) {

        // Get the class of each argument
        Class[] argtype = new Class[args.length];
        for (int i = 0; i < args.length; ++i)
            argtype[i] = getPrimitive(args[i].getClass());

        // Find an appropriate constructor by looking for exact match
        try {
            return clazz.getConstructor(argtype);
        } catch (SecurityException e) {
            throw new IllegalArgumentException("Security violation creating '"
                    + clazz.getName() + "': " + e);
        } catch (NoSuchMethodException e) {

            // No direct match, try checking for compatible constructors
            for (Constructor<T> c : (Constructor<T>[]) clazz.getConstructors())
                if (parametersMatches(c.getParameterTypes(), args))
                    return c;

        }

        // Nothing found
        throw new IllegalArgumentException("Error creating '" + clazz.getName()
                + "': No constructor matches the arguments "
                + Arrays.toString(argtype));
    }

    private <T> boolean parametersMatches(Class<?>[] args1, Object... args2) {
        if (args1.length != args2.length)
            return false;

        for (int i = 0; i < args1.length; ++i)
            if (!args1[i].isAssignableFrom(getPrimitive(args2[i].getClass())))
                return false;

        return true;
    }

    @SuppressWarnings("unchecked")
    private <T> Class<T> getClass(String name, Class<T> type) {
        // Construct complete type name (package + name)
        String fullName = type.getPackage().getName() + "." + name;

        // Try to find the class
        Class clazz = null;

        try {

            // Try the constructed name
            clazz = Class.forName(fullName);

        } catch (ClassNotFoundException e1) {

            try {

                // That failed, try the plain name
                clazz = Class.forName(name);

            } catch (ClassNotFoundException e2) {

                // Still not found? We give up
                throw new IllegalArgumentException("The '" + name + "' or '"
                        + fullName + "' class was not found");

            }
        }

        // Check that it implements the specified interface
        if (!type.isAssignableFrom(clazz))
            throw new IllegalArgumentException("" + clazz.getName()
                    + " is not of the required type " + type.getName());

        return clazz;
    }

    /**
     * Gets the primitive version of the given class type (aka. auto un-boxing).
     * If it doesn't correspond to a primitive, the type is returned as-is
     */
    private <T> Class<?> getPrimitive(Class<T> type) {
        if (type == Boolean.class)
            return boolean.class;
        else if (type == Character.class)
            return char.class;
        else if (type == Byte.class)
            return byte.class;
        else if (type == Short.class)
            return short.class;
        else if (type == Integer.class)
            return int.class;
        else if (type == Long.class)
            return long.class;
        else if (type == Float.class)
            return float.class;
        else if (type == Double.class)
            return double.class;
        else if (type == Void.class)
            return void.class;
        else
            // Not a primitive
            return type;
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
         * Name of the stream. If we are reading from file, then this is the
         * filename, if we are reading from the console, then it is null.
         */
        private String name;

        /**
         * Creates a new input stream for the given reader
         */
        public StringInputStream(Reader r, String name) {
            this.name = name;
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

            // shorter name for the file
            String fileName = (name == null ? "" : friendlyPath(name));

            // only include the filename if there actually is one
            String fmt = (name == null ? "line %d" : "line %d in file %s");
            String location = String.format(fmt, lineNumber, fileName);
            return location;
        }
    }

    /**
     * Normalize the path to something that the user see on the command-line
     */
    protected static String friendlyPath(String path) {
        // if we find the path to the user's home directory at the
        // start of the filename, then replace it with a tilde
        String homeDir = System.getProperty("user.home");
        if (path.substring(0, homeDir.length()).equals(homeDir)) {
            String relPath = path.substring(homeDir.length());
            path = new File("~", relPath).toString();
        }
        return path;
    }

}