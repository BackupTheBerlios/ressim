package no.uib.cipr.rs.meshgen.structured;

import no.uib.cipr.rs.geometry.Point3D;
import no.uib.cipr.rs.geometry.Vector3D;

/**
 * A trilinear map
 */
public class TrilinearMap {

    private Point3D x1, x2, x3, x4, x5, x6, x7, x8;

    /**
     * Creates a trilinear map for the given 8 corner points. These must be
     * given in cyclic order.
     */
    public TrilinearMap(Point3D p0, Point3D p1, Point3D p2, Point3D p3,
            Point3D p4, Point3D p5, Point3D p6, Point3D p7) {
        x1 = p0;
        x2 = p1;
        x3 = p2;
        x4 = p3;
        x5 = p4;
        x6 = p5;
        x7 = p6;
        x8 = p7;
    }

    /**
     * Creates a trilinear map for the given 8 corner points. These must be
     * given in cyclic order.
     */
    public TrilinearMap(Point3D[] p) {
        this(p[0], p[1], p[2], p[3], p[4], p[5], p[6], p[7]);
    }

    /**
     * Returns a point in physical coordinates for the given unit cube
     * coordinates.
     */
    public Point3D getPoint(double a, double b, double c) {
        double x = c
                * (b * (a * x4.x() + (1 - a) * x3.x()) + (1 - b)
                        * (a * x2.x() + (1 - a) * x1.x()))
                + (1 - c)
                * (b * (a * x8.x() + (1 - a) * x7.x()) + (1 - b)
                        * (a * x6.x() + (1 - a) * x5.x()));

        double y = c
                * (b * (a * x4.y() + (1 - a) * x3.y()) + (1 - b)
                        * (a * x2.y() + (1 - a) * x1.y()))
                + (1 - c)
                * (b * (a * x8.y() + (1 - a) * x7.y()) + (1 - b)
                        * (a * x6.y() + (1 - a) * x5.y()));

        double z = c
                * (b * (a * x4.z() + (1 - a) * x3.z()) + (1 - b)
                        * (a * x2.z() + (1 - a) * x1.z()))
                + (1 - c)
                * (b * (a * x8.z() + (1 - a) * x7.z()) + (1 - b)
                        * (a * x6.z() + (1 - a) * x5.z()));

        return new Point3D(x, y, z);
    }

    /**
     * Returns the center point.
     */
    public Point3D getCenterPoint() {
        return getPoint(.5, .5, .5);
    }

    /**
     * Returns the volume.
     */
    public double getVolume() {
        Vector3D r1 = new Vector3D(x3);
        Vector3D r2 = new Vector3D(x4);
        Vector3D r3 = new Vector3D(x1);
        Vector3D r4 = new Vector3D(x2);
        Vector3D r5 = new Vector3D(x7);
        Vector3D r6 = new Vector3D(x8);
        Vector3D r7 = new Vector3D(x5);
        Vector3D r8 = new Vector3D(x6);

        // multipole expansion terms
        Vector3D[][][] C = new Vector3D[2][2][2];

        C[0][0][0] = r1;
        C[1][0][0] = r2.minus(r1);
        C[0][1][0] = r3.minus(r1);
        C[0][0][1] = r5.minus(r1);
        C[1][1][0] = r4.plus(r1).minus((r3.plus(r2))); // r4+r1-r3-r2
        C[0][1][1] = r7.plus(r1).minus((r5.plus(r3))); // r7+r1-r5-r3
        C[1][0][1] = r6.plus(r1).minus((r5.plus(r2))); // r6+r1-r5-r2
        C[1][1][1] = r8.plus(r5.plus(r3.plus(r2))).minus(
                (r7.plus(r6.plus(r4.plus(r1))))); // r8+r5+r3+r2-r7-r6-r4-r1

        Permutation3[] permutations = new Permutation3[6];

        permutations[0] = new Permutation3(new int[] { 1, 2, 3 });
        permutations[1] = new Permutation3(new int[] { 1, 3, 2 });
        permutations[2] = new Permutation3(new int[] { 2, 1, 3 });
        permutations[3] = new Permutation3(new int[] { 2, 3, 1 });
        permutations[4] = new Permutation3(new int[] { 3, 1, 2 });
        permutations[5] = new Permutation3(new int[] { 3, 2, 1 });

        double volume = 0.0;

        for (Permutation3 perm : permutations) {

            int a = perm.getInt(0);
            int b = perm.getInt(1);
            int c = perm.getInt(2);

            for (int pb = 0; pb <= 1; pb++) {
                for (int pc = 0; pc <= 1; pc++) {
                    for (int qa = 0; qa <= 1; qa++) {
                        for (int qc = 0; qc <= 1; qc++) {
                            for (int ra = 0; ra <= 1; ra++) {
                                for (int rb = 0; rb <= 1; rb++) {

                                    double e = C[1][pb][pc].getComp(a)
                                            * C[qa][1][qc].getComp(b)
                                            * C[ra][rb][1].getComp(c);

                                    double d = (qa + ra + 1) * (pb + rb + 1)
                                            * (pc + qc + 1);

                                    volume += e / d;

                                }
                            }
                        }
                    }
                }
            }

            double mult = Math.pow(-1.0, perm.getSignature());

            volume = mult * volume;

        }
        return volume;

    }

    private class Permutation3 {

        private int[] array;

        private int signature;

        /**
         * Creates a permutation
         */
        public Permutation3(int[] array) {
            this.array = array;
            signature = computeSignature(array);
        }

        /**
         * Returns the permutation signature of the given array.
         */
        private int computeSignature(int[] anArray) {
            int s = 0;

            for (int i = 0; i < anArray.length; i++)
                for (int left1 = 0; left1 < i; left1++)
                    if (anArray[left1] > i)
                        s += 1;

            return s;
        }

        /**
         * Returns the number in the permutation list with the given index.
         */
        public final int getInt(int i) {
            if (i < 0 || i > array.length - 1)
                throw new IllegalArgumentException(
                        "Permutation index out of bounds");
            return array[i];
        }

        /**
         * Returns the permutation signature.
         */
        public final int getSignature() {
            return signature;
        }

    }

}