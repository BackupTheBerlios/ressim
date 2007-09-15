package no.uib.cipr.rs.fluid;

import java.util.Arrays;
import java.util.List;

/**
 * Possible fluid phases
 */
public enum Phase {

    WATER {
        @Override
        public char letter() {
            return 'w';
        }
    },

    OIL {
        @Override
        public char letter() {
            return 'o';
        }
    },

    GAS {
        @Override
        public char letter() {
            return 'g';
        }
    };

    /**
     * A one-letter phase name
     */
    public abstract char letter();

    private final static List<Phase> phases = Arrays.asList(WATER, OIL, GAS);

    public final static List<Phase> all() {
        return phases;
    }
}
