package no.uib.cipr.rs.meshgen.partition;

import no.uib.cipr.rs.util.Pair;

public class DomainLocal<X, Y> extends Pair<X, Y> {

    public DomainLocal(X domain, Y local) {
        super(domain, local);
    }

    public X getDomain() {
        return x();
    }

    public Y getLocal() {
        return y();
    }
}
