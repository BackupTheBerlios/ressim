package no.uib.cipr.rs.meshgen;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import no.uib.cipr.rs.Paths;
import no.uib.cipr.rs.geometry.Connection;
import no.uib.cipr.rs.geometry.Element;
import no.uib.cipr.rs.geometry.Mesh;
import no.uib.cipr.rs.geometry.SourceLocation;
import no.uib.cipr.rs.geometry.flux.AbsolutePermeability;
import no.uib.cipr.rs.geometry.flux.Conductivity;
import no.uib.cipr.rs.geometry.flux.RockHeatConductivity;
import no.uib.cipr.rs.geometry.flux.Transmissibility;
import no.uib.cipr.rs.geometry.flux.TransmissibilityComputer;
import no.uib.cipr.rs.util.Configuration;

/**
 * Mesh generator main class
 */
public class Main {

    public static void main(String[] args) throws FileNotFoundException,
            IOException {
        System.out.println("\tMesh generator\n");

        // The configuration file contains a description of the mesh, the
        // transmissibility calculation method, and source locations. Only the
        // mesh description part is required
        Paths.checkPresence(Paths.MESH_FILE);
        Configuration config = new Configuration(Paths.MESH_FILE);

        // Construct the mesh from a description
        Mesh mesh = MeshGenerator.generate(config);

        // Add in a dual continuum
        DualContinuum dual = new DualContinuum(config, mesh);
        mesh = dual.getMesh();

        // List some useful information about the mesh
        outputMeshInformation(mesh);

        // Prepare for transmissibility calculations
        TransmissibilityComputer tc = TransmissibilityComputer.create(config);

        // Get the source locations
        Configuration sources = config.getConfiguration("Sources");
        Map<String, SourceLocation> sourceMap = new HashMap<String, SourceLocation>();
        for (String key : sources.keys())
            sourceMap.put(key, new SourceLocation(sources, key, mesh));
        mesh.sources = sourceMap;

        // Ensure we're done with the configuration file
        config.ensureEmpty();

        // Create the output directory
        File gridding = new File(Paths.GRIDDING_OUTPUT);
        gridding.mkdirs();

        // Calculate transmissibilities on the mesh
        calculateTransmissibilities(mesh, tc);

        // Name for the mesh output file
        String meshFile = Paths.GRIDDING_OUTPUT + "/" + Paths.MESH_FILE;

        // Output the fine mesh
        System.out.print("Writing the mesh ... ");
        writeObjects(meshFile, mesh);
        System.out.println("done");
    }

    private static void outputMeshInformation(Mesh mesh) {
        // Output some sizes
        System.out.print(mesh);

        // Get the distinct rock region names
        Set<String> rockRegions = new HashSet<String>();
        for (Element el : mesh.elements())
            rockRegions.add(el.rock.getRegion());

        // Sort them
        List<String> regionList = new LinkedList<String>(rockRegions);
        Collections.sort(regionList);

        // And output
        System.out.print("RockRegions: ");
        for (String region : regionList)
            System.out.print(region + " ");
        System.out.println();
    }

    private static void writeObjects(String file, Object... objects)
            throws IOException {
        ObjectOutput out = new ObjectOutputStream(new BufferedOutputStream(
                new FileOutputStream(file)));

        for (Object o : objects)
            out.writeObject(o);

        out.close();
    }

    protected static void calculateTransmissibilities(Mesh mesh,
            TransmissibilityComputer tc) {

        // Have the Darcy transmissibilities been calculated?
        Conductivity K = new AbsolutePermeability();
        if (!haveDarcyTransmissibilities(mesh)) {
            List<? extends Collection<Transmissibility>> M = tc
                    .calculateTransmissibilities(mesh, K);

            for (Connection c : mesh.neighbourConnections())
                c.setDarcyTransmissibilities(M.get(c.index));
        }

        // Same for the Fourier transmissibilities, except this is only done if
        // the rock heat conductivity tensor is non-zero
        Conductivity k = new RockHeatConductivity();
        boolean thermal = TransmissibilityComputer.nonZeroTensor(mesh, k);
        if (thermal && !haveFourierTransmissibilities(mesh)) {
            List<? extends Collection<Transmissibility>> M = tc
                    .calculateTransmissibilities(mesh, k);

            for (Connection c : mesh.neighbourConnections())
                c.setFourierTransmissibilities(M.get(c.index));
        }

        // Check for non-neighbour connections without transmissibilities
        if (haveNonNeighbourConnectionsWithoutTransmissibilities(mesh, thermal))
            throw new IllegalArgumentException(
                    "Non-neighbour connections lack transmissibilities");
    }

    private static boolean haveDarcyTransmissibilities(Mesh mesh) {
        for (Connection c : mesh.neighbourConnections())
            if (c.MD == null)
                return false;
        return true;
    }

    private static boolean haveFourierTransmissibilities(Mesh mesh) {
        for (Connection c : mesh.neighbourConnections())
            if (c.MF == null)
                return false;
        return true;
    }

    private static boolean haveNonNeighbourConnectionsWithoutTransmissibilities(
            Mesh mesh, boolean thermal) {
        for (Connection c : mesh.nonNeighbourConnections())
            if (c.MD == null || (thermal && c.MF == null))
                return true;
        return false;
    }
}
