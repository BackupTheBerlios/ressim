package no.uib.cipr.matrix.distributed.test;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

    public static Test suite() {
        TestSuite suite = new TestSuite(
                "Test for no.uib.cipr.matrix.distributed.test");
        //$JUnit-BEGIN$
        suite.addTestSuite(PointToPointTest.class);
        suite.addTestSuite(CollectiveTest.class);
        suite.addTestSuite(DistIterativeSolverTest.class);
        //$JUnit-END$
        return suite;
    }

}
