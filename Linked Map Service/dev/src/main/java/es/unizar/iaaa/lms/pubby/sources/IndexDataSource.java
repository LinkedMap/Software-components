/**
 * Copyright (C) 2007 Richard Cyganiak (richard@cyganiak.de)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package es.unizar.iaaa.lms.pubby.sources;

import java.util.List;
import java.util.Map;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
 * A {@link DataSource} that wraps another data source and adds an index of the
 * resources in that data source.
 */
public class IndexDataSource implements DataSource {
    private final String indexIRI;
    private final DataSource wrapped;

    public IndexDataSource(String indexIRI, DataSource wrapped) {
        this.indexIRI = indexIRI;
        this.wrapped = wrapped;
    }

    @Override
    public boolean canDescribe(String absoluteIRI) {
        return indexIRI.equals(absoluteIRI) || wrapped.canDescribe(absoluteIRI);
    }

    @Override
    public Model describeResource(String iri) {
        if (!indexIRI.equals(iri))
            return wrapped.describeResource(iri);
        Model result = ModelFactory.createDefaultModel();
        result.setNsPrefix("sioc", SIOC_NS);
        result.setNsPrefix("rdfs", RDFS.getURI());
        Resource index = result.createResource(indexIRI);
        // TODO: Get label from the vocabulary store, and make it i18nable
        index.addProperty(RDFS.label, "Index of Resources", "en");
        for (Resource r : wrapped.getIndex()) {
            index.addProperty(siocContainerOf, r);
        }
        return result;
    }

    private final static String SIOC_NS = "http://rdfs.org/sioc/ns#";
    private final static Property siocContainerOf = ResourceFactory
        .createProperty(SIOC_NS + "container_of");

    @Override
    public Map<Property, Integer> getHighIndegreeProperties(String resourceIRI) {
        return wrapped.getHighIndegreeProperties(resourceIRI);
    }

    @Override
    public Map<Property, Integer> getHighOutdegreeProperties(String resourceIRI) {
        return wrapped.getHighOutdegreeProperties(resourceIRI);
    }

    /**
     * Describe the index resource, and extract all the statements that have our
     * property and the right subject/object.
     */
    @Override
    public Model listPropertyValues(String resourceIRI, Property property,
            boolean isInverse) {
        if (!indexIRI.equals(resourceIRI)) {
            return wrapped.listPropertyValues(resourceIRI, property, isInverse);
        }
        Model all = describeResource(resourceIRI);
        Resource r = all.getResource(resourceIRI);
        StmtIterator it = isInverse ? all.listStatements(null, property, r)
                : all.listStatements(r, property, (RDFNode) null);
        Model result = ModelFactory.createDefaultModel();
        while (it.hasNext()) {
            result.add(it.next());
        }
        return result;
    }

    @Override
    public List<Resource> getIndex() {
        // We could add the indexIRI as an additional resource here, but we
        // don't want it to show up in the list of resources generated in
        // describeResource(), and we don't want it turn an otherwise
        // empty dataset into a non-empty one.
        return wrapped.getIndex();
    }

}
