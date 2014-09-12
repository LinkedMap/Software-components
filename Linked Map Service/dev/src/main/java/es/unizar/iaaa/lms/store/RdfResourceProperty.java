package es.unizar.iaaa.lms.store;

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

import java.util.List;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Property;

import es.unizar.iaaa.lms.pubby.URIPrefixer;
import es.unizar.iaaa.lms.pubby.VocabularyStore;

public class RdfResourceProperty implements Comparable<RdfResourceProperty> {
    /**
     * 
     */
    private final RdfResource resourceDescription;
    private final Property predicate;
    private final URIPrefixer predicatePrefixer;
    private final boolean isInverse;
    private final List<RdfValue> simpleValues;
    private final List<RdfResource> complexValues;
    private final int omittedValues;
    private final VocabularyStore vocabularyStore;

    public RdfResourceProperty(RdfResource resourceDescription, Property predicate, boolean isInverse,
            List<RdfValue> simpleValues,
            List<RdfResource> complexVaues, int omittedValues,
            VocabularyStore vocabularyStore) {
        this.resourceDescription = resourceDescription;
        this.predicate = predicate;
        this.predicatePrefixer = new URIPrefixer(predicate, this.resourceDescription.getPrefixes());
        this.isInverse = isInverse;
        this.simpleValues = simpleValues;
        this.complexValues = complexVaues;
        this.omittedValues = omittedValues;
        this.vocabularyStore = vocabularyStore;
    }

    public boolean isInverse() {
        return isInverse;
    }

    public String getURI() {
        return predicate.getURI();
    }

    public String getBrowsableURL() {
        return null;
//      FIXME
//        HypermediaControls controls = HypermediaControls.createFromIRI(
//            predicate.getURI(), this.resourceDescription.config);
//        if (controls == null)
//            return predicate.getURI();
//        return controls.getBrowsableURL();
    }

    public boolean hasPrefix() {
        return predicatePrefixer.hasPrefix();
    }

    public String getPrefix() {
        return predicatePrefixer.getPrefix();
    }

    public String getLocalName() {
        return predicatePrefixer.getLocalName();
    }

    public String getLabel() {
        return getLabel(isMultiValued());
    }

    public String getLabel(boolean preferPlural) {
        Literal label = vocabularyStore.getLabel(predicate.getURI(),
            preferPlural);
        if (label == null)
            return null;
        return this.resourceDescription.toTitleCase(label.getLexicalForm(), label.getLanguage());
    }

    public String getInverseLabel() {
        return getInverseLabel(isMultiValued());
    }

    public String getInverseLabel(boolean preferPlural) {
        Literal label = vocabularyStore.getInverseLabel(predicate.getURI(),
            preferPlural);
        if (label == null)
            return null;
        return this.resourceDescription.toTitleCase(label.getLexicalForm(), label.getLanguage());
    }

    /**
     * Note: This bypasses conf:showLabels, always assuming <code>true</code>
     * 
     * @return "Is Widget Of", "Widgets", "ex:widget", whatever is most
     *         appropriate
     */
    public String getCompleteLabel() {
        if (isInverse && getInverseLabel() != null) {
            return getInverseLabel();
        }
        String result;
        if (getLabel() != null) {
            result = getLabel();
        } else if (hasPrefix()) {
            result = getPrefix() + ":" + getLocalName();
        } else {
            result = "?:" + getLocalName();
        }
        return isInverse ? "Is " + result + " of" : result;
    }

    public String getDescription() {
        return vocabularyStore.getDescription(predicate.getURI())
            .getLexicalForm();
    }

    public List<RdfValue> getSimpleValues() {
        return simpleValues;
    }

    public List<RdfResource> getComplexValues() {
        return complexValues;
    }

    public boolean hasOnlySimpleValues() {
        return omittedValues == 0 && complexValues.isEmpty();
    }

    public int getValueCount() {
        return omittedValues + complexValues.size() + simpleValues.size();
    }

    public boolean isMultiValued() {
        return simpleValues.size() + omittedValues + complexValues.size() > 1;
    }

    public String getValuesPageURL() {
        return null;
//      FIXME
//        if (this.resourceDescription.hypermediaResource == null) {
//            return null;
//        }
//        return isInverse ? this.resourceDescription.hypermediaResource
//            .getInverseValuesPageURL(predicate) : this.resourceDescription.hypermediaResource
//            .getValuesPageURL(predicate);
    }

    @Override
    public int compareTo(RdfResourceProperty other) {
        if (!(other instanceof RdfResourceProperty)) {
            return 0;
        }
        RdfResourceProperty otherProperty = other;
        int myWeight = this.resourceDescription.config.getVocabularyStore().getWeight(predicate,
            isInverse);
        int otherWeight = this.resourceDescription.config.getVocabularyStore().getWeight(
            otherProperty.predicate, other.isInverse);
        if (myWeight < otherWeight)
            return -1;
        if (myWeight > otherWeight)
            return 1;
        String propertyLocalName = getLocalName();
        String otherLocalName = otherProperty.getLocalName();
        if (propertyLocalName.compareTo(otherLocalName) != 0) {
            return propertyLocalName.compareTo(otherLocalName);
        }
        if (this.isInverse() != otherProperty.isInverse()) {
            return (this.isInverse()) ? 1 : -1;
        }
        return 0;
    }

    public String roleLabel(boolean showLabels) {
        if (showLabels && isInverse() && getInverseLabel() != null) {
            return "inverseLabel";
        } else if (showLabels && getLabel() != null) {
            return "label";
        } else if (hasPrefix()) {
            return "prefixed";
        } else {
            return "none";
        }
    }
}

