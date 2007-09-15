package no.uib.cipr.rs.meshgen.eclipse;

import java.util.Map;

import no.uib.cipr.rs.meshgen.eclipse.parse.EclipseMeshData;

/**
 * Class that is responsible for creating an RS mesh configuration based on
 * Eclipse mesh data.
 * 
 * TODO this should be replaced with MeshConfiguration when refactoring is
 * complete.
 */
public class OldMeshConfiguration {

    private MapConfiguration config;

    public OldMeshConfiguration(EclipseMeshData meshData) {

        // build configuration
        MapConfiguration mesh = new MapConfiguration();

        mesh.putString("type", "UnstructuredMesh");

        mesh.putInt("dimension", meshData.getDimension());

        mesh.putConfiguration("Points", getPointConfig(meshData));

        mesh.putConfiguration("Interfaces", getInterfaceConfig(meshData));

        mesh.putConfiguration("Elements", getElementConfig(meshData));

        mesh.putConfiguration("Connections", getConnectionConfig(meshData));

        mesh.putConfiguration("RockRegionMap", getRockRegionConfig(meshData));

        mesh.putConfiguration("RockData", getRockDataConfig(meshData));

        // TODO add in separate configurations
        mesh.putString("include", "wellsbc.inp");
        System.err
                .println("Put boundary conditions and/or wells in \"wellsbc.inp\"");

        config = new MapConfiguration();

        config.putConfiguration("Mesh", mesh);
    }

    /**
     * @param meshData
     * @return Point3D <code>Configuration</code>
     */
    private MapConfiguration getPointConfig(EclipseMeshData meshData) {
        MapConfiguration c = new MapConfiguration();

        c.putInt("NumberOfPoints", meshData.getNumPoints());
        c.putDoubleArray("Coordinates", meshData.getPointCoordinates());

        return c;
    }

    /**
     * @param meshData
     * @return Interface <code>Configuration</code>
     */
    private MapConfiguration getInterfaceConfig(EclipseMeshData meshData) {
        MapConfiguration c = new MapConfiguration();

        c.putInt("NumberOfInterfaces", meshData.getNumInterfaces());
        c.putIntArray("Points", meshData.getInterfacePoints());
        c.putDoubleArray("Areas", meshData.getInterfaceAreas());
        c.putDoubleArray("Normals", meshData.getInterfaceNormals());
        c.putDoubleArray("CenterPoints", meshData.getInterfaceCenters());

        return c;
    }

    /**
     * @param meshData
     * @return Element <code>Configuration</code>
     */
    private MapConfiguration getElementConfig(EclipseMeshData meshData) {
        MapConfiguration c = new MapConfiguration();

        c.putInt("NumberOfElements", meshData.getNumElements());
        c.putIntArray("Interfaces", meshData.getElementInterfaces());
        c.putDoubleArray("Volumes", meshData.getElementVolumes());
        c.putDoubleArray("Centers", meshData.getElementCenters());

        return c;
    }

    private MapConfiguration getConnectionConfig(EclipseMeshData meshData) {
        MapConfiguration c = new MapConfiguration();

        c.putInt("NumberOfConnections", meshData.getNumConnections());

        c.putIntArray("Connections", meshData.getConnections());

        return c;
    }

    private MapConfiguration getRockRegionConfig(EclipseMeshData meshData) {
        MapConfiguration c = new MapConfiguration();

        c.putIntArray("Rock", meshData.getUniformRegion());

        return c;
    }

    private MapConfiguration getRockDataConfig(EclipseMeshData meshData) {

        MapConfiguration c = new MapConfiguration();

        c.putString("type", "Global");

        Map<String, double[]> data = meshData.getRockDataMap();

        for (Map.Entry<String, double[]> e : data.entrySet())
            c.putDoubleArray(e.getKey(), e.getValue());

        return c;

    }

    public MapConfiguration getConfiguration() {
        return config;
    }

}
