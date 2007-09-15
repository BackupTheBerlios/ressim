import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.zip.GZIPOutputStream;

import com.jmatio.io.*;
import com.jmatio.types.*;
import no.uib.cipr.rs.meshgen.chevron.Data;

/**
 * 
 * @author roland.kaufmann@cipr.uib.no
 */
public class chevron {
    public static void main(String[] args) throws Exception {
        System.err.printf("Opening file...%n");
        String inputName  = (args.length < 1 ? "data.mat" : args[0]);
        String outputName = (args.length < 2 ? chgExt(inputName) : args[1]);
        MatFileReader m = new MatFileReader(new File(inputName));
        System.err.printf("Reading model...%n");
        MLStructure s = (MLStructure) m.getMLArray("Model");
        MLDouble n = (MLDouble) s.getField("n");
        Data data = new Data();
        int nx = n.get(0).intValue(); data.ni = nx;
        int ny = n.get(1).intValue(); data.nj = ny;
        int nz = n.get(2).intValue(); data.nk = nz;
        data.x = readCube((MLDouble) s.getField("X"));
        data.y = readCube((MLDouble) s.getField("Y"));
        data.z = readCube((MLDouble) s.getField("Z"));
        data.kx = readCube((MLDouble) s.getField("kx"));
        data.ky = readCube((MLDouble) s.getField("ky"));
        data.kz = readCube((MLDouble) s.getField("kz"));
        data.kxy = readCube((MLDouble) s.getField("kxy"));
        data.kxz = readCube((MLDouble) s.getField("kxz"));
        data.kyz = readCube((MLDouble) s.getField("kyz"));
        System.err.printf("Writing data...%n");
        FileOutputStream fos = new FileOutputStream(new File(outputName));
        try {
            GZIPOutputStream gos = new GZIPOutputStream(fos);
            try {
                ObjectOutputStream oos = new ObjectOutputStream(gos);
                try {
                    oos.writeObject(data);
                }
                finally {
                    oos.close();
                }
            }
            finally {
                gos.close();
            }
        }
        finally {
            fos.close();
        }
        System.err.printf("Done...%n");
    }
    
    static String chgExt(String orig) {
        if(orig.endsWith(".mat")) {
            orig = orig.substring(0, orig.length() - ".mat".length());
        }
        return orig + ".dat.gz";
    }
    
    static double[][][] readCube(MLDouble cube) {
        int[] d = cube.getDimensions();
        double[][][] a = new double[d[0]][][];
        for(int i = 0; i < d[0]; i++) {
            a[i] = new double[d[1]][];
            for(int j = 0; j < d[1]; j++) {
                a[i][j] = new double[d[2]];
                for(int k = 0; k < d[2]; k++) {
                    int ndx = k * d[0] * d[1] + j * d[0] + i;
                    double v = cube.get(ndx).doubleValue();
                    a[i][j][k] = v;
                }
            }
        }
        return a;
    }
}
