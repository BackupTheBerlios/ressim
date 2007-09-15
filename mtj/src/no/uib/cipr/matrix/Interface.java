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
 * Interfaces to low-level BLAS and LAPACK. Tries to use the native interface
 * <code>NNI</code> by default. Failing that, <code>JLAPACK</code> is used.
 */
class Interface {

    private Interface() {
        // No need to instantiate
    }

    static {
        // First try the native interface, failing that, use the Java interface
        try {
            blas = new NNI_BLASkernel();
        } catch (Throwable t) {
            blas = new JLAPACK_BLASkernel();
        }

        try {
            lapack = new NNI_LAPACKkernel();
        } catch (Throwable t) {
            lapack = new JLAPACK_LAPACKkernel();
        }
    }

    /**
     * Current BLAS kernel interface
     */
    private static BLASkernel blas;

    /**
     * Current LAPACK kernel interface
     */
    private static LAPACKkernel lapack;

    /**
     * Gets current BLAS kernel
     */
    public static BLASkernel blas() {
        return blas;
    }

    /**
     * Gets current LAPACK kernel
     */
    public static LAPACKkernel lapack() {
        return lapack;
    }

}
