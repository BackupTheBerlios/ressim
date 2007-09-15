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

import no.uib.cipr.matrix.BLASkernel.Transpose;
import no.uib.cipr.matrix.Matrix.Norm;

/**
 * Banded LU decomposition
 */
public class BandLU {

    /**
     * Matrix dimension
     */
    private final int n;

    /**
     * Number of bands in the matrix A
     */
    private final int kl, ku;

    /**
     * Holds the LU factors
     */
    private final BandMatrix LU;

    /**
     * Row pivotations
     */
    private final int[] ipiv;

    /**
     * True if the matrix was singular
     */
    private boolean singular;

    /**
     * Constructor for BandLU
     * 
     * @param n
     *            Matrix size
     * @param kl
     *            Number of lower matrix bands
     * @param ku
     *            Number of upper matrix bands
     */
    public BandLU(int n, int kl, int ku) {
        this.n = n;
        this.kl = kl;
        this.ku = ku;

        LU = new BandMatrix(n, kl, ku + kl);

        ipiv = new int[n];
    }

    /**
     * Creates an LU decomposition of the given matrix
     * 
     * @param A
     *            Matrix to decompose. Not modified
     * @return A LU decomposition of the matrix
     */
    public static BandLU factorize(BandMatrix A) {
        return new BandLU(A.numRows(), A.kl, A.ku).factor(A, false);
    }

    /**
     * Creates an LU decomposition of the given matrix
     * 
     * @param A
     *            Matrix to decompose. If the decomposition is in-place, its
     *            number of superdiagonals must equal <code>kl+ku</code>
     * @param inplace
     *            Wheter or not the decomposition should overwrite the passed
     *            matrix
     * @return The current decomposition
     */
    public BandLU factor(BandMatrix A, boolean inplace) {
        if (inplace)
            return factor(A);
        else
            return factor(new BandMatrix(A, kl, kl + ku));
    }

    /**
     * Creates an LU decomposition of the given matrix
     * 
     * @param A
     *            Matrix to decompose. It will be overwritten with the
     *            decomposition. Its number of superdiagonals must equal
     *            <code>kl+ku</code>
     * @return The current decomposition
     */
    public BandLU factor(BandMatrix A) {
        if (!(A.isSquare()))
            throw new IllegalArgumentException("!A.isSquare()");
        if (n != A.numRows())
            throw new IllegalArgumentException("n != A.numRows()");
        if (A.ku != ku + kl)
            throw new IllegalArgumentException("A.ku != ku + kl");

        singular = false;

        int info = Interface.lapack().gbtrf(n, n, kl, ku, A.getData(), ipiv);

        if (info > 0)
            singular = true;
        else if (info < 0)
            throw new IllegalArgumentException();

        LU.set(A);

        return this;
    }

    /**
     * Returns the lower triangular factor
     */
    public UnitLowerTriangBandMatrix getL() {
        return new UnitLowerTriangBandMatrix(LU, LU.numSubDiagonals(), false);
    }

    /**
     * Returns the upper triangular factor
     */
    public UpperTriangBandMatrix getU() {
        return new UpperTriangBandMatrix(LU, LU.numSuperDiagonals(), false);
    }

    /**
     * Returns the decomposition matrix
     */
    public BandMatrix getLU() {
        return LU;
    }

    /**
     * Returns the row pivots
     */
    public int[] getPivots() {
        return ipiv;
    }

    /**
     * Checks for singularity
     */
    public boolean isSingular() {
        return singular;
    }

    /**
     * Computes the reciprocal condition number, using either the infinity norm
     * of the 1 norm.
     * 
     * @param A
     *            The matrix this is a decomposition of
     * @param norm
     *            Either <code>Norm.One</code> or <code>Norm.Infinity</code>
     * @return The reciprocal condition number. Values close to unity indicate a
     *         well-conditioned system, while numbers close to zero do not.
     */
    public double rcond(Matrix A, Norm norm) {
        if (norm != Norm.One && norm != Norm.Infinity)
            throw new IllegalArgumentException(
                    "Only the 1 or the Infinity norms are supported");
        if (A.numRows() != n)
            throw new IllegalArgumentException("A.numRows() != n");
        if (!A.isSquare())
            throw new IllegalArgumentException("!A.isSquare()");

        double anorm = A.norm(norm);

        double[] work = new double[3 * n];
        int[] lwork = new int[n];

        double[] rcond = new double[1];
        int info = Interface.lapack().gbcon(norm, n, kl, ku, LU.getData(),
                ipiv, anorm, rcond, work, lwork);

        if (info < 0)
            throw new IllegalArgumentException();

        return rcond[0];
    }

    /**
     * Computes <code>A\B</code>, overwriting <code>B</code>
     */
    public DenseMatrix solve(DenseMatrix B) throws MatrixSingularException {
        return solve(B, Transpose.NoTranspose);
    }

    /**
     * Computes <code>A<sup>T</sup>\B</code>, overwriting <code>B</code>
     */
    public DenseMatrix transSolve(DenseMatrix B) throws MatrixSingularException {
        return solve(B, Transpose.Transpose);
    }

    private DenseMatrix solve(DenseMatrix B, Transpose trans)
            throws MatrixSingularException {
        if (singular)
            throw new MatrixSingularException();
        if (B.numRows() != n)
            throw new IllegalArgumentException("B.numRows() != n");

        int info = Interface.lapack().gbtrs(trans, n, kl, ku, B.numColumns(),
                LU.getData(), ipiv, B.getData());

        if (info < 0)
            throw new IllegalArgumentException();

        return B;
    }
}
