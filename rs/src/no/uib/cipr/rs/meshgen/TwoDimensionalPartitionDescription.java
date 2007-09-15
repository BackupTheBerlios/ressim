package no.uib.cipr.rs.meshgen;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import no.uib.cipr.rs.geometry.Mesh;
import no.uib.cipr.rs.geometry.SourceLocation;
import no.uib.cipr.rs.geometry.Subdomain;
import no.uib.cipr.rs.meshgen.partition.DomainLocal;
import no.uib.cipr.rs.meshgen.partition.DomainTopology;
import no.uib.cipr.rs.meshgen.partition.Partition2D;
import no.uib.cipr.rs.meshgen.partition.PartitionData;
import no.uib.cipr.rs.meshgen.partition.SubdomainMeshGenerator;
import no.uib.cipr.rs.meshgen.util.ArrayData;
import no.uib.cipr.rs.util.Configuration;
import no.uib.cipr.rs.util.Logger;

/**
 * Splits a fine mesh into subdomains following a two-dimensional splitting
 */
public class TwoDimensionalPartitionDescription extends PartitionDescription {

    // stores the number of domains in this partition
    private int numDomains;

    // underlying fine mesh
    private Mesh mesh;

    // stores geometrical and topological partition information
    private Partition2D partition;

    private DomainTopology topology;

    // list of submeshes on each subdomain
    private Mesh[] subMesh;

    // list of subdomains information on each subdomain
    private Subdomain[] subDomain;

    // list of source location sets in each subdomain
    private List<Set<SourceLocation>> sourceLocation;

    // set of source locations for the fine mesh
    private Set<SourceLocation> sourceLocations;

    public TwoDimensionalPartitionDescription(Configuration config,
            Set<SourceLocation> sourceLocations, Mesh mesh) {

        // Store the given fine mesh
        this.mesh = mesh;

        // create the partition information
        partition = new Partition2D(config);

        // store the global source locations
        this.sourceLocations = sourceLocations;

        // create the domain topology from the partition information and the
        // underlying fine mesh
        topology = new DomainTopology(partition, mesh);

        numDomains = partition.getNumDomains();
        Logger.print(config, numDomains + " subdomains");

        subMesh = new Mesh[numDomains];
        subDomain = new Subdomain[numDomains];
        sourceLocation = new ArrayList<Set<SourceLocation>>(numDomains);

        for (int i = 0; i < numDomains; i++)
            subMesh[i] = createSubMesh(i);

        PartitionData pd = new PartitionData(partition, topology, mesh);
        for (int i = 0; i < numDomains; i++)
            subDomain[i] = pd.getSubdomain(subMesh[i], i);

        // store source configurations
        for (int i = 0; i < numDomains; i++)
            sourceLocation.add(createSourceConfiguration(i));
    }

    /**
     * Creates the source locations for the given subdomain
     */
    private Set<SourceLocation> createSourceConfiguration(int i) {
        Set<SourceLocation> localSources = new HashSet<SourceLocation>();

        for (SourceLocation sl : sourceLocations) {
            String name = sl.name();
            List<Integer> local = new ArrayList<Integer>();
            for (int index = 0; index < sl.numElements(); ++index) {
                int global = sl.getElement(index);

                DomainLocal<Integer, Integer> domLoc = topology
                        .getDomainLocal(global);

                if (domLoc == null) {
                    throw new IllegalStateException(String.format(
                       "Source '%s' not found in any domains; is it on the border?",
                       sl.name()));
                }

                int dom = domLoc.getDomain();
                int loc = domLoc.getLocal();

                if (dom == i)
                    local.add(loc);
            }

            int[] elements = ArrayData.integerListToArray(local);
            localSources.add(new SourceLocation(elements, name));
        }

        return localSources;
    }

    /**
     * Returns a mesh configuration for the subdomain with the given index.
     */
    private Mesh createSubMesh(int i) {
        return new SubdomainMeshGenerator(topology, mesh, i).getMesh();
    }

    @Override
    public int getNumDomains() {
        return numDomains;
    }

    @Override
    public Mesh getMesh(int i) {
        return subMesh[i];
    }

    @Override
    public Subdomain getSubdomain(int i) {
        return subDomain[i];
    }

    @Override
    public Set<SourceLocation> getSourceLocations(int i) {
        return sourceLocation.get(i);
    }
}
