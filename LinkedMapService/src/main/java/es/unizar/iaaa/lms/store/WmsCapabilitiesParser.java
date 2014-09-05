package es.unizar.iaaa.lms.store;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.apache.log4j.Logger;
import org.springframework.http.InvalidMediaTypeException;
import org.springframework.http.MediaType;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.DCTerms;

import es.unizar.iaaa.lms.pubby.vocab.LDP;
import es.unizar.iaaa.lms.pubby.vocab.LMS;
import es.unizar.iaaa.lms.util.XPathHelper;

public class WmsCapabilitiesParser {

    private String base;
    private String slug;
    private Resource server;
    private Model serverModel;
    private Resource servers;
    private Model serversModel;
    private Resource layers;
    private Model layersModel;
    private List<String> layerList;
    private List<Model> layerListModels;
    private static Logger log = Logger.getLogger(WmsCapabilitiesParser.class);

    public WmsCapabilitiesParser(String base, String slug) {
        this.base = base;
        this.slug = slug;
        String containerId = base + "id/servers";
        serversModel = ModelFactory.createDefaultModel();
        servers = serversModel.createResource(containerId, LDP.Container);

        serverModel = ModelFactory.createDefaultModel();
        server = serverModel.createResource(containerId + "/" + slug, LMS.MapServer);
        serverModel.add(servers, LDP.contains, server);

    }

    public void parseDocument(InputStream is) throws Exception {
        XPathHelper helper;
        try {
            helper = new XPathHelper("//HTTP//@href", null);
            Document document = helper.parseXmlInputStream(is);
            String url = helper.evaluateXpath(document, XPathConstants.STRING,
                String.class);
            Resource onlineResource = serverModel.createResource(url);
            serverModel.add(server, LMS.onlineResource, onlineResource);
            
            processService(document);
            processOperation("GetCapabilities", document);
            processOperation("GetMap", document);
            processOperation("GetFeatureInfo", document);
            processLayers(document);
        } catch (Exception ex) {
            throw new Exception("Error reading " + getServiceId(), ex);
        }
    }

    public void addSparqlEndpoint(String url){
    	Resource sparqlEndpoint=serverModel.createResource(url);
        serverModel.add(server,LMS.sparqlEndpoint,sparqlEndpoint);
    }
    
    public void addDefaultGraph(String uri){
    	Resource defaultGraph=serverModel.createResource(uri);
        serverModel.add(server,LMS.defaultGraph,defaultGraph);
    }
    
    public String getServiceId() {
        return server.getURI();
    }

    public Model getServiceModel() {
        return serverModel;
    }

    public String getServerContainerId() {
        return servers.getURI();
    }

    public Model getServerContainerModel() {
        return serversModel;
    }

    protected void processService(Document document) throws XPathExpressionException {
        serverModel.add(server, DCTerms.title, extractString(document, "//Service/Title/text()"));
        serverModel.add(server, DCTerms.description, extractString(document, "//Service/Abstract/text()"));
    }

    protected String extractString(Node node, String path) throws XPathExpressionException {
        XPathHelper helper;
        helper = new XPathHelper(path, null);
        String title = helper.evaluateXpath(node,
            XPathConstants.STRING,
            String.class);
        return title;
    }

    protected void processLayers(Document document) throws XPathExpressionException {
        XPathHelper helper;
        helper = new XPathHelper("//Layer", null);
        NodeList namedLayers = helper.evaluateXpath(document,
            XPathConstants.NODESET,
            NodeList.class);

        String layerId = base + "id/layers";
        layersModel = ModelFactory.createDefaultModel();
        layers = layersModel.createResource(layerId, LDP.Container);

        layerList = new ArrayList<String>();
        layerListModels = new ArrayList<Model>();
        for (int i = 0; i < namedLayers.getLength(); i++) {

            Model layerModel = ModelFactory.createDefaultModel();
            Resource layer = layerModel.createResource(layerId + "/" + slug + "_" + i, LMS.Layer);
            layerModel.add(layers, LDP.contains, layer);
            addIfNotNull(layer, DCTerms.title, namedLayers.item(i), "Title/text()");
            addIfNotNull(layer, DCTerms.description, namedLayers.item(i), "Abstract/text()");
            addIfNotNull(layer, DCTerms.identifier, namedLayers.item(i), "Name/text()");
            layerList.add(layer.getURI());
            layerListModels.add(layerModel);
        }
    }

    protected void addIfNotNull(Resource resource, Property property, Node node, String xpath)
            throws XPathExpressionException {
        String value = extractString(node, xpath);
        if (value != null && value.trim().length() > 0) {
            resource.getModel().add(resource, property, value);
        }
    }

    protected void processOperation(String name,
            Document document) throws XPathExpressionException {
        XPathHelper helper;
        helper = new XPathHelper("//" + name, null);
        Node operationNode = helper.evaluateXpath(document,
            XPathConstants.NODE,
            Node.class);

        Resource operation = serverModel.createResource(LMS.Operation);
        serverModel.add(operation, LMS.name, name);
        serverModel.add(server, LMS.hasOperation, operation);

        helper = new XPathHelper("Format/text()", null);
        NodeList formats = helper.evaluateXpath(operationNode,
            XPathConstants.NODESET,
            NodeList.class);
        Resource parameter = serverModel.createResource(LMS.Domain);
        serverModel.add(parameter, LMS.name, "FORMAT");
        serverModel.add(operation, LMS.hasParameter, parameter);
        for (int i = 0; i < formats.getLength(); i++) {
            try {
                serverModel.add(parameter, LMS.allowedValue, MediaType
                    .parseMediaType(formats.item(i).getTextContent())
                    .toString());
            } catch (InvalidMediaTypeException e) {
                log.info("Can't parse " + formats.item(i).getTextContent()
                        + " in operation " + name);
            }
        }
        serverModel.write(System.err, "TURTLE");
    }

    public String getLayerContainerId() {
        return layers.getURI();
    }

    public Model getLayerContainerModel() {
        return layersModel;
    }

    public List<String> getLayerIds() {
        return layerList;
    }

    public List<Model> getLayerModels() {
        return layerListModels;
    }

}
