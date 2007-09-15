package no.uib.cipr.rs.geometry.flux;

import no.uib.cipr.rs.geometry.Element;
import no.uib.cipr.rs.geometry.Tensor3D;

/**
 * Rock heat conductivity tensor
 */
public class RockHeatConductivity implements Conductivity {

    public Tensor3D getConductivity(Element element) {
        return element.rock.getRockHeatConductivity();
    }

}
