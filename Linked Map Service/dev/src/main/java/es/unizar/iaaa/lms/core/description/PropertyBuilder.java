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
package es.unizar.iaaa.lms.core.description;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;

import es.unizar.iaaa.lms.pubby.VocabularyStore;

public class PropertyBuilder {
    /**
     * 
     */
    private final ResourceDescription resourceDescription;
    private final Property predicate;
    private final boolean isInverse;
    private final List<Value> values = new ArrayList<Value>();
    private final List<ResourceDescription> blankNodeDescriptions = new ArrayList<ResourceDescription>();
    private int highDegreeArcCount = 0;
    private VocabularyStore vocabularyStore;

    PropertyBuilder(ResourceDescription resourceDescription, Property predicate, boolean isInverse,
            VocabularyStore vocabularyStore) {
        this.resourceDescription = resourceDescription;
        this.predicate = predicate;
        this.isInverse = isInverse;
        this.vocabularyStore = vocabularyStore;
    }

    public void addValue(RDFNode valueNode) {
        if (valueNode.isAnon()) {
            blankNodeDescriptions.add(new ResourceDescription(valueNode
                .asResource(), this.resourceDescription.getModel(), this.resourceDescription.config));
            return;
        }
        values.add(new Value(this.resourceDescription, valueNode, predicate, vocabularyStore));
    }

    void addHighDegreeArcs(int count) {
        highDegreeArcCount += count;
    }

    public ResourceProperty toProperty() {
        Collections.sort(values);
        return new ResourceProperty(this.resourceDescription, predicate, isInverse, values,
            blankNodeDescriptions, highDegreeArcCount, vocabularyStore);
    }
}
