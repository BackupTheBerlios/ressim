package no.uib.cipr.rs.meshgen.lgr;

import static no.uib.cipr.rs.meshgen.structured.Orientation.BACK;
import static no.uib.cipr.rs.meshgen.structured.Orientation.BOTTOM;
import static no.uib.cipr.rs.meshgen.structured.Orientation.FRONT;
import static no.uib.cipr.rs.meshgen.structured.Orientation.LEFT;
import static no.uib.cipr.rs.meshgen.structured.Orientation.RIGHT;
import static no.uib.cipr.rs.meshgen.structured.Orientation.TOP;
import no.uib.cipr.rs.meshgen.structured.Orientation;

/**
 * Enumerates the various local edges of a hexahedral cell.
 */
enum EdgeLocation {

    /**
     * Top-front edge
     */
    TOPFRONT,

    /**
     * Top-back edge
     */
    TOPBACK,

    /**
     * Bottom-front edge
     */
    BOTTOMFRONT,

    /**
     * Bottom-back edge
     */
    BOTTOMBACK,

    /**
     * Top-left edge
     */
    TOPLEFT,

    /**
     * Top-right edge
     */
    TOPRIGHT,

    /**
     * Bottom-left edge
     */
    BOTTOMLEFT,

    /**
     * Bottom-right edge
     */
    BOTTOMRIGHT,

    /**
     * Front-left edge
     */
    FRONTLEFT,

    /**
     * Front-right edge
     */
    FRONTRIGHT,

    /**
     * Back-left edge
     */
    BACKLEFT,

    /**
     * Back-right edge
     */
    BACKRIGHT;

    public int getLocal() {
        switch (this) {
        case TOPFRONT:
            return 0;
        case TOPBACK:
            return 1;
        case BOTTOMFRONT:
            return 2;
        case BOTTOMBACK:
            return 3;
        case TOPLEFT:
            return 4;
        case TOPRIGHT:
            return 5;
        case BOTTOMLEFT:
            return 6;
        case BOTTOMRIGHT:
            return 7;
        case FRONTLEFT:
            return 8;
        case FRONTRIGHT:
            return 9;
        case BACKLEFT:
            return 10;
        case BACKRIGHT:
            return 12;
        default:
            throw new RuntimeException("Illegal EdgeLocation");
        }
    }

    /**
     * Returns the orientation of the face connected to the face with the given
     * orientation through the given edge location.
     */
    public static Orientation getConnectedOrientation(Orientation orient, EdgeLocation el) {
        switch (orient) {
        case TOP:
            switch (el) {
            case TOPFRONT:
                return FRONT;
            case TOPBACK:
                return BACK;
            case TOPLEFT:
                return LEFT;
            case TOPRIGHT:
                return RIGHT;
            default:
                throw new IllegalArgumentException("Illegal edge location, "
                        + el.toString() + ", for given orientation, "
                        + orient.toString());
            }
        case BOTTOM:
            switch (el) {
            case BOTTOMFRONT:
                return FRONT;
            case BOTTOMBACK:
                return BACK;
            case BOTTOMLEFT:
                return LEFT;
            case BOTTOMRIGHT:
                return RIGHT;
            default:
                throw new IllegalArgumentException("Illegal edge location, "
                        + el.toString() + ", for given orientation, "
                        + orient.toString());
            }
        case FRONT:
            switch (el) {
            case TOPFRONT:
                return TOP;
            case BOTTOMFRONT:
                return BOTTOM;
            case FRONTLEFT:
                return LEFT;
            case FRONTRIGHT:
                return RIGHT;
            default:
                throw new IllegalArgumentException("Illegal edge location, "
                        + el.toString() + ", for given orientation, "
                        + orient.toString());
            }
        case BACK:
            switch (el) {
            case TOPBACK:
                return TOP;
            case BOTTOMBACK:
                return BOTTOM;
            case BACKLEFT:
                return LEFT;
            case BACKRIGHT:
                return RIGHT;
            default:
                throw new IllegalArgumentException("Illegal edge location, "
                        + el.toString() + ", for given orientation, "
                        + orient.toString());
            }
        case LEFT:
            switch (el) {
            case TOPLEFT:
                return TOP;
            case BOTTOMLEFT:
                return BOTTOM;
            case FRONTLEFT:
                return FRONT;
            case BACKLEFT:
                return BACK;
            default:
                throw new IllegalArgumentException("Illegal edge location, "
                        + el.toString() + ", for given orientation, "
                        + orient.toString());
            }
        case RIGHT:
            switch (el) {
            case TOPRIGHT:
                return TOP;
            case BOTTOMRIGHT:
                return BOTTOM;
            case FRONTRIGHT:
                return FRONT;
            case BACKRIGHT:
                return BACK;
            default:
                throw new IllegalArgumentException("Illegal edge location, "
                        + el.toString() + ", for given orientation, "
                        + orient.toString());
            }
        default:
            throw new IllegalArgumentException("Illegal orientation");
        }
    }
}
