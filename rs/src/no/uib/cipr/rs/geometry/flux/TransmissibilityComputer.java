package no.uib.cipr.rs.geometry.flux;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import no.uib.cipr.rs.geometry.Element;
import no.uib.cipr.rs.geometry.Mesh;
import no.uib.cipr.rs.geometry.Tensor3D;
import no.uib.cipr.rs.util.Configuration;

/**
 * Computes transmissibility coefficients
 */
public abstract class TransmissibilityComputer {

    /**
     * Computational mesh
     */
    protected Mesh mesh;

    /**
     * Calculates new transmissibilities
     * 
     * @param mesh
     *            Mesh to calculate upon
     * @param K
     *            The geometric conductivity coefficients
     */
    public abstract List<? extends Collection<Transmissibility>> calculateTransmissibilities(
            Mesh mesh, Conductivity K);

    /**
     * Allocates the M-set
     */
    protected List<List<Transmissibility>> allocateM() {
        List<List<Transmissibility>> M = new ArrayList<List<Transmissibility>>(
                mesh.connections().size());
        for (int i = 0; i < mesh.connections().size(); ++i)
            M.add(new ArrayList<Transmissibility>());
        return M;
    }

    /**
     * Checks if the given conductivity is non-zero
     */
    public static boolean nonZeroTensor(Mesh mesh, Conductivity K) {
        for (Element el : mesh.elements())
            if (K.getConductivity(el) == Tensor3D.ZERO)
                return false;
        return true;
    }

    /**
     * Creates a transmissibility computer. Defaults to the O-MPFA method
     */
    public static TransmissibilityComputer create(Configuration config) {
        return config.getObject("TransmissibilityMethod",
                TransmissibilityComputer.class, O_MPFA.class);
    }
}
