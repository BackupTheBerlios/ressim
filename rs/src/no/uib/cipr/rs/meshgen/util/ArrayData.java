package no.uib.cipr.rs.meshgen.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public final class ArrayData {

    /**
     * This class can not be instantiated
     */
    private ArrayData() {
        // void
    }

    /**
     * Converts the given integer collection to native int-array
     */
    public static int[] integerCollectionToArray(Collection<Integer> list) {
        int[] cp = new int[list.size()];

        int i = 0;
        for (int j : list)
            cp[i++] = j;

        return cp;
    }

    /**
     * Converts the given integer list to native int-array
     */
    public static int[] integerListToArray(List<Integer> val) {
        int[] v = new int[val.size()];

        for (int i = 0; i < v.length; i++)
            v[i] = val.get(i);

        return v;
    }

    /**
     * Converts the given integer list to a String-array
     */
    public static String[] integerListToStringArray(List<Integer> val) {
        String[] v = new String[val.size()];

        for (int i = 0; i < v.length; i++)
            v[i] = val.get(i).toString();

        return v;
    }

    /**
     * Converts the given integer set to a sorted native int array.
     */
    public static int[] integerSetToSortedArray(Set<Integer> set) {
        SortedSet<Integer> val = new TreeSet<Integer>(set);

        int[] v = new int[val.size()];

        int i = 0;
        for (int e : val)
            v[i++] = e;

        return v;

    }

    /**
     * Converts the given double list to native double-array
     */
    public static double[] doubleListToArray(List<Double> val) {
        double[] v = new double[val.size()];

        for (int i = 0; i < v.length; i++)
            v[i] = val.get(i);

        return v;
    }

    /**
     * Returns an expanded array of doubles
     */
    public static double[] getExpanded(int[] sizes, double[] values) {
        double[] expanded = new double[getTotal(sizes)];

        for (int i = 0, counter = 0; i < sizes.length; i++)
            for (int j = 0; j < sizes[i]; j++, counter++)
                expanded[counter] = values[i];

        return expanded;
    }

    /**
     * Returns an expanded array of integers
     */
    public static int[] getExpanded(int[] sizes) {
        int[] expanded = new int[getTotal(sizes)];

        for (int i = 0, counter = 0; i < sizes.length; i++)
            for (int j = 0; j < sizes[i]; j++, counter++)
                expanded[counter] = i;

        return expanded;
    }

    /**
     * Returns the sum of the elements in the given array
     */
    public static int getTotal(int[] val) {
        int total = 0;

        for (int i : val)
            total += i;

        return total;
    }

    /**
     * Returns an array of doubles with values beginning with the given start
     * value and incremented with the given deltas.
     */
    public static double[] getExpandedPoints(double start, double[] delta) {
        double offset = start;

        double coord[] = new double[delta.length + 1];

        coord[0] = start;

        for (int i = 0; i < delta.length; i++) {
            offset += delta[i];
            coord[i + 1] = offset;
        }

        return coord;
    }

    /**
     * Returns an array of doubles with values that are the center points
     * relative to the given start point and deltas.
     * 
     * TODO not necessary?
     */
    public static double[] getExpandedCenters(double start, double[] delta) {
        double offset = start;

        double[] coord = new double[delta.length];

        for (int i = 0; i < delta.length; i++) {
            coord[i] = offset + 0.5 * delta[i];
            offset += delta[i];
        }

        return coord;
    }

    /**
     * Returns an initialized list of integers with the given size
     */
    public static List<Integer> getLinearListInteger(int size) {
        List<Integer> l = new ArrayList<Integer>();
        for (int i = 0; i < size; i++) {
            l.add(i);
        }
        return l;
    }

    /**
     * Returns an initialised list of integer lists with the given size.
     */
    public static List<List<Integer>> getListListInteger(int size) {
        List<List<Integer>> ll = new ArrayList<List<Integer>>();
        for (int i = 0; i < size; i++) {
            ll.add(new ArrayList<Integer>());
        }
        return ll;
    }

    /**
     * Returns a new list in the reverse order of the given list
     */
    public static List<IndexedPoint3D> reverse(List<IndexedPoint3D> l) {
        List<IndexedPoint3D> r = new ArrayList<IndexedPoint3D>(l.size());
        for (int i = l.size() - 1; i >= 0; i--)
            r.add(l.get(i));

        return r;
    }

    /**
     * Creates a list of booleans, filling it with the given value
     */
    public static List<Boolean> getListBoolean(int size, Boolean b) {
        List<Boolean> l = new ArrayList<Boolean>();
        for (int i = 0; i < size; i++)
            l.add(b);
        return l;
    }

    /**
     * Creates a list of integers, filling it with the value -1
     */
    public static List<Integer> getListInteger(int size) {
        List<Integer> l = new ArrayList<Integer>();
        for (int i = 0; i < size; i++)
            l.add(-1);
        return l;
    }

    /**
     * Creates a list of integer sets
     */
    public static List<Set<Integer>> getListSetInteger(int size) {
        List<Set<Integer>> ll = new ArrayList<Set<Integer>>();
        for (int i = 0; i < size; i++)
            ll.add(new LinkedHashSet<Integer>());
        return ll;
    }
}
