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
 * Banded Cholesky decomposition
 */
public class BandCholesky {

    /**
     * Matrix dimension
     */
    private final int n;

    /**
     * Number of bands in the matrix A
     */
    private final int kd;

    /**
     * Cholesky decomposition of a lower matrix
     */
    private LowerTriangBandMatrix Cl;

    /**
     * Cholesky decomposition of an upper matrix
     */
    private UpperTriangBandMatrix Cu;

    /**
     * If the matrix is SPD or not
     */
    private boolean notspd;

    /**
     * True for upper part, else false
     */
    private final boolean upper;

    /**
     * Constructor for BandCholesky
     * 
     * @param n
     *            Matrix size
     * @param kd
     *            Number of matrix bands
     * @param upper
     *            True for decomposing an upper symmetrical matrix, false for a
     *            lower symmetrical matrix
     */
    public BandCholesky(int n, int kd, boolean upper) {
        this.n = n;
        this.kd = kd;
        this.upper = upper;

        if (upper)
            Cu = new UpperTriangBandMatrix(n, kd);
        else
            Cl = new LowerTriangBandMatrix(n, kd);
    }

    /**
     * Creates a Cholesky decomposition of the given matrix
     * 
     * @param A
     *            Matrix to decompose. Not modified
     * @return A Cholesky decomposition of the matrix
     */
    public static BandCholesky factorize(LowerSPDBandMatrix A) {
        return new BandCholesky(A.numRows(), A.kl, false).factor(A);
    }

    /**
     * Creates a Cholesky decomposition of the given matrix
     * 
     * @param A
     *            Matrix to decompose. Not modified
     * @return A Cholesky decomposition of the matrix
     */
    public static BandCholesky factorize(UpperSPDBandMatrix A) {
        return new BandCholesky(A.numRows(), A.ku, true).factor(A);
    }

    /**
     * Creates a Cholesky decomposition of the given matrix
     * 
     * @param A
     *            Matrix to decompose. Overwritten on return
     * @return The current decomposition
     */
    public BandCholesky factor(LowerSPDBandMatrix A) {
        if (upper)
            throw new IllegalArgumentException(
                    "Cholesky decomposition constructed for upper matrices");

        return decompose(A);
    }

    /**
     * Creates a Cholesky decomposition of the given matrix
     * 
     * @param A
     *            Matrix to decompose. Overwritten on return
     * @return The current decomposition
     */
    public BandCholesky factor(UpperSPDBandMatrix A) {
        if (!upper)
            throw new IllegalArgumentException(
                    "Cholesky decomposition constructed for lower matrices");

        return decompose(A);
    }

    private BandCholesky decompose(AbstractBandMatrix A) {
        if (n != A.numRows())
            throw new IllegalArgumentException("n != A.numRows()");
        if (upper && A.ku != kd)
            throw new IllegalArgumentException("A.ku != kd");
        if (!upper && A.kl != kd)
            throw new IllegalArgumentException("A.kl != kd");

        notspd = false;

        int info = 0;
        if (upper)
            info = Interface.lapack().pbtrf(UpLo.Upper, n, kd, A.getData());
        else
            info = Interface.lapack().pbtrf(UpLo.Lower, n, kd, A.getData());

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
     * Returns the decomposition matrix. Only valid for decomposition of a lower
     * SPD matrix
     */
    public LowerTriangBandMatrix getL() {
        if (!upper)
            return Cl;
        else
            throw new UnsupportedOperationException();
    }

    /**
     * Returns the decomposition matrix. Only valid for decomposition of a upper
     * SPD matrix
     */
    public UpperTriangBandMatrix getU() {
        if (upper)
            return Cu;
        else
            throw new UnsupportedOperationException();
    }

    /**
     * Returns true if the matrix decomposed is symmetrical, positive definite
     */
    public boolean isSPD() {
        return !notspd;
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
        int[] lwork = new int[n];

        double[] rcond = new double[1];
        int info = 0;
        if (upper)
            info = Interface.lapack().pbcon(UpLo.Upper, n, kd, Cu.getData(),
                    anorm, rcond, work, lwork);
        else
            info = Interface.lapack().pbcon(UpLo.Lower, n, kd, Cl.getData(),
                    anorm, rcond, work, lwork);

        if (info < 0)
            throw new IllegalArgumentException();

        return rcond[0];
    }

    /**
     * Computes <code>A\B</code>, overwriting <code>B</code>
     */
    public DenseMatrix solve(DenseMatrix B) throws MatrixNotSPDException {
        if (notspd)
            throw new MatrixNotSPDException();
        if (B.numRows() != n)
            throw new IllegalArgumentException("B.numRows() != n");

        int info = 0;
        if (upper)
            info = Interface.lapack().pbtrs(UpLo.Upper, n, kd, B.numColumns(),
                    Cu.getData(), B.getData());
        else
            info = Interface.lapack().pbtrs(UpLo.Lower, n, kd, B.numColumns(),
                    Cl.getData(), B.getData());

        if (info < 0)
            throw new IllegalArgumentException();

        return B;
    }
}