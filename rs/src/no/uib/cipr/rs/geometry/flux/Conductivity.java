package no.uib.cipr.rs.geometry.flux;

import no.uib.cipr.rs.geometry.Element;
import no.uib.cipr.rs.geometry.Tensor3D;

/**
 * Conductivities for a transmissibility computer
 */
public interface Conductivity {

    /**
     * Gets the geometrical conductivity in the given element
     */
    Tensor3D getConductivity(Element element);

}
