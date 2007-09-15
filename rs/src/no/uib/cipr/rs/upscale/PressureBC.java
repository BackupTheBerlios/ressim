package no.uib.cipr.rs.upscale;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import no.uib.cipr.rs.geometry.Element;
import no.uib.cipr.rs.geometry.Mesh;
import no.uib.cipr.rs.geometry.Point3D;
import no.uib.cipr.rs.geometry.Subdomain;
import no.uib.cipr.rs.meshgen.structured.Orientation;
import no.uib.cipr.rs.util.Configuration;

class PressureBC {

    private Map<Integer, Double> bcY;

    private Map<Integer, Double> bcX;

    /**
     * Creates pressure boundary conditions based on the given configuration for
     * the subdomain in question. Default boundary condition type is 'Open'.
     */
    public PressureBC(Configuration config, Subdomain subdomain, Mesh mesh) {
        // read and set boundary condition type, default is open
        String bcType = config.getString("BoundaryConditions", "Open");

        bcX = computeXDirectionBC(bcType, subdomain, mesh);

        bcY = computeYDirectionBC(bcType, subdomain, mesh);
    }

    private Map<Integer, Double> computeXDirectionBC(String bcType,
            Subdomain subdomain, Mesh mesh) {
        Map<Integer, Double> bc = new HashMap<Integer, Double>();

        double x0 = subdomain.getX0();
        double x1 = subdomain.getX1();

        double L1 = x1 - x0;

        Collection<Orientation> orientations = null;
        if (bcType.equalsIgnoreCase("Open"))
            orientations = Orientation.getOrientations2D();
        else if (bcType.equalsIgnoreCase("Closed"))
            orientations = Arrays.asList(Orientation.RIGHT, Orientation.LEFT);
        else
            throw new IllegalArgumentException(
                    "BoundaryConditions must be of either Open or Closed type");

        for (Orientation orient : orientations) {
            for (int elem : subdomain.boundaryElements().get(orient)) {
                Element e = mesh.elements().get(elem);
                Point3D c = e.center;

                double p = 1.0 - (c.x() - x0) / L1;

                bc.put(elem, p);
            }
        }

        return bc;
    }

    private Map<Integer, Double> computeYDirectionBC(String bcType,
            Subdomain subdomain, Mesh mesh) {
        Map<Integer, Double> bc = new HashMap<Integer, Double>();

        double y0 = subdomain.getY0();
        double y1 = subdomain.getY1();

        double L2 = y1 - y0;

        Collection<Orientation> orientations = null;
        if (bcType.equalsIgnoreCase("Open"))
            orientations = Orientation.getOrientations2D();
        else if (bcType.equalsIgnoreCase("Closed"))
            orientations = Arrays.asList(Orientation.FRONT, Orientation.BACK);
        else
            throw new IllegalArgumentException(
                    "BoundaryConditions must be of either Open or Closed type");

        for (Orientation orient : orientations) {
            for (int elem : subdomain.boundaryElements().get(orient)) {
                Element e = mesh.elements().get(elem);
                Point3D c = e.center;

                double p = 1 - (c.y() - y0) / L2;

                bc.put(elem, p);
            }
        }

        return bc;
    }

    /**
     * Returns a map of boundary element indices to pressure values for the
     * y-direction.
     */
    public Map<Integer, Double> getYDirectionPressureBC() {
        return bcY;
    }

    /**
     * Returns a map of boundary element indices to pressure values for the
     * x-direction.
     */
    public Map<Integer, Double> getXDirectionPressureBC() {
        return bcX;
    }

}