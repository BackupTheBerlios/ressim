package no.uib.cipr.rs.geometry;

import junit.framework.JUnit4TestAdapter;
import junit.framework.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Suite containing unit tests for this package. Refer to this suite
 * in the program over-all Tests class, and let this class refer to
 * all the individual test (inner) classes in the package.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses( {
    /**
     * Each class in the package that contains unit tests, should
     * have an inner class called 'Tests'.
     */
    Delaunay2D.Tests.class,
    ConstrainedDelaunay2D.Tests.class,
    Projection.Tests.class
} )
public class Tests {
    public Tests() {}
    public static Test suite() { 
        return new JUnit4TestAdapter(Tests.class); 
    }
}