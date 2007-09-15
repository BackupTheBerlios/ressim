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

import no.uib.cipr.matrix.BLASkernel.Diag;
import no.uib.cipr.matrix.BLASkernel.Side;
import no.uib.cipr.matrix.BLASkernel.Transpose;
import no.uib.cipr.matrix.BLASkernel.UpLo;
import no.uib.cipr.matrix.Matrix.Norm;

/**
 * LAPACK kernel. Redirect calls to an underlying kernel implementation, thus
 * abstracting the details from the user-interface.
 */
interface LAPACKkernel {

    double lamch(String cmach);

    int laenv(int ispec, String name, String opts, int n1, int n2, int n3,
            int n4);

    // - SVD ------------------------------------------------------------------

    /**
     * The job the singular value solvers are to do. This only limits which
     * singular vectors are computed, all the singular values are always
     * computed
     */
    enum JobSVD {

        /**
         * Compute all of the singular vectors
         */
        All,

        /**
         * Compute parts of the singular vectors. For an <code>M*N</code>
         * matrix, this computes <code>min(M,N)</code> singular vectors
         */
        Part,

        /**
         * Overwrite passed data. For an <code>M*N</code> matrix, this either
         * overwrites the passed matrix with as many singular vectors as there
         * is room for. Details depend on the actual algorithm
         */
        Overwrite,

        /**
         * Do not compute any singular vectors
         */
        None

    }

    int gesvd(JobSVD jobu, JobSVD jobvt, int m, int n, double[] A, double[] s,
            double[] U, double[] Vt, double[] work, int lwork);

    int gesdd(JobSVD jobz, int m, int n, double[] A, double[] s, double[] U,
            double[] Vt, double[] work, int lwork, int[] iwork);

    int gelss(int m, int n, int nrhs, double[] A, double[] B, double[] s,
            double rcond, int[] rank, double[] work, int lwork);

    int gelsd(int m, int n, int nrhs, double[] A, double[] B, double[] s,
            double rcond, int[] rank, double[] work, int lwork, int[] iwork);

    // - Eigenvalues ----------------------------------------------------------

    /**
     * The job the eigenvectors solvers are to do
     */
    enum JobEig {

        /**
         * Only compute the eigenvalues
         */
        Eigenvalues,

        /**
         * Compute eigenvalues and eigenvectors
         */
        All

    }

    /**
     * When computing eigenvalues, this indicates which eigenvalues to locate.
     */
    enum JobEigRange {

        /**
         * All eigenvalues will be computed
         */
        All,

        /**
         * Eigenvalues in a given interval will be found
         */
        Interval,

        /**
         * The eigenvalues with the given indices are computed
         */
        Indices

    }

    int geev(JobEig jobvl, JobEig jobvr, int n, double[] A, double[] wr,
            double[] wi, double[] Vl, double[] Vr, double[] work, int lwork);

    int syev(JobEig jobz, UpLo uplo, int n, double[] A, double[] w,
            double[] work, int lwork);

    int syevr(JobEig jobz, JobEigRange range, UpLo uplo, int n, double[] A,
            double vl, double vu, int il, int iu, double abstol, int[] m,
            double[] w, double[] Z, int[] isuppz, double[] work, int lwork,
            int[] iwork, int liwork);

    int spev(JobEig jobz, UpLo uplo, int n, double[] Ap, double[] w,
            double[] Z, double[] work);

    int spevd(JobEig jobz, UpLo uplo, int n, double[] Ap, double[] w,
            double[] Z, double[] work, int lwork, int[] iwork, int liwork);

    int sbev(JobEig jobz, UpLo uplo, int n, int kd, double[] Ab, double[] w,
            double[] Z, double[] work);

    int sbevd(JobEig jobz, UpLo uplo, int n, int kd, double[] Ab, double[] w,
            double[] Z, double[] work, int lwork, int[] iwork, int liwork);

    int stev(JobEig jobz, int n, double[] d, double[] e, double[] Z,
            double[] work);

    int stevr(JobEig jobz, JobEigRange range, int n, double[] d, double[] e,
            double vl, double vu, int il, int iu, double abstol, int[] m,
            double[] w, double[] Z, int[] isuppz, double[] work, int lwork,
            int[] iwork, int liwork);

    // - QR -------------------------------------------------------------------

    int geqrf(int m, int n, double[] A, double[] tau, double[] work, int lwork);

    int ormqr(Side side, Transpose trans, int m, int n, int k, double[] A,
            double[] tau, double[] C, double[] work, int lwork);

    int orgqr(int m, int n, int k, double[] A, double[] tau, double[] work,
            int lwork);

    // - QL -------------------------------------------------------------------

    int geqlf(int m, int n, double[] A, double[] tau, double[] work, int lwork);

    int ormql(Side side, Transpose trans, int m, int n, int k, double[] A,
            double[] tau, double[] C, double[] work, int lwork);

    int orgql(int m, int n, int k, double[] A, double[] tau, double[] work,
            int lwork);

    // - RQ -------------------------------------------------------------------

    int gerqf(int m, int n, double[] A, double[] tau, double[] work, int lwork);

    int ormrq(Side side, Transpose trans, int m, int n, int k, double[] A,
            double[] tau, double[] C, double[] work, int lwork);

    int orgrq(int m, int n, int k, double[] A, double[] tau, double[] work,
            int lwork);

    // - LQ -------------------------------------------------------------------

    int gelqf(int m, int n, double[] A, double[] tau, double[] work, int lwork);

    int ormlq(Side side, Transpose trans, int m, int n, int k, double[] A,
            double[] tau, double[] C, double[] work, int lwork);

    int orglq(int m, int n, int k, double[] A, double[] tau, double[] work,
            int lwork);

    // - Least squares --------------------------------------------------------

    int gels(Transpose trans, int m, int n, int nrhs, double[] A, double[] B,
            double[] work, int lwork);

    // - Dense LU -------------------------------------------------------------

    int gesv(int n, int nrhs, double[] A, int[] ipiv, double[] B);

    int getrf(int m, int n, double[] A, int[] ipiv);

    int getrs(Transpose trans, int n, int nrhs, double[] A, int[] ipiv,
            double[] B);

    int gecon(Norm norm, int n, double[] A, double anorm, double[] rcond,
            double[] work, int[] iwork);

    // - Banded LU ------------------------------------------------------------

    int gbsv(int n, int kl, int ku, int nrhs, double[] Ab, int[] ipiv,
            double[] B);

    int gbtrf(int m, int n, int kl, int ku, double[] Ab, int[] ipiv);

    int gbtrs(Transpose trans, int n, int kl, int ku, int nrhs, double[] Ab,
            int[] ipiv, double[] B);

    int gbcon(Norm norm, int n, int kl, int ku, double[] Ab, int[] ipiv,
            double anorm, double[] rcond, double[] work, int[] iwork);

    // - Tridiagonal LU -------------------------------------------------------

    int gtsv(int n, int nrhs, double[] dl, double[] d, double[] du, double[] B);

    int gttrf(int n, double[] dl, double[] d, double[] du, double[] du2,
            int[] ipiv);

    int gttrs(Transpose trans, int n, int nrhs, double[] dl, double[] d,
            double[] du, double[] du2, int[] ipiv, double[] B);

    int gtcon(Norm norm, int n, double[] dl, double[] d, double[] du,
            double[] du2, int[] ipiv, double anorm, double[] rcond,
            double[] work, int[] iwork);

    // - Dense Cholesky -------------------------------------------------------

    int posv(UpLo uplo, int n, int nrhs, double[] A, double[] B);

    int potrf(UpLo uplo, int n, double[] A);

    int potrs(UpLo uplo, int n, int nrhs, double[] A, double[] B);

    int pocon(UpLo uplo, int n, double[] A, double anorm, double[] rcond,
            double[] work, int[] iwork);

    // - Packed Cholesky ------------------------------------------------------

    int ppsv(UpLo uplo, int n, int nrhs, double[] Ap, double[] B);

    int pptrf(UpLo uplo, int n, double[] Ap);

    int pptrs(UpLo uplo, int n, int nrhs, double[] Ap, double[] B);

    int ppcon(UpLo uplo, int n, double[] Ap, double anorm, double[] rcond,
            double[] work, int[] iwork);

    // - Banded Cholesky ------------------------------------------------------

    int pbsv(UpLo uplo, int n, int kd, int nrhs, double[] Ab, double[] B);

    int pbtrf(UpLo uplo, int n, int kd, double[] Ab);

    int pbtrs(UpLo uplo, int n, int kd, int nrhs, double[] Ab, double[] B);

    int pbcon(UpLo uplo, int n, int kd, double[] Ab, double anorm,
            double[] rcond, double[] work, int[] iwork);

    // - LDLT -----------------------------------------------------------------

    int sysv(UpLo uplo, int n, int nrhs, double[] A, int[] ipiv, double[] B,
            double[] work, int lwork);

    int spsv(UpLo uplo, int n, int nrhs, double[] Ap, int[] ipiv, double[] B);

    int ptsv(int n, int nrhs, double[] d, double[] e, double[] B);

    // - Triangular solver ----------------------------------------------------

    int trtrs(UpLo uplo, Transpose trans, Diag diag, int n, int nrhs,
            double[] A, int lda, double[] B);

    int tptrs(UpLo uplo, Transpose trans, Diag diag, int n, int nrhs,
            double[] Ap, double[] B);

    int tbtrs(UpLo uplo, Transpose trans, Diag diag, int n, int kd, int nrhs,
            double[] Ab, double[] B);

}
