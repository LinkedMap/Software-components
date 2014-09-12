/**
 * This file is part of Linked Map Service (LMS).
 *
 * Linked Map Service (LMS) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Linked Map Service (LMS) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Linked Map Service (LMS).  If not, see <http://www.gnu.org/licenses/>.
 */
package es.unizar.iaaa.lms.store;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDF;

import es.unizar.iaaa.lms.pubby.URIPrefixer;
import es.unizar.iaaa.lms.pubby.VocabularyStore;

public class RdfValue implements Comparable<RdfValue> {
    /**
     * 
     */
    private final RdfResource resourceDescription;
    private final RDFNode node;
    private URIPrefixer prefixer;
    private Property predicate;
    private VocabularyStore vocabularyStore;

    public RdfValue(RdfResource resourceDescription, RDFNode valueNode, Property predicate,
            VocabularyStore vocabularyStore) {
        this.resourceDescription = resourceDescription;
        this.node = valueNode;
        this.predicate = predicate;
        this.vocabularyStore = vocabularyStore;
        if (valueNode.isURIResource()) {
            prefixer = new URIPrefixer(
                valueNode.as(Resource.class), this.resourceDescription.getPrefixes());
        }
    }

    public Node getNode() {
        return node.asNode();
    }

    public String getBrowsableURL() {
        return null;
//      FIXME
//        if (!node.isURIResource())
//            return null;
//        HypermediaControls controls = HypermediaControls.createFromIRI(node
//            .asResource().getURI(), this.resourceDescription.config);
//        if (controls == null)
//            return node.asResource().getURI();
//        return controls.getPageURL();
    }

    public boolean hasPrefix() {
        return prefixer != null && prefixer.hasPrefix();
    }

    public String getPrefix() {
        if (prefixer == null) {
            return null;
        }
        return prefixer.getPrefix();
    }

    public String getLocalName() {
        if (prefixer == null) {
            return null;
        }
        return prefixer.getLocalName();
    }

    public String getLabel() {
        if (!node.isResource())
            return null;
        Literal result = null;
        if (node.isURIResource()) {
            if (predicate.equals(RDF.type)) {
                // Look up class labels in vocabulary store
                result = vocabularyStore.getLabel(node.asNode().getURI(),
                    false);
            } else if (node.isURIResource()) {
                // If it's not a class, see if we happen to have a label
                // cached
                result = vocabularyStore.getCachedLabel(node.asResource()
                    .getURI(), false);
            }
        }
        if (result == null) {
            // Use any label that may be included in the description model
            result = new RdfResource(node.asResource().getURI(), 
                this.resourceDescription.config, this.resourceDescription.model).getLabel();
        }
        if (result == null)
            return null;
        return this.resourceDescription.toTitleCase(result.getLexicalForm(), result.getLanguage());
    }

    public String getDescription() {
        return vocabularyStore.getDescription(node.asNode().getURI())
            .getLexicalForm();
    }

    public String getDatatypeLabel() {
        if (!node.isLiteral())
            return null;
        String uri = node.as(Literal.class).getDatatypeURI();
        if (uri == null)
            return null;
        URIPrefixer datatypePrefixer = new URIPrefixer(uri, this.resourceDescription.getPrefixes());
        if (datatypePrefixer.hasPrefix()) {
            return datatypePrefixer.toTurtle();
        } else {
            return "?:" + datatypePrefixer.getLocalName();
        }
    }

    public boolean isType() {
        return predicate.equals(RDF.type);
    }

    @Override
    public int compareTo(RdfValue other) {
        if (!(other instanceof RdfValue)) {
            return 0;
        }
        RdfValue otherValue = other;
        if (getNode().isURI() && otherValue.getNode().isURI()) {
            return getNode().getURI().compareTo(
                otherValue.getNode().getURI());
        }
        if (getNode().isURI()) {
            return 1;
        }
        if (otherValue.getNode().isURI()) {
            return -1;
        }
        if (getNode().isBlank() && otherValue.getNode().isBlank()) {
            return getNode().getBlankNodeLabel().compareTo(
                otherValue.getNode().getBlankNodeLabel());
        }
        if (getNode().isBlank()) {
            return 1;
        }
        if (otherValue.getNode().isBlank()) {
            return -1;
        }
        // TODO Typed literals, language literals
        return getNode().getLiteralLexicalForm().compareTo(
            otherValue.getNode().getLiteralLexicalForm());
    }
}
