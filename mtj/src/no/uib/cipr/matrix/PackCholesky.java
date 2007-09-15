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

package no.uib.cipr.matrix;

import no.uib.cipr.matrix.BLASkernel.UpLo;
import no.uib.cipr.matrix.Matrix.Norm;

/**
 * Packed Cholesky decomposition
 */
public class PackCholesky {

    /**
     * Matrix dimension
     */
    private final int n;

    /**
     * Cholesky decomposition of a lower matrix
     */
    private LowerTriangPackMatrix Cl;

    /**
     * Cholesky decomposition of an upper matrix
     */
    private UpperTriangPackMatrix Cu;

    /**
     * If the matrix is SPD or not
     */
    private boolean notspd;

    /**
     * True for upper part, else false
     */
    private final boolean upper;

    /**
     * Constructor for DenseCholesky
     * 
     * @param n
     *            Matrix size
     * @param upper
     *            True for decomposing an upper symmetrical matrix, false for a
     *            lower symmetrical matrix
     */
    public PackCholesky(int n, boolean upper) {
        this.n = n;
        this.upper = upper;

        if (upper)
            Cu = new UpperTriangPackMatrix(n);
        else
            Cl = new LowerTriangPackMatrix(n);
    }

    /**
     * Calculates a Cholesky decomposition
     * 
     * @param A
     *            Matrix to decompose. Not modified
     * @return The current decomposition
     */
    public static PackCholesky factorize(Matrix A) {
        return new PackCholesky(A.numRows(), true)
                .factor(new UpperSPDPackMatrix(A));
    }

    /**
     * Calculates a Cholesky decomposition
     * 
     * @param A
     *            Matrix to decompose. Overwritten on return
     * @return The current decomposition
     */
    public PackCholesky factor(LowerSPDPackMatrix A) {
        if (upper)
            throw new IllegalArgumentException(
                    "Cholesky decomposition constructed for upper matrices");

        return decompose(A);
    }

    /**
     * Calculates a Cholesky decomposition
     * 
     * @param A
     *            Matrix to decompose. Overwritten on return
     * @return The current decomposition
     */
    public PackCholesky factor(UpperSPDPackMatrix A) {
        if (!upper)
            throw new IllegalArgumentException(
                    "Cholesky decomposition constructed for lower matrices");

        return decompose(A);
    }

    private PackCholesky decompose(AbstractPackMatrix A) {
        if (n != A.numRows())
            throw new IllegalArgumentException("n != A.numRows()");

        notspd = false;

        int info = 0;
        if (upper)
            info = Interface.lapack().pptrf(UpLo.Upper, A.numRows(),
                    A.getData());
        else
            info = Interface.lapack().pptrf(UpLo.Lower, A.numRows(),
                    A.getData());

        if (info > 0)
            notspd = true;
        else if (info < 0)
            throw new IllegalArgumentException();

        if (upper)
            Cu.set(A);
        else
            Cl.set(A);

        return this;
    }

    /**
     * Returns true if the matrix decomposed is symmetrical, positive definite
     */
    public boolean isSPD() {
        return !notspd;
    }

    /**
     * Returns the decomposition matrix. Only valid for decomposition of a lower
     * SPD matrix
     */
    public LowerTriangPackMatrix getL() {
        if (!upper)
            return Cl;
        else
            throw new UnsupportedOperationException();
    }

    /**
     * Returns the decomposition matrix. Only valid for decomposition of a upper
     * SPD matrix
     */
    public UpperTriangPackMatrix getU() {
        if (upper)
            return Cu;
        else
            throw new UnsupportedOperationException();
    }

    /**
     * Solves for <code>B</code>, overwriting it on return
     */
    public DenseMatrix solve(DenseMatrix B) throws MatrixNotSPDException {
        if (notspd)
            throw new MatrixNotSPDException();
        if (B.numRows() != n)
            throw new IllegalArgumentException("B.numRows() != n");

        int info = 0;
        if (upper)
            info = Interface.lapack().pptrs(UpLo.Upper, Cu.numRows(),
                    B.numColumns(), Cu.getData(), B.getData());
        else
            info = Interface.lapack().pptrs(UpLo.Lower, Cl.numRows(),
                    B.numColumns(), Cl.getData(), B.getData());

        if (info < 0)
            throw new IllegalArgumentException();

        return B;
    }

    /**
     * Computes the reciprocal condition number
     * 
     * @param A
     *            The matrix this is a decomposition of
     * @return The reciprocal condition number. Values close to unity indicate a
     *         well-conditioned system, while numbers close to zero do not.
     */
    public double rcond(Matrix A) {
        if (A.numRows() != n)
            throw new IllegalArgumentException("A.numRows() != n");
        if (!A.isSquare())
            throw new IllegalArgumentException("!A.isSquare()");

        double anorm = A.norm(Norm.One);

        double[] work = new double[3 * n];
        int[] iwork = new int[n];

        double[] rcond = new double[1];
        int info = 0;
        if (upper)
            info = Interface.lapack().ppcon(UpLo.Upper, n, Cu.getData(), anorm,
                    rcond, work, iwork);
        else
            info = Interface.lapack().ppcon(UpLo.Lower, n, Cl.getData(), anorm,
                    rcond, work, iwork);

        if (info < 0)
            throw new IllegalArgumentException();

        return rcond[0];
    }

}
