package no.uib.cipr.rs.output;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import no.uib.cipr.rs.Paths;
import no.uib.cipr.rs.geometry.Connection;
import no.uib.cipr.rs.geometry.CornerPoint;
import no.uib.cipr.rs.geometry.Element;
import no.uib.cipr.rs.geometry.Interface;
import no.uib.cipr.rs.geometry.Mesh;
import no.uib.cipr.rs.util.Pair;

/**
 * Exports mesh data into GMV format. Subclasses may extend this to export
 * dynamic data as well
 */
public class GMVExport {

    /**
     * The underlying fixed mesh
     */
    protected final Mesh mesh;

    /**
     * True if the mesh has been written to file
     */
    protected boolean meshWritten = false;

    /**
     * Directory to output GMV data
     */
    protected final File gmv;

    /**
     * File to store the node data
     */
    private final File nodeFile;

    /**
     * File to store the cell data
     */
    private final File cellFile;

    /**
     * File to store the material data
     */
    private final File matFile;

    /**
     * File to store the surface data
     */
    private final File surfFile;

    /**
     * File to store the attributes
     */
    private final File attrFile;

    /**
     * File to store the flag data
     */
    private File flagFile;

    /**
     * True if the range values have been recorded. Used by the attribute file
     */
    private boolean hasRange;

    /**
     * List of cell ranges (min and max) for each cell variable
     */
    private final List<Pair<Double, Double>> cellRange;

    /**
     * List of cell ranges (min and max) for each node variable
     */
    private final List<Pair<Double, Double>> nodeRange;

    public GMVExport() throws IOException, ClassNotFoundException {
        gmv = new File(Paths.VISUALIZATION_OUTPUT);
        gmv.mkdirs();

        String meshFile = Paths.GRIDDING_OUTPUT + "/" + Paths.MESH_FILE;
        Paths.checkPresence(meshFile);

        mesh = Paths.readMesh();

        System.out.print(mesh);

        nodeFile = new File(gmv, ".nodes");
        cellFile = new File(gmv, ".cells");
        flagFile = new File(gmv, ".flags");
        matFile = new File(gmv, ".materials");
        surfFile = new File(gmv, ".surfaces");
        attrFile = new File(gmv, ".attr");

        cellRange = new LinkedList<Pair<Double, Double>>();
        nodeRange = new LinkedList<Pair<Double, Double>>();

        writeGMVRC();
    }

    /**
     * Writes a gmvrc file containing default settings
     */
    private void writeGMVRC() throws FileNotFoundException {
        PrintWriter out = new PrintWriter(new File(gmv, "gmvrc"));

        out.println("gmvrc");

        out.println("axis off");
        out.println("beep off");
        out.println("cellfaces on");
        out.println("celledges on");
        out.println("cycle off");
        out.println("interactivity 1");
        out.println("attributes " + attrFile.getName());

        out.println("end_gmvrc");

        out.close();
    }

    /**
     * Writes a GMV attributes file. Should be called after primary data output
     * is finished, as it collects data from the different cell/node variables
     */
    protected void writeGMVattributes() throws FileNotFoundException {
        PrintWriter out = new PrintWriter(attrFile);

        out.println("gmv_attributes");

        // 0 for orthogonal, 1 for perspective
        out.println("viewflag 1");

        // Thin lines without smoothing
        out.println("line_size 1 0");

        out.print("globfieldcmin " + cellRange.size() + " ");
        for (Pair<Double, Double> pair : cellRange)
            out.print(pair.x() + " ");
        out.println();

        out.print("globfieldcmax " + cellRange.size() + " ");
        for (Pair<Double, Double> pair : cellRange)
            out.print(pair.y() + " ");
        out.println();

        out.print("globfieldmin " + nodeRange.size() + " ");
        for (Pair<Double, Double> pair : nodeRange)
            out.print(pair.x() + " ");
        out.println();

        out.print("globfieldmax " + nodeRange.size() + " ");
        for (Pair<Double, Double> pair : nodeRange)
            out.print(pair.y() + " ");
        out.println();

        out.println("end_attributes");

        out.close();
    }

    public static void main(String[] args) throws Exception {
        System.out.println("\tGMV mesh export\n");

        GMVExport export = new GMVExport();
        export.output();
    }

    /**
     * Performs the GMV export of a <code>Mesh</code>
     */
    @SuppressWarnings("unused")
    protected void output() throws IOException, ClassNotFoundException {
        File meshFile = new File(gmv, "mesh");
        writeGMV(meshFile);
        writeGMVattributes();
    }

    /**
     * Writes a complete GMV input file in binary format. The mesh is written
     * out the first time, and afterwards it is only refered to
     */
    protected void writeGMV(File file) throws IOException {
        DataOutputStream out = new DataOutputStream(new BufferedOutputStream(
                new FileOutputStream(file)));

        out.writeBytes("gmvinput");

        // Binary with 4-byte (32bit) integers and 8-byte (64bit) reals
        out.writeBytes("ieeei4r8");

        writeMesh(out);
        writeCellVariables(out);
        writeCellSubVariables(out);
        writeSurfaceVariables(out);
        writeSurfaceFlags(out);

        out.writeBytes(to8("endgmv"));

        out.close();
    }

    /**
     * Writes cell subset variable data
     */
    private void writeCellSubVariables(DataOutputStream out) throws IOException {
        out.writeBytes(to8("subvars"));

        for (CellSubVariable v : cellSubVariables()) {
            out.writeBytes(to8(v.getName()));
            out.writeInt(0);

            int[] indices = v.getIndices();
            double[] values = v.getValues();

            out.writeInt(indices.length);
            for (int i : indices)
                out.writeInt(i + 1);

            for (double d : values)
                out.writeDouble(d);
        }

        out.writeBytes(to8("endsubv"));
    }

    /**
     * Writes variable data
     */
    private void writeCellVariables(DataOutputStream out) throws IOException {
        out.writeBytes("variable");

        if (!hasRange) {
            for (CellVariable v : cellVariables())
                cellRange.add(new Pair<Double, Double>(v.min(), v.max()));

            for (NodeVariable v : nodeVariables())
                nodeRange.add(new Pair<Double, Double>(v.min(), v.max()));

            hasRange = true;
        }

        for (CellVariable v : cellVariables()) {
            out.writeBytes(to8(v.getName()));
            out.writeInt(0);

            for (Element el : mesh.elements())
                out.writeDouble(v.get(el));
        }

        for (NodeVariable v : nodeVariables()) {
            out.writeBytes(to8(v.getName()));
            out.writeInt(1);

            for (CornerPoint p : mesh.points())
                out.writeDouble(v.get(p));
        }

        out.writeBytes(to8("endvars"));
    }

    /**
     * Writes surface variable data
     */
    private void writeSurfaceVariables(DataOutputStream out) throws IOException {
        out.writeBytes("surfvars");

        for (FaceVariable v : faceVariables()) {
            out.writeBytes(to8(v.getName()));

            for (Interface intf : mesh.interfaces())
                out.writeDouble(v.get(intf));
        }

        out.writeBytes("endsvars");
    }

    /**
     * TODO call Ortega
     */
    private void writeSurfaceFlags(DataOutputStream out) throws IOException {
        out.writeBytes("surfflag");

        // orientation flag
        out.writeBytes(to8("orientations"));
        out.writeInt(3);
        out.writeBytes(to8("N/A"));
        out.writeBytes(to8("here"));
        out.writeBytes(to8("there"));

        for (Interface intf : mesh.interfaces())
            if (intf.boundary)
                out.writeInt(1);
            else if (mesh.hereInterface(mesh.connection(intf)) == intf)
                out.writeInt(2);
            else
                out.writeInt(3);

        // boundary interior flag
        out.writeBytes(to8("boundary"));
        out.writeInt(2);
        out.writeBytes(to8("boundary"));
        out.writeBytes("interior");

        for (Interface intf : mesh.interfaces())
            if (intf.boundary)
                out.writeInt(1);
            else
                out.writeInt(2);

        out.writeBytes("endsflag");
    }

    /**
     * Writes the mesh, if needed, and refers to it in the main output file
     */
    private void writeMesh(DataOutputStream out) throws IOException {
        if (!meshWritten) {
            writeNodes();
            writeCells();
            writeFlags();
            writeMaterials();
            writeSurfaces();
            meshWritten = true;
        }

        referMesh(out);
    }

    /**
     * Adds mesh references to the given stream
     */
    private void referMesh(DataOutputStream out) throws IOException {
        out.writeBytes("nodes   fromfile\"" + nodeFile.getName() + "\"");
        out.writeBytes("cells   fromfile\"" + cellFile.getName() + "\"");
        out.writeBytes("flags   fromfile\"" + flagFile.getName() + "\"");
        out.writeBytes("materialfromfile\"" + matFile.getName() + "\"");
        out.writeBytes("surface fromfile\"" + surfFile.getName() + "\"");
    }

    /**
     * Converts a string to 8 characters. Truncates/pads as necessary
     */
    private String to8(String s) {
        if (s.length() > 8)

            // Truncate the string
            return s.substring(0, 8);

        else if (s.length() < 8) {

            // Pad the string with spaces
            int n = 8 - s.length();
            for (int i = 0; i < n; ++i)
                s += " ";
            return s;
        } else

            // Correct length
            return s;
    }

    private void writeMaterials() throws IOException {
        DataOutputStream materials = new DataOutputStream(
                new BufferedOutputStream(new FileOutputStream(matFile)));

        materials.writeBytes("gmvinput");

        // Binary with 4-byte (32bit) integers and 8-byte (64bit) reals
        materials.writeBytes("ieeei4r8");

        materials.writeBytes("material");

        List<String> list = new ArrayList<String>();

        for (Element el : mesh.elements()) {
            String regionName = el.rock.getRegion();
            if (!list.contains(regionName))
                list.add(regionName);
        }

        materials.writeInt(list.size());
        materials.writeInt(0);

        for (String material : list)
            materials.writeBytes(to8(material));

        for (Element el : mesh.elements())
            materials.writeInt(list.indexOf(el.rock.getRegion()) + 1);

        materials.writeBytes(to8("endgmv"));

        materials.close();

    }

    private void writeFlags() throws IOException {
        DataOutputStream flags = new DataOutputStream(new BufferedOutputStream(
                new FileOutputStream(flagFile)));

        flags.writeBytes("gmvinput");

        // Binary with 4-byte (32bit) integers and 8-byte (64bit) reals
        flags.writeBytes("ieeei4r8");

        flags.writeBytes(to8("flags"));

        for (CellFlag f : cellFlagData()) {
            flags.writeBytes(to8(f.getName()));

            String[] flagTypes = f.getFlagTypeNames();
            flags.writeInt(flagTypes.length);

            flags.writeInt(0);

            for (String type : flagTypes)
                flags.writeBytes(to8(type));

            for (int id : f.getFlagIDs())
                flags.writeInt(id);
        }

        flags.writeBytes("endflags");

        flags.writeBytes(to8("endgmv"));

        flags.close();
    }

    private void writeSurfaces() throws IOException {
        DataOutputStream surfaces = new DataOutputStream(
                new BufferedOutputStream(new FileOutputStream(surfFile)));

        surfaces.writeBytes("gmvinput");

        // Binary with 4-byte (32bit) integers and 8-byte (64bit) reals
        surfaces.writeBytes("ieeei4r8");

        surfaces.writeBytes("surface ");
        surfaces.writeInt(mesh.interfaces().size());

        for (Interface intf : mesh.interfaces()) {

            // special case for one-node interfaces
            if (mesh.points(intf).size() == 1) {
                surfaces.writeInt(2);
                for (CornerPoint p : mesh.points(intf)) {
                    surfaces.writeInt(p.index + 1);
                    surfaces.writeInt(p.index + 1);
                }
            }
            // standard situation
            else {
                surfaces.writeInt(mesh.points(intf).size());
                for (CornerPoint p : mesh.points(intf))
                    surfaces.writeInt(p.index + 1);
            }

        }

        surfaces.writeBytes("endgmv  ");

        surfaces.close();
    }

    private void writeNodes() throws IOException {
        DataOutputStream nodes = new DataOutputStream(new BufferedOutputStream(
                new FileOutputStream(nodeFile)));

        nodes.writeBytes("gmvinput");

        // Binary with 4-byte (32bit) integers and 8-byte (64bit) reals
        nodes.writeBytes("ieeei4r8");

        nodes.writeBytes("nodes   ");
        nodes.writeInt(mesh.points().size());

        for (CornerPoint p : mesh.points())
            nodes.writeDouble(p.coordinate.x());
        for (CornerPoint p : mesh.points())
            nodes.writeDouble(p.coordinate.y());
        for (CornerPoint p : mesh.points())
            nodes.writeDouble(p.coordinate.z());

        nodes.writeBytes("endgmv  ");

        nodes.close();
    }

    private void writeCells() throws IOException {
        DataOutputStream cells = new DataOutputStream(new BufferedOutputStream(
                new FileOutputStream(cellFile)));

        cells.writeBytes("gmvinput");

        // Binary with 4-byte (32bit) integers and 8-byte (64bit) reals
        cells.writeBytes("ieeei4r8");

        cells.writeBytes(to8("cells"));
        cells.writeInt(mesh.elements().size());

        writeCellGeometries(cells);

        cells.writeBytes(to8("endgmv"));

        cells.close();
    }

    private void writeCellGeometries(DataOutputStream cells) throws IOException {
        for (Element e : mesh.elements()) {

            switch (mesh.points(e).size()) {
            case 4:
                cells.writeBytes(to8("quad"));

                cells.writeInt(mesh.points(e).size());
                for (CornerPoint p : mesh.points(e))
                    cells.writeInt(p.index + 1);

                break;
            case 3:
                cells.writeBytes(to8("tri"));

                cells.writeInt(mesh.points(e).size());
                for (CornerPoint p : mesh.points(e))
                    cells.writeInt(p.index + 1);

                break;
            case 2:
                cells.writeBytes(to8("line"));

                cells.writeInt(mesh.points(e).size());
                for (CornerPoint p : mesh.points(e))
                    cells.writeInt(p.index + 1);

                break;
            default:
                cells.writeBytes(to8("general"));
                cells.writeInt(mesh.interfaces(e).size());

                for (Interface i : mesh.interfaces(e))
                    cells.writeInt(mesh.points(i).size());

                for (Interface i : mesh.interfaces(e))
                    for (CornerPoint p : mesh.points(i))
                        cells.writeInt(p.index + 1);
            }
        }
    }

    /**
     * The node-centered variables
     */
    protected Collection<NodeVariable> nodeVariables() {
        List<NodeVariable> collection = Collections.emptyList();
        return collection;
    }

    /**
     * The cell-centered subset variables
     */
    protected Collection<CellSubVariable> cellSubVariables() {
        List<CellSubVariable> collection = new ArrayList<CellSubVariable>();

        for (String q : mesh.sources())
            collection.add(new SourceLocations(q));

        return collection;
    }

    /**
     * The cell flags
     */
    protected Collection<CellFlag> cellFlagData() {
        List<CellFlag> flags = new ArrayList<CellFlag>();

        return flags;
    }

    /**
     * The cell-centered variables
     */
    protected Collection<CellVariable> cellVariables() {
        PermXY permxy = new PermXY();
        PermXZ permxz = new PermXZ();
        PermYZ permyz = new PermYZ();

        // Are any off-diagonals present?
        boolean tensor = false;
        for (Element el : mesh.elements())
            if (permxy.get(el) != 0 || permxz.get(el) != 0
                    || permyz.get(el) != 0) {
                tensor = true;
                break;
            }

        PermXX permxx = new PermXX();
        PermYY permyy = new PermYY();
        PermZZ permzz = new PermZZ();

        // If not a tensor, are all diagonal entries equal?
        boolean isotropic = true;
        if (!tensor)
            for (Element el : mesh.elements()) {
                double xx = permxx.get(el);
                double yy = permyy.get(el);
                double zz = permzz.get(el);
                if (xx != yy || xx != zz || yy != zz) {
                    isotropic = false;
                    break;
                }
            }

        // First add in the appropriate permeabilities
        Collection<CellVariable> list = new ArrayList<CellVariable>();
        if (tensor)
            list.addAll(Arrays.asList(permxx, permyy, permzz, permxy, permxz,
                    permyz));
        else if (!isotropic)
            list.addAll(Arrays.asList(permxx, permyy, permzz));
        else
            list.add(permxx);

        list.addAll(Arrays.asList(new Porosity(), new Volume(), new CenterX(),
                new CenterY(), new CenterZ()));

        return list;
    }

    /**
     * The face-centered variables
     */
    protected Collection<FaceVariable> faceVariables() {
        FluxMult flux = new FluxMult();

        boolean hasMult = false;
        for (Interface intf : mesh.interfaces())
            if (flux.get(intf) != 1) {
                hasMult = true;
                break;
            }

        Collection<FaceVariable> list = new ArrayList<FaceVariable>();

        if (hasMult)
            list.add(flux);

        list.addAll(Arrays.asList(new Area(), new NormalX(), new NormalY(),
                new NormalZ()));

        return list;
    }

    /**
     * Cell subset variable
     */
    protected static interface CellSubVariable {

        /**
         * Gets the variable name
         */
        String getName();

        /**
         * Returns the cell indices
         */
        int[] getIndices();

        /**
         * Returns the cell values
         */
        double[] getValues();
    }

    /**
     * Source locations
     */
    private class SourceLocations implements CellSubVariable {

        private final String name;

        public SourceLocations(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public int[] getIndices() {
            List<Element> ind = mesh.elements(name);
            int[] indices = new int[ind.size()];
            for (int i = 0; i < indices.length; ++i)
                indices[i] = ind.get(i).index;
            return indices;
        }

        public double[] getValues() {
            double[] values = new double[mesh.elements(name).size()];
            Arrays.fill(values, 1);
            return values;
        }
    }

    /**
     * Cell centered variable
     */
    protected static abstract class CellVariable {

        /**
         * Gets the variable name
         */
        public abstract String getName();

        /**
         * Gets the value for a cell centered variable
         */
        public abstract double get(Element el);

        /**
         * Smallest possible value
         */
        public double min() {
            return 0;
        }

        /**
         * Largest possible value
         */
        public double max() {
            return 1e-5;
        }
    }

    /**
     * Node centered variable
     */
    protected static abstract class NodeVariable {

        /**
         * Gets the variable name
         */
        public abstract String getName();

        /**
         * Gets the value for a node centered variable
         */
        public abstract double get(CornerPoint p);

        /**
         * Smallest possible value
         */
        public double min() {
            return -Double.MAX_VALUE;
        }

        /**
         * Largest possible value
         */
        public double max() {
            return Double.MAX_VALUE;
        }
    }

    /**
     * Face centered variable
     */
    protected static interface FaceVariable {

        /**
         * Gets the variable name
         */
        String getName();

        /**
         * Gets the value for a face centered variable
         */
        double get(Interface intf);

    }

    /**
     * Cell flags
     */
    protected static interface CellFlag {

        /**
         * Gets the flag name
         */
        String getName();

        /**
         * Gets the flag type names
         */
        String[] getFlagTypeNames();

        /**
         * Gets the array of cell flag IDs.
         */
        int[] getFlagIDs();

    }

    static class PermXX extends CellVariable {
        @Override
        public String getName() {
            return "PermXX";
        }

        @Override
        public double get(Element el) {
            return el.rock.getAbsolutePermeability().xx();
        }

        @Override
        public double min() {
            return 0;
        }
    }

    static class PermYY extends CellVariable {
        @Override
        public String getName() {
            return "PermYY";
        }

        @Override
        public double get(Element el) {
            return el.rock.getAbsolutePermeability().yy();
        }

        @Override
        public double min() {
            return 0;
        }
    }

    static class PermZZ extends CellVariable {
        @Override
        public String getName() {
            return "PermZZ";
        }

        @Override
        public double get(Element el) {
            return el.rock.getAbsolutePermeability().zz();
        }

        @Override
        public double min() {
            return 0;
        }
    }

    static class PermXY extends CellVariable {
        @Override
        public String getName() {
            return "PermXY";
        }

        @Override
        public double get(Element el) {
            return el.rock.getAbsolutePermeability().xy();
        }

        @Override
        public double min() {
            return 0;
        }
    }

    static class PermXZ extends CellVariable {
        @Override
        public String getName() {
            return "PermXZ";
        }

        @Override
        public double get(Element el) {
            return el.rock.getAbsolutePermeability().xz();
        }

        @Override
        public double min() {
            return 0;
        }
    }

    static class PermYZ extends CellVariable {
        @Override
        public String getName() {
            return "PermYZ";
        }

        @Override
        public double get(Element el) {
            return el.rock.getAbsolutePermeability().yz();
        }

        @Override
        public double min() {
            return 0;
        }
    }

    static class Porosity extends CellVariable {
        @Override
        public String getName() {
            return "Porosity";
        }

        @Override
        public double get(Element el) {
            return el.rock.getInitialPorosity();
        }

        @Override
        public double min() {
            return 0;
        }

        @Override
        public double max() {
            return 1;
        }
    }

    static class Volume extends CellVariable {
        @Override
        public String getName() {
            return "Volume";
        }

        @Override
        public double get(Element el) {
            return el.volume;
        }

        @Override
        public double min() {
            return 0;
        }
    }

    class CenterX extends CellVariable {
        @Override
        public String getName() {
            return "CenterX";
        }

        @Override
        public double get(Element el) {
            return el.center.x();
        }
    }

    class CenterY extends CellVariable {
        @Override
        public String getName() {
            return "CenterY";
        }

        @Override
        public double get(Element el) {
            return el.center.y();
        }
    }

    class CenterZ extends CellVariable {
        @Override
        public String getName() {
            return "CenterZ";
        }

        @Override
        public double get(Element el) {
            return el.center.z();
        }
    }

    static class Area implements FaceVariable {
        public String getName() {
            return "Area";
        }

        public double get(Interface intf) {
            return intf.area;
        }
    }

    static class NormalX implements FaceVariable {
        public String getName() {
            return "NormalX";
        }

        public double get(Interface intf) {
            return intf.normal.x();
        }
    }

    static class NormalY implements FaceVariable {
        public String getName() {
            return "NormalY";
        }

        public double get(Interface intf) {
            return intf.normal.y();
        }
    }

    static class NormalZ implements FaceVariable {
        public String getName() {
            return "NormalZ";
        }

        public double get(Interface intf) {
            return intf.normal.z();
        }
    }

    // transmissibility multiplier; for interfaces this is defined as the
    // multiplier for the neighbour connection in which it participates
    // (currently, an interface can only participate in at most one
    // neighbour connection (none if it is at the boundary)). the name is
    // abbreviated to fit in most list boxes.
    class FluxMult implements FaceVariable {
        public String getName() {
            return "FluxMult";
        }

        public double get(Interface intf) {
            // check if we are connected
            Connection c = mesh.connection(intf);

            // if we haven't any connections, then return the default
            // value for the multiplier to indicate that there isn't
            // anything magic about this (lack of) connection
            double m = (c != null ? c.multiplier : 1d);

            return m;
        }
    }
}
