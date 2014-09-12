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
package es.unizar.iaaa.lms.aat;

import static org.springframework.test.util.AssertionErrors.assertTrue;

import java.io.StringReader;

import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.sparql.vocabulary.FOAF;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.hp.hpl.jena.vocabulary.OWL2;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

public class ResourceResultMatcher {

    private String resource;

    private String base;

    private String lang;

    public ResourceResultMatcher(String resource, String base, String lang) {
        this.resource = resource;
        this.base = base;
        this.lang = lang;
    }

    public ResultMatcher isDocument() {
        return new ResultMatcher() {

            @Override
            public void match(MvcResult result) throws Exception {
                Model m = ModelFactory.createDefaultModel();
                m.read(new StringReader(result.getResponse()
                    .getContentAsString()), base, lang);
                assertTrue("Resource " + resource
                        + " is not declared as foaf:Document",
                    m.contains(m.createResource(resource), RDF.type,
                        FOAF.Document));
            }

        };
    }

    public ResultMatcher describes(final String describedResource) {
        return new ResultMatcher() {

            @Override
            public void match(MvcResult result) throws Exception {
                Model m = ModelFactory.createDefaultModel();
                m.read(new StringReader(result.getResponse()
                    .getContentAsString()), base, lang);
                assertTrue(
                    "Resource " + resource + " not related with "
                            + describedResource + " with foaf:topic",
                    m.contains(m.createResource(resource), FOAF.topic,
                        m.createResource(describedResource)));
                assertTrue(
                    "Resource " + resource + " not related with "
                            + describedResource + " with foaf:primaryTopic",
                    m.contains(m.createResource(resource), FOAF.primaryTopic,
                        m.createResource(describedResource)));
            }

        };
    }

    public ResultMatcher isFormatOf(final String document) {
        return new ResultMatcher() {

            @Override
            public void match(MvcResult result) throws Exception {
                Model m = ModelFactory.createDefaultModel();
                m.read(new StringReader(result.getResponse()
                    .getContentAsString()), base, lang);
                assertTrue(
                    "Resource " + resource + " not related with "
                            + document + " with dct:isFormatOf",
                    m.contains(m.createResource(resource), DCTerms.isFormatOf,
                        m.createResource(document)));
            }

        };
    }

    public ResultMatcher notHasFormat() {
        return new ResultMatcher() {

            @Override
            public void match(MvcResult result) throws Exception {
                Model m = ModelFactory.createDefaultModel();
                m.read(new StringReader(result.getResponse()
                    .getContentAsString()), base, lang);
                assertTrue(
                    "Resource " + resource
                            + " has a statement with dct:hasFormat",
                    !m.contains(m.createResource(resource), DCTerms.hasFormat));
            }

        };
    }

    public ResultMatcher hasFormat(final String representation) {
        return new ResultMatcher() {

            @Override
            public void match(MvcResult result) throws Exception {
                Model m = ModelFactory.createDefaultModel();
                m.read(new StringReader(result.getResponse()
                    .getContentAsString()), base, lang);
                assertTrue(
                    "Resource " + resource + " not related with "
                            + representation + " with dct:hasFormat",
                    m.contains(m.createResource(resource), DCTerms.hasFormat,
                        m.createResource(representation)));
            }

        };
    }

    public ResultMatcher notIsFormatOf() {
        return new ResultMatcher() {

            @Override
            public void match(MvcResult result) throws Exception {
                Model m = ModelFactory.createDefaultModel();
                m.read(new StringReader(result.getResponse()
                    .getContentAsString()), base, lang);
                assertTrue(
                    "Resource " + resource
                            + " has a statement with dct:isFormatOf",
                    !m.contains(m.createResource(resource), DCTerms.isFormatOf));
            }

        };
    }

    public ResultMatcher hasProperty(final String property, final String object) {
        return new ResultMatcher() {

            @Override
            public void match(MvcResult result) throws Exception {
                Model m = ModelFactory.createDefaultModel();
                m.read(new StringReader(result.getResponse()
                    .getContentAsString()), base, lang);
                assertTrue(
                    "Resource " + resource + " not related with "
                            + object + " with " + property,
                    m.contains(m.createResource(resource),
                        m.createProperty(property),
                        m.createResource(object)));
            }

        };
    }

    public ResultMatcher isOntology() {
        return new ResultMatcher() {

            @Override
            public void match(MvcResult result) throws Exception {
                Model m = ModelFactory.createDefaultModel();
                m.read(new StringReader(result.getResponse()
                    .getContentAsString()), base, lang);
                assertTrue(
                    "Resource " + resource + " is not an Ontology",
                    m.contains(m.createResource(resource),
                        RDF.type, OWL2.Ontology));
            }

        };
    }

    public ResultMatcher isOWLClass() {
        return new ResultMatcher() {

            @Override
            public void match(MvcResult result) throws Exception {
                Model m = ModelFactory.createDefaultModel();
                m.read(new StringReader(result.getResponse()
                    .getContentAsString()), base, lang);
                assertTrue(
                    "Resource " + resource + " is not a owl:Class",
                    m.contains(m.createResource(resource),
                        RDF.type, OWL2.Class));
            }

        };
    }

    public ResultMatcher label(final String label, final String labelLang) {
        return new ResultMatcher() {

            @Override
            public void match(MvcResult result) throws Exception {
                Model m = ModelFactory.createDefaultModel();
                m.read(new StringReader(result.getResponse()
                    .getContentAsString()), base, lang);
                assertTrue(
                    "Resource " + resource + " does not have label '" + label
                            + "'@" + labelLang,
                    m.contains(m.createResource(resource),
                        RDFS.label, label, labelLang));
            }

        };
    }

    public ResultMatcher isDefinedBy(final String definition) {
        return new ResultMatcher() {

            @Override
            public void match(MvcResult result) throws Exception {
                Model m = ModelFactory.createDefaultModel();
                m.read(new StringReader(result.getResponse()
                    .getContentAsString()), base, lang);
                assertTrue(
                    "Resource " + resource + " is not defined by " + definition,
                    m.contains(m.createResource(resource),
                        RDFS.isDefinedBy, m.createResource(definition)));
            }

        };
    }

}
