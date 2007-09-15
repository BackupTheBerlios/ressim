package no.uib.cipr.rs.meshgen.triangle;

import java.io.Closeable;

interface Source extends Closeable {
    void readAll(PointHandler pointHandler, FractureHandler fractureHandler)
            throws TriExc;
}
