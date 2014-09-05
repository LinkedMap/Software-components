package es.unizar.iaaa.lms.core.domain;

import com.hp.hpl.jena.rdf.model.Model;

import es.unizar.iaaa.lms.store.RdfConfiguration;
import es.unizar.iaaa.lms.store.RdfResource;

public class Layer extends RdfResource {

    public Layer(String uri, RdfConfiguration config,  Model model) {
        super(uri, config, model);
    }

}
