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
package es.unizar.iaaa.lms.web.controller;

import static es.unizar.iaaa.lms.util.Util.getHeaders;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.jena.atlas.json.JsonObject;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.geotools.geojson.geom.GeometryJSON;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.sparql.engine.http.HttpQuery;
import com.hp.hpl.jena.sparql.util.StringUtils;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

import es.unizar.iaaa.lms.core.endpoint.AbstractEndpoint;
import es.unizar.iaaa.lms.store.Datastore;
import es.unizar.iaaa.lms.store.RdfConfiguration;
import es.unizar.iaaa.lms.store.RdfRepresentation;
import es.unizar.iaaa.lms.store.RdfResource;
import es.unizar.iaaa.lms.store.ResourceComment;
import es.unizar.iaaa.lms.store.ResourceDetails;
import es.unizar.iaaa.lms.store.ResourceMapping;
import es.unizar.iaaa.lms.util.Link;
import es.unizar.iaaa.lms.web.view.HeadersAwareThymeleafView;

@Controller
public class CopyOfServiceController extends AbstractController {

    @Autowired
    private Datastore datastore;
    
    @Autowired
    private RdfConfiguration config;

    public ResponseEntity<String> data(HttpServletRequest request,
            HttpServletResponse response, boolean varying) {
        endpoint = configuration.findEndpoint(request);
        if (endpoint == null) {
            return notHandled(request, response);
        }
        if (!endpoint.accepts(request)) {
            return notAcceptable(endpoint, request, response, varying);
        }
        RdfResource description = endpoint.description(request, AbstractEndpoint.TEXT_TURTLE, config);
        if (description == null) {
            return notFound(endpoint, request, response, varying);
        }

        HttpHeaders headers = new HttpHeaders();
        RdfRepresentation lr = description.getRepresentation();
        headers
            .add("Link", new Link(lr.getResource().getURI()).toString());
        headers.add("Link", new Link(lr.getPrimaryTopic()).withAboutRel()
            .toString());
        headers.add("Link", new Link(lr.getCanonical()).withCanonicalRel()
            .toString());
        for (String al : lr.getHasFormats()) {
            headers.add("Link", new Link(al).withAlternateRel()
                .toString());
        }
        if (varying) {
            headers.add("Vary", "Accept");
        }

        mergeHeadersFromHttpServletResponse(headers, response);
        headers.setContentType(AbstractEndpoint.TEXT_TURTLE);
        endpoint.enrich(description, request, headers);

        StringWriter sw = new StringWriter();
        description.getModelWithRepresentation().write(sw, "TURTLE",
            request.getRequestURL().toString());
        return new ResponseEntity<String>(sw.toString(), linearize(headers),
            HttpStatus.OK);
    }

    @RequestMapping(
            value = { "/doc/{container}", "/doc/{container}/{resource}" },
            produces = "text/turtle")
    public ResponseEntity<String> dataImplicit(HttpServletRequest request,
            HttpServletResponse response) {
        return data(request, response, true);
    }

    @RequestMapping(value = { "/doc/{container}.ttl",
            "/doc/{container}/{resource}.ttl" }, produces = "text/turtle")
    public ResponseEntity<String> dataExplicit(HttpServletRequest request,
            HttpServletResponse response) {
        return data(request, response, false);
    }

    @RequestMapping(value = { "/doc/{container}",
            "/doc/{container}/{resource}" }, produces = "text/html")
    public String htmlImplicit(HttpServletRequest request,
            HttpServletResponse response, Model model)
            throws ServletException {
        return html(request, response, model, true);
    }

    @RequestMapping(value = { "/doc/{container}.html",
            "/doc/{container}/{resource}.html" }, produces = "text/html")
    public String htmlExplicit(HttpServletRequest request,
            HttpServletResponse response, Model model)
            throws ServletException {
        return html(request, response, model, false);
    }
    
    
       
    @RequestMapping(value = { "/doc/{container}.json",
    "/doc/{container}/{resource}.json" }, produces = "application/json")
    public ResponseEntity<?> jsonExplicit(HttpServletRequest request,
    		HttpServletResponse response, Model model)
    				throws Exception {
    	return json(request, response, false);
    }
    
   /* @RequestMapping(value = { "/doc/{container}.jpeg",
    "/doc/{container}/{resource}.jpeg" }, produces = "image/jpeg")
    public ResponseEntity<byte[]> jpegExplicit(HttpServletRequest request,
    		HttpServletResponse response, Model model)
    				throws ServletException {
    	return jpeg(request, response, false);
    }*/
    
    private ResponseEntity<byte[]> jpeg(HttpServletRequest request,
			HttpServletResponse response, boolean b) throws NotHandledException, NotAcceptableException {
    	endpoint = configuration.findEndpoint(request);

        if (endpoint == null) {
            throw new NotHandledException(request);
        }
        
       /* if (!endpoint.accepts(request)) {
            throw new NotAcceptableException(request);
        }*/
        HttpQuery httpQuery = new HttpQuery("http://www.ign.es/wms-inspire/ign-base");
        Enumeration<String> params=request.getParameterNames();
        while(params.hasMoreElements()){
        	String name=params.nextElement();
        	String value=request.getParameter(name);
        	httpQuery.addParam(name, value);
        }
        
        HttpHeaders headers = new HttpHeaders();
                      
        InputStream in = httpQuery.exec();
        
        headers.setContentType(MediaType.IMAGE_JPEG);

        try {
			return new ResponseEntity<byte[]>(IOUtils.toByteArray(in), headers, HttpStatus.CREATED);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			return null;
		}
               
        

	}

    public String format(String unformattedXml) {
        try {
        	 Source xmlInput = new StreamSource(new StringReader(unformattedXml));
             StringWriter stringWriter = new StringWriter();
             StreamResult xmlOutput = new StreamResult(stringWriter);
             TransformerFactory transformerFactory = TransformerFactory.newInstance();
             transformerFactory.setAttribute("indent-number", 3);
             Transformer transformer = transformerFactory.newTransformer(); 
             transformer.setOutputProperty(OutputKeys.INDENT, "yes");
             transformer.transform(xmlInput, xmlOutput);
             return xmlOutput.getWriter().toString();
        } catch (TransformerConfigurationException e) {
			return unformattedXml;
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			return unformattedXml;
		}
    }
    
   
    
	public String html(HttpServletRequest request,
            HttpServletResponse response, Model model, boolean varying)
            throws ServletException {
        endpoint = configuration.findEndpoint(request);

        if (endpoint == null) {
            throw new NotHandledException(request);
        }
        if (!endpoint.accepts(request)) {
            throw new NotAcceptableException(request);
        }
        RdfResource description = endpoint.description(request, MediaType.TEXT_HTML, config);
        if (description == null) {
            throw new NotFoundException(request);
        }

        HttpHeaders headers = new HttpHeaders();
        RdfRepresentation lr = description.getRepresentation();
        try{
        headers
            .add("Link", new Link(lr.getResource().getURI()).toString());
        headers.add("Link", new Link(lr.getPrimaryTopic()).withAboutRel()
            .toString());
        headers.add("Link", new Link(lr.getCanonical()).withCanonicalRel()
            .toString());
        for (String al : lr.getHasFormats()) {
            headers.add("Link", new Link(al).withAlternateRel()
                .toString());
        }
        }catch(Exception e){}
        if (varying) {
            headers.add("Vary", "Accept");
        }

        mergeHeadersFromHttpServletResponse(headers, response);

        endpoint.enrich(description, request, headers);

        
        try{
        ByteArrayOutputStream baos=new ByteArrayOutputStream();
        description.getModel().write(baos,"RDF/JSON");
        String jsonStr=new String(baos.toByteArray());
        JSONObject jsonObj = new JSONObject(jsonStr);
        jsonObj=jsonObj.getJSONObject(lr.getPrimaryTopic());
        Iterator<String> jsonObjIt=jsonObj.keys();
        String value=null;
        while(jsonObjIt.hasNext()){
        	String key=jsonObjIt.next();
        	if(key.endsWith("#jsonRepresentation")){
        		JSONArray jArray=jsonObj.getJSONArray(key);
        		if(jArray.length()>0){
        			value=StringEscapeUtils.unescapeJson(jArray.getJSONObject(0).getString("value"));
        		}
        	}
        }
        
        JSONObject obj=new JSONObject(value);
        
        ArrayList<ResourceDetails> lrd=new ArrayList<ResourceDetails>();
        JSONObject featuresObj=obj.getJSONObject("features");
        
        
        
        JSONArray jarray=featuresObj.getJSONArray("features");
        HashMap<String, ResourceDetails> featuresMap=new HashMap<String, ResourceDetails>();
        for(int i=0;i<jarray.length();i++){
        	String url=null;
        	ResourceDetails rd=new ResourceDetails();
        	JSONObject feature=jarray.getJSONObject(i);
        	
        	try{
        	JSONObject geometry = (JSONObject) feature.get("geometry");
        	GeometryJSON g = new GeometryJSON(0);
        	Geometry m = g.read(geometry.toString());
            Point point=m.getEnvelope().getCentroid();
            rd.setX(point.getX());
            rd.setY(point.getY());
        	}catch(Exception e){
        		//unable to extract geometry
        	}
        	
        	try{
        	JSONObject crs=feature.getJSONObject("crs");
        	JSONObject crsProps=crs.getJSONObject("properties");
        	
        	// srsName
        	rd.setSrs(crsProps.getString("name"));
        	}catch(Exception e){
        		//unable to extract crs
        	}
        	
        	JSONObject properties=feature.getJSONObject("properties");
        	
        	try{
        	//url
        	url=properties.getString("id");
        	rd.setUrl(url);
        	}catch(Exception e){
        		//unable to extract url
        	}
        	
        	//prov
        	try{
        	String prov=properties.getString("prov");
        	prov=prov.replace("\"", "");
        	prov=prov.replace("^^<http://www.w3.org/1999/02/22-rdf-syntax-ns#XMLLiteral>", "");
        	prov=URLDecoder.decode(prov,"UTF-8");
        	rd.setProvenance(format(prov));
        	}catch(Exception e){
        		//unable to extract prov
        	}
        	
        	//type
        	try{
        	ArrayList<String> types=new ArrayList<String>();
        	types.add(properties.getString("t"));
        	rd.setType(types);
        	}catch(Exception e){
        		//unable to extract types
        	}
        	
        	//dataset
        	try{
        		rd.setDataset(properties.getString("p"));
        	}catch(Exception e){
        		//unable to extract dataset
        	}
        	
        	//title
        	try{
        	rd.setTitle(properties.getString("label"));
        	}catch(Exception e){
        		//unable to extract title
        	}
        	ArrayList<ResourceComment> comments=new ArrayList<ResourceComment>();
        	//comments
        	try{
        	JSONArray commentsArray=properties.getJSONArray("comments");
        	for(int j=0;j<commentsArray.length();j++){
        		JSONObject comment=commentsArray.getJSONObject(j);
        		ResourceComment rc=new ResourceComment();
        		//valid
        		JSONObject valid=comment.getJSONObject("value");
        		rc.setValid(Boolean.parseBoolean(valid.getString("value")));
        		
        		//date
        		JSONObject date=comment.getJSONObject("date");
        		rc.setDate(date.getString("value"));
        		
        		//content
        		JSONObject content=comment.getJSONObject("comment");
        		rc.setComment(content.getString("value"));
        		
        		//creator
        		JSONObject creator=comment.getJSONObject("creator");
        		rc.setUser(creator.getString("value"));
        		comments.add(rc);
        	}
        	Collections.sort(comments);
        	Collections.reverse(comments);
        	rd.setComments(comments);
        	
        	}catch(Exception e){
        		//unable to extract comments
        	}
        	featuresMap.put(url, rd);
        	
        }
        
        // mappings
        JSONObject mappings=obj.getJSONObject("mappings");
        JSONArray mappingsArray=mappings.getJSONObject("results").getJSONArray("bindings");
        ArrayList<ResourceMapping> mappingsMap=new ArrayList<ResourceMapping>();
        for(int i=0;i<mappingsArray.length();i++){
        	JSONObject mapping=mappingsArray.getJSONObject(i);
        	ResourceMapping rm=new ResourceMapping();
        	// mapId
        	try{
        	JSONObject mapId=mapping.getJSONObject("mapId");
        	rm.setMappingId(mapId.getString("value"));
        	}catch(Exception e){
        		//unable to extract mapId
        	}
        	
        	//measure
        	try{
        	JSONObject measure=mapping.getJSONObject("measure");
        	rm.setMeasure(Float.parseFloat(measure.getString("value")));
        	}catch(Exception e){
        		//unable to extract measure
        	}
        	
        	//entity
        	String url=null;
        	try{
        	JSONObject ent=mapping.getJSONObject("ent");
        	url=ent.getString("value");
        	ResourceDetails rd=featuresMap.get(url);
        	rm.setRelatedResource(rd);
        	}catch(Exception e){
        		//unable to extract entity
        	}
        	
        	ArrayList<ResourceComment> comments=new ArrayList<ResourceComment>();
        	//comments
        	try{
        	JSONArray commentsArray=mapping.getJSONArray("comments");
        	for(int j=0;j<commentsArray.length();j++){
        		JSONObject comment=commentsArray.getJSONObject(j);
        		ResourceComment rc=new ResourceComment();
        		//valid
        		JSONObject valid=comment.getJSONObject("value");
        		rc.setValid(Boolean.parseBoolean(valid.getString("value")));
        		
        		//date
        		JSONObject date=comment.getJSONObject("date");
        		rc.setDate(date.getString("value"));
        		
        		//content
        		JSONObject content=comment.getJSONObject("comment");
        		rc.setComment(content.getString("value"));
        		
        		//creator
        		JSONObject creator=comment.getJSONObject("creator");
        		rc.setUser(creator.getString("value"));
        		comments.add(rc);
        	}
        	Collections.sort(comments);
        	Collections.reverse(comments);
        	rm.setComments(comments);
        	}catch(Exception e){
        		//unable to extract comments
        	}
        	if(featuresMap.containsKey(url)){
        		mappingsMap.add(rm);
        		featuresMap.remove(url);
        	}
        }
        
        if(featuresMap.size()==1){
        	ResourceDetails rd=featuresMap.get(featuresMap.keySet().iterator().next());
        	rd.setMappings(mappingsMap);
        	model.addAttribute("resourceDetails",rd);
        	return "/resourceDetail";
        }
        
        }catch(Exception e){
        	System.out.println(e);
        }
        
       // if(value==null){
        	model.addAttribute(HeadersAwareThymeleafView.HEADERS,
                    linearize(headers));
                model.addAttribute("uri", description.getResource().getURI());
                model.addAttribute("title", description.getTitle());
                model.addAttribute("comment", description.getComment());
                model.addAttribute("image", description.getImageURL());
                model.addAttribute("properties", description.getProperties());
                model.addAttribute("showLabels", endpoint.showLabels());
                
                return "/show";
      /*  }
        else{
        	JSONObject obj=new JSONObject(value);
        	JSONObject mappings=obj.getJSONObject("mappings");
        	JSONArray bindings=mappings.getJSONObject("results").getJSONArray("bindings");
        	for(int i=0;i<bindings.length();i++){
        		
        	}
        }*/
        
    }
    
    public ResponseEntity<?> json(HttpServletRequest request,
            HttpServletResponse response, boolean varying) throws Exception {
        endpoint = configuration.findEndpoint(request);
        if (endpoint == null) {
            return notHandled(request, response);
        }
     /*   if (!endpoint.accepts(request)) {
            return notAcceptable(endpoint, request, response, varying);
        }*/
        RdfResource description = endpoint.description(request, AbstractEndpoint.TEXT_TURTLE, config);
        if (description == null) {
            return notFound(endpoint, request, response, varying);
        }
        
        HttpHeaders headers = new HttpHeaders();
        RdfRepresentation lr = description.getRepresentation();
        
        ByteArrayOutputStream baos=new ByteArrayOutputStream();
        description.getModel().write(baos,"RDF/JSON");
        String jsonStr=new String(baos.toByteArray());
        JSONObject jsonObj = new JSONObject(jsonStr);
        jsonObj=jsonObj.getJSONObject(lr.getPrimaryTopic());
        Iterator<String> jsonObjIt=jsonObj.keys();
        String value=null;
        while(jsonObjIt.hasNext()){
        	String key=jsonObjIt.next();
        	if(key.endsWith("#jsonRepresentation")){
        		JSONArray jArray=jsonObj.getJSONArray(key);
        		if(jArray.length()>0){
        			value=StringEscapeUtils.unescapeJson(jArray.getJSONObject(0).getString("value"));
        		}
        	}
        }
        
               
        if(value==null){
        	value=jsonStr;
        }
        
        //String mappingsQuery="select ?ent ?measure { {  ?id a <http://knowledgeweb.semanticweb.org/heterogeneity/alignment#Cell> .  ?id <http://knowledgeweb.semanticweb.org/heterogeneity/alignment#entity2>  "+description.getIdentifier()+" .  ?id <http://knowledgeweb.semanticweb.org/heterogeneity/alignment#entity1> ?ent . ?id <http://knowledgeweb.semanticweb.org/heterogeneity/alignment#measure> ?measure }UNION{  ?id a <http://knowledgeweb.semanticweb.org/heterogeneity/alignment#Cell> .  ?id <http://knowledgeweb.semanticweb.org/heterogeneity/alignment#entity1>  <http://linkedmap.unizar.es/id/data/BCN-3961439> .  ?id <http://knowledgeweb.semanticweb.org/heterogeneity/alignment#entity2> ?ent . ?id <http://knowledgeweb.semanticweb.org/heterogeneity/alignment#measure> ?measure } }";
        
        return new ResponseEntity<String>(value,
            linearize(headers), HttpStatus.OK);
        
        
        
       
    }

}
