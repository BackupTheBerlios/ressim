package no.uib.cipr.rs.meshgen.eclipse;

import java.util.Map;

/**
 * This interface defines the functionality for output of RS readable mesh
 * files.
 */
public interface OutputMesh {

    int getDimension();

    int getNumPoints();

    double[] getPointCoordinates();

    int getNumInterfaces();

    int[] getInterfacePoints();

    double[] getInterfaceAreas();

    double[] getInterfaceNormals();

    double[] getInterfaceCenters();

    int getNumElements();

    int[] getElementInterfaces();

    double[] getElementVolumes();

    double[] getElementCenters();

    int getNumConnections();

    int[] getConnections();

    int[] getUniformRegion();

    Map<String, double[]> getRockDataMap();

}
