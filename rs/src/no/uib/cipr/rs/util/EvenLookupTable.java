package no.uib.cipr.rs.util;

/**
 * Lookup table for 1D, 2D, and 3D tabular data arranged in an evenly spaced
 * grid
 */
public class EvenLookupTable extends LookupTable {

    private static final long serialVersionUID = -2656797779726675531L;

    /**
     * Coordinate offsets along each direction
     */
    private double[] offset;

    /**
     * Spacings along each dimension
     */
    private double[] delta;

    public EvenLookupTable(String name, double[] offset, double[] delta,
            double[] data) {
        super(name, 1);

        this.offset = new double[] { offset[0] };
        this.delta = new double[] { delta[0] };

        int nx = data.length;
        data1 = new double[nx];
        System.arraycopy(data, 0, data1, 0, nx);

        coordinate = new double[dimension][nx];
        createCoordinates();

        checkTable();
    }

    public EvenLookupTable(String name, double[] offset, double[] delta,
            double[][] data) {
        super(name, 2);

        this.offset = new double[] { offset[0], offset[1] };
        this.delta = new double[] { delta[0], delta[1] };

        int nx = data.length;
        int ny = data[0].length;
        data2 = new double[nx][ny];
        for (int i = 0; i < nx; ++i)
            for (int j = 0; j < ny; ++j)
                data2[i][j] = data[i][j];

        coordinate = new double[][] { new double[nx], new double[ny] };
        createCoordinates();

        checkTable();
    }

    public EvenLookupTable(String name, double[] offset, double[] delta,
            double[][][] data) {
        super(name, 3);

        this.offset = new double[] { offset[0], offset[1], offset[2] };
        this.delta = new double[] { delta[0], delta[1], delta[2] };

        int nx = data.length;
        int ny = data[0].length;
        int nz = data[0][0].length;
        data3 = new double[nx][ny][nz];
        for (int i = 0; i < nx; ++i)
            for (int j = 0; j < ny; ++j)
                for (int k = 0; k < nz; ++k)
                    data3[i][j][k] = data[i][j][k];

        coordinate = new double[][] { new double[nx], new double[ny],
                new double[nz] };
        createCoordinates();

        checkTable();
    }

    public EvenLookupTable(Configuration config, String name) {
        super(config, name);
    }

    @Override
    void readTableCoordinates(Configuration config) {
        offset = new double[dimension];
        delta = new double[dimension];
        coordinate = new double[dimension][];

        int nx = 0, ny = 0, nz = 0;
        double xMax = 0, yMax = 0, zMax = 0;

        switch (dimension) {
        case 1:
            offset[0] = config.getDouble("xMin");

            nx = config.getInt("nx");

            xMax = config.getDouble("xMax");

            delta[0] = (xMax - offset[0]) / (nx - 1);

            coordinate[0] = new double[nx];

            break;
        case 2:
            offset[0] = config.getDouble("xMin");
            offset[1] = config.getDouble("yMin");

            nx = config.getInt("nx");
            ny = config.getInt("ny");

            xMax = config.getDouble("xMax");
            yMax = config.getDouble("yMax");

            delta[0] = (xMax - offset[0]) / (nx - 1);
            delta[1] = (yMax - offset[1]) / (ny - 1);

            coordinate[0] = new double[nx];
            coordinate[1] = new double[ny];

            break;
        case 3:
            offset[0] = config.getDouble("xMin");
            offset[1] = config.getDouble("yMin");
            offset[2] = config.getDouble("zMin");

            nx = config.getInt("nx");
            ny = config.getInt("ny");
            nz = config.getInt("nz");

            xMax = config.getDouble("xMax");
            yMax = config.getDouble("yMax");
            zMax = config.getDouble("zMax");

            delta[0] = (xMax - offset[0]) / (nx - 1);
            delta[1] = (yMax - offset[1]) / (ny - 1);
            delta[2] = (zMax - offset[2]) / (nz - 1);

            coordinate[0] = new double[nx];
            coordinate[1] = new double[ny];
            coordinate[2] = new double[nz];

            break;
        default:
            throw new UnsupportedOperationException();
        }

        createCoordinates();
    }

    /**
     * Creates the coordinates from the evenly spaced data
     */
    private void createCoordinates() {
        for (int i = 0; i < dimension; ++i) {
            coordinate[i][0] = offset[i];
            for (int j = 1; j < coordinate[i].length; ++j)
                coordinate[i][j] = coordinate[i][j - 1] + delta[i];
        }
    }

    @Override
    int getIndex(int d, double x) {
        int index = (int) ((x - offset[d]) / delta[d]);

        if (index < 0)
            index = 0;
        else if (index > coordinate[d].length - 2)
            index = coordinate[d].length - 2;

        return index;
    }
}
