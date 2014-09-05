package es.unizar.iaaa.lms.store;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.sparql.vocabulary.FOAF;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.hp.hpl.jena.vocabulary.RDF;

import es.unizar.iaaa.lms.pubby.vocab.LMS;

public class RdfRepresentation extends RdfResource {

    public RdfRepresentation(String representationUri, String resourceUri, RdfConfiguration config) {
        super(representationUri, config, ModelFactory.createDefaultModel());
        Model model = resource.getModel();
        Resource other = model.createResource(resourceUri);
        model.add(resource, RDF.type, FOAF.Document).add(resource, FOAF.primaryTopic, other)
            .add(resource, FOAF.topic, other);
    }

    public boolean isSpecificRepresentation() {
        URI uri = URI.create(getCanonical());
        String path = uri.getPath();
        String name = new File(path).getName();
        return name.contains(".");
    }

    public void isFormatOf(String otherUri) {
        Model model = resource.getModel();
        Resource other = model.createResource(otherUri);
        if (!resource.equals(other)) {
            model.add(resource, DCTerms.isFormatOf, other);
        }
    }

    public void hasFormat(String otherUri) {
        if (otherUri == null)
            return;
        Model model = resource.getModel();
        Resource other = model.createResource(otherUri);
        model.add(resource, DCTerms.hasFormat, other);
    }

    public String getCanonical() {
        Model model = resource.getModel();
        Statement st = model.getProperty(resource, LMS.canonical);
        return st == null ? uri : st.getObject().asResource().getURI();
    }

    public void setCanonical(String other) {
        if (other != null) {
            Model model = resource.getModel();
            model.add(resource, LMS.canonical, model.createResource(other));
        }
    }

    public String getPrimaryTopic() {
        return resource.getProperty(FOAF.primaryTopic).getObject().asResource().getURI();
    }

    public List<String> getHasFormats() {
        List<String> list = new ArrayList<String>();
        for (Statement st : resource.listProperties(DCTerms.hasFormat).toList()) {
            list.add(st.getObject().asResource().getURI());
        }
        return list;
    }

}
