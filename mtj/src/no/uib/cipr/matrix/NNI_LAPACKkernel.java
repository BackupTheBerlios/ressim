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
import no.uib.cipr.matrix.nni.LAPACK;

/**
 * LAPACK kernel with an NNI back-end
 */
class NNI_LAPACKkernel implements LAPACKkernel {

    /**
     * Constructor for NNI_LAPACKkernel
     */
    public NNI_LAPACKkernel() {
        LAPACK.init();
    }

    public int gbsv(int n, int kl, int ku, int nrhs, double[] Ab, int[] ipiv,
            double[] B) {
        int[] info = new int[1];
        LAPACK.gbsv(wrap(n), wrap(kl), wrap(ku), wrap(nrhs), Ab, ld(2 * kl + ku
                + 1), ipiv, B, ld(n), info);
        return info[0];
    }

    public int geev(JobEig jobvl, JobEig jobvr, int n, double[] A, double[] wr,
            double[] wi, double[] Vl, double[] Vr, double[] work, int lwork) {
        int[] info = new int[1];
        LAPACK.geev(jobEig(jobvl), jobEig(jobvr), wrap(n), A, ld(n), wr, wi,
                Vl, ld(n), Vr, ld(n), work, wrap(lwork), info);
        return info[0];
    }

    public int gels(Transpose trans, int m, int n, int nrhs, double[] A,
            double[] B, double[] work, int lwork) {
        int[] info = new int[1];
        LAPACK.gels(trans(trans), wrap(m), wrap(n), wrap(nrhs), A, ld(m), B,
                ld(n, m), work, wrap(lwork), info);
        return info[0];
    }

    public int gesv(int n, int nrhs, double[] A, int[] ipiv, double[] B) {
        int[] info = new int[1];
        LAPACK.gesv(wrap(n), wrap(nrhs), A, ld(n), ipiv, B, ld(n), info);
        return info[0];
    }

    public int gtsv(int n, int nrhs, double[] dl, double[] d, double[] du,
            double[] B) {
        int[] info = new int[1];
        LAPACK.gtsv(wrap(n), wrap(nrhs), dl, d, du, B, ld(n), info);
        return info[0];
    }

    public int laenv(int ispec, String name, String opts, int n1, int n2,
            int n3, int n4) {
        return LAPACK.laenv(ispec, name.toCharArray(), opts.toCharArray(), n1,
                n2, n3, n4);
    }

    public double lamch(String cmach) {
        return LAPACK.lamch(cmach.toCharArray());
    }

    public int pbsv(UpLo uplo, int n, int kd, int nrhs, double[] Ab, double[] B) {
        int[] info = new int[1];
        LAPACK.pbsv(uplo(uplo), wrap(n), wrap(kd), wrap(nrhs), Ab, ld(kd + 1),
                B, ld(n), info);
        return info[0];
    }

    public int pbtrf(UpLo uplo, int n, int kd, double[] Ab) {
        int[] info = new int[1];
        LAPACK.pbtrf(uplo(uplo), wrap(n), wrap(kd), Ab, ld(kd + 1), info);
        return info[0];
    }

    public int pbtrs(UpLo uplo, int n, int kd, int nrhs, double[] Ab, double[] B) {
        int[] info = new int[1];
        LAPACK.pbtrs(uplo(uplo), wrap(n), wrap(kd), wrap(nrhs), Ab, ld(kd + 1),
                B, ld(n), info);
        return info[0];
    }

    public int pbcon(UpLo uplo, int n, int kd, double[] Ab, double anorm,
            double[] rcond, double[] work, int[] iwork) {
        int[] info = new int[1];
        LAPACK.pbcon(uplo(uplo), wrap(n), wrap(kd), Ab, ld(kd + 1),
                wrap(anorm), rcond, work, iwork, info);
        return info[0];
    }

    public int posv(UpLo uplo, int n, int nrhs, double[] A, double[] B) {
        int[] info = new int[1];
        LAPACK.posv(uplo(uplo), wrap(n), wrap(nrhs), A, ld(n), B, ld(n), info);
        return info[0];
    }

    public int ppsv(UpLo uplo, int n, int nrhs, double[] Ap, double[] B) {
        int[] info = new int[1];
        LAPACK.ppsv(uplo(uplo), wrap(n), wrap(nrhs), Ap, B, ld(n), info);
        return info[0];
    }

    public int pptrf(UpLo uplo, int n, double[] Ap) {
        int[] info = new int[1];
        LAPACK.pptrf(uplo(uplo), wrap(n), Ap, info);
        return info[0];
    }

    public int pptrs(UpLo uplo, int n, int nrhs, double[] Ap, double[] B) {
        int[] info = new int[1];
        LAPACK.pptrs(uplo(uplo), wrap(n), wrap(nrhs), Ap, B, ld(n), info);
        return info[0];
    }

    public int ppcon(UpLo uplo, int n, double[] Ap, double anorm,
            double[] rcond, double[] work, int[] iwork) {
        int[] info = new int[1];
        LAPACK.ppcon(uplo(uplo), wrap(n), Ap, wrap(anorm), rcond, work, iwork,
                info);
        return info[0];
    }

    public int ptsv(int n, int nrhs, double[] d, double[] e, double[] B) {
        int[] info = new int[1];
        LAPACK.ptsv(wrap(n), wrap(nrhs), d, e, B, ld(n), info);
        return info[0];
    }

    public int sysv(UpLo uplo, int n, int nrhs, double[] A, int[] ipiv,
            double[] B, double[] work, int lwork) {
        int[] info = new int[1];
        LAPACK.sysv(uplo(uplo), wrap(n), wrap(nrhs), A, ld(n), ipiv, B, ld(n),
                work, wrap(lwork), info);
        return info[0];
    }

    public int spsv(UpLo uplo, int n, int nrhs, double[] Ap, int[] ipiv,
            double[] B) {
        int[] info = new int[1];
        LAPACK.spsv(uplo(uplo), wrap(n), wrap(nrhs), Ap, ipiv, B, ld(n), info);
        return info[0];
    }

    public int syev(JobEig jobz, UpLo uplo, int n, double[] A, double[] w,
            double[] work, int lwork) {
        int[] info = new int[1];
        LAPACK.syev(jobEig(jobz), uplo(uplo), wrap(n), A, ld(n), w, work,
                wrap(lwork), info);
        return info[0];
    }

    public int spev(JobEig jobz, UpLo uplo, int n, double[] Ap, double[] w,
            double[] Z, double[] work) {
        int[] info = new int[1];
        LAPACK.spev(jobEig(jobz), uplo(uplo), wrap(n), Ap, w, Z, ld(n), work,
                info);
        return info[0];
    }

    public int sbev(JobEig jobz, UpLo uplo, int n, int kd, double[] Ab,
            double[] w, double[] Z, double[] work) {
        int[] info = new int[1];
        LAPACK.sbev(jobEig(jobz), uplo(uplo), wrap(n), wrap(kd), Ab,
                ld(kd + 1), w, Z, ld(n), work, info);
        return info[0];
    }

    public int stev(JobEig jobz, int n, double[] d, double[] e, double[] Z,
            double[] work) {
        int[] info = new int[1];
        LAPACK.stev(jobEig(jobz), wrap(n), d, e, Z, ld(n), work, info);
        return info[0];
    }

    public int gesvd(JobSVD jobu, JobSVD jobvt, int m, int n, double[] A,
            double[] s, double[] U, double[] Vt, double[] work, int lwork) {
        int[] info = new int[1];
        LAPACK.gesvd(jobSVD(jobu), jobSVD(jobvt), wrap(m), wrap(n), A, ld(m),
                s, U, ld(m), Vt, ld(n), work, wrap(lwork), info);
        return info[0];
    }

    public int gesdd(JobSVD jobz, int m, int n, double[] A, double[] s,
            double[] U, double[] Vt, double[] work, int lwork, int[] iwork) {
        int[] info = new int[1];
        LAPACK.gesdd(jobSVD(jobz), wrap(m), wrap(n), A, ld(m), s, U, ld(m), Vt,
                ld(n), work, wrap(lwork), iwork, info);
        return info[0];
    }

    public int gelss(int m, int n, int nrhs, double[] A, double[] B,
            double[] s, double rcond, int[] rank, double[] work, int lwork) {
        int[] info = new int[1];
        LAPACK.gelss(wrap(m), wrap(n), wrap(nrhs), A, ld(m), B, ld(n, m), s,
                wrap(rcond), rank, work, wrap(lwork), info);
        return info[0];
    }

    public int gelsd(int m, int n, int nrhs, double[] A, double[] B,
            double[] s, double rcond, int[] rank, double[] work, int lwork,
            int[] iwork) {
        int[] info = new int[1];
        LAPACK.gelsd(wrap(m), wrap(n), wrap(nrhs), A, ld(m), B, ld(n, m), s,
                wrap(rcond), rank, work, wrap(lwork), iwork, info);
        return info[0];
    }

    private int[] wrap(int i) {
        return new int[] { i };
    }

    private double[] wrap(double d) {
        return new double[] { d };
    }

    private int[] ld(int n) {
        return wrap(Math.max(1, n));
    }

    private int[] ld(int m, int n) {
        return wrap(Math.max(1, Math.max(m, n)));
    }

    public int geqrf(int m, int n, double[] A, double[] tau, double[] work,
            int lwork) {
        int[] info = new int[1];
        LAPACK.geqrf(wrap(m), wrap(n), A, ld(m), tau, work, wrap(lwork), info);
        return info[0];
    }

    public int ormqr(Side side, Transpose trans, int m, int n, int k,
            double[] A, double[] tau, double[] C, double[] work, int lwork) {
        int[] info = new int[1];
        LAPACK.ormqr(side(side), trans(trans), wrap(m), wrap(n), wrap(k), A,
                side == Side.Left ? ld(m) : ld(n), tau, C, ld(m), work,
                wrap(lwork), info);
        return info[0];
    }

    public int orgqr(int m, int n, int k, double[] A, double[] tau,
            double[] work, int lwork) {
        int[] info = new int[1];
        LAPACK.orgqr(wrap(m), wrap(n), wrap(k), A, ld(m), tau, work,
                wrap(lwork), info);
        return info[0];
    }

    public int geqlf(int m, int n, double[] A, double[] tau, double[] work,
            int lwork) {
        int[] info = new int[1];
        LAPACK.geqlf(wrap(m), wrap(n), A, ld(m), tau, work, wrap(lwork), info);
        return info[0];
    }

    public int ormql(Side side, Transpose trans, int m, int n, int k,
            double[] A, double[] tau, double[] C, double[] work, int lwork) {
        int[] info = new int[1];
        LAPACK.ormql(side(side), trans(trans), wrap(m), wrap(n), wrap(k), A,
                side == Side.Left ? ld(m) : ld(n), tau, C, ld(m), work,
                wrap(lwork), info);
        return info[0];
    }

    public int orgql(int m, int n, int k, double[] A, double[] tau,
            double[] work, int lwork) {
        int[] info = new int[1];
        LAPACK.orgql(wrap(m), wrap(n), wrap(k), A, ld(m), tau, work,
                wrap(lwork), info);
        return info[0];
    }

    public int gerqf(int m, int n, double[] A, double[] tau, double[] work,
            int lwork) {
        int[] info = new int[1];
        LAPACK.gerqf(wrap(m), wrap(n), A, ld(m), tau, work, wrap(lwork), info);
        return info[0];
    }

    public int ormrq(Side side, Transpose trans, int m, int n, int k,
            double[] A, double[] tau, double[] C, double[] work, int lwork) {
        int[] info = new int[1];
        LAPACK.ormrq(side(side), trans(trans), wrap(m), wrap(n), wrap(k), A,
                ld(k), tau, C, ld(m), work, wrap(lwork), info);
        return info[0];
    }

    public int orgrq(int m, int n, int k, double[] A, double[] tau,
            double[] work, int lwork) {
        int[] info = new int[1];
        LAPACK.orgrq(wrap(m), wrap(n), wrap(k), A, ld(m), tau, work,
                wrap(lwork), info);
        return info[0];
    }

    public int gelqf(int m, int n, double[] A, double[] tau, double[] work,
            int lwork) {
        int[] info = new int[1];
        LAPACK.gelqf(wrap(m), wrap(n), A, ld(m), tau, work, wrap(lwork), info);
        return info[0];
    }

    public int ormlq(Side side, Transpose trans, int m, int n, int k,
            double[] A, double[] tau, double[] C, double[] work, int lwork) {
        int[] info = new int[1];
        LAPACK.ormlq(side(side), trans(trans), wrap(m), wrap(n), wrap(k), A,
                ld(k), tau, C, ld(m), work, wrap(lwork), info);
        return info[0];
    }

    public int orglq(int m, int n, int k, double[] A, double[] tau,
            double[] work, int lwork) {
        int[] info = new int[1];
        LAPACK.orglq(wrap(m), wrap(n), wrap(k), A, ld(m), tau, work,
                wrap(lwork), info);
        return info[0];
    }

    public int gbtrf(int m, int n, int kl, int ku, double[] Ab, int[] ipiv) {
        int[] info = new int[1];
        LAPACK.gbtrf(wrap(m), wrap(n), wrap(kl), wrap(ku), Ab, wrap(2 * kl + ku
                + 1), ipiv, info);
        return info[0];
    }

    public int gbtrs(Transpose trans, int n, int kl, int ku, int nrhs,
            double[] Ab, int[] ipiv, double[] B) {
        int[] info = new int[1];
        LAPACK.gbtrs(trans(trans), wrap(n), wrap(kl), wrap(ku), wrap(nrhs), Ab,
                wrap(2 * kl + ku + 1), ipiv, B, ld(n), info);
        return info[0];
    }

    public int getrf(int m, int n, double[] A, int[] ipiv) {
        int[] info = new int[1];
        LAPACK.getrf(wrap(m), wrap(n), A, ld(m), ipiv, info);
        return info[0];
    }

    public int getrs(Transpose trans, int n, int nrhs, double[] A, int[] ipiv,
            double[] B) {
        int[] info = new int[1];
        LAPACK.getrs(trans(trans), wrap(n), wrap(nrhs), A, ld(n), ipiv, B,
                ld(n), info);
        return info[0];
    }

    public int gecon(Norm norm, int n, double[] A, double anorm,
            double[] rcond, double[] work, int[] iwork) {
        int[] info = new int[1];
        LAPACK.gecon(norm(norm), wrap(n), A, ld(n), wrap(anorm), rcond, work,
                iwork, info);
        return info[0];
    }

    public int gbcon(Norm norm, int n, int kl, int ku, double[] Ab, int[] ipiv,
            double anorm, double[] rcond, double[] work, int[] iwork) {
        int[] info = new int[1];
        LAPACK.gbcon(norm(norm), wrap(n), wrap(kl), wrap(ku), Ab, ld(2 * kl
                + ku + 1), ipiv, wrap(anorm), rcond, work, iwork, info);
        return info[0];
    }

    public int gttrf(int n, double[] dl, double[] d, double[] du, double[] du2,
            int[] ipiv) {
        int[] info = new int[1];
        LAPACK.gttrf(wrap(n), dl, d, du, du2, ipiv, info);
        return info[0];
    }

    public int gttrs(Transpose trans, int n, int nrhs, double[] dl, double[] d,
            double[] du, double[] du2, int[] ipiv, double[] B) {
        int[] info = new int[1];
        LAPACK.gttrs(trans(trans), wrap(n), wrap(nrhs), dl, d, du, du2, ipiv,
                B, ld(n), info);
        return info[0];
    }

    public int gtcon(Norm norm, int n, double[] dl, double[] d, double[] du,
            double[] du2, int[] ipiv, double anorm, double[] rcond,
            double[] work, int[] iwork) {
        int[] info = new int[1];
        LAPACK.gtcon(norm(norm), wrap(n), dl, d, du, du2, ipiv, wrap(anorm),
                rcond, work, iwork, info);
        return info[0];
    }

    public int trtrs(UpLo uplo, Transpose trans, Diag diag, int n, int nrhs,
            double[] A, int lda, double[] B) {
        int[] info = new int[1];
        LAPACK.trtrs(uplo(uplo), trans(trans), diag(diag), wrap(n), wrap(nrhs),
                A, wrap(lda), B, ld(n), info);
        return info[0];
    }

    public int tptrs(UpLo uplo, Transpose trans, Diag diag, int n, int nrhs,
            double[] Ap, double[] B) {
        int[] info = new int[1];
        LAPACK.tptrs(uplo(uplo), trans(trans), diag(diag), wrap(n), wrap(nrhs),
                Ap, B, ld(n), info);
        return info[0];
    }

    public int tbtrs(UpLo uplo, Transpose trans, Diag diag, int n, int kd,
            int nrhs, double[] Ab, double[] B) {
        int[] info = new int[1];
        LAPACK.tbtrs(uplo(uplo), trans(trans), diag(diag), wrap(n), wrap(kd),
                wrap(nrhs), Ab, wrap(kd + 1), B, ld(n), info);
        return info[0];
    }

    public int potrf(UpLo uplo, int n, double[] A) {
        int[] info = new int[1];
        LAPACK.potrf(uplo(uplo), wrap(n), A, ld(n), info);
        return info[0];
    }

    public int potrs(UpLo uplo, int n, int nrhs, double[] A, double[] B) {
        int[] info = new int[1];
        LAPACK.potrs(uplo(uplo), wrap(n), wrap(nrhs), A, ld(n), B, ld(n), info);
        return info[0];
    }

    public int pocon(UpLo uplo, int n, double[] A, double anorm,
            double[] rcond, double[] work, int[] iwork) {
        int[] info = new int[1];
        LAPACK.pocon(uplo(uplo), wrap(n), A, ld(n), wrap(anorm), rcond, work,
                iwork, info);
        return info[0];
    }

    private char[] trans(Transpose trans) {
        if (trans == Transpose.NoTranspose)
            return new char[] { 'N' };
        else
            return new char[] { 'T' };
    }

    private char[] side(Side side) {
        if (side == Side.Left)
            return new char[] { 'L' };
        else
            return new char[] { 'R' };
    }

    private char[] uplo(UpLo uplo) {
        if (uplo == UpLo.Lower)
            return new char[] { 'L' };
        else
            return new char[] { 'U' };
    }

    private char[] diag(Diag diag) {
        if (diag == Diag.NonUnit)
            return new char[] { 'N' };
        else
            return new char[] { 'U' };
    }

    private char[] jobEig(JobEig job) {
        if (job == JobEig.Eigenvalues)
            return new char[] { 'N' };
        else
            return new char[] { 'V' };
    }

    private char[] jobEigRange(JobEigRange job) {
        if (job == JobEigRange.All)
            return new char[] { 'A' };
        else if (job == JobEigRange.Interval)
            return new char[] { 'V' };
        else
            return new char[] { 'I' };
    }

    private char[] jobSVD(JobSVD job) {
        if (job == JobSVD.All)
            return new char[] { 'A' };
        else if (job == JobSVD.Part)
            return new char[] { 'S' };
        else if (job == JobSVD.Overwrite)
            return new char[] { 'O' };
        else
            return new char[] { 'N' };
    }

    private char[] norm(Norm norm) {
        if (norm == Norm.One)
            return new char[] { '1' };
        else if (norm == Norm.Infinity)
            return new char[] { 'I' };
        else
            throw new IllegalArgumentException(
                    "Norm must be the 1 or the Infinity norm");
    }

    public int sbevd(JobEig jobz, UpLo uplo, int n, int kd, double[] Ab,
            double[] w, double[] Z, double[] work, int lwork, int[] iwork,
            int liwork) {
        int[] info = new int[1];
        LAPACK.sbevd(jobEig(jobz), uplo(uplo), wrap(n), wrap(kd), Ab,
                ld(kd + 1), w, Z, ld(n), work, wrap(lwork), iwork,
                wrap(liwork), info);
        return info[0];
    }

    public int spevd(JobEig jobz, UpLo uplo, int n, double[] Ap, double[] w,
            double[] Z, double[] work, int lwork, int[] iwork, int liwork) {
        int[] info = new int[1];
        LAPACK.spevd(jobEig(jobz), uplo(uplo), wrap(n), Ap, w, Z, ld(n), work,
                wrap(lwork), iwork, wrap(liwork), info);
        return info[0];
    }

    public int stevr(JobEig jobz, JobEigRange range, int n, double[] d,
            double[] e, double vl, double vu, int il, int iu, double abstol,
            int[] m, double[] w, double[] Z, int[] isuppz, double[] work,
            int lwork, int[] iwork, int liwork) {
        int[] info = new int[1];
        LAPACK.stevr(jobEig(jobz), jobEigRange(range), wrap(n), d, e, wrap(vl),
                wrap(vu), wrap(il), wrap(iu), wrap(abstol), m, w, Z, ld(n),
                isuppz, work, wrap(lwork), iwork, wrap(liwork), info);
        return info[0];
    }

    public int syevr(JobEig jobz, JobEigRange range, UpLo uplo, int n,
            double[] A, double vl, double vu, int il, int iu, double abstol,
            int[] m, double[] w, double[] Z, int[] isuppz, double[] work,
            int lwork, int[] iwork, int liwork) {
        int[] info = new int[1];
        LAPACK.syevr(jobEig(jobz), jobEigRange(range), uplo(uplo), wrap(n), A,
                ld(n), wrap(vl), wrap(vu), wrap(il), wrap(iu), wrap(abstol), m,
                w, Z, ld(n), isuppz, work, wrap(lwork), iwork, wrap(liwork),
                info);
        return info[0];
    }

}
