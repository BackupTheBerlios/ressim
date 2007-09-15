package no.uib.cipr.rs.meshgen.grdecl;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import no.uib.cipr.rs.geometry.Geometry;
import no.uib.cipr.rs.geometry.Mesh;
import no.uib.cipr.rs.geometry.Topology;
import no.uib.cipr.rs.rock.Rock;

/**
 * The way that the file format is built makes it hard to process individual
 * points and cells; we'll need to iterate through every member in a lot of
 * different arrays. Thus, we rather collect all these arrays in a semantic
 * object (the grid) that gets passed around.
 * 
 * @author roland.kaufmann@cipr.uib.no
 */
public class Grid {
    // ----------------------- Global grid members ---------------------------
    Format format;

    Placement placement;

    Petrophysics petrophysics;

    Grid(Format f) {
        format = f;
        placement = new Placement(this);
        petrophysics = new Petrophysics(this);
    }

    public static Mesh build(String fileName) throws FileNotFoundException,
            Exception {
        // parse the specified file name
        InputStream in = new FileInputStream(fileName);
        Parser parser = new Parser(new Lexer(in));

        // read the grid into memory
        Grid grid = parser.read();

        // let the grid finish its own construction based on the input
        Mesh mesh = grid.buildMesh();
        
        // this is the value with which we are going to continue working
        return mesh;
    }

    Mesh buildMesh() {

        // fill out the missing information about geometry and topology
        Geometry geometry = new Geometry();
        Topology topology = new Topology();
        placement.complete(geometry, topology);

        // complete the mesh by adding block data
        Rock[] rocks = petrophysics.buildRocks();

        // create mesh object which is used by the simulator
        Mesh mesh = new Mesh(geometry, topology, rocks);
        return mesh;
    }

    /**
     * Method for spawning a new iterator over a certain element of the grid,
     * such as all pillars, all blocks etc.
     */
    Iterable<PillarNav> pillars() {
        return new NavIt<PillarNav>(new PillarNav(format));
    }

    Iterable<BlockNav> blocks() {
        return new NavIt<BlockNav>(new BlockNav(format));
    }

    Iterable<CornerNav> corners() {
        return new NavIt<CornerNav>(new CornerNav(format));
    }

    Iterable<HingeNav> hinges(PillarNav pn) {
        return new NavIt<HingeNav>(new HingeNav(pn));
    }
}
