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

#include <jni.h>
#include <cblas.h>

JNIEXPORT jdouble JNICALL Java_no_uib_cipr_matrix_nni_BLAS_dot
(JNIEnv *env, jclass clazz, jint N , jdoubleArray X, jint incX, jdoubleArray Y,
 jint incY) {
  jdouble *Xl = NULL, *Yl = NULL, ret = 0.;
  Xl = (*env)->GetPrimitiveArrayCritical(env, X, NULL);
  Yl = (*env)->GetPrimitiveArrayCritical(env, Y, NULL);
  ret = cblas_ddot(N, Xl, incX, Yl, incY);
  (*env)->ReleasePrimitiveArrayCritical(env, X, Xl, JNI_ABORT);
  (*env)->ReleasePrimitiveArrayCritical(env, Y, Yl, JNI_ABORT);
  return ret;
}

JNIEXPORT jdouble JNICALL Java_no_uib_cipr_matrix_nni_BLAS_nrm2
(JNIEnv *env, jclass clazz, jint N, jdoubleArray X, jint incX) {
  jdouble *Xl = NULL, ret = 0.;
  Xl = (*env)->GetPrimitiveArrayCritical(env, X, NULL);
  ret = cblas_dnrm2(N, Xl, incX);
  (*env)->ReleasePrimitiveArrayCritical(env, X, Xl, JNI_ABORT);
  return ret;
}

JNIEXPORT jdouble JNICALL Java_no_uib_cipr_matrix_nni_BLAS_asum
(JNIEnv *env, jclass clazz, jint N, jdoubleArray X, jint incX) {
  jdouble *Xl = NULL, ret = 0.;
  Xl = (*env)->GetPrimitiveArrayCritical(env, X, NULL);
  ret = cblas_dasum(N, Xl, incX);
  (*env)->ReleasePrimitiveArrayCritical(env, X, Xl, JNI_ABORT);
  return ret;
}

JNIEXPORT jint JNICALL Java_no_uib_cipr_matrix_nni_BLAS_idamax
(JNIEnv *env, jclass clazz, jint N, jdoubleArray X, jint incX) {
  jdouble *Xl = NULL;
  jint ret = 0;
  Xl = (*env)->GetPrimitiveArrayCritical(env, X, NULL);
  ret = cblas_idamax(N, Xl, incX);
  (*env)->ReleasePrimitiveArrayCritical(env, X, Xl, JNI_ABORT);
  return ret;
}

JNIEXPORT void JNICALL Java_no_uib_cipr_matrix_nni_BLAS_swap
(JNIEnv *env, jclass clazz, jint N, jdoubleArray X, jint incX, jdoubleArray Y,
 jint incY) {
  jdouble *Xl = NULL, *Yl = NULL;
  Xl = (*env)->GetPrimitiveArrayCritical(env, X, NULL);
  Yl = (*env)->GetPrimitiveArrayCritical(env, Y, NULL);
  cblas_dswap(N, Xl, incX, Yl, incY);
  (*env)->ReleasePrimitiveArrayCritical(env, X, Xl, 0);
  (*env)->ReleasePrimitiveArrayCritical(env, Y, Yl, 0);
}

JNIEXPORT void JNICALL Java_no_uib_cipr_matrix_nni_BLAS_copy
(JNIEnv *env, jclass clazz, jint N, jdoubleArray X, jint incX, jdoubleArray Y,
 jint incY) {
  jdouble *Xl = NULL, *Yl = NULL;
  Xl = (*env)->GetPrimitiveArrayCritical(env, X, NULL);
  Yl = (*env)->GetPrimitiveArrayCritical(env, Y, NULL);
  cblas_dcopy(N, Xl, incX, Yl, incY);
  (*env)->ReleasePrimitiveArrayCritical(env, X, Xl, JNI_ABORT);
  (*env)->ReleasePrimitiveArrayCritical(env, Y, Yl, 0);
}

JNIEXPORT void JNICALL Java_no_uib_cipr_matrix_nni_BLAS_axpy
(JNIEnv *env, jclass clazz, jint N, jdouble alpha, jdoubleArray X, jint incX,
 jdoubleArray Y, jint incY) {
  jdouble *Xl = NULL, *Yl = NULL;
  Xl = (*env)->GetPrimitiveArrayCritical(env, X, NULL);
  Yl = (*env)->GetPrimitiveArrayCritical(env, Y, NULL);
  cblas_daxpy(N, alpha, Xl, incX, Yl, incY);
  (*env)->ReleasePrimitiveArrayCritical(env, X, Xl, JNI_ABORT);
  (*env)->ReleasePrimitiveArrayCritical(env, Y, Yl, 0);
}

JNIEXPORT void JNICALL Java_no_uib_cipr_matrix_nni_BLAS_rotg
(JNIEnv *env, jclass clazz, jdoubleArray a, jdoubleArray b, jdoubleArray c,
 jdoubleArray s) {
  jdouble *al = NULL, *bl = NULL, *cl = NULL, *sl = NULL;
  al = (*env)->GetPrimitiveArrayCritical(env, a, NULL);
  bl = (*env)->GetPrimitiveArrayCritical(env, b, NULL);
  cl = (*env)->GetPrimitiveArrayCritical(env, c, NULL);
  sl = (*env)->GetPrimitiveArrayCritical(env, s, NULL);
  cblas_drotg(al, bl, cl, sl);
  (*env)->ReleasePrimitiveArrayCritical(env, a, al, 0);
  (*env)->ReleasePrimitiveArrayCritical(env, b, bl, 0);
  (*env)->ReleasePrimitiveArrayCritical(env, c, cl, 0);
  (*env)->ReleasePrimitiveArrayCritical(env, s, sl, 0);
}

JNIEXPORT void JNICALL Java_no_uib_cipr_matrix_nni_BLAS_rotmg
(JNIEnv *env, jclass clazz, jdoubleArray d1, jdoubleArray d2, jdoubleArray b1,
 jdouble b2, jdoubleArray P) {
  jdouble *d1l = NULL, *d2l = NULL, *b1l = NULL, *Pl = NULL;
  d1l = (*env)->GetPrimitiveArrayCritical(env, d1, NULL);
  d2l = (*env)->GetPrimitiveArrayCritical(env, d2, NULL);
  b1l = (*env)->GetPrimitiveArrayCritical(env, b1, NULL);
  Pl = (*env)->GetPrimitiveArrayCritical(env, P, NULL);
  cblas_drotmg(d1l, d2l, b1l, b2, Pl);
  (*env)->ReleasePrimitiveArrayCritical(env, d1, d1l, 0);
  (*env)->ReleasePrimitiveArrayCritical(env, d2, d2l, 0);
  (*env)->ReleasePrimitiveArrayCritical(env, b1, b1l, 0);
  (*env)->ReleasePrimitiveArrayCritical(env, P, Pl, 0);
}

JNIEXPORT void JNICALL Java_no_uib_cipr_matrix_nni_BLAS_rot
(JNIEnv *env, jclass clazz, jint N, jdoubleArray X, jint incX, jdoubleArray Y,
 jint incY, jdouble c, jdouble s) {
  jdouble *Xl = NULL, *Yl = NULL;
  Xl = (*env)->GetPrimitiveArrayCritical(env, X, NULL);
  Yl = (*env)->GetPrimitiveArrayCritical(env, Y, NULL);
  cblas_drot(N, Xl, incX, Yl, incY, c, s);
  (*env)->ReleasePrimitiveArrayCritical(env, X, Xl, 0);
  (*env)->ReleasePrimitiveArrayCritical(env, Y, Yl, 0);
}

JNIEXPORT void JNICALL Java_no_uib_cipr_matrix_nni_BLAS_rotm
(JNIEnv *env, jclass clazz, jint N, jdoubleArray X, jint incX, jdoubleArray Y,
 jint incY, jdoubleArray P) {
  jdouble *Xl = NULL, *Yl = NULL, *Pl = NULL;
  Xl = (*env)->GetPrimitiveArrayCritical(env, X, NULL);
  Yl = (*env)->GetPrimitiveArrayCritical(env, Y, NULL);
  Pl = (*env)->GetPrimitiveArrayCritical(env, P, NULL);
  cblas_drotm(N, Xl, incX, Yl, incY, Pl);
  (*env)->ReleasePrimitiveArrayCritical(env, X, Xl, 0);
  (*env)->ReleasePrimitiveArrayCritical(env, Y, Yl, 0);
  (*env)->ReleasePrimitiveArrayCritical(env, P, Pl, JNI_ABORT);
}

JNIEXPORT void JNICALL Java_no_uib_cipr_matrix_nni_BLAS_scal
(JNIEnv *env, jclass clazz, jint N, jdouble alpha, jdoubleArray X, jint incX) {
  jdouble *Xl = NULL;
  Xl = (*env)->GetPrimitiveArrayCritical(env, X, NULL);
  cblas_dscal(N, alpha, Xl, incX);
  (*env)->ReleasePrimitiveArrayCritical(env, X, Xl, 0);
}

JNIEXPORT void JNICALL Java_no_uib_cipr_matrix_nni_BLAS_gemv
(JNIEnv *env, jclass clazz, jint order, jint TransA, jint M, jint N,
 jdouble alpha, jdoubleArray A, jint lda, jdoubleArray X, jint incX,
 jdouble beta, jdoubleArray Y, jint incY) {
  jdouble *Al = NULL, *Xl = NULL, *Yl = NULL;
  Al = (*env)->GetPrimitiveArrayCritical(env, A, NULL);
  Xl = (*env)->GetPrimitiveArrayCritical(env, X, NULL);
  Yl = (*env)->GetPrimitiveArrayCritical(env, Y, NULL);
  cblas_dgemv((enum CBLAS_ORDER) order, (enum CBLAS_TRANSPOSE) TransA,
	      M, N, alpha, Al, lda, Xl, incX, beta, Yl, incY);
  (*env)->ReleasePrimitiveArrayCritical(env, A, Al, JNI_ABORT);
  (*env)->ReleasePrimitiveArrayCritical(env, X, Xl, JNI_ABORT);
  (*env)->ReleasePrimitiveArrayCritical(env, Y, Yl, 0);
}

JNIEXPORT void JNICALL Java_no_uib_cipr_matrix_nni_BLAS_gbmv
(JNIEnv *env, jclass clazz, jint order, jint TransA, jint M, jint N, jint KL,
 jint KU, jdouble alpha, jdoubleArray A, jint lda, jdoubleArray X, jint incX,
 jdouble beta, jdoubleArray Y, jint incY) {
  jdouble *Al = NULL, *Xl = NULL, *Yl = NULL;
  Al = (*env)->GetPrimitiveArrayCritical(env, A, NULL);
  Xl = (*env)->GetPrimitiveArrayCritical(env, X, NULL);
  Yl = (*env)->GetPrimitiveArrayCritical(env, Y, NULL);
  cblas_dgbmv((enum CBLAS_ORDER) order, (enum CBLAS_TRANSPOSE) TransA,
	      M, N, KL, KU, alpha, Al, lda, Xl, incX, beta, Yl, incY);
  (*env)->ReleasePrimitiveArrayCritical(env, A, Al, JNI_ABORT);
  (*env)->ReleasePrimitiveArrayCritical(env, X, Xl, JNI_ABORT);
  (*env)->ReleasePrimitiveArrayCritical(env, Y, Yl, 0);
}

JNIEXPORT void JNICALL Java_no_uib_cipr_matrix_nni_BLAS_trmv
(JNIEnv *env, jclass clazz, jint order, jint Uplo, jint TransA, jint Diag,
 jint N, jdoubleArray A, jint lda, jdoubleArray X, jint incX) {
  jdouble *Al = NULL, *Xl = NULL;
  Al = (*env)->GetPrimitiveArrayCritical(env, A, NULL);
  Xl = (*env)->GetPrimitiveArrayCritical(env, X, NULL);
  cblas_dtrmv((enum CBLAS_ORDER) order, (enum CBLAS_UPLO) Uplo,
	      (enum CBLAS_TRANSPOSE) TransA, (enum CBLAS_DIAG) Diag,
	      N, Al, lda, Xl, incX);
  (*env)->ReleasePrimitiveArrayCritical(env, A, Al, JNI_ABORT);
  (*env)->ReleasePrimitiveArrayCritical(env, X, Xl, 0);
}

JNIEXPORT void JNICALL Java_no_uib_cipr_matrix_nni_BLAS_tbmv
(JNIEnv *env, jclass clazz, jint order, jint Uplo, jint TransA, jint Diag,
 jint N, jint K, jdoubleArray A, jint lda, jdoubleArray X, jint incX) {
  jdouble *Al = NULL, *Xl = NULL;
  Al = (*env)->GetPrimitiveArrayCritical(env, A, NULL);
  Xl = (*env)->GetPrimitiveArrayCritical(env, X, NULL);
  cblas_dtbmv((enum CBLAS_ORDER) order, (enum CBLAS_UPLO) Uplo,
	      (enum CBLAS_TRANSPOSE) TransA, (enum CBLAS_DIAG) Diag,
	      N, K, Al, lda, Xl, incX);
  (*env)->ReleasePrimitiveArrayCritical(env, A, Al, JNI_ABORT);
  (*env)->ReleasePrimitiveArrayCritical(env, X, Xl, 0);
}

JNIEXPORT void JNICALL Java_no_uib_cipr_matrix_nni_BLAS_tpmv
(JNIEnv *env, jclass clazz, jint order, jint Uplo, jint TransA, jint Diag,
 jint N, jdoubleArray Ap, jdoubleArray X, jint incX) {
  jdouble *Apl = NULL, *Xl = NULL;
  Apl = (*env)->GetPrimitiveArrayCritical(env, Ap, NULL);
  Xl = (*env)->GetPrimitiveArrayCritical(env, X, NULL);
  cblas_dtpmv((enum CBLAS_ORDER) order, (enum CBLAS_UPLO) Uplo,
	      (enum CBLAS_TRANSPOSE) TransA, (enum CBLAS_DIAG) Diag,
	      N, Apl, Xl, incX);
  (*env)->ReleasePrimitiveArrayCritical(env, Ap, Apl, JNI_ABORT);
  (*env)->ReleasePrimitiveArrayCritical(env, X, Xl, 0);
}

JNIEXPORT void JNICALL Java_no_uib_cipr_matrix_nni_BLAS_trsv
(JNIEnv *env, jclass clazz, jint order, jint Uplo, jint TransA, jint Diag,
 jint N, jdoubleArray A, jint lda, jdoubleArray X, jint incX) {
  jdouble *Al = NULL, *Xl = NULL;
  Al = (*env)->GetPrimitiveArrayCritical(env, A, NULL);
  Xl = (*env)->GetPrimitiveArrayCritical(env, X, NULL);
  cblas_dtrsv((enum CBLAS_ORDER) order, (enum CBLAS_UPLO) Uplo,
	      (enum CBLAS_TRANSPOSE) TransA, (enum CBLAS_DIAG) Diag,
	      N, Al, lda, Xl, incX);
  (*env)->ReleasePrimitiveArrayCritical(env, A, Al, JNI_ABORT);
  (*env)->ReleasePrimitiveArrayCritical(env, X, Xl, 0);
}

JNIEXPORT void JNICALL Java_no_uib_cipr_matrix_nni_BLAS_tbsv
(JNIEnv *env, jclass clazz, jint order, jint Uplo, jint TransA, jint Diag,
 jint N, jint K, jdoubleArray A, jint lda, jdoubleArray X, jint incX) {
  jdouble *Al = NULL, *Xl = NULL;
  Al = (*env)->GetPrimitiveArrayCritical(env, A, NULL);
  Xl = (*env)->GetPrimitiveArrayCritical(env, X, NULL);
  cblas_dtbsv((enum CBLAS_ORDER) order, (enum CBLAS_UPLO) Uplo,
	      (enum CBLAS_TRANSPOSE) TransA, (enum CBLAS_DIAG) Diag,
	      N, K, Al, lda, Xl, incX);
  (*env)->ReleasePrimitiveArrayCritical(env, A, Al, JNI_ABORT);
  (*env)->ReleasePrimitiveArrayCritical(env, X, Xl, 0);
}

JNIEXPORT void JNICALL Java_no_uib_cipr_matrix_nni_BLAS_tpsv
(JNIEnv *env, jclass clazz, jint order, jint Uplo, jint TransA, jint Diag,
 jint N, jdoubleArray Ap, jdoubleArray X, jint incX) {
  jdouble *Apl = NULL, *Xl = NULL;
  Apl = (*env)->GetPrimitiveArrayCritical(env, Ap, NULL);
  Xl = (*env)->GetPrimitiveArrayCritical(env, X, NULL);
  cblas_dtpsv((enum CBLAS_ORDER) order, (enum CBLAS_UPLO) Uplo,
	      (enum CBLAS_TRANSPOSE) TransA, (enum CBLAS_DIAG) Diag,
	      N, Apl, Xl, incX);
  (*env)->ReleasePrimitiveArrayCritical(env, Ap, Apl, JNI_ABORT);
  (*env)->ReleasePrimitiveArrayCritical(env, X, Xl, 0);
}

JNIEXPORT void JNICALL Java_no_uib_cipr_matrix_nni_BLAS_symv
(JNIEnv *env, jclass clazz, jint order, jint Uplo, jint N, jdouble alpha,
 jdoubleArray A, jint lda, jdoubleArray X, jint incX, jdouble beta,
 jdoubleArray Y, jint incY) {
  jdouble *Al = NULL, *Xl = NULL, *Yl = NULL;
  Al = (*env)->GetPrimitiveArrayCritical(env, A, NULL);
  Xl = (*env)->GetPrimitiveArrayCritical(env, X, NULL);
  Yl = (*env)->GetPrimitiveArrayCritical(env, Y, NULL);
  cblas_dsymv((enum CBLAS_ORDER) order, (enum CBLAS_UPLO) Uplo,
	      N, alpha, Al, lda, Xl, incX, beta, Yl, incY);
  (*env)->ReleasePrimitiveArrayCritical(env, A, Al, JNI_ABORT);
  (*env)->ReleasePrimitiveArrayCritical(env, X, Xl, JNI_ABORT);
  (*env)->ReleasePrimitiveArrayCritical(env, Y, Yl, 0);
}

JNIEXPORT void JNICALL Java_no_uib_cipr_matrix_nni_BLAS_sbmv
(JNIEnv *env, jclass clazz, jint order, jint Uplo, jint N, jint K,
 jdouble alpha, jdoubleArray A, jint lda, jdoubleArray X, jint incX,
 jdouble beta, jdoubleArray Y, jint incY) {
  jdouble *Al = NULL, *Xl = NULL, *Yl = NULL;
  Al = (*env)->GetPrimitiveArrayCritical(env, A, NULL);
  Xl = (*env)->GetPrimitiveArrayCritical(env, X, NULL);
  Yl = (*env)->GetPrimitiveArrayCritical(env, Y, NULL);
  cblas_dsbmv((enum CBLAS_ORDER) order, (enum CBLAS_UPLO) Uplo,
	      N, K, alpha, Al, lda, Xl, incX, beta, Yl, incY);
  (*env)->ReleasePrimitiveArrayCritical(env, A, Al, JNI_ABORT);
  (*env)->ReleasePrimitiveArrayCritical(env, X, Xl, JNI_ABORT);
  (*env)->ReleasePrimitiveArrayCritical(env, Y, Yl, 0);
}

JNIEXPORT void JNICALL Java_no_uib_cipr_matrix_nni_BLAS_spmv
(JNIEnv *env, jclass clazz, jint order, jint Uplo, jint N, jdouble alpha,
 jdoubleArray Ap, jdoubleArray X, jint incX, jdouble beta, jdoubleArray Y,
 jint incY) {
  jdouble *Apl = NULL, *Xl = NULL, *Yl = NULL;
  Apl = (*env)->GetPrimitiveArrayCritical(env, Ap, NULL);
  Xl = (*env)->GetPrimitiveArrayCritical(env, X, NULL);
  Yl = (*env)->GetPrimitiveArrayCritical(env, Y, NULL);
  cblas_dspmv((enum CBLAS_ORDER) order, (enum CBLAS_UPLO) Uplo,
	      N, alpha, Apl, Xl, incX, beta, Yl, incY);
  (*env)->ReleasePrimitiveArrayCritical(env, Ap, Apl, JNI_ABORT);
  (*env)->ReleasePrimitiveArrayCritical(env, X, Xl, JNI_ABORT);
  (*env)->ReleasePrimitiveArrayCritical(env, Y, Yl, 0);
}

JNIEXPORT void JNICALL Java_no_uib_cipr_matrix_nni_BLAS_ger
(JNIEnv *env, jclass clazz, jint order, jint M, jint N, jdouble alpha,
 jdoubleArray X, jint incX, jdoubleArray Y, jint incY, jdoubleArray A,
 jint lda) {
  jdouble *Al = NULL, *Xl = NULL, *Yl = NULL;
  Al = (*env)->GetPrimitiveArrayCritical(env, A, NULL);
  Xl = (*env)->GetPrimitiveArrayCritical(env, X, NULL);
  Yl = (*env)->GetPrimitiveArrayCritical(env, Y, NULL);
  cblas_dger((enum CBLAS_ORDER) order,
	     M, N, alpha, Xl, incX, Yl, incY, Al, lda);
  (*env)->ReleasePrimitiveArrayCritical(env, A, Al, 0);
  (*env)->ReleasePrimitiveArrayCritical(env, X, Xl, JNI_ABORT);
  (*env)->ReleasePrimitiveArrayCritical(env, Y, Yl, JNI_ABORT);
}

JNIEXPORT void JNICALL Java_no_uib_cipr_matrix_nni_BLAS_syr
(JNIEnv *env, jclass clazz, jint order, jint Uplo, jint N, jdouble alpha,
 jdoubleArray X, jint incX, jdoubleArray A, jint lda) {
  jdouble *Al = NULL, *Xl = NULL;
  Al = (*env)->GetPrimitiveArrayCritical(env, A, NULL);
  Xl = (*env)->GetPrimitiveArrayCritical(env, X, NULL);
  cblas_dsyr((enum CBLAS_ORDER) order, (enum CBLAS_UPLO) Uplo,
	     N, alpha, Xl, incX, Al, lda);
  (*env)->ReleasePrimitiveArrayCritical(env, A, Al, 0);
  (*env)->ReleasePrimitiveArrayCritical(env, X, Xl, JNI_ABORT);
}

JNIEXPORT void JNICALL Java_no_uib_cipr_matrix_nni_BLAS_spr
(JNIEnv *env, jclass clazz, jint order, jint Uplo, jint N, jdouble alpha,
 jdoubleArray X, jint incX, jdoubleArray Ap) {
  jdouble *Apl = NULL, *Xl = NULL;
  Apl = (*env)->GetPrimitiveArrayCritical(env, Ap, NULL);
  Xl = (*env)->GetPrimitiveArrayCritical(env, X, NULL);
  cblas_dspr((enum CBLAS_ORDER) order, (enum CBLAS_UPLO) Uplo,
	     N, alpha, Xl, incX, Apl);
  (*env)->ReleasePrimitiveArrayCritical(env, Ap, Apl, 0);
  (*env)->ReleasePrimitiveArrayCritical(env, X, Xl, JNI_ABORT);
}

JNIEXPORT void JNICALL Java_no_uib_cipr_matrix_nni_BLAS_syr2
(JNIEnv *env, jclass clazz, jint order, jint Uplo, jint N, jdouble alpha,
 jdoubleArray X, jint incX, jdoubleArray Y, jint incY, jdoubleArray A,
 jint lda) {
  jdouble *Al = NULL, *Xl = NULL, *Yl = NULL;
  Al = (*env)->GetPrimitiveArrayCritical(env, A, NULL);
  Xl = (*env)->GetPrimitiveArrayCritical(env, X, NULL);
  Yl = (*env)->GetPrimitiveArrayCritical(env, Y, NULL);
  cblas_dsyr2((enum CBLAS_ORDER) order, (enum CBLAS_UPLO) Uplo,
	      N, alpha, Xl, incX, Yl, incY, Al, lda);
  (*env)->ReleasePrimitiveArrayCritical(env, A, Al, 0);
  (*env)->ReleasePrimitiveArrayCritical(env, X, Xl, JNI_ABORT);
  (*env)->ReleasePrimitiveArrayCritical(env, Y, Yl, JNI_ABORT);
}

JNIEXPORT void JNICALL Java_no_uib_cipr_matrix_nni_BLAS_spr2
(JNIEnv *env, jclass clazz, jint order, jint Uplo, jint N, jdouble alpha,
 jdoubleArray X, jint incX, jdoubleArray Y, jint incY, jdoubleArray A) {
  jdouble *Al = NULL, *Xl = NULL, *Yl = NULL;
  Al = (*env)->GetPrimitiveArrayCritical(env, A, NULL);
  Xl = (*env)->GetPrimitiveArrayCritical(env, X, NULL);
  Yl = (*env)->GetPrimitiveArrayCritical(env, Y, NULL);
  cblas_dspr2((enum CBLAS_ORDER) order, (enum CBLAS_UPLO) Uplo,
	      N, alpha, Xl, incX, Yl, incY, Al);
  (*env)->ReleasePrimitiveArrayCritical(env, A, Al, 0);
  (*env)->ReleasePrimitiveArrayCritical(env, X, Xl, JNI_ABORT);
  (*env)->ReleasePrimitiveArrayCritical(env, Y, Yl, JNI_ABORT);
}

JNIEXPORT void JNICALL Java_no_uib_cipr_matrix_nni_BLAS_gemm
(JNIEnv *env, jclass clazz, jint Order, jint TransA, jint TransB, jint M,
 jint N, jint K, jdouble alpha, jdoubleArray A, jint lda, jdoubleArray B,
 jint ldb, jdouble beta, jdoubleArray C, jint ldc) {
  jdouble *Al = NULL, *Bl = NULL, *Cl = NULL;
  Al = (*env)->GetPrimitiveArrayCritical(env, A, NULL);
  Bl = (*env)->GetPrimitiveArrayCritical(env, B, NULL);
  Cl = (*env)->GetPrimitiveArrayCritical(env, C, NULL);
  cblas_dgemm((enum CBLAS_ORDER) Order, (enum CBLAS_TRANSPOSE) TransA,
	      (enum CBLAS_TRANSPOSE) TransB,
	      M, N, K, alpha, Al, lda, Bl, ldb, beta, Cl, ldc);
  (*env)->ReleasePrimitiveArrayCritical(env, A, Al, JNI_ABORT);
  (*env)->ReleasePrimitiveArrayCritical(env, B, Bl, JNI_ABORT);
  (*env)->ReleasePrimitiveArrayCritical(env, C, Cl, 0);
}

JNIEXPORT void JNICALL Java_no_uib_cipr_matrix_nni_BLAS_symm
(JNIEnv *env, jclass clazz, jint Order, jint Side, jint Uplo, jint M, jint N,
 jdouble alpha, jdoubleArray A, jint lda, jdoubleArray B, jint ldb,
 jdouble beta, jdoubleArray C, jint ldc) {
  jdouble *Al = NULL, *Bl = NULL, *Cl = NULL;
  Al = (*env)->GetPrimitiveArrayCritical(env, A, NULL);
  Bl = (*env)->GetPrimitiveArrayCritical(env, B, NULL);
  Cl = (*env)->GetPrimitiveArrayCritical(env, C, NULL);
  cblas_dsymm((enum CBLAS_ORDER) Order, (enum CBLAS_SIDE) Side,
	      (enum CBLAS_UPLO) Uplo,
	      M, N, alpha, Al, lda, Bl, ldb, beta, Cl, ldc);
  (*env)->ReleasePrimitiveArrayCritical(env, A, Al, JNI_ABORT);
  (*env)->ReleasePrimitiveArrayCritical(env, B, Bl, JNI_ABORT);
  (*env)->ReleasePrimitiveArrayCritical(env, C, Cl, 0);
}

JNIEXPORT void JNICALL Java_no_uib_cipr_matrix_nni_BLAS_syrk
(JNIEnv *env, jclass clazz, jint Order, jint Uplo, jint Trans, jint N, jint K,
 jdouble alpha, jdoubleArray A, jint lda, jdouble beta, jdoubleArray C,
 jint ldc) {
  jdouble *Al = NULL, *Cl = NULL;
  Al = (*env)->GetPrimitiveArrayCritical(env, A, NULL);
  Cl = (*env)->GetPrimitiveArrayCritical(env, C, NULL);
  cblas_dsyrk((enum CBLAS_ORDER) Order, (enum CBLAS_UPLO) Uplo,
	      (enum CBLAS_TRANSPOSE) Trans,
	      N, K, alpha, Al, lda, beta, Cl, ldc);
  (*env)->ReleasePrimitiveArrayCritical(env, A, Al, JNI_ABORT);
  (*env)->ReleasePrimitiveArrayCritical(env, C, Cl, 0);
}

JNIEXPORT void JNICALL Java_no_uib_cipr_matrix_nni_BLAS_syr2k
(JNIEnv *env, jclass clazz, jint Order, jint Uplo, jint Trans, jint N, jint K,
 jdouble alpha, jdoubleArray A, jint lda, jdoubleArray B, jint ldb,
 jdouble beta, jdoubleArray C, jint ldc) {
  jdouble *Al = NULL, *Bl = NULL, *Cl = NULL;
  Al = (*env)->GetPrimitiveArrayCritical(env, A, NULL);
  Bl = (*env)->GetPrimitiveArrayCritical(env, B, NULL);
  Cl = (*env)->GetPrimitiveArrayCritical(env, C, NULL);
  cblas_dsyr2k((enum CBLAS_ORDER) Order, (enum CBLAS_UPLO) Uplo,
	       (enum CBLAS_TRANSPOSE) Trans,
	       N, K, alpha, Al, lda, Bl, ldb, beta, Cl, ldc);
  (*env)->ReleasePrimitiveArrayCritical(env, A, Al, JNI_ABORT);
  (*env)->ReleasePrimitiveArrayCritical(env, B, Bl, JNI_ABORT);
  (*env)->ReleasePrimitiveArrayCritical(env, C, Cl, 0);
}

JNIEXPORT void JNICALL Java_no_uib_cipr_matrix_nni_BLAS_trmm
(JNIEnv *env, jclass clazz, jint Order, jint Side, jint Uplo, jint TransA,
 jint Diag, jint M, jint N, jdouble alpha, jdoubleArray A, jint lda,
 jdoubleArray B, jint ldb) {
  jdouble *Al = NULL, *Bl = NULL;
  Al = (*env)->GetPrimitiveArrayCritical(env, A, NULL);
  Bl = (*env)->GetPrimitiveArrayCritical(env, B, NULL);
  cblas_dtrmm((enum CBLAS_ORDER) Order, (enum CBLAS_SIDE) Side,
	      (enum CBLAS_UPLO) Uplo, (enum CBLAS_TRANSPOSE) TransA,
	      (enum CBLAS_DIAG) Diag,
	      M, N, alpha, Al, lda, Bl, ldb);
  (*env)->ReleasePrimitiveArrayCritical(env, A, Al, JNI_ABORT);
  (*env)->ReleasePrimitiveArrayCritical(env, B, Bl, 0);
}

JNIEXPORT void JNICALL Java_no_uib_cipr_matrix_nni_BLAS_trsm
(JNIEnv *env, jclass clazz, jint Order, jint Side, jint Uplo, jint TransA,
 jint Diag, jint M, jint N, jdouble alpha, jdoubleArray A, jint lda,
 jdoubleArray B, jint ldb) {
  jdouble *Al = NULL, *Bl = NULL;
  Al = (*env)->GetPrimitiveArrayCritical(env, A, NULL);
  Bl = (*env)->GetPrimitiveArrayCritical(env, B, NULL);
  cblas_dtrsm((enum CBLAS_ORDER) Order, (enum CBLAS_SIDE) Side,
	      (enum CBLAS_UPLO) Uplo, (enum CBLAS_TRANSPOSE) TransA,
	      (enum CBLAS_DIAG) Diag,
	      M, N, alpha, Al, lda, Bl, ldb);
  (*env)->ReleasePrimitiveArrayCritical(env, A, Al, JNI_ABORT);
  (*env)->ReleasePrimitiveArrayCritical(env, B, Bl, 0);
}

/*
 * For f2c
 */
void MAIN__() {
}

