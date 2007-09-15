package no.uib.cipr.rs.fluid;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import no.uib.cipr.rs.util.Configuration;

/**
 * Component database
 */
public class Components implements Serializable, Iterable<Component> {

    private static final long serialVersionUID = 3905525990674608183L;

    /**
     * Stores the components in a map for quick name-based retrieval
     */
    private Map<String, Component> mapComponent;

    /**
     * The components in indexed order
     */
    private Component[] indexedComponent;

    /**
     * Hydrocarbon and other miscible components
     */
    private Component[] hc;

    /**
     * Creates the component database
     * 
     * @param config
     *                Configuration data
     */
    public Components(Configuration config) {
        Configuration sub = config.getConfiguration("Components");

        mapComponent = new HashMap<String, Component>();

        // Get the components, and store in the component mapping
        for (String component : sub.keys()) {

            Component nu = new Component(component.toUpperCase(), sub
                    .getConfiguration(component));

            mapComponent.put(nu.name(), nu);
        }

        // Add water, if not given by the user
        if (!mapComponent.containsKey("H2O")) {
            Component water = new Component("H2O", sub.getConfiguration("H2O"));
            mapComponent.put(water.name(), water);
        }

        // Sort the components, keeping water first
        List<Component> listComponent = new ArrayList<Component>(mapComponent
                .values());
        Collections.sort(listComponent);

        // Store the components indexed as well
        indexedComponent = new Component[listComponent.size()];
        int i = 0;
        for (Component nu : listComponent) {
            nu.setIndex(i);
            indexedComponent[i++] = nu;
        }

        setHydrocarbonComponents();

        // Output the components
        System.out.println(sub.trace() + toString());
    }

    /**
     * Sets the hydrocarbon components array
     */
    private void setHydrocarbonComponents() {
        int i;
        hc = new Component[numComponents() - 1];
        for (i = 0; i < numComponents() - 1; ++i)
            hc[i] = indexedComponent[i + 1];
    }

    /**
     * Returns the given component.
     */
    public Component getComponent(String name) {
        Component c = mapComponent.get(name.toUpperCase());
        if (c != null)
            return c;
        else
            throw new IllegalArgumentException("Component "
                    + name.toUpperCase() + " is not in the component database");
    }

    /**
     * Returns the given component number
     */
    public Component getComponent(int i) {
        return indexedComponent[i];
    }

    /**
     * Gets the number of components
     */
    public int numComponents() {
        return indexedComponent.length;
    }

    public Iterator<Component> iterator() {
        return Arrays.asList(indexedComponent).iterator();
    }

    /*
     * This also works as something to iterate over, and can be faster
     */
    public Component[] all() {
        return indexedComponent;
    }

    @Override
    public String toString() {
        StringBuilder string = new StringBuilder();
        string.append('(').append(numComponents()).append(')').append(' ');
        for (Component nu : this)
            string.append(nu).append(' ');
        return string.toString();
    }

    /**
     * Gets the water component
     */
    public Component water() {
        return indexedComponent[0];
    }

    /**
     * Gets the hydrocarbon components
     */
    public Component[] hc() {
        return hc;
    }
}
