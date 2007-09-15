package no.uib.cipr.rs.geometry.flux;

import java.util.Arrays;
import java.util.List;

import no.uib.cipr.rs.geometry.Element;
import no.uib.cipr.rs.geometry.Interface;
import no.uib.cipr.rs.geometry.Mesh;
import no.uib.cipr.rs.geometry.NeighbourConnection;
import no.uib.cipr.rs.geometry.Point3D;
import no.uib.cipr.rs.geometry.Tensor3D;
import no.uib.cipr.rs.geometry.Vector3D;
import no.uib.cipr.rs.util.Configuration;

/**
 * Two-point flux discretisation for an integrated finite difference method
 */
public class TPFA extends TransmissibilityComputer {

    /**
     * Sets up the flux discretisation
     */
    public TPFA(Configuration config) {
        System.out.println(config.trace()
                + "Using two-point flux approximation");
    }

    @Override
    public List<List<Transmissibility>> calculateTransmissibilities(Mesh mesh,
            Conductivity K) {
        this.mesh = mesh;

        List<List<Transmissibility>> M = allocateM();

        for (NeighbourConnection c : mesh.neighbourConnections()) {
            double t = calculateTransmissibility(c, K);

            Transmissibility ti = new Transmissibility(c.hereElement, t);
            Transmissibility tj = new Transmissibility(c.thereElement, -t);

            M.set(c.index, Arrays.asList(ti, tj));
        }

        return M;
    }

    /**
     * Calculates the transmissibility coefficient for a grid connection
     */
    private double calculateTransmissibility(NeighbourConnection c,
            Conductivity K) {
        Interface is = mesh.hereInterface(c);
        Interface js = mesh.thereInterface(c);

        Element ei = mesh.element(is);
        Element ej = mesh.element(js);

        Tensor3D Ki = K.getConductivity(ei);
        Tensor3D Kj = K.getConductivity(ej);

        // Compute || K*n ||
        double Kin = Ki.multNorm(is.normal);
        double Kjn = Kj.multNorm(js.normal);

        double A = is.area;

        if (Kin != 0 && Kjn != 0)
            return Kin * Kjn * A
                    / (Kjn * distance(ei, is) + Kjn * distance(ej, js));
        else
            return 0;
    }

    /**
     * Computes the distance between the centers of the given element and
     * interface
     * 
     * @return The Euclidian distance (2-norm)
     */
    private double distance(Element el, Interface intf) {
        Point3D elp = el.center, intp = intf.center;

        Vector3D difference = new Vector3D(elp, intp);

        return difference.norm2();
    }

}
