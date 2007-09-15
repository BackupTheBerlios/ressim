package no.uib.cipr.rs.upscale;

import no.uib.cipr.rs.geometry.Element;
import no.uib.cipr.rs.geometry.Tensor3D;
import no.uib.cipr.rs.util.Configuration;

/**
 * A class representing a power average of permeability.
 */
public class PowerAverage extends UpscalingMethod {

    // power average exponent in x-direction
    private double omegaX;

    // power average exponent in y-direction
    private double omegaY;

    /**
     * Creates a power average upscaling method.
     */
    public PowerAverage(Configuration config) {
        super();

        // get power average parameters.
        omegaX = config.getDouble("OmegaX");
        checkExponent(config, omegaX);

        omegaY = config.getDouble("OmegaY");
        checkExponent(config, omegaY);

        computeAveragedPorosity();

        computeAveragedPermeability();
    }

    private void checkExponent(Configuration config, double omega) {
        if (omega == 0.0)
            throw new IllegalArgumentException(config.trace()
                    + "Power average exponent can not be zero");

        if (omega < -1 || omega > 1)
            throw new IllegalArgumentException(config.trace()
                    + "Power average exponent must lie between -1 and 1");

    }

    private void computeAveragedPermeability() {
        for (int i = 0; i < numDomains; i++) {
            double Vb = 0;

            double permx = 0;
            double permy = 0;

            // TODO only for inner region
            for (int elem : subdomains[i].innerElements()) {
                Element fine = meshes[i].elements().get(elem);

                double dV = fine.volume;

                double kx = fine.rock.getAbsolutePermeability().xx();
                double ky = fine.rock.getAbsolutePermeability().yy();

                permx += (Math.pow(kx, omegaX) * dV);
                permy += (Math.pow(ky, omegaY) * dV);

                Vb += dV;
            }

            if (Vb == 0.0)
                throw new IllegalArgumentException("Domain " + i
                        + " has zero volume");

            // TODO check that Vb equals coarse domain volume.

            permx = Math.pow(permx / Vb, 1.0 / omegaX);
            permy = Math.pow(permy / Vb, 1.0 / omegaY);

            permeability[i] = new Tensor3D(permx, permy, 0.0);
        }
    }

    private void computeAveragedPorosity() {
        for (int i = 0; i < numDomains; i++) {
            double Vb = 0.0;

            double phi = 0;
            for (Element fine : meshes[i].elements()) {
                double dV = fine.volume;
                phi += fine.rock.getInitialPorosity() * dV;
                Vb += dV;
            }

            if (Vb == 0.0)
                throw new IllegalArgumentException("Domain " + i
                        + " has zero volume");

            porosity[i] = phi / Vb;
        }
    }
}
