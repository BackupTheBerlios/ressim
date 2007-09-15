package no.uib.cipr.rs.meshgen.structured;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public enum Orientation {

    /**
     * Top face orientation
     */
    TOP,

    /**
     * Bottom face orientation
     */
    BOTTOM,

    /**
     * Front face orientation
     */
    FRONT,

    /**
     * Back face orientation
     */
    BACK,

    /**
     * Left face orientation
     */
    LEFT,

    /**
     * Right face orientation
     */
    RIGHT;

    /**
     * Returns a collection of orientations in the given dimension (1, 2, or 3)
     */
    public static Collection<Orientation> getOrientations(int dimension) {
        switch (dimension) {
        case 1:
            return getOrientations1D();
        case 2:
            return getOrientations2D();
        case 3:
            return getOrientations3D();
        default:
            throw new IllegalArgumentException("dimension must be 1, 2, or 3");
        }
    }

    private static List<Orientation> orientation1D = Arrays
            .asList(new Orientation[] { LEFT, RIGHT });

    /**
     * Returns a collection of orientations in 1D.
     */
    public static Collection<Orientation> getOrientations1D() {
        return orientation1D;
    }

    private static List<Orientation> orientation2D = Arrays
            .asList(new Orientation[] { FRONT, BACK, LEFT, RIGHT });

    /**
     * Returns a collection of orientations in 2D.
     */
    public static Collection<Orientation> getOrientations2D() {
        return orientation2D;
    }

    private static List<Orientation> orientation3D = Arrays
            .asList(new Orientation[] { TOP, BOTTOM, FRONT, BACK, LEFT, RIGHT });

    /**
     * Returns a collection of orientations in 3D.
     */
    public static Collection<Orientation> getOrientations3D() {
        return orientation3D;
    }

    /**
     * Returns a collection of the forward orientations in the given dimension
     * (1, 2, or 3)
     */
    public static Collection<Orientation> getForwardOrientations(int dimension) {
        switch (dimension) {
        case 1:
            return getForwardOrientations1D();
        case 2:
            return getForwardOrientations2D();
        case 3:
            return getForwardOrientations3D();
        default:
            throw new IllegalArgumentException("dimension must be 1, 2, or 3");
        }
    }

    private static List<Orientation> forwardOrientation1D = Arrays
            .asList(new Orientation[] { RIGHT });

    /**
     * Returns a collection of the forward orientations in 1D.
     */
    public static Collection<Orientation> getForwardOrientations1D() {
        return forwardOrientation1D;
    }

    private static List<Orientation> forwardOrientation2D = Arrays
            .asList(new Orientation[] { BACK, RIGHT });

    /**
     * Returns a collection of the forward orientations in 2D.
     */
    public static Collection<Orientation> getForwardOrientations2D() {
        return forwardOrientation2D;
    }

    private static List<Orientation> forwardOrientation3D = Arrays
            .asList(new Orientation[] { BOTTOM, BACK, RIGHT });

    /**
     * Returns a collection of the forward orientations in 3D.
     */
    public static Collection<Orientation> getForwardOrientations3D() {
        return forwardOrientation3D;
    }

    /**
     * Returns the orientation opposite to this.
     */
    public Orientation getOpposite() {
        switch (this) {
        case TOP:
            return BOTTOM;
        case BOTTOM:
            return TOP;
        case FRONT:
            return BACK;
        case BACK:
            return FRONT;
        case LEFT:
            return RIGHT;
        case RIGHT:
            return LEFT;
        default:
            throw new RuntimeException();
        }
    }

    /**
     * Returns the orientations different from the given orientation.
     */
    public static Collection<Orientation> getOtherOrientations3D(
            Orientation orient) {
        switch (orient) {
        case TOP:
            return Arrays.asList(new Orientation[] { BOTTOM, FRONT, BACK, LEFT,
                    RIGHT });
        case BOTTOM:
            return Arrays.asList(new Orientation[] { TOP, FRONT, BACK, LEFT,
                    RIGHT });
        case FRONT:
            return Arrays.asList(new Orientation[] { TOP, BOTTOM, BACK, LEFT,
                    RIGHT });
        case BACK:
            return Arrays.asList(new Orientation[] { TOP, BOTTOM, FRONT, LEFT,
                    RIGHT });
        case LEFT:
            return Arrays.asList(new Orientation[] { TOP, BOTTOM, FRONT, BACK,
                    RIGHT });
        case RIGHT:
            return Arrays.asList(new Orientation[] { TOP, BOTTOM, FRONT, BACK,
                    LEFT });
        default:
            throw new IllegalArgumentException("Illegal orientation type");
        }
    }

    /**
     * Returns the local index of this orientation.
     * 
     */
    public int getLocalIndex() {
        switch (this) {
        case TOP:
            return 0;
        case BOTTOM:
            return 1;
        case FRONT:
            return 2;
        case BACK:
            return 3;
        case LEFT:
            return 4;
        case RIGHT:
            return 5;
        default:
            throw new RuntimeException();
        }
    }
}
