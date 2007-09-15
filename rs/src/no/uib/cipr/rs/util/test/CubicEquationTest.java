package no.uib.cipr.rs.util.test;

import no.uib.cipr.rs.util.CubicEquation;
import junit.framework.TestCase;

/**
 * Tests the cubic equation solver
 */
public class CubicEquationTest extends TestCase {

    /**
     * Tests <code>x^3=0</code> yields <code>x=0</code>
     */
    public void testZero() {
        CubicEquation eqn = new CubicEquation(0, 0, 0);

        assertEquals(0, eqn.getLargestRoot(), 1e-10);
        assertEquals(0, eqn.getSmallestRoot(), 1e-10);
    }

    /**
     * Tests <code>x^3=1</code> yields <code>x=1</code>
     */
    public void testOne() {
        CubicEquation eqn = new CubicEquation(-1, 0, 0);

        assertEquals(1, eqn.getLargestRoot(), 1e-10);
        assertEquals(1, eqn.getSmallestRoot(), 1e-10);
    }

    /**
     * Tests <code>x^3-x^2-x+1=0</code> yields <code>x=-1,+1</code>
     */
    public void testTwo() {
        CubicEquation eqn = new CubicEquation(1, -1, -1);

        assertEquals(1, eqn.getLargestRoot(), 1e-10);
        assertEquals(-1, eqn.getSmallestRoot(), 1e-10);
    }

    /**
     * Tests <code>x^3-x=0</code> yields <code>x=-1,0,+1</code>
     */
    public void testThree() {
        CubicEquation eqn = new CubicEquation(0, -1, 0);

        assertEquals(1, eqn.getLargestRoot(), 1e-10);
        assertEquals(-1, eqn.getSmallestRoot(), 1e-10);
    }

}
