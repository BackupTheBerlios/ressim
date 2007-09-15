package no.uib.cipr.rs;

import junit.framework.JUnit4TestAdapter;
import junit.framework.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Main suite for running all unit tests. This may be run with both
 * a JUnit 3.81 and a JUnit 4.1 compatible tool. The name 'Tests' 
 * (plural) is chosen to avoid conflicts with the Test annotation name.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses( {
    /**
     * Each package should contain a Tests class which defines the
     * suite of tests for that package.
     */
    no.uib.cipr.rs.geometry.Tests.class,
    no.uib.cipr.rs.util.Tests.class
} )
class Tests {
    public Tests() {}
    public static Test suite() { 
        return new JUnit4TestAdapter(Tests.class); 
    }
}