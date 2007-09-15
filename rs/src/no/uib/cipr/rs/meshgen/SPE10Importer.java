package no.uib.cipr.rs.meshgen;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StreamTokenizer;

import no.uib.cipr.rs.geometry.Mesh;
import no.uib.cipr.rs.geometry.Tensor3D;
import no.uib.cipr.rs.meshgen.structured.Box;
import no.uib.cipr.rs.meshgen.structured.CartesianTopology3D;
import no.uib.cipr.rs.meshgen.structured.StructuredGeometry3D;
import no.uib.cipr.rs.rock.Rock;
import no.uib.cipr.rs.util.Configuration;
import no.uib.cipr.rs.util.Conversions;

/**
 * Parsing of SPE 10, model 2, permeability file
 */
public class SPE10Importer extends MeshGenerator {

    private int nz;

    private int ny;

    private int nx;

    private int total;

    private double dx;

    private double dy;

    private double dz;

    public SPE10Importer(Configuration config) throws IOException {
        System.out.println("\tSPE10 case interpreter\n");

        // Create the topology (hardcoded)
        CartesianTopology3D topology = createTopology();

        // Pick a subset of it
        Box box = new Box(config.getIntArray("Box"), topology);

        // Create a hardcoded uniform geometry in that box
        StructuredGeometry3D geometry = createGeometry(topology, box);

        // Read in the rock data, which is mostly what this filter is for
        Rock[] rocks = createRocks(config, topology, box);

        // Create the mesh
        mesh = new Mesh(geometry, topology, rocks);
    }

    private Rock[] createRocks(Configuration config,
            CartesianTopology3D topology, Box box)
            throws FileNotFoundException, IOException {
        // read the SPE10 data files
        String permFile = config.getString("PermFile");
        StreamTokenizer permInputStream = new StreamTokenizer(
                new BufferedReader(new FileReader(permFile)));

        String poroFile = config.getString("PoroFile");
        StreamTokenizer poroInputStream = new StreamTokenizer(
                new BufferedReader(new FileReader(poroFile)));

        // temporary storage of data from files
        double[] kx = new double[total];
        double[] ky = new double[total];
        double[] kz = new double[total];

        double[] phi = new double[total];

        // read data from files
        System.out.println("Reading Kx...");
        readDouble(kx, permInputStream);

        System.out.println("Reading Ky...");
        readDouble(ky, permInputStream);

        System.out.println("Reading Kz...");
        readDouble(kz, permInputStream);

        System.out.println("Reading Phi...");
        readDouble(phi, poroInputStream);

        // get data within box
        System.out.println("Extracting box data...");

        double[] permx = box.get(kx, topology);
        double[] permy = box.get(ky, topology);
        double[] permz = box.get(kz, topology);

        double[] poro = box.get(phi, topology);

        Rock[] rocks = new Rock[total];
        for (int i = 0; i < total; ++i) {
            Tensor3D K = new Tensor3D(permx[i], permy[i], permz[i]);
            rocks[i] = new Rock(poro[i], 0, K, "rock");
        }

        return rocks;
    }

    private StructuredGeometry3D createGeometry(CartesianTopology3D topology,
            Box box) {
        total = topology.getNumElements();

        dx = 20 * Conversions.feetInMeter; // ~6.0 m
        dy = 10 * Conversions.feetInMeter; // ~3.0 m
        dz = -2 * Conversions.feetInMeter; // ~-0.6 m

        return new StructuredGeometry3D(topology, box, dx, dy, dz);
    }

    private CartesianTopology3D createTopology() {
        // sizes of complete model
        nx = 60;
        ny = 220;
        nz = 85;

        CartesianTopology3D topology = new CartesianTopology3D(nx, ny, nz);
        return topology;
    }

    private void readDouble(double[] val, StreamTokenizer is)
            throws IOException {
        for (int k = 0, index = 0; k < nz; k++)
            for (int j = 0; j < ny; j++)
                for (int i = 0; i < nx; i++) {
                    is.nextToken();
                    if (is.ttype == StreamTokenizer.TT_WORD)
                        val[index++] = Double.parseDouble(is.sval);
                    else
                        throw new RuntimeException("Premature end-of-file?");
                }
    }
}
