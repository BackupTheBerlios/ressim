package no.uib.cipr.rs.meshgen.grdecl;

import no.uib.cipr.rs.geometry.Point3D;

/**
 * The parser has an intimate relationship with the grid; it will have full
 * access to all its inner parts (because it is building it). The parser is
 * a separate object in order to keep the syntax of the input file away from
 * its logical organization (the grid).
 * 
 * @author roland.kaufmann@cipr.uib.no
 */
class Parser {
    // semantic value that is implicitly passed along to each production
    Grid g;
    
    // reference to the stream from which we will be pulling tokens
    Lexer lexer;
    
    // setup a parser that is capable of splitting tokens like we want
    Parser(Lexer lexer) {
        this.lexer = lexer;
    }
    
    /**
     * Parser productions, on the form readXxx(). Start production return the
     * entire grid that has been read. Normally .grdecl files are included in
     * the main data file, so they don't contain the GRID and END keywords
     * themselves (those are in the parent .data file).
     */
    Grid read() throws Exception {
        readKeywords(new Keyword[] {
                specgrid, coord, zcorn, actnum, poro, permx, permy, permz
        });
        return g;
    }
    
    /**
     * A keyword is a tagged function representing a grammar production, i.e.
     * a non-terminal in computer linguistics parlance.
     */
    private abstract class Keyword {
        // text of leading keyword        
        String keyword;
        Keyword(String keyword) { this.keyword = keyword; }
        
        // read the entire production. the default behaviour is to read data for
        // the section (the section will know how much to read from the semantic
        // values passed down to it) and then end with a slash. it returns
        // whether parsing should continue after this point or not.
        boolean readProduction() throws Exception {
            readSection();
            String next = lexer.next().get();
            if(!next.equals("/")) {
                throw new Exception(String.format("Keyword %s was expected to " +
                        "terminate with '/' but '%s' was found'", keyword, next));
            }
            return true;
        }
        
        // abstract method that implements reading from a section. it is empty
        // so that non-terminals that override the entire production reading
        // does not have to implement this method (that they don't use).
        void readSection() throws Exception {}
    }
    
    /**
     * Make up a comma-separated list of keywords that was expected at this
     * point. This method has the abiliy to (1) put commas between the names
     * and (2) look into the keyword class and extract its text representation. 
     */
    static String toString(Keyword[] keywords) {
        StringBuilder buf = new StringBuilder();
        for(int i = 0; i < keywords.length; ++i) {
            if(i != 0) {
                buf.append(", ");
            }
            buf.append('\'');
            buf.append(keywords[i].keyword);
            buf.append('\'');
        }
        return buf.toString();
    }    
    
    /**
     * Read subsections for possible keywords until end-of-section marker is
     * found (i.e. a slash) at the top level. 
     */
    void readKeywords(Keyword[] keywords) throws Exception {
        process_token:
        // loop as long as there are more input in the file
        while(!lexer.next().done()) {
            // if the keyword is the slash, then this section ends and we back-
            // track to the previous level (by returning from this method, since
            // the parser is recursive descent).
            if(lexer.get().equals("/")) {
                break process_token;
            }
            // walk through each of the possible keywords. if we find a match,
            // then read the production of this keyword and continue to the next
            // keyword that follows it (the entire production, that is).
            for(Keyword k : keywords) {
                if(lexer.get().equals(k.keyword)) {
                    k.readProduction();
                    continue process_token;
                }
            }
            // if we didn't match any of the keywords then throw an exception
            // TODO: Add line and column in the file here
            throw new Exception(String.format("Expected one of: %s; but found '%s'",
                    toString(keywords), lexer.get()));
        }
    }

    // specgrid sets the format of the grid
    final Keyword specgrid = new Keyword("specgrid") {
        @Override void readSection() throws Exception {
            // size of the each of the dimensions, as if the grid was structured
            Lexer.IntSubstream is = lexer.new IntSubstream();
            int ni = is.next().get();
            int nj = is.next().get();
            int nk = is.next().get();
            
            // number of coordinate systems; should be only one in this version
            int nc = is.next().get();
            if(nc != 1) {
                throw new Exception("More than one coordinate system not supported");
            }
            
            // radial notation for coordinates
            Lexer.BooleanSubstream bs = lexer.new BooleanSubstream();
            boolean radial = bs.next().get();
            if(radial == true) {
                throw new Exception("Radial coordinate notation not supported");
            }
            
            // communicate the format to the grid input
            g = new Grid(new Format(ni, nj, nk));
        }
    };
    
    /**
     * Read a point from a data section. A point is represented by its x, y and
     * z-values (in that order) following sequentially in the substream. 
     */
    Point3D readPoint(Lexer.DoubleSubstream ds) throws Exception {
        double x = ds.next().get();
        double y = ds.next().get();
        double z = ds.next().get();
        return new Point3D(x, y, z);
    }

    // coord shows us the placement of coordinate lines in the grid
    final Keyword coord = new Keyword("coord") {
        @Override void readSection() throws Exception {
            // open a substream of doubles from which we will be reading the
            // data along with letting the navigator walk us through the grid
            Lexer.DoubleSubstream ds = lexer.new DoubleSubstream();
            
            // read all coordinate lines from this section (the section should
            // be completely exhausted when we are done)
            for(PillarNav p : g.pillars()) {
                // read a pair of points which will describe the vector for the
                // next coordinate line
                Point3D start = readPoint(ds);
                Point3D end   = readPoint(ds);
                g.placement.setCoord(p, start, end);
            }
        }
    };
        
    // depths (in form of z-coordinate) for each corner in the grid (no corners
    // are shared between blocks, even if they have the same depth).
    final Keyword zcorn = new Keyword("zcorn") {
        @Override void readSection() throws Exception {
            Lexer.DoubleSubstream ds = lexer.new DoubleSubstream();
            for(CornerNav c : g.corners()) {
                double depth = ds.next().get();
                g.placement.setDepth(c, depth);
            }            
        }
    };

    // read a double precision data item for each block
    double[] readBlockData() throws Exception {
        Lexer.DoubleSubstream ds = lexer.new DoubleSubstream();        
        double[] a = new double[g.format.numOfBlocks()];
        for(BlockNav b : g.blocks()) {
            a[b.block()] = ds.next().get();
        }
        return a;
    }

    // read a boolean flag for each block
    boolean[] readBlockFlags() throws Exception {
        Lexer.BooleanSubstream bs = lexer.new BooleanSubstream();        
        boolean[] a = new boolean[g.format.numOfBlocks()];
        for(BlockNav b : g.blocks()) {
            a[b.block()] = bs.next().get();
        }
        return a;
    }

    // flag for each block that determines if it is active or not
    final Keyword actnum = new Keyword("actnum") {
        @Override void readSection() throws Exception {
            g.placement.setActive(readBlockFlags());
        }
    };
    
    // porosity data for each block
    final Keyword poro = new Keyword("poro") {
        @Override void readSection() throws Exception {
            g.petrophysics.setPoro(readBlockData());
        }
    };    

    // permeability data for each block
    final Keyword permx = new Keyword("permx") {
        @Override void readSection() throws Exception {
            g.petrophysics.setPermX(readBlockData());
        }
    };    

    final Keyword permy = new Keyword("permy") {
        @Override void readSection() throws Exception {
            g.petrophysics.setPermY(readBlockData());
        }
    };    

    final Keyword permz = new Keyword("permz") {
        @Override void readSection() throws Exception {
            g.petrophysics.setPermZ(readBlockData());
        }
    };        
}