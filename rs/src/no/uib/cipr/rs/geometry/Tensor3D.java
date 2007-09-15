package no.uib.cipr.rs.geometry;

import java.io.Serializable;

/**
 * 3D symmetrical, positive definite tensor
 */
public class Tensor3D implements Serializable {

    private static final long serialVersionUID = 3618983381984228660L;

    /**
     * A zero-tensor
     */
    public static final Tensor3D ZERO = new Tensor3D(0, 0, 0);

    /**
     * Main diagonal entries
     */
    private final double xx, yy, zz;

    /**
     * Off-diagonal entries.
     */
    private final double yz, xz, xy;

    /**
     * Creates a diagonal, isotropic tensor
     */
    public Tensor3D(double d) {
        this(d, d, d);
    }

    /**
     * Creates a full, symmetrical tensor
     */
    public Tensor3D(double xx, double yy, double zz, double xy, double xz,
            double yz) {
        this.xx = xx;
        this.yy = yy;
        this.zz = zz;

        this.xy = xy;
        this.xz = xz;
        this.yz = yz;
    }

    /**
     * Creates a diagonal tensor
     */
    public Tensor3D(double xx, double yy, double zz) {
        this(xx, yy, zz, 0, 0, 0);
    }

    /**
     * Multiplies the tensor with a vector
     * 
     * @param v
     *            Vector to multiply with
     * @return New vector holding K*n
     */
    public Vector3D mult(Vector3D v) {
        double x = xx * v.x() + xy * v.y() + xz * v.z();
        double y = xy * v.x() + yy * v.y() + yz * v.z();
        double z = xz * v.x() + yz * v.y() + zz * v.z();

        return new Vector3D(x, y, z);
    }

    /**
     * Computes the 2-norm of the tensor-vector product
     * 
     * @param v
     *            Vector to multiply with
     * @return The norm ||K*v||_2
     */
    public double multNorm(Vector3D v) {
        double x = xx * v.x() + xy * v.y() + xz * v.z();
        double y = xy * v.x() + yy * v.y() + yz * v.z();
        double z = xz * v.x() + yz * v.y() + zz * v.z();

        return Math.sqrt(x * x + y * y + z * z);
    }

    @Override
    public String toString() {
        return "[" + xx + "\t" + xy + "\t" + xz + "]\n" + "[" + xy + "\t" + yy
                + "\t" + yz + "]\n" + "[" + xz + "\t" + yz + "\t" + zz + "]\n";
    }

    public double xx() {
        return xx;
    }

    public double yy() {
        return yy;
    }

    public double zz() {
        return zz;
    }

    public double xy() {
        return xy;
    }

    public double xz() {
        return xz;
    }

    public double yz() {
        return yz;
    }

    /**
     * Returns a scaled tensor
     */
    public Tensor3D scale(double scale) {
        return new Tensor3D(xx * scale, yy * scale, zz * scale, xy * scale, xz
                * scale, yz * scale);
    }

    public double horizontal() {
        return (xx + yy) / 2;
    }

    public double vertical() {
        return zz;
    }
}
