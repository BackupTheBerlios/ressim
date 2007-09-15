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

package no.uib.cipr.matrix.test;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Runs all the tests
 */
public class AllTests {

    public static Test suite() {
        TestSuite suite = new TestSuite("Test for mt.test");
        // $JUnit-BEGIN$
        suite.addTestSuite(UpperTriangPackMatrixTest.class);
        suite.addTestSuite(UpperTriangDenseMatrixTest.class);
        suite.addTestSuite(PackCholeskyTest.class);
        suite.addTestSuite(QRTest.class);
        suite.addTestSuite(LowerSPDDenseMatrixTest.class);
        suite.addTestSuite(LowerSymmDenseMatrixTest.class);
        suite.addTestSuite(SymmBandEigenvalueTest.class);
        suite.addTestSuite(UnitLowerTriangDenseMatrixTest.class);
        suite.addTestSuite(SPDTridiagMatrixTest.class);
        suite.addTestSuite(LowerTriangPackMatrixTest.class);
        suite.addTestSuite(LowerSPDBandMatrixTest.class);
        suite.addTestSuite(UpperTriangBandMatrixTest.class);
        suite.addTestSuite(UpperSymmDenseMatrixTest.class);
        suite.addTestSuite(TridiagMatrixTest.class);
        suite.addTestSuite(UnitLowerTriangPackMatrixTest.class);
        suite.addTestSuite(BandMatrixTest.class);
        suite.addTestSuite(QLTest.class);
        suite.addTestSuite(LowerTriangBandMatrixTest.class);
        suite.addTestSuite(UnitUpperTriangDenseMatrixTest.class);
        suite.addTestSuite(SymmTridiagMatrixTest.class);
        suite.addTestSuite(UpperSPDDenseMatrixTest.class);
        suite.addTestSuite(SymmDenseEigenvalueTest.class);
        suite.addTestSuite(DenseCholeskyTest.class);
        suite.addTestSuite(UpperSPDBandMatrixTest.class);
        suite.addTestSuite(UpperSymmPackMatrixTest.class);
        suite.addTestSuite(LowerTriangDenseMatrixTest.class);
        suite.addTestSuite(UnitLowerTriangBandMatrixTest.class);
        suite.addTestSuite(RQTest.class);
        suite.addTestSuite(SquareDenseMatrixTest.class);
        suite.addTestSuite(DenseLUTest.class);
        suite.addTestSuite(SymmPackEigenvalueTest.class);
        suite.addTestSuite(UpperSPDPackMatrixTest.class);
        suite.addTestSuite(LQTest.class);
        suite.addTestSuite(UnitUpperTriangBandMatrixTest.class);
        suite.addTestSuite(DenseVectorTest.class);
        suite.addTestSuite(SymmTridiagEigenvalueTest.class);
        suite.addTestSuite(BandCholeskyTest.class);
        suite.addTestSuite(LowerSymmPackMatrixTest.class);
        suite.addTestSuite(SingularvalueTest.class);
        suite.addTestSuite(BandLUTest.class);
        suite.addTestSuite(UpperSymmBandMatrixTest.class);
        suite.addTestSuite(LowerSPDPackMatrixTest.class);
        suite.addTestSuite(LowerSymmBandMatrixTest.class);
        suite.addTestSuite(DenseMatrixTest.class);
        suite.addTestSuite(UnitUpperTriangPackMatrixTest.class);
        // $JUnit-END$
        return suite;
    }
}
