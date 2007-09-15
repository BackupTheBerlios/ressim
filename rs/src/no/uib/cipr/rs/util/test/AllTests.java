package no.uib.cipr.rs.util.test;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

    public static void main(String[] args) {
        junit.textui.TestRunner.run(AllTests.suite());
    }

    public static Test suite() {
        TestSuite suite = new TestSuite("Test for no.uib.cipr.rs.util.test");
        // $JUnit-BEGIN$
        suite.addTestSuite(LookupTable1DTest.class);
        suite.addTestSuite(LookupTable2DTest.class);
        suite.addTestSuite(LookupTable3DTest.class);
        suite.addTestSuite(EvenLookupTable1DTest.class);
        suite.addTestSuite(EvenLookupTable2DTest.class);
        suite.addTestSuite(EvenLookupTable3DTest.class);
        suite.addTestSuite(CubicEquationTest.class);
        // $JUnit-END$
        return suite;
    }

}
