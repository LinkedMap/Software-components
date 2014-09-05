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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import com.hp.hpl.jena.n3.IRIResolver;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.shared.JenaException;
import com.hp.hpl.jena.util.FileManager;

import es.unizar.iaaa.lms.pubby.sources.DataSource;
import es.unizar.iaaa.lms.pubby.sources.FilteredDataSource;
import es.unizar.iaaa.lms.pubby.sources.ModelDataSource;
import es.unizar.iaaa.lms.pubby.sources.RemoteSPARQLDataSource;
import es.unizar.iaaa.lms.pubby.sources.RewrittenDataSource;
import es.unizar.iaaa.lms.pubby.sources.StrabonRemoteSPARQLDataSource;
import es.unizar.iaaa.lms.pubby.vocab.CONF;
import es.unizar.iaaa.lms.pubby.vocab.LDP;
import es.unizar.iaaa.lms.pubby.vocab.LMS;
import es.unizar.iaaa.lms.store.WmsCapabilitiesParser;

/**
 * A dataset block in the server's configuration.
 */
public class Dataset extends ResourceReader {
    private final DataSource dataSource;
    private final MetadataConfiguration metadata;
    private final ApplicationContext context;
    private static final Logger LOG = LoggerFactory.getLogger(Dataset.class);

    // TODO: This is a rather dirty hack. We may need DataSource.getProvenance()
    // or something
    private RemoteSPARQLDataSource sparqlDataSource = null;

    public Dataset(Resource dataset, PubbyRepository configuration,
            ApplicationContext ctx) {
        super(dataset);
        context = ctx;
        dataSource = buildDataSource(configuration, ctx);
        metadata = new MetadataConfiguration(dataset, sparqlDataSource);
    }

    public ApplicationContext getContext() {
        return context;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public MetadataConfiguration getMetadataConfiguration() {
        return metadata;
    }

    public boolean supportsIRIs() {
        return getBoolean(CONF.supportsIRIs, true);
    }

    public boolean addSameAsStatements() {
        return getBoolean(CONF.addSameAsStatements, false);
    }

    public boolean supportsSPARQL11() {
        return getBoolean(CONF.supportsSPARQL11, false);
    }

    /**
     * Gets all values of <tt>conf:browsableNamespace</tt> declared on the
     * dataset resource. Does not include values inherited from the
     * configuration resource.
     * 
     * @return Namespace IRIs of browsable namespaces
     */
    public Set<String> getBrowsableNamespaces() {
        return getIRIs(CONF.browsableNamespace);
    }

    private DataSource buildDataSource(PubbyRepository configuration,
            ApplicationContext ctx) {
        requireExactlyOneOf(CONF.strabonSparqlEndpoint, CONF.loadRDF, CONF.loadWMS);
        

        DataSource result;
        if (hasProperty(CONF.strabonSparqlEndpoint)) {

            // SPARQL data source
        	//getProperties(p)
        	HashMap<String, String> hm=new HashMap<String, String>();
        	if(getResource(CONF.distributedSparqlEndpoints)!=null){
        	Iterator<Statement> it=getResource(CONF.distributedSparqlEndpoints).listProperties();
        	while(it.hasNext()){
        		Statement st=it.next();
        		
        		String object=st.asTriple().getObject().toString();
        		String predicate=st.asTriple().getPredicate().toString();
        		hm.put(predicate, object);
        	}
        	}
            String endpointURL = getIRI(CONF.strabonSparqlEndpoint);
            String defaultGraphURI = getIRI(CONF.sparqlDefaultGraph);
            sparqlDataSource = new StrabonRemoteSPARQLDataSource(endpointURL,
                defaultGraphURI, supportsSPARQL11(),
                getStrings(CONF.resourceDescriptionQuery),
                getStrings(CONF.propertyListQuery),
                getStrings(CONF.inversePropertyListQuery),
                getStrings(CONF.anonymousPropertyDescriptionQuery),
                getStrings(CONF.anonymousInversePropertyDescriptionQuery),
                configuration.getVocabularyStore()
                    .getHighIndegreeProperties(), configuration
                    .getVocabularyStore().getHighOutdegreeProperties(),hm);
            if (hasProperty(CONF.contentType)) {
                sparqlDataSource
                    .setGraphContentType(getString(CONF.contentType));
            }
            for (String param : getStrings(CONF.queryParamSelect)) {
                sparqlDataSource.addSelectQueryParam(param);
            }
            for (String param : getStrings(CONF.queryParamGraph)) {
                sparqlDataSource.addGraphQueryParam(param);
            }
            result = sparqlDataSource;

        }
        else if (hasProperty(CONF.sparqlEndpoint)&& !hasProperty(CONF.loadWMS)) {

            // SPARQL data source
            String endpointURL = getIRI(CONF.sparqlEndpoint);
            String defaultGraphURI = getIRI(CONF.sparqlDefaultGraph);
            sparqlDataSource = new RemoteSPARQLDataSource(endpointURL,
                defaultGraphURI, supportsSPARQL11(),
                getStrings(CONF.resourceDescriptionQuery),
                getStrings(CONF.propertyListQuery),
                getStrings(CONF.inversePropertyListQuery),
                getStrings(CONF.anonymousPropertyDescriptionQuery),
                getStrings(CONF.anonymousInversePropertyDescriptionQuery),
                configuration.getVocabularyStore()
                    .getHighIndegreeProperties(), configuration
                    .getVocabularyStore().getHighOutdegreeProperties());
            if (hasProperty(CONF.contentType)) {
                sparqlDataSource
                    .setGraphContentType(getString(CONF.contentType));
            }
            for (String param : getStrings(CONF.queryParamSelect)) {
                sparqlDataSource.addSelectQueryParam(param);
            }
            for (String param : getStrings(CONF.queryParamGraph)) {
                sparqlDataSource.addGraphQueryParam(param);
            }
            result = sparqlDataSource;

        } else {

            // File data source
            Model data = ModelFactory.createDefaultModel();
            for (String fileName : getIRIs(CONF.loadRDF)) {
                // If the location is a local file, then use webBase as base URI
                // to resolve relative URIs in the file. Having file:/// URIs in
                // there would likely not be useful to anyone.
                fileName = IRIResolver.resolveGlobal(fileName);
                String base = (fileName.startsWith("file:/") ? configuration
                    .getWebApplicationBaseURI() : fileName);

                loadRDF(data, fileName, base, ctx);
            }

            if (getIRIs(CONF.loadWMS).size() > 0) {
                data.add(createContainer(getIRI(CONF.datasetBase)
                        + "id/servers"));
            }

            for (String fileName : getIRIs(CONF.loadWMS)) {
                // If the location is a local file, then use webBase as base URI
                // to resolve relative URIs in the file. Having file:/// URIs in
                // there would likely not be useful to anyone.
                fileName = IRIResolver.resolveGlobal(fileName);

                String slug = new File(fileName).getName().split("\\.")[0];
                WmsCapabilitiesParser parser = new WmsCapabilitiesParser(getIRI(CONF.datasetBase), slug);
                try {
                    InputStream is = ctx.getResource(fileName).getInputStream();
                    parser.parseDocument(is);
                    
                    String sparqlEndpoint=getIRI(CONF.sparqlEndpoint);
                    if(sparqlEndpoint!=null){
                    	parser.addSparqlEndpoint(sparqlEndpoint);
                    }

                    String defaultGraph=getIRI(CONF.defaultGraph);
                    if(defaultGraph!=null){
                    	parser.addDefaultGraph(defaultGraph);
                    }
                    
                    
                    data.add(parser.getServiceModel());
                    data.add(parser.getServerContainerModel());
                    
                	
                    is.close();
                } catch (Exception ex) {
                    throw new PubbyRepositoryException("Error reading <"
                            + fileName + ">: " + ex.getMessage());

                }
            }

            result = new ModelDataSource(data);
            
        }

        // If conf:datasetURIPattern is set, then filter the dataset
        // accordingly.
        if (hasProperty(CONF.datasetURIPattern)) {
            final Pattern pattern = Pattern
                .compile(getString(CONF.datasetURIPattern));
            result = new FilteredDataSource(result) {
                @Override
                public boolean canDescribe(String absoluteIRI) {
                    return pattern.matcher(absoluteIRI).find();
                }
            };
        }

        IRIRewriter rewriter = IRIRewriter.identity;

        // If conf:datasetBase is set (and different from conf:webBase),
        // rewrite the IRIs accordingly
        // Base IRI for IRIs considered to be "in" the data source
        String fullWebBase = configuration.getWebApplicationBaseURI()
                + configuration.getWebResourcePrefix();
        String datasetBase = getIRI(CONF.datasetBase, fullWebBase);
        if (!datasetBase.equals(fullWebBase)) {
        	/*rewriter = IRIRewriter.createNamespaceBased(datasetBase,
                    "http://linkedmap.unizar.es/lms/");*/
            rewriter = IRIRewriter.createNamespaceBased(datasetBase,
                fullWebBase);
        }

        // Escape special characters in IRIs
        // rewriter = IRIRewriter.chain(rewriter, new
        // PubbyIRIEscaper(fullWebBase,
        // !supportsIRIs()));

        result = new RewrittenDataSource(result, rewriter,
            addSameAsStatements());

        // Determine all browsable namespaces for this dataset
        final Set<String> browsableNamespaces = new HashSet<String>();
        browsableNamespaces.add(fullWebBase);
        for (String iri : getBrowsableNamespaces()) {
            browsableNamespaces.add(iri);
        }
        browsableNamespaces.addAll(configuration.getBrowsableNamespaces());

        // Filter the dataset to keep only those resources in the datasetBase
        // and in browsable namespaces, unless it's an annotation provider
        if (!hasType(CONF.AnnotationProvider)) {
            result = new FilteredDataSource(result) {
                @Override
                public boolean canDescribe(String absoluteIRI) {
                    for (String namespace : browsableNamespaces) {
                        if (absoluteIRI.startsWith(namespace))
                           return true;
                    }
                    return false;
                }

            };
        }

        return result;
    }

    // private Model loadWMS(Model model, String src, String uri,
    // ApplicationContext ctx) {
    // Resource container = model.createResource(uri, LDP.Container);
    // String slug = new File(src).getName().split("\\.")[0];
    // Resource server = model.createResource(uri + "/" + slug, LMS.MapServer);
    // model.add(container, LDP.contains, server);
    //
    // XPathHelper helper;
    // try {
    // helper = new XPathHelper("//HTTP//@href", null);
    // Document document = helper.parseXmlInputStream(ctx.getResource(src)
    // .getInputStream());
    // String url = helper.evaluateXpath(document, XPathConstants.STRING,
    // String.class);
    //
    // Resource onlineResource = model.createResource(url);
    // model.add(server, LMS.onlineResource, onlineResource);
    //
    // processOperation("GetCapabilities", model, server, document);
    // processOperation("GetMap", model, server, document);
    // processOperation("GetFeatureInfo", model, server, document);
    //
    // } catch (Exception ex) {
    // throw new PubbyRepositoryException("Error reading <"
    // + src + ">: " + ex.getMessage());
    // }
    //
    // return model;
    // }
    //
    // protected void processOperation(String name, Model model, Resource
    // server,
    // Document document) throws XPathExpressionException {
    // XPathHelper helper;
    // helper = new XPathHelper("//" + name, null);
    // Node operationNode = helper.evaluateXpath(document,
    // XPathConstants.NODE,
    // Node.class);
    //
    // Resource operation = model.createResource(LMS.Operation);
    // model.add(operation, LMS.name, name);
    // model.add(server, LMS.hasOperation, operation);
    //
    // helper = new XPathHelper("Format/text()", null);
    // NodeList formats = helper.evaluateXpath(operationNode,
    // XPathConstants.NODESET,
    // NodeList.class);
    // Resource parameter = model.createResource(LMS.Domain);
    // model.add(parameter, LMS.name, "FORMAT");
    // model.add(operation, LMS.hasParameter, parameter);
    // for (int i = 0; i < formats.getLength(); i++) {
    // try {
    // model.add(parameter, LMS.allowedValue, MediaType
    // .parseMediaType(formats.item(i).getTextContent())
    // .toString());
    // } catch (InvalidMediaTypeException e) {
    // LOG.info("Can't parse " + formats.item(i).getTextContent()
    // + " in operation " + name);
    // }
    // }
    // }

    private Model createContainer(String uri) {
        Model m = ModelFactory.createDefaultModel();
        m.createResource(uri, LDP.Container);
        return m;
    }

    protected void loadRDF(Model model, String src, String base,
            ApplicationContext ctx) {
        try {
            Model m;
            if (ctx.getResource(src).exists()) {
                m = ModelFactory.createDefaultModel();
                m.read(ctx.getResource(src).getInputStream(),
                    base, "TURTLE");
            } else {
                m = FileManager.get().loadModel(src, base, null);
            }
            model.add(m);

            // We'd like to do simply data.setNsPrefix(m), but that
            // leaves relative
            // namespace URIs like <#> unresolved, so we do a big dance
            // to make them
            // absolute.
            for (String prefix : m.getNsPrefixMap().keySet()) {
                String uri = IRIResolver.resolve(m.getNsPrefixMap()
                    .get(prefix), base);
                model.setNsPrefix(prefix, uri);
            }
        } catch (JenaException ex) {
            throw new PubbyRepositoryException("Error reading <"
                    + src + ">: " + ex.getMessage());
        } catch (IOException ex) {
            throw new PubbyRepositoryException("Error reading <"
                    + src + ">: " + ex.getMessage());
        }
    }
}
