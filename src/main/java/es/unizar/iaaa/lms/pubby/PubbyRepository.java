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
package es.unizar.iaaa.lms.pubby;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Set;

import javax.servlet.ServletContext;

import org.springframework.context.ApplicationContext;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.shared.impl.PrefixMappingImpl;
import com.hp.hpl.jena.sparql.vocabulary.FOAF;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.hp.hpl.jena.vocabulary.DC_11;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;
import com.hp.hpl.jena.vocabulary.XSD;

import es.unizar.iaaa.lms.pubby.sources.DataSource;
import es.unizar.iaaa.lms.pubby.sources.IndexDataSource;
import es.unizar.iaaa.lms.pubby.sources.MergeDataSource;
import es.unizar.iaaa.lms.pubby.sources.ModelDataSource;
import es.unizar.iaaa.lms.pubby.vocab.CONF;

/**
 * The server's configuration.
 */
public class PubbyRepository extends ResourceReader {

    public static PubbyRepository create(Model model, ApplicationContext actx,
            ServletContext sctx) throws NoSuchElementException, IOException {
        StmtIterator it = model.listStatements(null, RDF.type,
            CONF.Configuration);
        if (!it.hasNext()) {
            throw new IllegalArgumentException(
                "No resource with type conf:Configuration found in configuration file");
        }
        return new PubbyRepository(it.nextStatement().getSubject(), actx, sctx);
    }

    private final PrefixMapping prefixes;
    private final String webBase;
    private final Collection<Property> labelProperties;
    private final Collection<Property> commentProperties;
    private final Collection<Property> imageProperties;
    private final ArrayList<Dataset> datasets = new ArrayList<Dataset>();
    private final Map<String, Dataset> namedDatasets = new HashMap<String, Dataset>();
    private final VocabularyStore vocabularyStore = new VocabularyStore();
    private final DataSource dataSource;
    private final String indexIRI;
    private final Set<String> allBrowsableNamespaces = new HashSet<String>();

    public PubbyRepository(Resource configuration, ApplicationContext actx,
            ServletContext sctx) throws IOException {
        super(configuration);
        webBase = getRequiredIRI(CONF.webBase) + sctx.getContextPath() + "/";

        // Create datasets from conf:dataset
        for (Resource r : getResources(CONF.dataset)) {
            Dataset ds = new Dataset(r, this, actx);
            datasets.add(ds);
            if (!r.isAnon()) {
                namedDatasets.put(r.getURI(), ds);
            }
            allBrowsableNamespaces.addAll(ds.getBrowsableNamespaces());
        }
        allBrowsableNamespaces.add(getWebApplicationBaseURI()
                + getWebResourcePrefix());
        allBrowsableNamespaces.addAll(getBrowsableNamespaces());

        // Create datasets from conf:loadVocabularyFromURL
        for (String sourceURL : getIRIs(CONF.loadVocabulary)) {
            Model m = ModelFactory.createDefaultModel();
            Resource dummyDataset = m.createResource();
            dummyDataset.addProperty(CONF.loadRDF, m.createResource(sourceURL));
            dummyDataset.addProperty(RDF.type, CONF.AnnotationProvider);
            datasets.add(new Dataset(dummyDataset, this, actx));
        }

        labelProperties = getProperties(CONF.labelProperty);
        if (labelProperties.isEmpty()) {
            labelProperties.add(RDFS.label);
            labelProperties.add(DC_11.title);
            labelProperties.add(DCTerms.title);
            labelProperties.add(FOAF.name);
        }
        commentProperties = getProperties(CONF.commentProperty);
        if (commentProperties.isEmpty()) {
            commentProperties.add(RDFS.comment);
            commentProperties.add(DC_11.description);
            commentProperties.add(DCTerms.description);
        }
        imageProperties = getProperties(CONF.imageProperty);
        if (imageProperties.isEmpty()) {
            imageProperties.add(FOAF.depiction);
        }

        prefixes = new PrefixMappingImpl();
        if (hasProperty(CONF.usePrefixesFrom)) {
            for (String iri : getIRIs(CONF.usePrefixesFrom)) {
                if (actx.getResource(iri).exists()) {
                    Model m = ModelFactory.createDefaultModel();
                    m.read(actx.getResource(iri).getInputStream(), iri,
                        "TURTLE");
                    prefixes.setNsPrefixes(m);
                } else {
                    prefixes.setNsPrefixes(FileManager.get().loadModel(iri));
                }
            }
        } else {
            prefixes.setNsPrefixes(getModel());
        }
        if (prefixes.getNsURIPrefix(CONF.NS) != null) {
            prefixes.removeNsPrefix(prefixes.getNsURIPrefix(CONF.NS));
        }
        // If no prefix is defined for the RDF and XSD namespaces, set them,
        // unless that would overwrite something. This is the namespaces that
        // have syntactic sugar in Turtle.
        ModelUtil.addNSIfUndefined(prefixes, "rdf", RDF.getURI());
        ModelUtil.addNSIfUndefined(prefixes, "xsd", XSD.getURI());
        dataSource = buildDataSource();

        // Vocabulary data source contains our normal data sources plus
        // the configuration model, so that we can read labels etc from
        // the configuration file
        DataSource vocabularyDataSource = new MergeDataSource(
            new ModelDataSource(getModel()), getDataSource());
        vocabularyStore.setDataSource(vocabularyDataSource);
        vocabularyStore.setDefaultLanguage(getDefaultLanguage());

        // Sanity check to spot typical configuration problem
       /* if (dataSource.getIndex().isEmpty()) {
            throw new PubbyRepositoryException("The index is empty. "
                    + "Try adding conf:datasetBase to your datasets, "
                    + "check any conf:datasetURIPatterns, "
                    + "and check that all data sources actually contain data.");
        }*/
        String resourceBase = getWebApplicationBaseURI()
                + getWebResourcePrefix();
        if (hasProperty(CONF.indexResource)) {
            indexIRI = getIRI(CONF.indexResource);
            // Sanity check to spot typical configuration problem
            if (dataSource.describeResource(indexIRI).isEmpty()) {
                throw new PubbyRepositoryException("conf:indexResource <"
                        + indexIRI + "> not found in data sets. "
                        + "Try disabling the conf:indexResource to get "
                        + "a list of found resources.");
            }
        } else {
            indexIRI = resourceBase;
        }
    }

    private DataSource buildDataSource() {
        List<DataSource> sources = new ArrayList<DataSource>(datasets.size());
        for (Dataset dataset : datasets) {
            sources.add(dataset.getDataSource());
        }
        DataSource result = new MergeDataSource(sources, prefixes);
        // If we don't have an indexResource, and there is no resource
        // at the home URL in any of the datasets, then add an
        // index builder. It will be responsible for handling the
        // homepage/index resource.
        // TODO: Shouldn't we make the index data source available even if there
        // is an indexResource?
//        String indexIRI = webBase + getWebResourcePrefix();
//        if (!hasProperty(CONF.indexResource)
//                && result.describeResource(indexIRI).isEmpty()) {
//            result = new IndexDataSource(indexIRI, result);
//        }
        return result;
    }

    /**
     * A composite {@link DataSource} representing the merge of all datasets.
     */
    public DataSource getDataSource() {
        return dataSource;
    }

    /**
     * The <code>conf:dataset</code> blocks.
     */
    public List<Dataset> getDatasets() {
        return datasets;
    }

    /**
     * @param relativeRequestURI
     *            URI relative to the Pubby root (<code>conf:webBase</code>)
     * @param isRelativeToPubbyRoot
     *            If true, the IRI is relative to the Pubby root (
     *            <code>conf:webBase</code>); otherwise, the IRI is relative to
     *            some non-resource namespace such as <code>/page/</code>. The
     *            distinction matters if <code>conf:webResourcePrefix</code> is
     *            set.
     */
    public HypermediaControls getControls(String relativeRequestURI,
            boolean isRelativeToPubbyRoot) {
        String relativeIRI = IRIEncoder.toIRI(relativeRequestURI);
        if (isRelativeToPubbyRoot) {
            if (!relativeIRI.startsWith(getWebResourcePrefix()))
                return null;
        } else {
            // relativeIRI = relativeIRI
            // .substring(getWebResourcePrefix().length());
        }
        return HypermediaControls.createFromPubbyPath(relativeIRI, this);
    }

    public PrefixMapping getPrefixes() {
        return prefixes;
    }

    public Collection<Property> getLabelProperties() {
        return labelProperties;
    }

    public Collection<Property> getCommentProperties() {
        return commentProperties;
    }

    public Collection<Property> getImageProperties() {
        return imageProperties;
    }

    public String getDefaultLanguage() {
        return getString(CONF.defaultLanguage, "en");
    }

    /**
     * The "home" resource. If its IRI is not the web server base, then the web
     * server will redirect there.
     */
    public String getIndexIRI() {
        return indexIRI;
    }

    public String getProjectLink() {
        return getIRI(CONF.projectHomepage);
    }

    public String getProjectName() {
        return getString(CONF.projectName);
    }

    public String getWebApplicationBaseURI() {
        return webBase;
    }

    public String getWebResourcePrefix() {
        return getString(CONF.webResourcePrefix, "");
    }

    public boolean showLabels() {
        return getBoolean(CONF.showLabels, true);
    }

    public VocabularyStore getVocabularyStore() {
        return vocabularyStore;
    }

    /**
     * Gets all values of <tt>conf:browsableNamespace</tt> declared on the
     * configuration resource. Does not include values declared on specific
     * datasets.
     * 
     * @return Namespace IRIs of browsable namespaces
     */
    public Set<String> getBrowsableNamespaces() {
        return getIRIs(CONF.browsableNamespace);
    }

    public boolean isBrowsable(String iri) {
        for (String namespace : allBrowsableNamespaces) {
            if (iri.startsWith(namespace))
                return true;
        }
        return false;
    }

    public Dataset getNamedDataset(String name) {
        return namedDatasets.get(name);
    }

    public String findFirstNamedDataset(String uri) {
        for (Entry<String, Dataset> e : namedDatasets.entrySet()) {
            if (e.getValue().getDataSource().canDescribe(uri)) {
                return e.getKey();
            }
        }
        return null;
    }
}
