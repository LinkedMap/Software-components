package es.unizar.iaaa.lms.store;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.NodeIterator;

import es.unizar.iaaa.lms.pubby.vocab.LDP;

public class RdfContainer extends RdfResource {

    public RdfContainer(String uri, RdfConfiguration config, Model model) {
        super(uri, config, model);
    }

    @Override
    public boolean isContainer() {
        return true;
    }

    @Override
    public RdfContainer asContainer() {
        return this;
    }

    public int size() {
        int cnt = 0;
        NodeIterator it = model.listObjectsOfProperty(resource, LDP.contains);
        while (it.hasNext()) {
            it.next();
            cnt++;
        }
        return cnt;
    }
}
