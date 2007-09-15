package no.uib.cipr.rs.geometry;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import no.uib.cipr.rs.geometry.flux.Transmissibility;
import no.uib.cipr.rs.util.Tolerances;

/**
 * Connection between two elements.
 */
public class Connection implements Serializable {

    private static final long serialVersionUID = 7010839784633358112L;

    /**
     * Connection index. This index is unique across the whole set of both
     * neighbour and non-neighbour connections
     */
    public final int index;

    /**
     * Index to the here element
     */
    public int hereElement;

    /**
     * Index to the there element
     */
    public int thereElement;

    /**
     * Multiplier to use for Darcy flux calculations.
     */
    public final double multiplier;

    /**
     * Transmissibilities for the absolute permeability (Darcy flux) and the
     * rock heat conductivity (Fourier flux)
     */
    public Transmissibility[] MD, MF;

    /**
     * Creates a connection between two elements. The connection can not be used
     * before it is populated.
     * 
     * @param index
     *                Global index of this connection
     * @param multiplier
     *                Value of flux multiplier
     */
    Connection(int index, double multiplier, int hereElement, int thereElement) {
        this.index = index;
        this.multiplier = multiplier;
        this.hereElement = hereElement;
        this.thereElement = thereElement;
    }

    public void setDarcyTransmissibilities(Collection<Transmissibility> M) {
        check(M);

        // TODO remove this one?
        for (Transmissibility t : M)
            t.scale(multiplier);

        MD = M.toArray(new Transmissibility[M.size()]);
    }

    public void setFourierTransmissibilities(Collection<Transmissibility> M) {
        check(M);
        MF = M.toArray(new Transmissibility[M.size()]);
    }

    /**
     * This checks that the transmissibilities sum to one, and that both here
     * and there elements are included, and that there are no duplicates
     */
    private void check(Collection<Transmissibility> M) {

        double sum = 0;
        Set<Integer> Mi = new HashSet<Integer>();
        for (Transmissibility t : M) {
            sum += t.k;

            int el = t.element;

            if (Mi.contains(el))
                throw new AssertionError(
                        "Duplicate transmissibility over connection "
                                + (index + 1));
            else
                Mi.add(el);
        }

        if (Math.abs(sum) > Tolerances.largeEps)
            throw new IllegalArgumentException(
                    "Transmissibilities on connection " + (index + 1)
                            + " sums to " + sum + ", which isn't zero");

        if (!Mi.contains(hereElement) || !Mi.contains(thereElement))
            throw new IllegalArgumentException(
                    "Transmissibilities over connection "
                            + (index + 1)
                            + " does not include the elements on either side of it");
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder("Connection: ");
        s.append("i = " + index + ",\n");

        s.append("\t here: " + hereElement + ",\n");
        s.append("\t there: " + thereElement + ",\n");

        return s.toString();
    }

}