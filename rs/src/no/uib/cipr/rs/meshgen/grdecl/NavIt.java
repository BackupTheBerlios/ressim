package no.uib.cipr.rs.meshgen.grdecl;

import java.util.Iterator;

/**
 * You should not use this class directly; rather you should call the various
 * iteration methods in the Grid class to get a for-each iterator of elements.
 * 
 * The language-level for-each loop is OK enough from a client point of view,
 * but it has a serious usability problem from the implementor's by requiring
 * that the next() method should both return an element *and* advance the
 * iterator (breaking the Command-Query idiom). Essentially it require us to
 * initialize the iterator at one element *before* the first one, which may not
 * be practical in all situations (it is also incompatible with the way 
 * classical for-loops are written). Thus, we implement a wrapper class that
 * takes away the pain by attaching an extra state of whether we have already
 * seen an element, bridging between the 'advance-to-first' and 'advance-between-
 * elements' paradigms. It also turns out that not having to have the navigators
 * themselves be valid iterators reduces the clutter introduced by casts
 * necessary to get the generic types correct. We use PillarNav as the base
 * class because it contains all the methods necessary to advance the iterator.
 * 
 * @author roland.kaufmann@cipr.uib.no
 */
class NavIt<T extends PillarNav> implements Iterable<T>, Iterator<T> {
    // the navigator which we encapsulate
    T it;
    NavIt(T it) { this.it = it; }

    // state machine that determines whether we are at the first iteration, at
    // which we don't increment the counter (hasNext() has already shielded us 
    // from an invalid method invocation anyway, so we don't *gain* anything 
    // from testing); otherwise call the advance method. set the flag so that
    // the actual increment operation is delayed to the next method called here.
    private boolean shouldAdvance = false;    
    private void advance(boolean isAdvanceOp) {
        if(shouldAdvance) {
            it.next();            
        }
        shouldAdvance = isAdvanceOp;
    }
    
    public boolean hasNext() {
        advance(false);
        return !it.done();        
    }

    public T next() {
        advance(true);
        return it;
    }

    // we can iterate over ourself!
    public Iterator<T> iterator() { return this; }

    // stubbed out to implement the language-level iteration interface
    public void remove() { throw new UnsupportedOperationException(); }            
}