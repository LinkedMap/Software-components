package es.unizar.iaaa.lms.store;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;

import es.unizar.iaaa.lms.pubby.VocabularyStore;

public class RdfPropertyBuilder {
    /**
     * 
     */
    private final RdfResource resourceDescription;
    private final Property predicate;
    private final boolean isInverse;
    private final List<RdfValue> values = new ArrayList<RdfValue>();
    private final List<RdfResource> blankNodeDescriptions = new ArrayList<RdfResource>();
    private int highDegreeArcCount = 0;
    private VocabularyStore vocabularyStore;

    RdfPropertyBuilder(RdfResource resourceDescription, Property predicate, boolean isInverse,
            VocabularyStore vocabularyStore) {
        this.resourceDescription = resourceDescription;
        this.predicate = predicate;
        this.isInverse = isInverse;
        this.vocabularyStore = vocabularyStore;
    }

    public void addValue(RDFNode valueNode) {
        if (valueNode.isAnon()) {
            blankNodeDescriptions.add(new RdfResource(valueNode
                .asResource().getURI(),  this.resourceDescription.config, this.resourceDescription.getModel()));
            return;
        }
        values.add(new RdfValue(this.resourceDescription, valueNode, predicate, vocabularyStore));
    }

    void addHighDegreeArcs(int count) {
        highDegreeArcCount += count;
    }

    public RdfResourceProperty toProperty() {
        Collections.sort(values);
        return new RdfResourceProperty(this.resourceDescription, predicate, isInverse, values,
            blankNodeDescriptions, highDegreeArcCount, vocabularyStore);
    }
}
