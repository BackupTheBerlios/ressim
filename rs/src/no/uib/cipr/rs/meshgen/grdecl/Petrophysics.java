package no.uib.cipr.rs.meshgen.grdecl;

import no.uib.cipr.rs.geometry.Tensor3D;
import no.uib.cipr.rs.rock.Rock;

class Petrophysics {
    private double[/*block*/] poro;
    private double[/*block*/] permx;
    private double[/*block*/] permy;
    private double[/*block*/] permz;
    
    private Grid grid;
    
    Petrophysics(Grid grid) {
        this.grid = grid;
    }

    void setPoro(double[] porosity) {
        poro = porosity;
    }
    void setPermX(double[] permeability) {
        permx = permeability;
    }
    void setPermY(double[] permeability) {
        permy = permeability;
    }
    void setPermZ(double[] permeability) {
        permz = permeability;
    }
    
    // TODO: Only include blocks that are marked as active
    Rock[] buildRocks() {        
        Rock[] rocks = new Rock[grid.format.numOfBlocks()];
        
        for(BlockNav bn : grid.blocks()) {
            int block = bn.block();
            
            // porosity; if none was specified in the file, assume same value
            // throughout the entire grid. this let us run the transmissibility
            // calculation with only permeability and no porosity defined.
            double phi = (poro == null ? 1.0 : poro[block]);
            
            // no compaction
            double cr = 0;
            
            // diagonal tensor for permeability
            // TODO: In Eclipse, the permeability is specified relative to the
            // layer. Thus, "horizontally" means the vector between the left
            // and the right side of the cell. The tensor read from Eclipse
            // should be transformed into a Cartesian tensor before stored in
            // the simulator.
            Tensor3D K = new Tensor3D(permx[block], permy[block], permz[block]);
            
            // single rock region named 'rock'
            String rock = "rock";
            
            rocks[block] = new Rock(phi, cr, K, rock);
        }
        return rocks;        
    }    
}
