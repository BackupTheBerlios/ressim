package no.uib.cipr.rs.upscale;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;

import no.uib.cipr.rs.Paths;
import no.uib.cipr.rs.geometry.Tensor3D;
import no.uib.cipr.rs.util.Configuration;

public class Main {

    public static void main(String[] args) throws FileNotFoundException,
            IOException {
        System.out.println("\tAbsolute permeability upscaling\n");

        // read configuration from file
        Paths.checkPresence(Paths.UPSCALE_FILE);
        Configuration config = new Configuration(Paths.UPSCALE_FILE);

        // construct upscaling method
        UpscalingMethod method = UpscalingMethod.create(config);

        config.ensureEmpty();

        Tensor3D[] perm = method.getPermeability();

        double[] poro = method.getPorosity();

        dumpPermeability(perm);

        dumpPorosity(poro);

        int i = 1;
        System.out.println("matlab:function K = get_tensors");
        for (Tensor3D k : perm) {
            System.out.println("matlab:\tK(:,:," + i + ") = "
                    + new TensorOutput(k).toString() + ";\n");
            i++;
        }
    }

    /**
     * Writing porosities to file 'poro.input'
     */
    private static void dumpPorosity(double[] poro) throws IOException {
        writeArrayToFile(poro, "poro.input");
    }

    /**
     * Writing permeabilities to file 'permx.input', 'permy.input', etc
     */
    private static void dumpPermeability(Tensor3D[] perm) throws IOException {
        int n = perm.length;

        double[] val = new double[n];

        for (int i = 0; i < n; i++)
            val[i] = perm[i].xx();
        writeArrayToFile(val, "permx.input");

        for (int i = 0; i < n; i++)
            val[i] = perm[i].yy();
        writeArrayToFile(val, "permy.input");

        for (int i = 0; i < n; i++)
            val[i] = perm[i].zz();
        writeArrayToFile(val, "permz.input");

        for (int i = 0; i < n; i++)
            val[i] = perm[i].xy();
        writeArrayToFile(val, "permxy.input");

        for (int i = 0; i < n; i++)
            val[i] = perm[i].xz();
        writeArrayToFile(val, "permxz.input");

        for (int i = 0; i < n; i++)
            val[i] = perm[i].yz();
        writeArrayToFile(val, "permyz.input");
    }

    private static void writeArrayToFile(double[] array, String file)
            throws FileNotFoundException {
        PrintWriter out = new PrintWriter(file);

        for (double d : array) {
            out.print(d);
            out.print(" ");
        }

        out.println();
        out.close();
    }
}
