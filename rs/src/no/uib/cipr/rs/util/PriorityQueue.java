package no.uib.cipr.rs.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.Test;

/**
 * Priority queue that delivers the least element of those currently stored.
 * 
 * This implementation is comparator- and storage-agnostic since arrays don't
 * implement the List interface and native types don't implement Comparable.
 * 
 * This class may also be extended for implementations where the key that is
 * compared for ordering is not the data value returned from the queue.
 * 
 * @author roland.kaufmann@cipr.uib.no
 */
abstract class AbstractPriorityQueue<T> implements Iterator<T> {
    // number of elements currently in the heap. it starts out empty. since we
    // don't manage the memory ourselves, 
    private int heapSize /* = 0 */;
    
    /**
     * Helper method for navigating the heap 
     */
    private final int left(int i) {
        return 2*(i+1) - 1;
    }
    
    private final int right(int i) {
        return 2*(i+1) + 1 - 1;
    }
    
    private final int parent(int i) {
        return (i+1) / 2 - 1;
    }
    
    private final int root() {
        return 0;        
    }
    
    private final int last() {
        return heapSize - 1;
    }
    
    /**
     * Swap elements in two different positions. This mainly involves moving the
     * priority information, but any data that is associated with each cell
     * should also be moved accordingly.
     * 
     * @param i
     *  Zero-based index of the first item to be swapped. After the exchange, 
     *  the content that was previously at the second position should now be 
     *  here.
     * @param j
     *  Zero-based index of the second item to be swapped. After the exchange, 
     *  the content that was previously at the first position should now be here.
     */
    protected abstract void swap(int i, int j);    
    
    /**
     * Determine ranking between two elements. This relation should be anti-
     * symmetric and transitive. It may be reflexive too, but that will incur
     * a performance penalty (unneccesary work).
     * 
     * @param i
     *  Zero-based index of the item which we are comparing.
     * @param j
     *  Zero-based index of the item that is should be compared against.
     * @return
     *  True if the item at the first position has greater priority (less cost)
     *  than the one in the second position.
     */
    protected abstract boolean lessThan(int i, int j);

    protected abstract void set(T t, int index);
    protected abstract T get(int index);
        
    public AbstractPriorityQueue<T> push(T t) {        
        set(t, heapSize);
        heapSize++;
        int i = last(); // bubble(last());
        while(i != root()) {
            final int parent = parent(i);
            
            if(lessThan(parent,i)) {
                break;
            }
            
            swap(i, parent);
            i = parent; // bubble(parent);
        }
        
        // continue with this object in pipe
        return this;
    }
    
    public T peek() {
        assert (heapSize > 0);
        return get(0); 
    }
    
    public AbstractPriorityQueue<T> pop() {
        // we cannot pop items from an empty queue! (alternatively we could just
        // return at this point, leaving the tree as it were).
        assert (heapSize > 0);
        
        // put the node to be extracted at the end of the list and then shrink
        // the list by one item to make it disappear
        swap(last(), root());
        heapSize--;
        
        // the root node is now the one that was previously at the bottom of the
        // tree 
        int i = root(); // heapify(root())        
        for(;;) {
            final int left = left(i);
            final int right = right(i);

            // start out by considering this node as the least (i.e. the null
            // hypothesis is that the node is in its right place in the
            // hierarchy -- that it is less than both its children).
            int least = i;
            
            // if we have smaller node as the left child, note so
            if(left <= last()) {
                if(lessThan(left, least)) {
                    least = left;
                }
            }
    
            // if we have an even lesser node as the right child, note so
            if(right <= last()) {
                if(lessThan(right, least)) {
                    least = right;
                }                
            }
    
            // if there is no need to swap, then the heap property is now
            // maintained (base case) and the recursion may end
            if(least == i) {
                break;
            }
            
            // trade places with the child that was the least; the heap
            // property is now locally satisfied for this node, but possibly
            // not for the subtree where we put the larger node
            swap(i, least);
            
            // tail recursion: continue to heapify down the tree in the
            // direcion of the child that we swapped
            i = least; // heapify(least)
        }
        
        // continue with this object in pipe
        return this;
    }
    
    private boolean isEmpty() {
        return last() < root();
    }

    public T next() {
        T t = peek();
        pop();
        return t;
    }
    
    public boolean hasNext() {
        return !isEmpty();
    }
    
    public void remove() {
        throw new UnsupportedOperationException();
    }
}

/**
 * Priority queue for object-derived types.
 * 
 * @author roland.kaufmann@cipr.uib.no
 *
 * @param <T>
 *  Datatype of the individual elements in the queue.
 */
public class PriorityQueue<T extends Comparable<? super T>> 
        extends AbstractPriorityQueue<T> {
    protected List<T> items;
    
    @Override
    protected void swap(int i, int j) {
        T t = items.get(i);
        T u = items.get(j);
        items.set(i, u);
        items.set(j, t);
    }
    
    @Override
    protected boolean lessThan(int i, int j) {
        T t = items.get(i);
        T u = items.get(j);
        return t.compareTo(u) < 0;
    }
    
    @Override
    protected T get(int index) {
        return items.get(index);
    }
    
    @Override
    protected void set(T t, int index) {
        items.add(index, t);
    }
    
    /**
     * Use this constructor from subclasses to implement the priority queue with
     * a list of your own choice. 
     */
    protected PriorityQueue(List<T> newList) {
        this.items = newList;
    }

    public PriorityQueue(int numOfElements) {
        this(new ArrayList<T>(numOfElements));
    }
    
    public PriorityQueue() {
        // this seemingly magic number is the initial capacity of the array list
        // if nothing else is specified; we trust that Sun has gathered some
        // statistics on this and found this number the most likely (just wished
        // it was tucked away in a constant somewhere).
        this(10);
    }
    
    public static class Tests {
        @Test
        public void heapSort() {
            int[] A = new int[] { 15, 13, 9, 5, 12, 8, 7, 4, 0, 6, 2, 1 };
            int[] B = new int[] { 0, 1, 2, 4, 5, 6, 7, 8, 9, 12, 13, 15 };
            int[] C = new int[A.length];
            
            // fill the queue with some initial data
            PriorityQueue<Integer> p = new PriorityQueue<Integer>(A.length);
            for(int i = 0; i < A.length; i++) {
                p.push(A[i]);
            }
            
            // extract all the items in sorted order
            for(int j = 0; p.hasNext(); p.pop()) {
                C[j++] = p.peek();
            }
            
            // verify that these are the same as the correct solution
            for(int k = 0; k < B.length; k++) {
                assert B[k] == C[k];
            }
        }
    }
}
