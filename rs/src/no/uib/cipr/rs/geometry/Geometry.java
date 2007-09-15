package no.uib.cipr.rs.geometry;

import java.io.Serializable;
import java.util.Arrays;

/**
 * An unstructured geometry.
 */
public class Geometry implements Serializable {

    private static final long serialVersionUID = 2333237359312318207L;

    private double[] volumes;

    private double[] areas;

    private Point3D[] points;

    private Point3D[] interfaceCenters;

    private Vector3D[] interfaceNormals;

    private Point3D[] elementCenters;

    private double[] neighbourMultipliers;

    private double[] nonNeighbourMultipliers;

    public Geometry() {
        // subclass constructor
    }

    /**
     * Allocates storage for the geometry
     */
    public void setSizes(Topology topology) {
        setNumPoints(topology.getNumPoints());
        setNumInterfaces(topology.getNumInterfaces());
        setNumElements(topology.getNumElements());
        setNumConnections(topology.getNumNeighbourConnections(), topology
                .getNumNonNeighbourConnections());
    }

    public void setNumPoints(int numPoints) {
        points = new Point3D[numPoints];
    }

    public void setNumInterfaces(int numInterfaces) {
        areas = new double[numInterfaces];

        interfaceCenters = new Point3D[numInterfaces];
        interfaceNormals = new Vector3D[numInterfaces];
    }

    public void setNumElements(int numElements) {
        volumes = new double[numElements];

        elementCenters = new Point3D[numElements];
    }

    public void setNumConnections(int numNeighbourConnections,
            int numNonNeighbourConnections) {
        neighbourMultipliers = new double[numNeighbourConnections];
        nonNeighbourMultipliers = new double[numNonNeighbourConnections];

        // Default the transmissibility multipliers to 1
        Arrays.fill(neighbourMultipliers, 1);
        Arrays.fill(nonNeighbourMultipliers, 1);
    }

    /**
     * Sets point geometry
     */
    public void buildPoint(int p, Point3D point) {
        points[p] = point;
    }

    /**
     * Sets element geometry data
     */
    public void buildElement(int el, double volume, Point3D elementCenter) {
        assert volume >= 0.;
        volumes[el] = volume;
        elementCenters[el] = elementCenter;
    }

    /**
     * Sets interface geometry data
     */
    public void buildInterface(int intf, double area, Point3D interfaceCenter,
            Vector3D interfaceNormal) {
        assert area >= 0.;
        areas[intf] = area;
        interfaceCenters[intf] = interfaceCenter;
        interfaceNormals[intf] = interfaceNormal;
    }

    /**
     * Sets a neighbour connection transmissibility multiplier. Don't call
     * unless multiplier differs from 1, as that's the default
     */
    public void setNeighbourMultiplier(int connection, double multiplier) {
        neighbourMultipliers[connection] = multiplier;
    }

    /**
     * Sets a non-neighbour connection transmissibility multiplier. Don't call
     * unless multiplier differs from 1, as that's the default
     */
    public void setNonNeighbourMultiplier(int connection, double multiplier) {
        nonNeighbourMultipliers[connection] = multiplier;
    }

    /**
     * Returns center point of the element with the given index.
     */
    public Point3D getElementCenter(int i) {
        return elementCenters[i];
    }

    /**
     * Returns the volume of the element with the given index.
     */
    public double getElementVolume(int i) {
        return volumes[i];
    }

    /**
     * Returns the area of the interface with the given index.
     */
    public double getInterfaceArea(int i) {
        return areas[i];
    }

    /**
     * Returns the center point of the interface with the given index.
     */
    public Point3D getInterfaceCenter(int i) {
        return interfaceCenters[i];
    }

    /**
     * Returns the normal of the interface with the given index.
     */
    public Vector3D getNormal(int i) {
        return interfaceNormals[i];
    }

    /**
     * Returns the point with the given index.
     */
    public Point3D getPoint(int i) {
        return points[i];
    }

    /**
     * Returns the flux multiplier for the neighbour connection with the given
     * index.
     */
    public double getNeighbourConnectionFluxMultiplier(int i) {
        return neighbourMultipliers[i];
    }

    /**
     * Returns the flux multiplier for the non-neighbour connection with the
     * given index.
     */
    public double getNonNeighbourConnectionFluxMultiplier(int i) {
        return nonNeighbourMultipliers[i];
    }

}
