package es.unizar.iaaa.lms.pubby.vocab;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;

public class LDP {

    public static final String NS = "http://www.w3.org/ns/ldp#";

    private static final Model m = ModelFactory.createDefaultModel();

    public static final Resource Container = m.createResource(NS
            + "Container");

    public static final Property contains = m.createProperty(NS
            + "contains");
}
