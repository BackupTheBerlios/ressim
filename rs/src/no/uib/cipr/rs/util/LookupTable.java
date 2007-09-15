package no.uib.cipr.rs.util;

/**
 * Lookup table for 1D, 2D, and 3D tabular data arranged in a regular grid
 */
public class LookupTable implements Function {

    private static final long serialVersionUID = -422987096072643117L;

    /**
     * Name of the lookup table
     */
    private final String name;

    /**
     * Dimension of the table
     */
    final int dimension;

    /**
     * The coordinates along each table dimension. Within each dimension, the
     * coordinates are increasing
     */
    double[][] coordinate;

    /**
     * Data in the 1D case
     */
    double[] data1;

    /**
     * Data in the 2D case
     */
    double[][] data2;

    /**
     * Data in the 3D case
     */
    double[][][] data3;

    LookupTable(String name, int dimension) {
        this.name = name;
        this.dimension = dimension;
    }

    public LookupTable(String name, double[] xcoord, double[] data) {
        this(name, 1);

        int nx = xcoord.length;
        coordinate = new double[dimension][nx];
        data1 = new double[nx];

        System.arraycopy(xcoord, 0, coordinate[0], 0, nx);
        System.arraycopy(data, 0, data1, 0, nx);

        checkTable();
    }

    public LookupTable(String name, double[] xcoord, double[] ycoord,
            double[][] data) {
        this(name, 2);

        int nx = xcoord.length;
        int ny = ycoord.length;
        coordinate = new double[dimension][];
        coordinate[0] = new double[nx];
        coordinate[1] = new double[ny];
        data2 = new double[nx][ny];

        System.arraycopy(xcoord, 0, coordinate[0], 0, nx);
        System.arraycopy(ycoord, 0, coordinate[1], 0, ny);
        for (int i = 0; i < nx; ++i)
            for (int j = 0; j < ny; ++j)
                data2[i][j] = data[i][j];

        checkTable();
    }

    public LookupTable(String name, double[] xcoord, double[] ycoord,
            double[] zcoord, double[][][] data) {
        this(name, 3);

        int nx = xcoord.length;
        int ny = ycoord.length;
        int nz = zcoord.length;
        coordinate = new double[dimension][];
        coordinate[0] = new double[nx];
        coordinate[1] = new double[ny];
        coordinate[2] = new double[nz];
        data3 = new double[nx][ny][nz];

        System.arraycopy(xcoord, 0, coordinate[0], 0, nx);
        System.arraycopy(ycoord, 0, coordinate[1], 0, ny);
        System.arraycopy(zcoord, 0, coordinate[2], 0, nz);
        for (int i = 0; i < nx; ++i)
            for (int j = 0; j < ny; ++j)
                for (int k = 0; k < nz; ++k)
                    data3[i][j][k] = data[i][j][k];

        checkTable();
    }

    public LookupTable(Configuration config, String name) {
        this(name, config.getInt("Dimension"));

        readTableCoordinates(config);
        readTableData(config);

        checkTable();
    }

    void readTableCoordinates(Configuration config) {
        coordinate = new double[dimension][];
        for (int i = 0; i < dimension; ++i)
            coordinate[i] = config.getDoubleArray("coord" + (i + 1));
    }

    private void readTableData(Configuration config) {
        // Read the data in as a linear array ...
        double[] linearData = config.getDoubleArray("data");

        // ... check its size ...
        int totalSize = 1;
        for (int i = 0; i < dimension; ++i)
            totalSize *= coordinate[i].length;
        if (linearData.length != totalSize)
            throw new IllegalArgumentException(config.trace()
                    + " Length of data-array must be " + totalSize
                    + ", but it was " + linearData.length);

        // ... and reshape it into a multi-dimensional array for fast and easy
        // access later
        int nx = 0, ny = 0, nz = 0;
        switch (dimension) {
        case 1:
            data1 = linearData;
            break;
        case 2:
            nx = coordinate[0].length;
            ny = coordinate[1].length;

            data2 = new double[nx][ny];
            for (int i = 0; i < nx; ++i)
                for (int j = 0; j < ny; ++j)
                    data2[i][j] = linearData[i + j * nx];

            break;
        case 3:
            nx = coordinate[0].length;
            ny = coordinate[1].length;
            nz = coordinate[2].length;

            data3 = new double[nx][ny][nz];
            for (int i = 0; i < nx; ++i)
                for (int j = 0; j < ny; ++j)
                    for (int k = 0; k < nz; ++k)
                        data3[i][j][k] = linearData[i + j * nx + k * nx * ny];

            break;
        default:
            throw new UnsupportedOperationException();
        }

    }

    /**
     * Performs consistency checks on the table data
     */
    void checkTable() {
        checkNumberOfCoordinates();
        checkSorted();
    }

    /**
     * Checks that there is at least two coordinates along each direction
     */
    private void checkNumberOfCoordinates() {
        for (int i = 0; i < dimension; ++i)
            if (coordinate[i].length < 2)
                throw new IllegalArgumentException("Table \"" + name
                        + "\" has less than two data points along dimension "
                        + (i + 1) + ".");
    }

    /**
     * Check that the coordinates are sorted along each direction
     */
    private void checkSorted() {
        for (int i = 0; i < dimension; ++i)
            for (int j = 1; j < coordinate[i].length; ++j)
                if (coordinate[i][j] <= coordinate[i][j - 1])
                    throw new IllegalArgumentException("Table \"" + name
                            + "\" is not sorted along dimension " + (i + 1)
                            + ", entry number " + (j + 1) + " to " + (j + 2)
                            + ".");
    }

    public boolean isDimension(int d) {
        return d == dimension;
    }

    public double get(double... x) {
        return evaluate(-1, x);
    }

    public double deriv(int n, double... x) {
        return evaluate(n, x);
    }

    /**
     * Internal table look-up and differentiation.
     * 
     * @param n
     *            If equal to -1, the function is evaluated. If n is zero or
     *            positive, the table is differentiated along dimension (n+1)
     */
    private double evaluate(int n, double... x) {
        if (x.length != dimension)
            throw new IllegalArgumentException("x.length != dimension");

        // Derivatives outside the bounds are zero
        if (n >= 0 && !inBounds(x[n], n))
            return 0;

        int xi = 0, yi = 0, zi = 0;
        double xw = 0, yw = 0, zw = 0;
        double phi1 = 0, phi2 = 0, phi3 = 0, phi4 = 0, phi5 = 0, phi6 = 0, phi7 = 0, phi8 = 0;
        double delta = 0;

        switch (dimension) {
        case 1:

            xi = getIndex(0, x[0]);

            xw = getWeight(0, xi, x[0]);

            switch (n) {
            case -1: // evaluate
                phi1 = 1 - xw;
                phi2 = xw;
                break;
            case 0: // x-derivative
                delta = coordinate[0][xi + 1] - coordinate[0][xi];
                phi1 = -1 / delta;
                phi2 = 1 / delta;
                break;
            default:
                throw new IllegalArgumentException();
            }

            return phi1 * data1[xi] + phi2 * data1[xi + 1];

        case 2:

            xi = getIndex(0, x[0]);
            yi = getIndex(1, x[1]);

            xw = getWeight(0, xi, x[0]);
            yw = getWeight(1, yi, x[1]);

            switch (n) {
            case -1: // evaluate
                phi1 = (1 - xw) * (1 - yw);
                phi2 = xw * (1 - yw);
                phi3 = (1 - xw) * yw;
                phi4 = xw * yw;
                break;
            case 0: // x-derivative
                delta = coordinate[0][xi + 1] - coordinate[0][xi];
                phi1 = (-1 + yw) / delta;
                phi2 = (1 - yw) / delta;
                phi3 = -yw / delta;
                phi4 = yw / delta;
                break;
            case 1: // y-derivative
                delta = coordinate[1][yi + 1] - coordinate[1][yi];
                phi1 = (-1 + xw) / delta;
                phi2 = -xw / delta;
                phi3 = (1 - xw) / delta;
                phi4 = xw / delta;
                break;
            default:
                throw new IllegalArgumentException();
            }

            return phi1 * data2[xi][yi] + phi2 * data2[xi + 1][yi] + phi3
                    * data2[xi][yi + 1] + phi4 * data2[xi + 1][yi + 1];

        case 3:

            xi = getIndex(0, x[0]);
            yi = getIndex(1, x[1]);
            zi = getIndex(2, x[2]);

            xw = getWeight(0, xi, x[0]);
            yw = getWeight(1, yi, x[1]);
            zw = getWeight(2, zi, x[2]);

            switch (n) {
            case -1: // evaluate
                phi1 = (1 - xw) * (1 - yw) * (1 - zw);
                phi2 = xw * (1 - yw) * (1 - zw);
                phi3 = (1 - xw) * yw * (1 - zw);
                phi4 = xw * yw * (1 - zw);
                phi5 = (1 - xw) * (1 - yw) * zw;
                phi6 = xw * (1 - yw) * zw;
                phi7 = (1 - xw) * yw * zw;
                phi8 = xw * yw * zw;
                break;
            case 0: // x-derivative
                delta = coordinate[0][xi + 1] - coordinate[0][xi];
                phi1 = -(1 - yw) * (1 - zw) / delta;
                phi2 = (1 - yw) * (1 - zw) / delta;
                phi3 = -yw * (1 - zw) / delta;
                phi4 = yw * (1 - zw) / delta;
                phi5 = -(1 - yw) * zw / delta;
                phi6 = (1 - yw) * zw / delta;
                phi7 = -yw * zw / delta;
                phi8 = yw * zw / delta;
                break;
            case 1: // y-derivative
                delta = coordinate[1][yi + 1] - coordinate[1][yi];
                phi1 = -(1 - xw) * (1 - zw) / delta;
                phi2 = -xw * (1 - zw) / delta;
                phi3 = (1 - xw) * (1 - zw) / delta;
                phi4 = xw * (1 - zw) / delta;
                phi5 = -(1 - xw) * zw / delta;
                phi6 = -xw * zw / delta;
                phi7 = (1 - xw) * zw / delta;
                phi8 = xw * zw / delta;
                break;
            case 2: // z-derivative
                delta = coordinate[2][zi + 1] - coordinate[2][zi];
                phi1 = -(1 - xw) * (1 - yw) / delta;
                phi2 = -xw * (1 - yw) / delta;
                phi3 = -(1 - xw) * yw / delta;
                phi4 = -xw * yw / delta;
                phi5 = (1 - xw) * (1 - yw) / delta;
                phi6 = xw * (1 - yw) / delta;
                phi7 = (1 - xw) * yw / delta;
                phi8 = xw * yw / delta;
                break;
            default:
                throw new IllegalArgumentException();
            }

            return phi1 * data3[xi][yi][zi] + phi2 * data3[xi + 1][yi][zi]
                    + phi3 * data3[xi][yi + 1][zi] + phi4
                    * data3[xi + 1][yi + 1][zi] + phi5 * data3[xi][yi][zi + 1]
                    + phi6 * data3[xi + 1][yi][zi + 1] + phi7
                    * data3[xi][yi + 1][zi + 1] + phi8
                    * data3[xi + 1][yi + 1][zi + 1];

        default:
            throw new UnsupportedOperationException();
        }
    }

    /**
     * Checks if a coordinate is within bounds along a given axis
     * 
     * @param x
     *            Coordinate
     * @param d
     *            Axis (dimension)
     */
    private boolean inBounds(double x, int d) {
        return x >= coordinate[d][0]
                && x <= coordinate[d][coordinate[d].length - 1];
    }

    /**
     * Finds the relative position of the given coordinate between the two
     * adjacent table coordinates.
     * 
     * @param d
     *            Current dimension
     * @param index
     *            Index as returned by getIndex
     * @param x
     *            Coordinate along given dimension
     * @return Relative position, scaled between 0 and 1
     */
    private double getWeight(int d, int index, double x) {
        double min = coordinate[d][index], max = coordinate[d][index + 1];

        if (x <= min)
            return 0;
        else if (x >= max)
            return 1;
        else
            return (x - min) / (max - min);
    }

    /**
     * Gets the coordinate index into the coordinate-array for the given
     * coordinate along the given dimension
     * 
     * @param d
     *            Dimension to search
     * @param x
     *            Coordinate along given dimension
     * @return Index into the coordinate array such that coordinate[d][index]
     *         &lt; x &lt; coordinate[d][index+1]. If x is too small, the
     *         zero'th index is returned, or if x is too great, the second last
     *         index is returned.
     */
    int getIndex(int d, double x) {
        double[] index = coordinate[d];

        if (x <= index[0])
            return 0;
        else if (x >= index[index.length - 2])
            return index.length - 2;

        int begin = 0, end = index.length - 1;
        int mid = (end + begin) / 2;

        // The usual binary search
        while (begin <= end) {
            mid = (end + begin) / 2;

            if (index[mid] < x)
                begin = mid + 1;
            else
                end = mid - 1;
        }

        return begin - 1;
    }

    public double maxOutput() {
        double max = Double.NEGATIVE_INFINITY;

        switch (dimension) {
        case 1:

            for (int i = 0; i < data1.length; ++i)
                max = Math.max(max, data1[i]);

            return max;
        case 2:

            for (int i = 0; i < data2.length; ++i)
                for (int j = 0; j < data2[i].length; ++j)
                    max = Math.max(max, data2[i][j]);

            return max;
        case 3:

            for (int i = 0; i < data3.length; ++i)
                for (int j = 0; j < data3[i].length; ++j)
                    for (int k = 0; k < data3[i][j].length; ++k)
                        max = Math.max(max, data3[i][j][k]);

            return max;
        default:
            throw new UnsupportedOperationException();
        }
    }

    public double minOutput() {
        double min = Double.POSITIVE_INFINITY;

        switch (dimension) {
        case 1:

            for (int i = 0; i < data1.length; ++i)
                min = Math.min(min, data1[i]);

            return min;
        case 2:

            for (int i = 0; i < data2.length; ++i)
                for (int j = 0; j < data2[i].length; ++j)
                    min = Math.min(min, data2[i][j]);

            return min;
        case 3:

            for (int i = 0; i < data3.length; ++i)
                for (int j = 0; j < data3[i].length; ++j)
                    for (int k = 0; k < data3[i][j].length; ++k)
                        min = Math.min(min, data3[i][j][k]);

            return min;
        default:
            throw new UnsupportedOperationException();
        }
    }

    public double maxInput(int d) {
        return coordinate[d][0];
    }

    public double minInput(int d) {
        return coordinate[d][coordinate[d].length - 1];
    }

    @Override
    public String toString() {
        return name;
    }
}
