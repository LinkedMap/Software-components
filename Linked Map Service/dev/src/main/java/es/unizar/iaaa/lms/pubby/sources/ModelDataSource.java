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

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

/**
 * A data source backed by a Jena model.
 */
public class ModelDataSource implements DataSource {
    private Model model;

    public ModelDataSource(Model model) {
        this.model = model;
    }

    @Override
    public boolean canDescribe(String absoluteIRI) {
        return true;
    }

    @Override
    public Model describeResource(String resourceURI) {
    	try{
    		Resource r = ResourceFactory.createResource(resourceURI);
    		Query query = QueryFactory.create("DESCRIBE <" + resourceURI + ">");
    		QueryExecution qexec = QueryExecutionFactory.create(query, model);
    		Model resultModel = qexec.execDescribe();
    		qexec.close();
    		return resultModel;
    	}catch(Exception e){
    		Model model = ModelFactory.createDefaultModel();
    		return model;
    	}
    }

    @Override
    public Map<Property, Integer> getHighIndegreeProperties(String resourceURI) {
        return null;
    }

    @Override
    public Map<Property, Integer> getHighOutdegreeProperties(String resourceURI) {
        return null;
    }

    @Override
    public Model listPropertyValues(String resourceURI, Property property,
            boolean isInverse) {
        return model;
    }

    @Override
    public List<Resource> getIndex() {
        List<Resource> result = new ArrayList<Resource>();
        ResIterator subjects = model.listSubjects();
        while (subjects.hasNext() && result.size() < DataSource.MAX_INDEX_SIZE) {
            Resource r = subjects.next();
            if (r.isAnon())
                continue;
            result.add(r);
        }
        NodeIterator objects = model.listObjects();
        while (objects.hasNext() && result.size() < DataSource.MAX_INDEX_SIZE) {
            RDFNode o = objects.next();
            if (!o.isURIResource())
                continue;
            result.add(o.asResource());
        }
        return result;
    }

}
