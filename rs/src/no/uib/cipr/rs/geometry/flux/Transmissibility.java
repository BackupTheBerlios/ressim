package no.uib.cipr.rs.geometry.flux;

import java.io.Serializable;

/**
 * A single transmissibility
 */
public class Transmissibility implements Serializable {

    private static final long serialVersionUID = -8081609907238672500L;

    /**
     * Transmissibility coefficient. The dimension is the dimension of the
     * conductivity tensor multiplied by meter (length)
     */
    public double k;

    /**
     * Coupled element index
     */
    public final int element;

    /**
     * Sets up the transmissibility
     * 
     * @param el
     *                Control volume index
     * @param t
     *                Transmissibility coefficient
     */
    public Transmissibility(int el, double t) {
        this.element = el;
        this.k = t;
    }

    @Override
    public String toString() {
        return "[" + element + " : " + k + "]";
    }

    void add(double tp) {
        k += tp;
    }

    /**
     * Scales the transmissibility by a multiplier
     */
    public void scale(double multiplier) {
        k *= multiplier;
    }
}