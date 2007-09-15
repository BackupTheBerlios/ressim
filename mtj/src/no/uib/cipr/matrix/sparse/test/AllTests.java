/*
 * Copyright (C) 2003-2006 Bjørn-Ove Heimsund
 * 
 * This file is part of MTJ.
 * 
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation; either version 2.1 of the License, or (at your
 * option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

package no.uib.cipr.matrix.sparse.test;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Test of SMT
 */
public class AllTests {

    public static Test suite() {
        TestSuite suite = new TestSuite("Test for smt.test");
        //$JUnit-BEGIN$
        suite.addTestSuite(QMRTest.class);
        suite.addTestSuite(BiCGstabILUTest.class);
        suite.addTestSuite(ChebyshevSSORTest.class);
        suite.addTestSuite(CompDiagMatrixTest.class);
        suite.addTestSuite(CompColMatrixTest.class);
        suite.addTestSuite(ICCTest.class);
        suite.addTestSuite(CGSDiagonalTest.class);
        suite.addTestSuite(SparseVectorTest.class);
        suite.addTestSuite(ILUTest.class);
        suite.addTestSuite(CGSSORTest.class);
        suite.addTestSuite(QMRILUTTest.class);
        suite.addTestSuite(ChebyshevICCTest.class);
        suite.addTestSuite(BiCGTest.class);
        suite.addTestSuite(CGTest.class);
        suite.addTestSuite(QMRDiagonalTest.class);
        suite.addTestSuite(GMRESDiagonalTest.class);
        suite.addTestSuite(CGDiagonalTest.class);
        suite.addTestSuite(GMRESILUTTest.class);
        suite.addTestSuite(ChebyshevAMGTest.class);
        suite.addTestSuite(ChebyshevTest.class);
        suite.addTestSuite(BiCGstabDiagonalTest.class);
        suite.addTestSuite(BiCGILUTest.class);
        suite.addTestSuite(BiCGstabILUTTest.class);
        suite.addTestSuite(ILUTTest.class);
        suite.addTestSuite(CGSILUTTest.class);
        suite.addTestSuite(CGSTest.class);
        suite.addTestSuite(CGAMGTest.class);
        suite.addTestSuite(FlexCompRowMatrixTest.class);
        suite.addTestSuite(FlexCompColMatrixTest.class);
        suite.addTestSuite(BiCGstabTest.class);
        suite.addTestSuite(CompRowMatrixTest.class);
        suite.addTestSuite(QMRILUTest.class);
        suite.addTestSuite(GMRESTest.class);
        suite.addTestSuite(BiCGDiagonalTest.class);
        suite.addTestSuite(CGICCTest.class);
        suite.addTestSuite(ChebyshevDiagonalTest.class);
        suite.addTestSuite(GMRESILUTest.class);
        suite.addTestSuite(CGSILUTest.class);
        //$JUnit-END$
        return suite;
    }
}
