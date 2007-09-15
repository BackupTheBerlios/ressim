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

/**
 * BLAS kernel. Redirect calls to an underlying kernel implementation, thus
 * abstracting the details from the user-interface.
 */
interface BLASkernel {

    /**
     * Transpose enumeration
     */
    enum Transpose {

        /**
         * Do not transpose
         */
        NoTranspose,

        /**
         * Transpose
         */
        Transpose

    }

    /**
     * Upper/lower enumeration
     */
    enum UpLo {

        /**
         * Matrix is stored in upper part
         */
        Upper,

        /**
         * Matrix is stored in lower part
         */
        Lower

    }

    /**
     * Diagonal enumeration
     */
    enum Diag {

        /**
         * Matrix is not unit diagonal
         */
        NonUnit,

        /**
         * Matrix is unit diagonal
         */
        Unit

    }

    /**
     * Side enumeration
     */
    enum Side {

        /**
         * Apply operation from left
         */
        Left,

        /**
         * Apply operation from right
         */
        Right

    }

    double dot(int N, double[] X, double[] Y);

    double nrm2(int N, double[] X);

    double asum(int N, double[] X);

    int idamax(int N, double[] X);

    void swap(int N, double[] X, double[] Y);

    void copy(int N, double[] X, double[] Y);

    void axpy(int N, double alpha, double[] X, double[] Y);

    void scal(int N, double alpha, double[] X);

    void gemv(Transpose TransA, int M, int N, double alpha, double[] A,
            int lda, double[] X, double beta, double[] Y);

    void gbmv(Transpose TransA, int M, int N, int KL, int KU, double alpha,
            double[] A, int lda, double[] X, double beta, double[] Y);

    void trmv(UpLo uplo, Transpose TransA, Diag diag, int N, double[] A,
            int lda, double[] X);

    void tbmv(UpLo uplo, Transpose TransA, Diag diag, int N, int K, double[] A,
            int lda, double[] X);

    void tpmv(UpLo uplo, Transpose TransA, Diag diag, int N, double[] Ap,
            double[] X);

    void trsv(UpLo uplo, Transpose TransA, Diag diag, int N, double[] A,
            int lda, double[] X);

    void tbsv(UpLo uplo, Transpose TransA, Diag diag, int N, int K, double[] A,
            int lda, double[] X);

    void tpsv(UpLo uplo, Transpose TransA, Diag diag, int N, double[] Ap,
            double[] X);

    void symv(UpLo uplo, int N, double alpha, double[] A, int lda, double[] X,
            double beta, double[] Y);

    void sbmv(UpLo uplo, int N, int K, double alpha, double[] A, int lda,
            double[] X, double beta, double[] Y);

    void spmv(UpLo uplo, int N, double alpha, double[] Ap, double[] X,
            double beta, double[] Y);

    void ger(int M, int N, double alpha, double[] X, double[] Y, double[] A,
            int lda);

    void syr(UpLo uplo, int N, double alpha, double[] X, double[] A, int lda);

    void spr(UpLo uplo, int N, double alpha, double[] X, double[] Ap);

    void syr2(UpLo uplo, int N, double alpha, double[] X, double[] Y,
            double[] A, int lda);

    void spr2(UpLo uplo, int N, double alpha, double[] X, double[] Y, double[] A);

    void gemm(Transpose TransA, Transpose TransB, int M, int N, int K,
            double alpha, double[] A, int lda, double[] B, int ldb,
            double beta, double[] C, int ldc);

    void symm(Side side, UpLo uplo, int M, int N, double alpha, double[] A,
            int lda, double[] B, int ldb, double beta, double[] C, int ldc);

    void syrk(UpLo uplo, Transpose Trans, int N, int K, double alpha,
            double[] A, int lda, double beta, double[] C, int ldc);

    void syr2k(UpLo uplo, Transpose Trans, int N, int K, double alpha,
            double[] A, int lda, double[] B, int ldb, double beta, double[] C,
            int ldc);

    void trmm(Side side, UpLo uplo, Transpose TransA, Diag diag, int M, int N,
            double alpha, double[] A, int lda, double[] B, int ldb);

    void trsm(Side side, UpLo uplo, Transpose TransA, Diag diag, int M, int N,
            double alpha, double[] A, int lda, double[] B, int ldb);

}
