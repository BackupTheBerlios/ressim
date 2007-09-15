package no.uib.cipr.rs.util;

/**
 * Function which has just a single, constant value
 */
public class ConstantValue implements Function {

    private static final long serialVersionUID = 3258135764738191669L;

    /**
     * The constant value
     */
    private double value;

    /**
     * Name of the function
     */
    private String name;

    /**
     * Creates an instance
     * 
     * @param name
     *            Name of the function
     * @param value
     *            Value of the function
     */
    public ConstantValue(String name, double value) {
        this.name = name;
        this.value = value;
    }

    /**
     * Creates an instance
     * 
     * @param config
     *            Extracts the "value" key from there
     * @param name
     *            Name of the function
     */
    public ConstantValue(Configuration config, String name) {
        this.name = name;
        value = config.getDouble("value");
    }

    /**
     * Creates an instance
     * 
     * @param value
     *            Value of the function
     */
    public ConstantValue(@SuppressWarnings("unused")
    Configuration config, double value) {
        name = null;
        this.value = value;
    }

    /**
     * Creates an instance
     * 
     * @param value
     *            Value of the function
     */
    public ConstantValue(double value) {
        this((String) null, value);
    }

    public boolean isDimension(int d) {
        return true;
    }

    public double get(double... x) {
        return value;
    }

    public double deriv(int n, double... x) {
        return 0;
    }

    public double maxOutput() {
        return value;
    }

    public double minOutput() {
        return value;
    }

    public double maxInput(int n) {
        return Double.POSITIVE_INFINITY;
    }

    public double minInput(int n) {
        return Double.NEGATIVE_INFINITY;
    }

    @Override
    public String toString() {
        return name == null ? String.valueOf(value) : name;
    }
}
