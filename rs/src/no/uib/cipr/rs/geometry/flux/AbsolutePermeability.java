package no.uib.cipr.rs.geometry.flux;

import no.uib.cipr.rs.geometry.Element;
import no.uib.cipr.rs.geometry.Tensor3D;

/**
 * Absolute permeability conductivity tensor
 */
public class AbsolutePermeability implements Conductivity {

    public Tensor3D getConductivity(Element element) {
        return element.rock.getAbsolutePermeability();
    }

}
