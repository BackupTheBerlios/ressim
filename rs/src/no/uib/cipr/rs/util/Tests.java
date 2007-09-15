package no.uib.cipr.rs.util;

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
    no.uib.cipr.rs.util.PriorityQueue.Tests.class,
    no.uib.cipr.rs.util.test.CubicEquationTest.class
    //no.uib.cipr.rs.util.test.LookupTable1DTest.class,
    //no.uib.cipr.rs.util.test.LookupTable2DTest.class,
    //no.uib.cipr.rs.util.test.LookupTable3DTest.class,
    //no.uib.cipr.rs.util.test.EvenLookupTable1DTest.class,
    //no.uib.cipr.rs.util.test.EvenLookupTable2DTest.class,
    //no.uib.cipr.rs.util.test.EvenLookupTable3DTest.class    
} )
public class Tests {
    public Tests() {}
    public static Test suite() { 
        return new JUnit4TestAdapter(Tests.class); 
    }
}