package no.uib.cipr.rs.meshgen.eclipse;

import java.util.Map;


public class MeshConfiguration {

    private MapConfiguration config;

    public MeshConfiguration(OutputMesh mesh) {
        // build configuration
        MapConfiguration temp = new MapConfiguration();

        temp.putString("type", "UnstructuredMesh");

        temp.putInt("dimension", mesh.getDimension());

        temp.putConfiguration("Points", getPointConfig(mesh));

        temp.putConfiguration("Interfaces", getInterfaceConfig(mesh));

        temp.putConfiguration("Elements", getElementConfig(mesh));

        temp.putConfiguration("Connections", getConnectionConfig(mesh));

        temp.putConfiguration("RockRegionMap", getRockRegionConfig(mesh));

        temp.putConfiguration("RockData", getRockDataConfig(mesh));

        temp.putConfiguration("Sources", getSourcesConfig());

        config = new MapConfiguration();

        config.putConfiguration("Mesh", temp);
    }

    private MapConfiguration getSourcesConfig() {

        MapConfiguration c = new MapConfiguration();

        c.putString("include", "sources.inp");

        return c;
    }

    /**
     * @param mesh
     * @return Point3D <code>Configuration</code>
     */
    private MapConfiguration getPointConfig(OutputMesh mesh) {
        MapConfiguration c = new MapConfiguration();

        c.putInt("NumberOfPoints", mesh.getNumPoints());
        c.putDoubleArray("Coordinates", mesh.getPointCoordinates());

        return c;
    }

    /**
     * @param mesh
     * @return Interface <code>Configuration</code>
     */
    private MapConfiguration getInterfaceConfig(OutputMesh mesh) {
        MapConfiguration c = new MapConfiguration();

        c.putInt("NumberOfInterfaces", mesh.getNumInterfaces());
        c.putIntArray("Points", mesh.getInterfacePoints());
        c.putDoubleArray("Areas", mesh.getInterfaceAreas());
        c.putDoubleArray("Normals", mesh.getInterfaceNormals());
        c.putDoubleArray("CenterPoints", mesh.getInterfaceCenters());

        return c;
    }

    /**
     * @param mesh
     * @return Element <code>Configuration</code>
     */
    private MapConfiguration getElementConfig(OutputMesh mesh) {
        MapConfiguration c = new MapConfiguration();

        c.putInt("NumberOfElements", mesh.getNumElements());
        c.putIntArray("Interfaces", mesh.getElementInterfaces());
        c.putDoubleArray("Volumes", mesh.getElementVolumes());
        c.putDoubleArray("Centers", mesh.getElementCenters());

        return c;
    }

    private MapConfiguration getConnectionConfig(OutputMesh mesh) {
        MapConfiguration c = new MapConfiguration();

        c.putInt("NumberOfConnections", mesh.getNumConnections());

        c.putIntArray("Connections", mesh.getConnections());

        return c;
    }

    private MapConfiguration getRockRegionConfig(OutputMesh mesh) {
        MapConfiguration c = new MapConfiguration();

        c.putIntArray("Rock", mesh.getUniformRegion());

        return c;
    }

    private MapConfiguration getRockDataConfig(OutputMesh mesh) {

        MapConfiguration c = new MapConfiguration();

        c.putString("type", "Global");

        Map<String, double[]> data = mesh.getRockDataMap();

        for (Map.Entry<String, double[]> e : data.entrySet())
            c.putDoubleArray(e.getKey(), e.getValue());

        return c;

    }

    public MapConfiguration getConfiguration() {
        return config;
    }

}
