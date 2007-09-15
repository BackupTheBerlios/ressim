package no.uib.cipr.rs.output;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Pattern;

import no.uib.cipr.rs.Paths;
import no.uib.cipr.rs.field.CS;
import no.uib.cipr.rs.field.Field;
import no.uib.cipr.rs.fluid.Component;
import no.uib.cipr.rs.fluid.Components;
import no.uib.cipr.rs.fluid.Phase;
import no.uib.cipr.rs.geometry.Element;
import no.uib.cipr.rs.geometry.Interface;
import no.uib.cipr.rs.geometry.NeighbourConnection;

/**
 * Exports simulation data into GMV format
 */
public class GMVFieldExport extends GMVExport {

    /**
     * Current field data for all the ranks. Stored here for easy access from
     * the CellVariable subclasses
     */
    Field field;

    /**
     * Directory to read data from
     */
    private final File output;

    private GMVFieldExport() throws IOException, ClassNotFoundException {
        super();

        Paths.checkPresence(Paths.SIMULATION_OUTPUT);
        output = new File(Paths.SIMULATION_OUTPUT);
    }

    public static void main(String[] args) throws FileNotFoundException,
            IOException, ClassNotFoundException {
        System.out.println("\tGMV field export\n");

        GMVFieldExport export = new GMVFieldExport();
        export.output();
    }

    /**
     * Performs the GMV field export
     */
    @Override
    protected void output() throws IOException, ClassNotFoundException {

        /*
         * Notes on the output algorithm:
         * 
         * 1. The mesh is stored in separate files, refered by the output files
         * at each time step
         * 
         * 2. At each timestep, we output the data into some temporary file
         * 
         * 3. The temporary files are copied into final output files in sequence
         * by the simulation time step
         */

        File[] files = output.listFiles();

        System.out.print("Reading " + files.length + " fields ");

        SortedMap<Double, File> map = new TreeMap<Double, File>();

        for (File file : files) {

            field = readField(file);
            if (field == null)
                continue;

            double time = field.getTime();

            // Write the field to a temporary file, which is to be renamed
            File tmp = File.createTempFile(String.valueOf(time), null);
            map.put(time, tmp);
            tmp.deleteOnExit();

            writeGMV(tmp);

            System.out.print(".");
        }
        System.out.println(" done");

        // Output the attributes file
        writeGMVattributes();

        // Delete old GMV files before writing the new ones
        File[] oldFiles = gmv.listFiles(new GMVFileFilter());
        System.out.print("Removing " + oldFiles.length + " files ");
        for (File file : oldFiles) {
            file.delete();
            System.out.print(".");
        }
        System.out.println(" done");

        // Put the files in correct order
        int num = 0;
        System.out.print("Writing output files ");
        for (File file : map.values()) {
            File newName = new File(gmv, String.format("%04d", num++));
            copy(file, newName);
            System.out.print(".");
        }
        System.out.println(" done");
    }

    /**
     * Copies the given file to a new file with another name
     */
    private void copy(File file, File newName) throws FileNotFoundException,
            IOException {
        byte[] buffer = new byte[1024];

        BufferedInputStream in = new BufferedInputStream(new FileInputStream(
                file), buffer.length);
        BufferedOutputStream out = new BufferedOutputStream(
                new FileOutputStream(newName), buffer.length);

        while (in.available() > 0) {
            int length = in.read(buffer);
            out.write(buffer, 0, length);
        }

        out.close();
        in.close();
    }

    /**
     * De-serializes a field
     */
    private Field readField(File file) throws IOException,
            FileNotFoundException, ClassNotFoundException {
        ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(
                new FileInputStream(file)));

        try {
            return (Field) in.readObject();
        } catch (IOException e) {
            return null;
        } finally {
            in.close();
        }
    }

    @Override
    protected Collection<CellVariable> cellVariables() {
        List<CellVariable> list = new ArrayList<CellVariable>();

        for (Phase phase : Phase.values()) {
            list.add(new Saturation(phase));
            list.add(new Pressure(phase));
            list.add(new Mobility(phase));
            list.add(new MolarDensity(phase));

            list.add(new CellDarcyFluxX(phase));
            list.add(new CellDarcyFluxY(phase));
            list.add(new CellDarcyFluxZ(phase));
        }

        Components components = field.getComponents();
        for (Component nu : components)
            list.add(new MoleFraction(nu));
        list.add(new Moles());

        list.add(new ResidualVolume());
        list.add(new Temperature());

        list.addAll(super.cellVariables());

        return list;
    }

    @Override
    protected Collection<FaceVariable> faceVariables() {
        List<FaceVariable> list = new ArrayList<FaceVariable>();

        for (Phase phase : Phase.values()) {
            list.add(new DarcyFluxX(phase));
            list.add(new DarcyFluxY(phase));
            list.add(new DarcyFluxZ(phase));
        }

        list.addAll(super.faceVariables());

        return list;
    }

    /**
     * Checks if a file is a GMV input file for the given case
     */
    private static class GMVFileFilter implements FileFilter {
        public boolean accept(File pathname) {
            String name = pathname.getName();
            String pattern = "\\d\\d\\d\\d";

            return Pattern.matches(pattern, name);
        }
    }

    private class Moles extends CellVariable {

        @Override
        public String getName() {
            return "N";
        }

        @Override
        public double min() {
            return 0;
        }

        @Override
        public double get(Element el) {
            return field.getControlVolume(el).getComposition().getMoles();
        }
    }

    private class MoleFraction extends CellVariable {

        private final Component nu;

        public MoleFraction(Component nu) {
            this.nu = nu;
        }

        @Override
        public String getName() {
            return nu.name();
        }

        @Override
        public double min() {
            return 0;
        }

        @Override
        public double max() {
            return 1;
        }

        @Override
        public double get(Element el) {
            return field.getControlVolume(el).getComposition().getMoleFraction(
                    nu);
        }
    }

    private class Pressure extends CellVariable {

        private final Phase phase;

        public Pressure(Phase phase) {
            this.phase = phase;
        }

        @Override
        public String getName() {
            return "p" + phase.letter();
        }

        @Override
        public double min() {
            return 0;
        }

        @Override
        public double get(Element el) {
            return field.getControlVolume(el).getPhasePressure(phase);
        }
    }

    private class Saturation extends CellVariable {

        private final Phase phase;

        public Saturation(Phase phase) {
            this.phase = phase;
        }

        @Override
        public String getName() {
            return "S" + phase.letter();
        }

        @Override
        public double min() {
            return 0;
        }

        @Override
        public double max() {
            return 1;
        }

        @Override
        public double get(Element el) {
            return field.getControlVolume(el).getSaturation(phase);
        }
    }

    private class Mobility extends CellVariable {

        private final Phase phase;

        public Mobility(Phase phase) {
            this.phase = phase;
        }

        @Override
        public String getName() {
            return "lambda" + phase.letter();
        }

        @Override
        public double min() {
            return 0;
        }

        @Override
        public double get(Element el) {
            return field.getControlVolume(el).getPhaseMobility(phase);
        }
    }

    private class MolarDensity extends CellVariable {

        private final Phase phase;

        public MolarDensity(Phase phase) {
            this.phase = phase;
        }

        @Override
        public String getName() {
            return "xi" + phase.letter();
        }

        @Override
        public double min() {
            return 0;
        }

        @Override
        public double get(Element el) {
            return field.getControlVolume(el).getEquationOfStateData(phase)
                    .getMolarDensity();
        }
    }

    private class Temperature extends CellVariable {
        @Override
        public String getName() {
            return "T";
        }

        @Override
        public double min() {
            return 0;
        }

        @Override
        public double get(Element el) {
            return field.getControlVolume(el).getTemperature();
        }
    }

    private class CellDarcyFluxX extends CellVariable {

        private final Phase phase;

        public CellDarcyFluxX(Phase phase) {
            this.phase = phase;
        }

        @Override
        public String getName() {
            return "ux" + phase.letter();
        }

        @Override
        public double get(Element el) {
            double flux = 0;

            for (Interface intf : mesh.interfaces(el)) {
                if (intf.boundary)
                    continue;

                NeighbourConnection c = mesh.connection(intf);
                flux += field.getControlSurface(c).getDarcyFlux(phase)
                        * mesh.hereInterface(c).normal.x() / intf.area;
            }

            return flux;
        }
    }

    private class CellDarcyFluxY extends CellVariable {

        private final Phase phase;

        public CellDarcyFluxY(Phase phase) {
            this.phase = phase;
        }

        @Override
        public String getName() {
            return "uy" + phase.letter();
        }

        @Override
        public double get(Element el) {
            double flux = 0;

            for (Interface intf : mesh.interfaces(el)) {
                if (intf.boundary)
                    continue;

                NeighbourConnection c = mesh.connection(intf);
                flux += field.getControlSurface(c).getDarcyFlux(phase)
                        * mesh.hereInterface(c).normal.y() / intf.area;
            }

            return flux;
        }
    }

    private class CellDarcyFluxZ extends CellVariable {

        private final Phase phase;

        public CellDarcyFluxZ(Phase phase) {
            this.phase = phase;
        }

        @Override
        public String getName() {
            return "uz" + phase.letter();
        }

        @Override
        public double get(Element el) {
            double flux = 0;

            for (Interface intf : mesh.interfaces(el)) {
                if (intf.boundary)
                    continue;

                NeighbourConnection c = mesh.connection(intf);
                flux += field.getControlSurface(c).getDarcyFlux(phase)
                        * mesh.hereInterface(c).normal.z() / intf.area;
            }

            return flux;
        }
    }

    private class DarcyFluxX implements FaceVariable {

        private final Phase phase;

        public DarcyFluxX(Phase phase) {
            this.phase = phase;
        }

        public String getName() {
            return "ux" + phase.letter();
        }

        @Override
        public double get(Interface intf) {
            if (intf.boundary)
                return 0;

            NeighbourConnection c = mesh.connection(intf);
            Interface here = mesh.hereInterface(c);

            CS cs = field.getControlSurface(c);

            return cs.getDarcyFlux(phase) * here.normal.x() / here.area;
        }
    }

    private class DarcyFluxY implements FaceVariable {

        private final Phase phase;

        public DarcyFluxY(Phase phase) {
            this.phase = phase;
        }

        public String getName() {
            return "uy" + phase.letter();
        }

        @Override
        public double get(Interface intf) {
            if (intf.boundary)
                return 0;

            NeighbourConnection c = mesh.connection(intf);
            Interface here = mesh.hereInterface(c);

            CS cs = field.getControlSurface(c);

            return cs.getDarcyFlux(phase) * here.normal.y() / here.area;
        }
    }

    private class DarcyFluxZ implements FaceVariable {

        private final Phase phase;

        public DarcyFluxZ(Phase phase) {
            this.phase = phase;
        }

        public String getName() {
            return "uz" + phase.letter();
        }

        @Override
        public double get(Interface intf) {
            if (intf.boundary)
                return 0;

            NeighbourConnection c = mesh.connection(intf);
            Interface here = mesh.hereInterface(c);

            CS cs = field.getControlSurface(c);

            return cs.getDarcyFlux(phase) * here.normal.z() / here.area;
        }
    }

    private class ResidualVolume extends CellVariable {
        @Override
        public String getName() {
            return "R";
        }

        @Override
        public double min() {
            return 0;
        }

        @Override
        public double get(Element el) {
            return field.getControlVolume(el).getResidualVolume();
        }
    }
}
