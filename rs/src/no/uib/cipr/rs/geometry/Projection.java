package no.uib.cipr.rs.geometry;
import org.junit.Assert;
import org.junit.Test;

import no.uib.cipr.matrix.DenseMatrix;
import no.uib.cipr.matrix.NotConvergedException;
import no.uib.cipr.matrix.SVD;

/**
 * Uses singular value decomposition to perform a principal component analysis
 * for projecting from a three-dimensional space to a two-dimensional subspace.
 * 
 * The points are transformed to a coordinate system that is "natural" for them,
 * i.e. it is the system where most of the variance is taken out along the first 
 * axes. Thus, one may perform the data reduction by simply ignoring the latter
 * dimensions of the points. Think of this as rotating the point cloud so that
 * you could watch them from the "side".
 * 
 * Loosely, a matrix tells us how to transform a vector into another vector. A
 * vector may be viewed as a point in a coordinate system. The eigenvectors of
 * the matrix describes the axes, and the eigenvalues tells us the importance of
 * each of them. If we use the correlation matrix as our transformation, we find
 * the coordinate system with the most variation along the primary axes.
 * 
 * See http://www.cs.cmu.edu/~elaw/papers/pca.pdf for a tutorial on principal
 * component analysis, and http://www.uwlax.edu/faculty/will/svd/index.html for
 * a tutorial on the singular value decomposition method that are used.
 * 
 * @author roland.kaufmann@cipr.uib.no
 */
public class Projection {
    /**
     * Eigenvectors/factor scores for each of the principal axis. An element
     * describes the contribution to the principal axis (row) by the dimension
     * in the standard coordinate system (column).
     */
    double ax, ay, az, bx, by, bz, cx, cy, cz;
    
    /**
     * The dependency on the dimensionality of our point algebra is declared
     * here, as well as serving as a tag for each of the places this assumption
     * is baked into our own code.
     */
    private static final int THREE = Point3D.DIMENSIONS;
    
    /**
     * Construct a projection from a set of input points.
     * 
     * @param original
     *  Input data set. The set is not referred to after the projection is
     *  constructed and may be reused for other purposes.
     */
    public Projection(Point3D[] original) {
        // n = size (X, 1);
        int n = original.length;
        
        // calculate the mean of the data set by first summing all the 
        // points, component-wise, and then dividing on the total count 
        // afterwards.
        // m = mean (X, 1);
        double mx = 0., my = 0., mz = 0.;
        for(int i = 0; i < original.length; i++) {
            Point3D p = original[i];
            mx += p.x(); my += p.y(); mz += p.z();
        }                   
        mx /= n; my /= n; mz /= n;
                
        // covariance matrix must be of an input set that has zero mean,
        // thus the mean is subtracted. also, we divide the sum of variances
        // by the count (equals dividing the standard deviation by the
        // square root). result is setup in a matrix where each column is
        // the component and row is the observation.
        // Y = ( X - repmat (m, n, 1)) ./ sqrt (n);
        DenseMatrix d = new DenseMatrix(original.length, THREE);
        double sqrtN = Math.sqrt(n);        
        for(int i = 0; i < original.length; i++) {
            Point3D p = original[i];
            d.set(i, 0, ( p.x() - mx ) / sqrtN);
            d.set(i, 1, ( p.y() - my ) / sqrtN);
            d.set(i, 2, ( p.z() - mz ) / sqrtN);
        }
        
        // find the eigenvectors of the covariance matrix using the singular
        // value decomposition method. this does the heavy numerical lifting
        // of the algorithm. the eigenvectors span a space with axis 
        // perpendicular to eachother and form a hanger matrix that can be 
        // used to transform points from the standard, Cartesian coordinate 
        // system, into the natural system of the points.
        // [U, S, V] = svd (Y);
        SVD s = new SVD(original.length, THREE);
        try {
            s = s.factor(d);
        }
        catch(NotConvergedException e) {
            throw new RuntimeException("Unable to find common plane", e);
        }
        DenseMatrix hangers = s.getVt();
        
        // extract factor scores for each eigenvector from the hanger matrix
        ax = hangers.get(0, 0); ay = hangers.get(0, 1); az = hangers.get(0, 2);
        bx = hangers.get(1, 0); by = hangers.get(1, 1); bz = hangers.get(1, 2);
        cx = hangers.get(2, 0); cy = hangers.get(2, 1); cz = hangers.get(2, 2);
    }    
       
    /**
     * Transform a point from the standard, Cartesian space into the space
     * defined by the eigenvectors of the original data set. The data set is
     * not updated by this point (unless it is one of the original points, in
     * which case its information is already embedded in the projection).
     * 
     * @param p
     *  Point that is to be projected into the natural space. This may be one
     *  of the original data input points.
     * @return
     *  A point in the space that is natural for the original data set.
     */
    private Point3D project(Point3D p) {
        // project each point to its natural system by hitting them with
        // the eigenvalues. (we could transform other points than the
        // originals by storing the eigenvectors and performing 
        // transformations
        // Z = X * V;
        //*/
        double x = p.x(), y = p.y(), z = p.z();
        double a = x * ax + y * ay + z * az;
        double b = x * bx + y * by + z * bz;
        double c = x * cx + y * cy + z * cz;
        return new Point3D(a, b, c);
        /*/
        double[] coords = new double[THREE];
        for(int j = 0; j < THREE; j++) { // j = principal axis
            coords[j] = 0.;
            for(int k = 0; k < THREE; k++) { // k = standard axis
                coords[j] += p.get(k) * this.hangers.get(j, k);
            }
        }
        return new Point3D(coords[0], coords[1], coords[2]);
        //*/
    }

    /**
     * Convenience method that transforms an array of points (all using a
     * common buffer). The second parameter is the output array that is also
     * returned. 
     */
    public Point3D[] project(Point3D[] original, Point3D[] result) {
        for(int i = 0; i < original.length; i++) {
            result[i] = project(original[i]);
        }
        return result;
    }
    
    /**
     * Convenience method that transforms a set of points into their own
     * principal components, returning values into an existing array.
     */
    public static final Point3D[] transform(Point3D[] points, Point3D[] result) {
        return new Projection(points).project(points, result);
    }
    
    /**
     * Convenience method that transforms a set of points into their own
     * principal components, allocating all intermediate storage necessary.
     */
    public static final Point3D[] transform(Point3D[] points) {
        return transform(points, new Point3D[points.length]);
    }
    
    /**
     * Unit tests to assert correctness of the operations in the parent class. 
     */
    public static class Tests {
        /**
         * Project a triangle already laying in the X-Y plane.
         */
        @Test
        public void triangle() {
            Point3D[] p = new Point3D[] {
                    new Point3D(1.,  1.,  1.),
                    new Point3D(2.,  1.,  1.),
                    new Point3D(1.5, 1.5, 1.)         
            };
            Point3D[] correct = new Point3D[] {
                    new Point3D(1.,  1.,  1.),
                    new Point3D(2.,  1.,  1.),
                    new Point3D(1.5, 1.5, 1.)
            };
            Assert.assertEquals(Projection.transform(p), correct);
        }
        
        /**
         * Project a square standing in the Y-Z plane.
         */
        @Test
        public void square() {
            Point3D[] p = new Point3D[] {
                    new Point3D(0., 1., 1.),
                    new Point3D(0., 2., 1.),
                    new Point3D(0., 1., 2.),
                    new Point3D(0., 2., 2.)
            };
            double sqrt2 = Math.sqrt(2.);
            Point3D[] correct = new Point3D[] {
                    new Point3D(sqrt2, 0., 0.),
                    new Point3D(3./2.*sqrt2, sqrt2/2., 0.),
                    new Point3D(3./2.*sqrt2, -sqrt2/2., 0.),
                    new Point3D(2.*sqrt2, 0., 0.)
            };
            Assert.assertEquals(Projection.transform(p), correct);
        }
    }
}