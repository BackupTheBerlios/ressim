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

import org.netlib.blas.Dasum;
import org.netlib.blas.Daxpy;
import org.netlib.blas.Dcopy;
import org.netlib.blas.Ddot;
import org.netlib.blas.Dgbmv;
import org.netlib.blas.Dgemm;
import org.netlib.blas.Dgemv;
import org.netlib.blas.Dger;
import org.netlib.blas.Dnrm2;
import org.netlib.blas.Dsbmv;
import org.netlib.blas.Dscal;
import org.netlib.blas.Dspmv;
import org.netlib.blas.Dspr;
import org.netlib.blas.Dspr2;
import org.netlib.blas.Dswap;
import org.netlib.blas.Dsymm;
import org.netlib.blas.Dsymv;
import org.netlib.blas.Dsyr;
import org.netlib.blas.Dsyr2;
import org.netlib.blas.Dsyr2k;
import org.netlib.blas.Dsyrk;
import org.netlib.blas.Dtbmv;
import org.netlib.blas.Dtbsv;
import org.netlib.blas.Dtpmv;
import org.netlib.blas.Dtpsv;
import org.netlib.blas.Dtrmm;
import org.netlib.blas.Dtrmv;
import org.netlib.blas.Dtrsm;
import org.netlib.blas.Dtrsv;
import org.netlib.blas.Idamax;

/**
 * BLAS kernel with a JLAPACK back-end
 */
class JLAPACK_BLASkernel implements BLASkernel {

    public double dot(int N, double[] X, double[] Y) {
        return Ddot.ddot(N, X, 0, 1, Y, 0, 1);
    }

    public double nrm2(int N, double[] X) {
        return Dnrm2.dnrm2(N, X, 0, 1);
    }

    public double asum(int N, double[] X) {
        return Dasum.dasum(N, X, 0, 1);
    }

    public int idamax(int N, double[] X) {
        return Idamax.idamax(N, X, 0, 1);
    }

    public void swap(int N, double[] X, double[] Y) {
        Dswap.dswap(N, X, 0, 1, Y, 0, 1);
    }

    public void copy(int N, double[] X, double[] Y) {
        Dcopy.dcopy(N, X, 0, 1, Y, 0, 1);
    }

    public void axpy(int N, double alpha, double[] X, double[] Y) {
        Daxpy.daxpy(N, alpha, X, 0, 1, Y, 0, 1);
    }

    public void scal(int N, double alpha, double[] X) {
        Dscal.dscal(N, alpha, X, 0, 1);
    }

    public void gemv(Transpose TransA, int M, int N, double alpha, double[] A,
            int lda, double[] X, double beta, double[] Y) {
        Dgemv.dgemv(trans(TransA), M, N, alpha, A, 0, lda, X, 0, 1, beta, Y, 0,
                1);
    }

    public void gbmv(Transpose TransA, int M, int N, int KL, int KU,
            double alpha, double[] A, int lda, double[] X, double beta,
            double[] Y) {
        Dgbmv.dgbmv(trans(TransA), M, N, KL, KU, alpha, A, 0, lda, X, 0, 1,
                beta, Y, 0, 1);
    }

    public void trmv(UpLo uplo, Transpose TransA, Diag diag, int N, double[] A,
            int lda, double[] X) {
        Dtrmv.dtrmv(uplo(uplo), trans(TransA), diag(diag), N, A, 0, lda, X, 0,
                1);
    }

    public void tbmv(UpLo uplo, Transpose TransA, Diag diag, int N, int K,
            double[] A, int lda, double[] X) {
        Dtbmv.dtbmv(uplo(uplo), trans(TransA), diag(diag), N, K, A, 0, lda, X,
                0, 1);
    }

    public void tpmv(UpLo uplo, Transpose TransA, Diag diag, int N,
            double[] Ap, double[] X) {
        Dtpmv.dtpmv(uplo(uplo), trans(TransA), diag(diag), N, Ap, 0, X, 0, 1);
    }

    public void trsv(UpLo uplo, Transpose TransA, Diag diag, int N, double[] A,
            int lda, double[] X) {
        Dtrsv.dtrsv(uplo(uplo), trans(TransA), diag(diag), N, A, 0, lda, X, 0,
                1);
    }

    public void tbsv(UpLo uplo, Transpose TransA, Diag diag, int N, int K,
            double[] A, int lda, double[] X) {
        Dtbsv.dtbsv(uplo(uplo), trans(TransA), diag(diag), N, K, A, 0, lda, X,
                0, 1);
    }

    public void tpsv(UpLo uplo, Transpose TransA, Diag diag, int N,
            double[] Ap, double[] X) {
        Dtpsv.dtpsv(uplo(uplo), trans(TransA), diag(diag), N, Ap, 0, X, 0, 1);
    }

    public void symv(UpLo uplo, int N, double alpha, double[] A, int lda,
            double[] X, double beta, double[] Y) {
        Dsymv.dsymv(uplo(uplo), N, alpha, A, 0, lda, X, 0, 1, beta, Y, 0, 1);
    }

    public void sbmv(UpLo uplo, int N, int K, double alpha, double[] A,
            int lda, double[] X, double beta, double[] Y) {
        Dsbmv.dsbmv(uplo(uplo), N, K, alpha, A, 0, lda, X, 0, 1, beta, Y, 0, 1);
    }

    public void spmv(UpLo uplo, int N, double alpha, double[] Ap, double[] X,
            double beta, double[] Y) {
        Dspmv.dspmv(uplo(uplo), N, alpha, Ap, 0, X, 0, 1, beta, Y, 0, 1);
    }

    public void ger(int M, int N, double alpha, double[] X, double[] Y,
            double[] A, int lda) {
        Dger.dger(M, N, alpha, X, 0, 1, Y, 0, 1, A, 0, lda);
    }

    public void syr(UpLo uplo, int N, double alpha, double[] X, double[] A,
            int lda) {
        Dsyr.dsyr(uplo(uplo), N, alpha, X, 0, 1, A, 0, lda);
    }

    public void spr(UpLo uplo, int N, double alpha, double[] X, double[] Ap) {
        Dspr.dspr(uplo(uplo), N, alpha, X, 0, 1, Ap, 0);
    }

    public void syr2(UpLo uplo, int N, double alpha, double[] X, double[] Y,
            double[] A, int lda) {
        Dsyr2.dsyr2(uplo(uplo), N, alpha, X, 0, 1, Y, 0, 1, A, 0, lda);
    }

    public void spr2(UpLo uplo, int N, double alpha, double[] X, double[] Y,
            double[] A) {
        Dspr2.dspr2(uplo(uplo), N, alpha, X, 0, 1, Y, 0, 1, A, 0);
    }

    public void gemm(Transpose TransA, Transpose TransB, int M, int N, int K,
            double alpha, double[] A, int lda, double[] B, int ldb,
            double beta, double[] C, int ldc) {
        Dgemm.dgemm(trans(TransA), trans(TransB), M, N, K, alpha, A, 0, lda, B,
                0, ldb, beta, C, 0, ldc);
    }

    public void symm(Side side, UpLo uplo, int M, int N, double alpha,
            double[] A, int lda, double[] B, int ldb, double beta, double[] C,
            int ldc) {
        Dsymm.dsymm(side(side), uplo(uplo), M, N, alpha, A, 0, lda, B, 0, ldb,
                beta, C, 0, ldc);
    }

    public void syrk(UpLo uplo, Transpose Trans, int N, int K, double alpha,
            double[] A, int lda, double beta, double[] C, int ldc) {
        Dsyrk.dsyrk(uplo(uplo), trans(Trans), N, K, alpha, A, 0, lda, beta, C,
                0, ldc);
    }

    public void syr2k(UpLo uplo, Transpose Trans, int N, int K, double alpha,
            double[] A, int lda, double[] B, int ldb, double beta, double[] C,
            int ldc) {
        Dsyr2k.dsyr2k(uplo(uplo), trans(Trans), N, K, alpha, A, 0, lda, B, 0,
                ldb, beta, C, 0, ldc);
    }

    public void trmm(Side side, UpLo uplo, Transpose TransA, Diag diag, int M,
            int N, double alpha, double[] A, int lda, double[] B, int ldb) {
        Dtrmm.dtrmm(side(side), uplo(uplo), trans(TransA), diag(diag), M, N,
                alpha, A, 0, lda, B, 0, ldb);
    }

    public void trsm(Side side, UpLo uplo, Transpose TransA, Diag diag, int M,
            int N, double alpha, double[] A, int lda, double[] B, int ldb) {
        Dtrsm.dtrsm(side(side), uplo(uplo), trans(TransA), diag(diag), M, N,
                alpha, A, 0, lda, B, 0, ldb);
    }

    private String trans(Transpose trans) {
        if (trans == Transpose.NoTranspose)
            return "N";
        else
            return "T";
    }

    private String uplo(UpLo uplo) {
        if (uplo == UpLo.Lower)
            return "L";
        else
            return "U";
    }

    private String diag(Diag diag) {
        if (diag == Diag.NonUnit)
            return "N";
        else
            return "U";
    }

    private String side(Side side) {
        if (side == Side.Left)
            return "L";
        else
            return "R";
    }

}
