package no.uib.cipr.rs.field;

import static no.uib.cipr.rs.util.Constants.g;

import java.io.Serializable;

import no.uib.cipr.rs.fluid.Phase;
import no.uib.cipr.rs.fluid.PhaseData;
import no.uib.cipr.rs.fluid.PhaseDataDouble;
import no.uib.cipr.rs.geometry.Connection;
import no.uib.cipr.rs.geometry.Element;
import no.uib.cipr.rs.geometry.Mesh;
import no.uib.cipr.rs.geometry.flux.Transmissibility;

/**
 * The system state on a control surface
 */
public class CS implements Serializable {

    private static final long serialVersionUID = 3283738858388922920L;

    /**
     * Associated grid connection
     */
    private final transient Connection c;

    /**
     * Centrifuge rotational speed [rad/s] = [1/s]
     */
    private final double omega;

    /**
     * Coordinates of the centrifuge rotational center [m]. The centrifuge axis
     * is along the z-axis, and hence orthogonal to the gravity direction
     */
    private final double ax, ay;

    /**
     * Surface integrated Darcy phase fluxes [m^3/s]
     */
    private final PhaseDataDouble u = new PhaseDataDouble();

    /**
     * Average specific phase weights on the control surface [kg/(m^2*s^2)]
     */
    private final PhaseDataDouble rho = new PhaseDataDouble();

    /**
     * Upstream directions for each phase
     */
    private transient final PhaseData<Element> upstream = new PhaseData<Element>();

    /**
     * Sets up the control surface
     */
    public CS(Connection c, double omega, double ax, double ay) {
        this.c = c;
        this.omega = omega;
        this.ax = ax;
        this.ay = ay;
    }

    /**
     * Gets the surface integrated Darcy phase flux
     */
    public double getDarcyFlux(Phase phase) {
        return u.get(phase);
    }

    /**
     * Gets the upstream element
     */
    public Element getUpstream(Phase phase) {
        return upstream.get(phase);
    }

    /**
     * Gets the average phase mass density
     * 
     * @return [kg/m^3]
     */
    public double getRho(Phase phase) {
        return rho.get(phase);
    }

    /**
     * Calculates secondary variables associated with a control surface
     * 
     * @param cv
     *                All the control volumes. Only the control volumes in the
     *                flux molecules are actually accessed, and the access is
     *                just for reading, not writing
     */
    void calculateSecondaries(CV[] cv, Mesh mesh) {
        // Calculates the average rho
        calculateMassDensity(cv);

        // Calculates the Darcy phase fluxes and upstream direction
        calculateDarcyFlux(cv, mesh);
    }

    /**
     * Calculates the average phase mass density
     */
    private void calculateMassDensity(CV[] cv) {
        CV here = cv[c.hereElement];
        CV there = cv[c.thereElement];

        rho.zero();

        for (Phase phase : Phase.all()) {
            double Shere = here.getSaturation(phase);
            double Sthere = there.getSaturation(phase);

            double S = Shere + Sthere;
            double rhoHere = here.getMassDensity(phase);
            double rhoThere = there.getMassDensity(phase);

            double av_rho = (Shere * rhoHere + Sthere * rhoThere) / S;

            // In case S is very small, rho might become excessively large
            if (!Double.isNaN(av_rho))
                rho.set(phase, av_rho);
        }
    }

    /**
     * Calculates the volumetric Darcy phase fluxes
     */
    private void calculateDarcyFlux(CV[] cv, Mesh mesh) {
        for (Phase phase : Phase.all()) {

            double D = 0;
            for (Transmissibility t : c.MD) {

                Element ek = mesh.element(t);
                double tk = t.k;

                // Phase potential
                double psi = cv[ek.index].getPhasePressure(phase)
                        + rho.get(phase) * bodyForce(ek);

                D += tk * psi;
            }

            // Upstream direction
            Element up = (D > 0) ? mesh.here(c) : mesh.there(c);
            upstream.set(phase, up);

            // Upstream mobility
            double lambda = cv[up.index].getPhaseMobility(phase);

            u.set(phase, lambda * D);
        }
    }

    /**
     * Calculates the body force for the given element. This includes both
     * gravity and centrifugal forces
     */
    public double bodyForce(Element el) {
        double x = el.center.x();
        double y = el.center.y();
        double z = el.center.z();

        double distance = (ax - x) * (ax - x) + (ay - y) * (ay - y);

        // Can probably do some pre-calculation. Check by profiling ...
        return g * z - (omega * omega / 2) * distance;
    }
}
