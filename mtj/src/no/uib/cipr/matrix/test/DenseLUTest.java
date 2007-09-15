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

import no.uib.cipr.matrix.DenseLU;
import no.uib.cipr.matrix.DenseMatrix;
import no.uib.cipr.matrix.Matrices;
import no.uib.cipr.matrix.Matrix;
import junit.framework.TestCase;

/**
 * Tests the dense LU decomposition
 */
public class DenseLUTest extends TestCase {

    /**
     * Matrix to decompose
     */
    private DenseMatrix A;

    private DenseMatrix I;

    private final int max = 100;

    public DenseLUTest(String arg0) {
        super(arg0);
    }

    @Override
    protected void setUp() throws Exception {
        int n = Utilities.getInt(1, max);
        A = new DenseMatrix(n, n);
        Utilities.populate(A);
        Utilities.addDiagonal(A, 1);
        while (Utilities.singular(A))
            Utilities.addDiagonal(A, 1);

        I = Matrices.identity(n);
    }

    @Override
    protected void tearDown() throws Exception {
        A = null;
        I = null;
    }

    public void testDenseLU() {
        int n = A.numRows();
        DenseLU lu = new DenseLU(n, n);
        lu.factor(A.copy());

        lu.solve(I);

        Matrix J = I.mult(A, new DenseMatrix(n, n));
        for (int i = 0; i < n; ++i)
            for (int j = 0; j < n; ++j)
                if (i != j)
                    assertEquals(J.get(i, j), 0, 1e-10);
                else
                    assertEquals(J.get(i, j), 1, 1e-10);
    }

    public void testDenseLUtranspose() {
        int n = A.numRows();
        DenseLU lu = new DenseLU(n, n);
        lu.factor(A.copy());

        lu.transSolve(I);

        Matrix J = I.transAmult(A, new DenseMatrix(n, n));
        for (int i = 0; i < n; ++i)
            for (int j = 0; j < n; ++j)
                if (i != j)
                    assertEquals(J.get(i, j), 0, 1e-10);
                else
                    assertEquals(J.get(i, j), 1, 1e-10);
    }

    public void testDenseLUrcond() {
        int n = A.numRows();
        DenseLU lu = new DenseLU(n, n);
        lu.factor(A.copy());

        lu.rcond(A, Matrix.Norm.One);
        lu.rcond(A, Matrix.Norm.Infinity);
    }
}
