package no.uib.cipr.rs.util;

/**
 * Sorts arbitrary compositions of data (e.g. two arrays which logically are
 * related). It only implements the sorting logic and does not make any
 * assumptions about data storage and handling (which is left for the subclass
 * to implement).
 * 
 * @author roland.kaufmann@cipr.uib.no
 */
public abstract class Sorter {
    
    /**
     * Report the length of the collecion that is to be sorted.
     * 
     * @return
     *  Number of elements in the underlaying collection, which is one less
     *  than the highest index, in the cases where there is a positive number.
     */
    protected abstract int length();
    
    /**
     * Determines the partial order between elements of the underlaying 
     * collection. Note that the test is not reflexive; it should report true
     * if and only if the first argument is strictly lesser than the second.
     * 
     * @param i
     *  Zero-based index of the first element to be evaluated.
     * @param j
     *  Zero-based index of the second element to be evaluated.
     * @return
     *  True if a[i] &lt; a[j], false otherwise (including equal).
     */
    protected abstract boolean lessThan(int i, int j);

    /**
     * Swap the position of two elements in the underlaying collecion.
     * 
     * @param i
     *  Zero-based index of the first element.
     * @param j
     *  Zero-based index of the second element.
     */
    protected abstract void swap(int i, int j);
    
    /**
     * Perform a sort on the underlaying collection. The sorting will be done
     * in-place in the original collection.
     */
    public void sort() {
        qsort(0, length() - 1);
    }
    
    // helper routine that implements the QuickSort algorithm
    private final void qsort(int low, int high) {
        // if it is simple enough, the do the sort ourselves (base case)
        if(low+1 >= high) {
            if(lessThan(high, low)) {
                swap(low, high);
            }
        }
        else {
            // get the median value, which we are going to use for pivot
            int middle = (low + high)/2;
            
            // sort low, middle, high to prevent degenerate cases
            if(lessThan(middle, low)) {
                swap(low, middle);
            }
            if(lessThan(high, low)) {
                swap(low, high);
            }
            if(lessThan(high, middle)) {
                swap(middle, high);
            }
            
            // place pivot at position high-1
            int pivot = high - 1;
            swap(middle, pivot);
            
            // begin partitioning; start from each end
            int i = low;
            int j = high;            
            for(;;) {
                // sort upwards from the beginning for an element greater than
                // the pivot (it should be on the other side)
                do {
                    i++;
                } while(lessThan(i, pivot));
                
                // sort downwards from the end for an element lesser than the
                // pivot (it should be on the other side)
                do {
                    j--;
                } while(lessThan(pivot, j));
                
                // if these two indices crossed, then we are done (because
                // everything with index smaller than i is less, and everything 
                // with index larger than j is greater than pivot
                if(j <= i) {
                    break;
                }
                
                // exchange these two elements to get on their right side of 
                // the pivot, and continue to search for more
                swap(i, j);
            }
            
            // restore pivot element to position between larger elements and
            // smaller elements.
            swap(i, pivot);
            
            // recursively sort smaller elements (leave pivot)
            qsort(low, i - 1);
            
            // recursively sort larger elements (leave pivot)
            qsort(i + 1, high);
        }
    }
}
