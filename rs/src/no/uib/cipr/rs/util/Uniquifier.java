package no.uib.cipr.rs.util;

/**
 * Compacts a data array by creating a new representation that is both sorted
 * and unique, i.e. no value appears more than once. Provides the opportunity
 * to remap an array of indices into the old array to indices into the new 
 * array. Requires O(n*log n) time and O(n) space.
 * 
 * @author roland.kaufmann@cipr.uib.no
 */
public class Uniquifier {
    // references to the array that is being uniquified    
    private double[] value;
    
    // mapping table of its indices (from the old array to the new array). for
    // each element reverse[j]=i holds that what was earlier in value[j] is
    // now in value[i].
    private int[] reverse;

    // sorting algebra that sorts both the values and retain the indices
    private static class IndexValueCosorter extends Sorter {
        // how close can values become before they are considered equal
        private double tolerance;
        
        // keys for comparison
        private double[] value;
        
        // mapping, which will become from the new array to the old array
        private int[] index;
        
        // hold all (extra) input variables to the sort operation in our
        // own members so that we can refer to them during the virtual call
        IndexValueCosorter(double[] value, int[] index, double tolerance) {
            this.tolerance = tolerance;
            this.value     = value;
            this.index     = index;
        }
        
        @Override
        protected int length() {
            return value.length;
        }
        @Override
        protected boolean lessThan(int a, int b) {
            // don't differentiate between values that are almost equal
            return (value[b] - value[a]) > tolerance;
        }
        @Override
        protected void swap(int a, int b) {
            // perform the swap itself
            double d = value[a];
            value[a] = value[b];
            value[b] = d;
            
            // crux of this subclass: also keep track in the corresponding
            // index array which elements that have been moved where
            int i = index[a];
            index[a] = index[b];
            index[b] = i;
        }
    }
    
    /**
     * Create a unique (and sorted) representation of a data column. All
     * processing is done in the constructor of this object (i.e. the object
     * that is constructed represents the result of the process).
     * 
     * @param data
     *  Array containing data that should be compacted and remapped. This
     *  array will be destroyed as part of the process, and should no longer
     *  be referred to.
     * @param tolerance
     *  Tolerance within which the value should lay for them to be considered
     *  equal in the compaction process.
     * @param adopt
     *  If set to true, the function will use the existing area as scratch
     *  and overwrite the value. In that case, the input array should not
     *  be used again.
     */
    public Uniquifier(double[] data) {
        this(data, Tolerances.smallEps);
    }
    public Uniquifier(double[] data, boolean adopt) {
        this(data, Tolerances.smallEps, adopt);
    }
    public Uniquifier(double[] data, double tolerance) {
        this(data, tolerance, false);
    }
    public Uniquifier(double[] data, double tolerance, boolean adopt) {
        // initially we operate on the input data. clone it here if we intend
        // to keep the original array
        value = adopt ? data : data.clone();
        
        // allocate as many index elements as there are elements in the
        // original array. fill this array with the numbers from zero and
        // up to its length; initially every element is located at it index. 
        // during the sort operation the invariant is that for index[i]=j
        // what is now in data[i] was earlier in data[j]
        int[] index = new int[value.length];
        for(int i = 0; i < index.length; i++) {
            index[i] = i;
        }
        
        // sort the values while keeping track of which old index corresponds
        // to which new index
        new IndexValueCosorter(value, index, tolerance).sort();
        
        // compose a reverse-lookup map that tells us which element in the
        // index array that points to a particular item. there should be a
        // bijection between the index array and the reverse array afterwards            
        reverse = new int[index.length];
      
        // every value is to move down this many positions; this is the amount
        // of compaction that can be made of the data array
        int compaction = 0;
        
        for(int i = 1; i < value.length; i++) {
            // 'i - compaction' is the position of the i'th element in the new 
            // array; however observe that compaction may change during the
            // first statement, so the expression is not the same throughout
            // this entire block (however, logically it is)
            
            // if this element is equal to the one that preceeds it (taking
            // into account that we are compacting as we go),
            if(Math.abs(value[i] - value[i - compaction - 1]) < tolerance) {
                compaction++;
            }
            // otherwise, just move the item down the number of elements with
            // which we are currently compacting the array. note that i - 
            // compaction will always be less than i, so it is safe to write to 
            // this element given that we take elements from further up
            else {
                value[i - compaction] = value[i];
            }
            // adjust the index map so that we point at the new position
            // including compaction) instead
            reverse[index[i]] = i - compaction;
        }
        
        // create a new data array consisting only of the values we need
        // (the index map must retain the original length)
        if(compaction > 0) {
            double[] original = value;
            value = new double[original.length - compaction];
            System.arraycopy(original, 0, value, 0, value.length);
        }
    }
    
    /**
     * Result of the uniquificaion process. If this class was a function, then
     * this would be its return value.
     * 
     * @return
     *  Array containing unique values. This array may consequently be smaller
     *  than the original.
     */    
    public double[] get() {
        return value;
    }
    
    /**
     * Substitute old index pointers with new ones.
     * 
     * @param pointer
     *  Array containing indices into the old, original array that was passed
     *  to this algebra. After this function completes, the indices will be
     *  replaced with indices to elements in the new array containing the same
     *  values (up to tolerance).
     * @return
     *  The same array that was passed, to allow the function to be used in a
     *  call chain.
     */
    public int[] remap(int[] pointer) {
        return remap(pointer, 0, pointer.length);
    }
    
    /**
     * Remap a portion of an array. Useable if you have stored indices that
     * points to various other arrays, and they are stored consecutively based
     * on which array they point to. 
     */
    public int[] remap(int[] pointer, int start, int end) {
        for(int i = start; i < end; i++) {
            pointer[i] = remap(pointer[i]);
        }
        return pointer;
    }
    
    /**
     * Remap a single value.
     * 
     * @param
     *  Old index into the array, from before the array was uniquified.
     * @return
     *  New index into the array, which points to the same value (that has been
     *  moved to a new location).
     */
    public int remap(int oldIndex) {
        return reverse[oldIndex];
    }
}