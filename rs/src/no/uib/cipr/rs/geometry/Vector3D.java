package no.uib.cipr.rs.geometry;

import java.io.Serializable;

/**
 * 3D vector
 */
public class Vector3D implements Serializable {

    private static final long serialVersionUID = 3256727286047979314L;

    /**
     * Components of the vector
     */
    private double x, y, z;

    /**
     * Creates a vector
     * 
     * @param x
     *                X-component
     * @param y
     *                Y-component
     * @param z
     *                Z-component
     */
    public Vector3D(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * Creates a vector from point i to point j
     * 
     * @param i
     *                Point i
     * @param j
     *                Point j
     */
    public Vector3D(Point3D i, Point3D j) {
        x = j.x() - i.x();
        y = j.y() - i.y();
        z = j.z() - i.z();
    }

    /**
     * Creates a vector from vector i to point j
     * 
     * @param i
     *                Vector i
     * @param j
     *                Point j
     */
    public Vector3D(Vector3D i, Point3D j) {
        x = j.x() - i.x();
        y = j.y() - i.y();
        z = j.z() - i.z();
    }

    /**
     * Creates a vector from origin to point
     * 
     * @param point
     *                end point of vector
     */
    public Vector3D(Point3D point) {
        x = point.x();
        y = point.y();
        z = point.z();
    }

    /**
     * Computes vector innerproduct with the given vector
     * 
     * @param v
     *                Vector to dot with
     * @return Innerproduct
     */
    public double dot(Vector3D v) {
        return x * v.x + y * v.y + z * v.z;
    }

    /**
     * Length/magnitude of the vector using Pythagoras' theorem in three
     * dimensions (first calculating the projection in the plane using two of
     * the coordinates, and then using this result together with the third to
     * calculate the sides of the triangle in the plane normal to the former).
     * 
     * @return Length of the vector v, written |v|, as a scalar.
     */
    public double norm2() {
        return Math.sqrt(x * x + y * y + z * z);
    }

    /**
     * Computes this vector multiplied with scalar
     * 
     * @param b
     *                Scalar
     * @return Vector3D
     */
    public Vector3D mult(double b) {
        return new Vector3D(x * b, y * b, z * b);
    }

    /**
     * Create unit vector. This method should be used instead of multiplying
     * with the inverse of the norm, to avoid precision issues (if the vector is
     * very large, then we'd multiply a large number with a very small one).
     * 
     * @return A new vector that points in the same direction as this one, but
     *         with a length of one.
     */
    public Vector3D unitize() {
        double length = this.norm2();
        return new Vector3D(x / length, y / length, z / length);
    }

    /**
     * Computes the vector addition with the given vector
     * 
     * @param v
     *                Given vector
     * @return Result vector
     * 
     */
    public Vector3D plus(Vector3D v) {
        return new Vector3D(x + v.x, y + v.y, z + v.z);
    }

    /**
     * Computes the vector addition with the given constructed vector
     * 
     * @param a
     *                Scaling factor
     * @param p
     *                Point
     * @return Result vector
     * 
     */
    public Vector3D plus(double a, Point3D p) {
        return new Vector3D(x + a * p.x(), y + a * p.y(), z + a * p.z());
    }

    /**
     * Computes the vector subtraction with the given vector
     * 
     * @param v
     *                Given vector
     * @return Result vector
     * 
     */
    public Vector3D minus(Vector3D v) {
        return new Vector3D(x - v.x, y - v.y, z - v.z);
    }

    /**
     * Computes the vector cross product
     * 
     * @param v
     *                Given vector
     * @return Result vector
     */
    public Vector3D cross(Vector3D v) {
        double vx = y * v.z - z * v.y;
        double vy = z * v.x - x * v.z;
        double vz = x * v.y - y * v.x;

        return new Vector3D(vx, vy, vz);
    }

    /**
     * Returns the vector component indexed from 1 to 3.
     */
    public double getComp(int a) {
        switch (a) {
        case 1:
            return x;
        case 2:
            return y;
        case 3:
            return z;
        default:
            throw new IllegalArgumentException();
        }
    }

    @Override
    public String toString() {
        return "[" + x + ", " + y + ", " + z + "]";
    }

    public double x() {
        return x;
    }

    public double y() {
        return y;
    }

    public double z() {
        return z;
    }

}