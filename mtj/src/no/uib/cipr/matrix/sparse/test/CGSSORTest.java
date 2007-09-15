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

package no.uib.cipr.matrix.sparse.test;

import no.uib.cipr.matrix.sparse.CompRowMatrix;
import no.uib.cipr.matrix.sparse.SSOR;

/**
 * Test of CG with SSOR
 */
public class CGSSORTest extends CGTest {

    public CGSSORTest(String arg0) {
        super(arg0);
    }

    @Override
    protected void createSolver() throws Exception {
        super.createSolver();
        double omega = Math.random() + 1;
        M = new SSOR(new CompRowMatrix(A), true, omega, omega);
    }

}