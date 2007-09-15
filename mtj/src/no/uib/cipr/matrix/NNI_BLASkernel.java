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

import no.uib.cipr.matrix.nni.BLAS;

/**
 * BLAS kernel with an NNI back-end
 */
class NNI_BLASkernel implements BLASkernel {

    /**
     * Constructor for NNI_BLASkernel
     */
    public NNI_BLASkernel() {
        BLAS.init();
    }

    public double dot(int N, double[] X, double[] Y) {
        return BLAS.dot(N, X, 1, Y, 1);
    }

    public double nrm2(int N, double[] X) {
        return BLAS.nrm2(N, X, 1);
    }

    public double asum(int N, double[] X) {
        return BLAS.asum(N, X, 1);
    }

    public int idamax(int N, double[] X) {
        return BLAS.idamax(N, X, 1);
    }

    public void swap(int N, double[] X, double[] Y) {
        BLAS.swap(N, X, 1, Y, 1);
    }

    public void copy(int N, double[] X, double[] Y) {
        BLAS.copy(N, X, 1, Y, 1);
    }

    public void axpy(int N, double alpha, double[] X, double[] Y) {
        BLAS.axpy(N, alpha, X, 1, Y, 1);
    }

    public void scal(int N, double alpha, double[] X) {
        BLAS.scal(N, alpha, X, 1);
    }

    public void gemv(Transpose TransA, int M, int N, double alpha, double[] A,
            int lda, double[] X, double beta, double[] Y) {
        BLAS.gemv(BLAS.ColMajor, trans(TransA), M, N, alpha, A, lda, X, 1,
                beta, Y, 1);
    }

    public void gbmv(Transpose TransA, int M, int N, int KL, int KU,
            double alpha, double[] A, int lda, double[] X, double beta,
            double[] Y) {
        BLAS.gbmv(BLAS.ColMajor, trans(TransA), M, N, KL, KU, alpha, A, lda, X,
                1, beta, Y, 1);
    }

    public void trmv(UpLo uplo, Transpose TransA, Diag diag, int N, double[] A,
            int lda, double[] X) {
        BLAS.trmv(BLAS.ColMajor, uplo(uplo), trans(TransA), diag(diag), N, A,
                lda, X, 1);
    }

    public void tbmv(UpLo uplo, Transpose TransA, Diag diag, int N, int K,
            double[] A, int lda, double[] X) {
        BLAS.tbmv(BLAS.ColMajor, uplo(uplo), trans(TransA), diag(diag), N, K,
                A, lda, X, 1);
    }

    public void tpmv(UpLo uplo, Transpose TransA, Diag diag, int N,
            double[] Ap, double[] X) {
        BLAS.tpmv(BLAS.ColMajor, uplo(uplo), trans(TransA), diag(diag), N, Ap,
                X, 1);
    }

    public void trsv(UpLo uplo, Transpose TransA, Diag diag, int N, double[] A,
            int lda, double[] X) {
        BLAS.trsv(BLAS.ColMajor, uplo(uplo), trans(TransA), diag(diag), N, A,
                lda, X, 1);
    }

    public void tbsv(UpLo uplo, Transpose TransA, Diag diag, int N, int K,
            double[] A, int lda, double[] X) {
        BLAS.tbsv(BLAS.ColMajor, uplo(uplo), trans(TransA), diag(diag), N, K,
                A, lda, X, 1);
    }

    public void tpsv(UpLo uplo, Transpose TransA, Diag diag, int N,
            double[] Ap, double[] X) {
        BLAS.tpsv(BLAS.ColMajor, uplo(uplo), trans(TransA), diag(diag), N, Ap,
                X, 1);
    }

    public void symv(UpLo uplo, int N, double alpha, double[] A, int lda,
            double[] X, double beta, double[] Y) {
        BLAS
                .symv(BLAS.ColMajor, uplo(uplo), N, alpha, A, lda, X, 1, beta,
                        Y, 1);
    }

    public void sbmv(UpLo uplo, int N, int K, double alpha, double[] A,
            int lda, double[] X, double beta, double[] Y) {
        BLAS.sbmv(BLAS.ColMajor, uplo(uplo), N, K, alpha, A, lda, X, 1, beta,
                Y, 1);
    }

    public void spmv(UpLo uplo, int N, double alpha, double[] Ap, double[] X,
            double beta, double[] Y) {
        BLAS.spmv(BLAS.ColMajor, uplo(uplo), N, alpha, Ap, X, 1, beta, Y, 1);
    }

    public void ger(int M, int N, double alpha, double[] X, double[] Y,
            double[] A, int lda) {
        BLAS.ger(BLAS.ColMajor, M, N, alpha, X, 1, Y, 1, A, lda);
    }

    public void syr(UpLo uplo, int N, double alpha, double[] X, double[] A,
            int lda) {
        BLAS.syr(BLAS.ColMajor, uplo(uplo), N, alpha, X, 1, A, lda);
    }

    public void spr(UpLo uplo, int N, double alpha, double[] X, double[] Ap) {
        BLAS.spr(BLAS.ColMajor, uplo(uplo), N, alpha, X, 1, Ap);
    }

    public void syr2(UpLo uplo, int N, double alpha, double[] X, double[] Y,
            double[] A, int lda) {
        BLAS.syr2(BLAS.ColMajor, uplo(uplo), N, alpha, X, 1, Y, 1, A, lda);
    }

    public void spr2(UpLo uplo, int N, double alpha, double[] X, double[] Y,
            double[] Ap) {
        BLAS.spr2(BLAS.ColMajor, uplo(uplo), N, alpha, X, 1, Y, 1, Ap);
    }

    public void gemm(Transpose TransA, Transpose TransB, int M, int N, int K,
            double alpha, double[] A, int lda, double[] B, int ldb,
            double beta, double[] C, int ldc) {
        BLAS.gemm(BLAS.ColMajor, trans(TransA), trans(TransB), M, N, K, alpha,
                A, lda, B, ldb, beta, C, ldc);
    }

    public void symm(Side side, UpLo uplo, int M, int N, double alpha,
            double[] A, int lda, double[] B, int ldb, double beta, double[] C,
            int ldc) {
        BLAS.symm(BLAS.ColMajor, side(side), uplo(uplo), M, N, alpha, A, lda,
                B, ldb, beta, C, ldc);
    }

    public void syrk(UpLo uplo, Transpose Trans, int N, int K, double alpha,
            double[] A, int lda, double beta, double[] C, int ldc) {
        BLAS.syrk(BLAS.ColMajor, uplo(uplo), trans(Trans), N, K, alpha, A, lda,
                beta, C, ldc);
    }

    public void syr2k(UpLo uplo, Transpose Trans, int N, int K, double alpha,
            double[] A, int lda, double[] B, int ldb, double beta, double[] C,
            int ldc) {
        BLAS.syr2k(BLAS.ColMajor, uplo(uplo), trans(Trans), N, K, alpha, A,
                lda, B, ldb, beta, C, ldc);
    }

    public void trmm(Side side, UpLo uplo, Transpose TransA, Diag diag, int M,
            int N, double alpha, double[] A, int lda, double[] B, int ldb) {
        BLAS.trmm(BLAS.ColMajor, side(side), uplo(uplo), trans(TransA),
                diag(diag), M, N, alpha, A, lda, B, ldb);
    }

    public void trsm(Side side, UpLo uplo, Transpose TransA, Diag diag, int M,
            int N, double alpha, double[] A, int lda, double[] B, int ldb) {
        BLAS.trsm(BLAS.ColMajor, side(side), uplo(uplo), trans(TransA),
                diag(diag), M, N, alpha, A, lda, B, ldb);
    }

    private int trans(Transpose trans) {
        if (trans == Transpose.NoTranspose)
            return BLAS.NoTrans;
        else
            return BLAS.Trans;
    }

    private int side(Side side) {
        if (side == Side.Left)
            return BLAS.Left;
        else
            return BLAS.Right;
    }

    private int uplo(UpLo uplo) {
        if (uplo == UpLo.Lower)
            return BLAS.Lower;
        else
            return BLAS.Upper;
    }

    private int diag(Diag diag) {
        if (diag == Diag.NonUnit)
            return BLAS.NonUnit;
        else
            return BLAS.Unit;
    }

}
