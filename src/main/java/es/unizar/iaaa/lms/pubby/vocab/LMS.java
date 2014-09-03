package es.unizar.iaaa.lms.pubby.vocab;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;

public class LMS {

    public static final String NS = "http://linkedmap.unizar.es/def/lms/";

    private static final Model m = ModelFactory.createDefaultModel();

    public static final Resource MapServer = m.createResource(NS
            + "MapServer");

    public static final Resource Layer = m.createResource(NS
            + "Layer");

    public static final Resource Operation = m.createResource(NS
            + "Operation");

    public static final Resource Domain = m.createResource(NS
            + "Domain");

    public static final Property onlineResource = m.createProperty(NS
            + "onlineResource");
    
    public static final Property sparqlEndpoint = m.createProperty(NS+"sparqlEndpoint");
    
    public static final Property defaultGraph = m.createProperty(NS+"defaultGraph");

    public static final Property hasOperation = m.createProperty(NS
            + "hasOperation");

    public static final Property hasParameter = m.createProperty(NS
            + "hasParameter");

    public static final Property allowedValue = m.createProperty(NS
            + "allowedValue");

    public static final Property canonical = m.createProperty(NS
            + "canonical");

    public static Property name = m.createProperty(NS
            + "name");

}
