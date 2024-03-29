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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;

import es.unizar.iaaa.lms.pubby.ModelUtil;

/**
 * A {@link DataSource} that wraps another data source in such a way that the
 * resulting data source is only capable of describing a subset of the IRI
 * space. This is usually done for performance, to prevent the underlying data
 * source from attempting to describe resources that we know it doesn't have
 * anything of value about.
 */
public abstract class FilteredDataSource implements DataSource {
    private final DataSource wrapped;

    public FilteredDataSource(DataSource wrapped) {
        this.wrapped = wrapped;
    }

    /**
     * Determines whether a given IRI is considered to be described in the
     * wrapped data source or not.
     * 
     * @param absoluteIRI
     *            Any syntactically valid IRI
     * @return <code>true</code> if that IRI is described in the data source
     */
    @Override
    public abstract boolean canDescribe(String absoluteIRI);

    @Override
    public Model describeResource(String iri) {
        if (!canDescribe(iri))
            return ModelUtil.EMPTY_MODEL;
        return wrapped.describeResource(iri);
    }

    @Override
    public Map<Property, Integer> getHighIndegreeProperties(String resourceIRI) {
        if (!canDescribe(resourceIRI))
            return null;
        return wrapped.getHighIndegreeProperties(resourceIRI);
    }

    @Override
    public Map<Property, Integer> getHighOutdegreeProperties(String resourceIRI) {
        if (!canDescribe(resourceIRI))
            return null;
        return wrapped.getHighOutdegreeProperties(resourceIRI);
    }

    @Override
    public Model listPropertyValues(String resourceIRI, Property property,
            boolean isInverse) {
        if (!canDescribe(resourceIRI))
            return ModelUtil.EMPTY_MODEL;
        return wrapped.listPropertyValues(resourceIRI, property, isInverse);
    }

    @Override
    public List<Resource> getIndex() {
        List<Resource> result = new ArrayList<Resource>();
        for (Resource r : wrapped.getIndex()) {
            if (canDescribe(r.getURI())) {
                result.add(r);
            }
        }
        return result;
    }

}
