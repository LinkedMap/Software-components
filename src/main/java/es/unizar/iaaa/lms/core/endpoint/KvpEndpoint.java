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
package es.unizar.iaaa.lms.core.endpoint;

import static es.unizar.iaaa.lms.util.Util.getHeaders;
import static es.unizar.iaaa.lms.util.Util.removeHeaders;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPathConstants;

import org.apache.commons.io.IOUtils;
import org.springframework.boot.autoconfigure.security.SecurityProperties.Headers;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.engine.http.HttpQuery;

import es.unizar.iaaa.lms.core.domain.ResourceSet;
import es.unizar.iaaa.lms.pubby.vocab.LMS;
import es.unizar.iaaa.lms.store.RdfConfiguration;
import es.unizar.iaaa.lms.store.RdfResource;
import es.unizar.iaaa.lms.util.XPathHelper;
import es.unizar.iaaa.lms.web.controller.ServiceController;

public class KvpEndpoint extends AbstractEndpoint {

	private Endpoint mapStore;

	private Endpoint mapCollection;

	private Endpoint infoStore;

	private Endpoint infoCollection;

	@Override
	public boolean accepts(HttpServletRequest request) {
		return true;
	}

	public KvpEndpoint(String path, String description) {
		super("GET", path, description);
	}

	@Override
	public ResponseEntity<?> proxy(HttpServletRequest request,
			RdfResource description) throws Exception {
		if (isOperation(request, "GetCapabilities")) {
			return doProxiedGetCapabilitiesRequest(request, description);

		} else if ((isOperation(request, "GetMap"))
				&& (containsParameter(request, "FORMAT", "application/json"))) {
			String defaultGraph = defaultGraph(description);
			String query = "query="
					+ URLEncoder.encode(sparqlQuery(request, defaultGraph),
							"UTF-8")
					+ "&view=HTML&format=GeoJSON&handle=download";
			String sparqlEndpoint = sparqlEndpoint(description);

			ResponseEntity<byte[]> re = new RestTemplate().exchange(
					sparqlEndpoint + query, HttpMethod.GET,
					new HttpEntity<String>(null, getHeaders(request)),
					byte[].class);
			HttpHeaders hh = removeHeaders(re.getHeaders(), "Transfer-Encoding");
			return new ResponseEntity<byte[]>(re.getBody(), hh,
					re.getStatusCode());

			/*
			 * String query=sparqlQuery(request); HttpQuery httpQuery = new
			 * HttpQuery("http://linkedmap.unizar.es/strabonendpoint/Query");
			 * httpQuery.addParam("query", query);
			 * 
			 * 
			 * httpQuery.addParam("view", "HTML"); httpQuery.addParam("format",
			 * "GeoJSON"); httpQuery.addParam("handle","download");
			 * 
			 * InputStream in = httpQuery.exec(); HttpHeaders hh =
			 * getHeaders(request); return new
			 * ResponseEntity<byte[]>(IOUtils.toByteArray(in), hh,
			 * HttpStatus.OK);
			 */
		} else {
			return doOtherRequests(request, description);
		}
	}

	private String sparqlQuery(HttpServletRequest request, String graph) {
		String sparqlSelectSinRel = "select ?id ?label ?p ?y ?t ?hasMapping ?numberOfMappings where{ graph <${GRAPH}>{   ?id <http://www.opengis.net/ont/geosparql#hasGeometry> ?fGeom. ?id <http://linkedmap.unizar.es/def/silk#hasSilkMappings> ?hasMapping . ?id <http://linkedmap.unizar.es/def/silk#numberOfSilkMappings> ?numberOfMappings .  ?id <http://www.w3.org/2000/01/rdf-schema#label> ?label .   ?id <http://purl.org/dc/terms/isPartOf> ?p .   ?fGeom <http://www.opengis.net/ont/geosparql#asWKT> ?y .   ?id <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ?t .FILTER (<http://www.opengis.net/def/function/geosparql/sfIntersects>(?y,\"POLYGON((${XMIN} ${YMIN}, ${XMIN} ${YMAX}, ${XMAX} ${YMAX}, ${XMAX} ${YMIN}, ${XMIN} ${YMIN}))\") && ?t != <http://www.opengis.net/ont/geosparql#Feature>) . }}limit 200";
		// String sparqlSelectSinRel =
		// "select ?id ?label ?p ?y ?t where{ graph <http://linkedmap.unizar.es/graph/BCN>{   ?id <http://www.opengis.net/ont/geosparql#hasGeometry> ?fGeom.   ?id <http://www.w3.org/2000/01/rdf-schema#label> ?label .   ?id <http://purl.org/dc/terms/isPartOf> ?p .   ?fGeom <http://www.opengis.net/ont/geosparql#asWKT> ?y .   ?id <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ?t .FILTER (<http://www.opengis.net/def/function/geosparql/sfIntersects>(?y,\"POLYGON((${XMIN} ${YMIN}, ${XMIN} ${YMAX}, ${XMAX} ${YMAX}, ${XMAX} ${YMIN}, ${XMIN} ${YMIN}))\") && ?t != <http://www.opengis.net/ont/geosparql#Feature>) . }}limit 200";
		String sparqlSelectConRel = "select ?id ?label ?p ?y ?t ?hasMapping ?numberOfMappings where{ graph <${GRAPH}>{   ?id <http://www.opengis.net/ont/geosparql#hasGeometry> ?fGeom. ?id <http://linkedmap.unizar.es/def/silk#hasSilkMappings> ?hasMapping . ?id <http://linkedmap.unizar.es/def/silk#numberOfSilkMappings> ?numberOfMappings .  ?id <http://www.w3.org/2000/01/rdf-schema#label> ?label .   ?id <http://purl.org/dc/terms/isPartOf> ?p .   ?fGeom <http://www.opengis.net/ont/geosparql#asWKT> ?y .   ?id <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ?t .FILTER (<http://www.opengis.net/def/function/geosparql/sfIntersects>(?y,\"POLYGON((${XMIN} ${YMIN}, ${XMIN} ${YMAX}, ${XMAX} ${YMAX}, ${XMAX} ${YMIN}, ${XMIN} ${YMIN}))\") && ?t != <http://www.opengis.net/ont/geosparql#Feature>) . }}limit 200";
		// String sparqlSelectConRel =
		// "select ?id ?label ?p ?y ?t (COALESCE(?hm, \"false\"^^<http://www.w3.org/2001/XMLSchema#boolean>) AS ?hasMapping) where{ {   ?id <http://www.opengis.net/ont/geosparql#hasGeometry> ?fGeom.   ?id <http://www.w3.org/2000/01/rdf-schema#label> ?label .   ?id <http://purl.org/dc/terms/isPartOf> ?p .   ?fGeom <http://www.opengis.net/ont/geosparql#asWKT> ?y .   ?id <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ?t .   ?id <http://linkedmap.unizar.es/def/silk#hasSilkMappings> ?hm . FILTER (<http://www.opengis.net/def/function/geosparql/sfIntersects>(?y,\"POLYGON((${XMIN} ${YMIN}, ${XMIN} ${YMAX}, ${XMAX} ${YMAX}, ${XMAX} ${YMIN}, ${XMIN} ${YMIN}))\") && ?t != <http://www.opengis.net/ont/geosparql#Feature> && ((?p = <http://linkedmap.unizar.es/id/datasets/BCN>)|| (?p = <http://linkedmap.unizar.es/id/datasets/BTN>))) } UNION{ ?id <http://www.opengis.net/ont/geosparql#hasGeometry> ?fGeom.   ?id <http://www.w3.org/2000/01/rdf-schema#label> ?label .   ?id <http://purl.org/dc/terms/isPartOf> ?p .   ?fGeom <http://www.opengis.net/ont/geosparql#asWKT> ?y .   ?id <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ?t . FILTER (<http://www.opengis.net/def/function/geosparql/sfIntersects>(?y,\"POLYGON((${XMIN} ${YMIN}, ${XMIN} ${YMAX}, ${XMAX} ${YMAX}, ${XMAX} ${YMIN}, ${XMIN} ${YMIN}))\") && ?t != <http://www.opengis.net/ont/geosparql#Feature> && ((?p = <http://linkedmap.unizar.es/id/datasets/BCN>)|| (?p = <http://linkedmap.unizar.es/id/datasets/BTN>)) && (?a != <http://linkedmap.unizar.es/def/silk#hasSilkMappings>) ) FILTER NOT EXISTS {?id <http://linkedmap.unizar.es/def/silk#hasSilkMappings> ?hm} }}limit 200";
		// String sparqlSelectConRel
		// ="select ?id ?label ?p ?y ?t ?hm where { graph <http://linkedmap.unizar.es/graph/BCN>{	?id <http://www.opengis.net/ont/geosparql#hasGeometry> ?fGeom.    ?id <http://www.w3.org/2000/01/rdf-schema#label> ?label .	?id <http://purl.org/dc/terms/isPartOf> ?p .	?fGeom <http://www.opengis.net/ont/geosparql#asWKT> ?y .	?id <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ?t .        ?id <http://linkedmap.unizar.es/def/silk#hasSilkMappings> ?hm .	FILTER (<http://www.opengis.net/def/function/geosparql/sfIntersects>(?y,\"POLYGON((${XMIN} ${YMIN}, ${XMIN} ${YMAX}, ${XMAX} ${YMAX}, ${XMAX} ${YMIN}, ${XMIN} ${YMIN}))\") && ?t != <http://www.opengis.net/ont/geosparql#Feature>) .	}}limit 500";
		// String sparqlSelectConRel
		// ="select ?id ?label ?p ?y ?t ?hm where { graph <http://linkedmap.unizar.es/graph/BCN>{	?id <http://www.opengis.net/ont/geosparql#hasGeometry> ?fGeom.    ?id <http://www.w3.org/2000/01/rdf-schema#label> ?label .	?id <http://purl.org/dc/terms/isPartOf> ?p .	?fGeom <http://www.opengis.net/ont/geosparql#asWKT> ?y .	?id <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ?t .        ?id <http://linkedmap.unizar.es/def/silk#hasSilkMappings> ?hm .	FILTER (<http://www.opengis.net/def/function/geosparql/sfIntersects>(?y,\"POLYGON((${XMIN} ${YMIN}, ${XMIN} ${YMAX}, ${XMAX} ${YMAX}, ${XMAX} ${YMIN}, ${XMIN} ${YMIN}))\") && ?t = <http://linkedmap.unizar.es/def/bcn25/ENT_POB>) .	}}limit 500";

		Enumeration<String> params = request.getParameterNames();
		String bbox = null;
		String crs = null;
		String version = null;
		while (params.hasMoreElements()) {
			String pName = params.nextElement();
			if (pName.equalsIgnoreCase("bbox")) {
				bbox = request.getParameter(pName);
			} else if (pName.equalsIgnoreCase("crs")) {
				crs = request.getParameter(pName);
			} else if (pName.equalsIgnoreCase("version")) {
				version = request.getParameter(pName);
			}
		}
		// 33.595509271122,-19.799302555777,53.480763177372,22.388197444223
		String xmin = "-19.799302555777";
		String xmax = "22.388197444223";
		String ymin = "33.595509271122";
		String ymax = "53.480763177372";
		if (bbox != null) {
			String[] coordinates = bbox.split("\\,");
			if (coordinates.length == 4) {
				if (version != null && version.equalsIgnoreCase("1.3.0")) {
					ymin = coordinates[0];
					xmin = coordinates[1];
					ymax = coordinates[2];
					xmax = coordinates[3];
				} else {
					xmin = coordinates[0];
					ymin = coordinates[1];
					xmax = coordinates[2];
					ymax = coordinates[3];
				}
			}
		}
		String sparqlSelect = null;
		if (Float.parseFloat(xmax) - Float.parseFloat(xmin) > 1) {
			sparqlSelect = sparqlSelectConRel;
			System.out.println("Con relaciones!");
		} else {
			sparqlSelect = sparqlSelectSinRel;
			System.out.println("Sin relaciones!");
		}
		sparqlSelect = sparqlSelect.replace("${XMIN}", xmin);
		sparqlSelect = sparqlSelect.replace("${XMAX}", xmax);
		sparqlSelect = sparqlSelect.replace("${YMIN}", ymin);
		sparqlSelect = sparqlSelect.replace("${YMAX}", ymax);

		sparqlSelect = sparqlSelect.replace("${GRAPH}", graph);
		return sparqlSelect;

	}

	private boolean containsParameter(HttpServletRequest request,
			String paramName, String paramValue) {
		Enumeration<String> params = request.getParameterNames();
		while (params.hasMoreElements()) {
			String p = params.nextElement();
			if (p.equalsIgnoreCase(paramName)) {
				String[] pValues = request.getParameterValues(p);
				for (int i = 0; i < pValues.length; i++) {
					String pValue = pValues[i];
					if (pValue.equalsIgnoreCase(paramValue)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	private ResponseEntity<?> doOtherRequests(HttpServletRequest request,
			RdfResource description) {
		/*
		 * HttpQuery httpQuery = new HttpQuery(onlineResource(description));
		 * Enumeration<String> params=request.getParameterNames();
		 * httpQuery.setAccept(request.getHeader("Accept")); String query="";
		 * while(params.hasMoreElements()){ String pName=params.nextElement();
		 * String value=request.getParameter(pName);
		 * query=query+"&"+pName+"="+value; httpQuery.addParam(pName,value); }
		 * 
		 * InputStream in = httpQuery.exec(); try{ HttpHeaders
		 * hh=getHeaders(request);
		 * hh.setContentType(MediaType.valueOf(httpQuery.getContentType()));
		 * return new ResponseEntity<byte[]>(IOUtils.toByteArray(in), hh,
		 * HttpStatus.OK); }catch(Exception e){}
		 */
		ResponseEntity<byte[]> re = new RestTemplate().exchange(
				onlineResource(description) + request.getQueryString(),
				HttpMethod.GET, new HttpEntity<String>(null,
						getHeaders(request)), byte[].class);
		HttpHeaders hh = removeHeaders(re.getHeaders(), "Transfer-Encoding");
		return new ResponseEntity<byte[]>(re.getBody(), hh, re.getStatusCode());
		// return null;
	}

	private ResponseEntity<?> doProxiedGetCapabilitiesRequest(
			HttpServletRequest request, RdfResource description)
			throws Exception {
		// Removing "referer" header is due to a server bug
		// in one of the WMS used.
		ResponseEntity<byte[]> re = new RestTemplate().exchange(
				onlineResource(description) + request.getQueryString(),
				HttpMethod.GET,
				new HttpEntity<String>(null, removeHeaders(getHeaders(request),
						"referer")), byte[].class);
		XPathHelper helper = new XPathHelper("//HTTP//@href", null);

		// FIXME Detect string encoding
		Document document = helper.parseXmlString(new String(re.getBody(),
				Charset.forName("UTF-8")));
		NodeList nodes = helper.evaluateXpath(document, XPathConstants.NODESET,
				NodeList.class);
		for (int i = 0; i < nodes.getLength(); i++) {
			nodes.item(i).setTextContent(request.getRequestURL() + "?");
		}
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		getTransformer().transform(new DOMSource(document),
				new StreamResult(bos));

		HttpHeaders hh = removeHeaders(re.getHeaders(), "Content-Length");
		return new ResponseEntity<byte[]>(bos.toByteArray(), hh,
				re.getStatusCode());
	}

	public boolean isOperation(HttpServletRequest request, String operationName) {
		for (String s : request.getParameterMap().keySet()) {
			if (s.equalsIgnoreCase("request")) {
				return operationName.equalsIgnoreCase(request.getParameter(s));
			}
		}
		return false;
	}

	@Override
	public RdfResource description(HttpServletRequest request,
			MediaType mediaType, RdfConfiguration config) {
		RdfResource ld = super.description(request, mediaType, config);
		if (ld == null) {
			return null;
		}

		Resource res = ld.getResource();
		return new ResourceSet(res).find("onlineResource").exists() ? ld : null;
	}

	private String onlineResource(RdfResource rd) {
		Resource res = rd.getResource();
		ResourceSet rs = new ResourceSet(res);
		String uri = rs.find("onlineResource").first().asResource().getURI();

		return uri.contains("?") ? uri : (uri + "?");
	}

	private String sparqlEndpoint(RdfResource rd) {
		try {

			Resource res = rd.getResource();
			ResourceSet rs = new ResourceSet(res);
			String uri = rs.find("sparqlEndpoint").first().asResource()
					.getURI();
			if (uri == null) {
				uri = "http://linkedmap.unizar.es/strabonendpoint/Query";
			}
			return uri.contains("?") ? uri : (uri + "?");
		} catch (Exception e) {
			String uri = "http://linkedmap.unizar.es/strabonendpoint/Query";

			return uri.contains("?") ? uri : (uri + "?");
		}
	}

	private String defaultGraph(RdfResource rd) {
		try {
			Resource res = rd.getResource();
			ResourceSet rs = new ResourceSet(res);
			String uri = rs.find("defaultGraph").first().asResource().getURI();
			if (uri == null) {
				uri = "http://linkedmap.unizar.es/graph/BCN";
			}
			return uri;
		} catch (Exception e) {
			return "http://linkedmap.unizar.es/graph/BCN";
		}
	}

	/**
	 * Create a XML transformer.
	 * 
	 * @return a transformer
	 * @throws TransformerConfigurationException
	 *             if the transformer cannot be created
	 */
	private Transformer getTransformer()
			throws TransformerConfigurationException {
		TransformerFactory tf = TransformerFactory.newInstance();
		Transformer transformer = tf.newTransformer();
		transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
		transformer.setOutputProperty(OutputKeys.METHOD, "xml");
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
		return transformer;
	}

	public void setMapStore(Endpoint mapsStore) {
		this.mapStore = mapsStore;
	}

	public void setMapCollection(Endpoint mapsCollection) {
		this.mapCollection = mapsCollection;
	}

	public Endpoint getMapStore() {
		return mapStore;
	}

	public Endpoint getMapCollection() {
		return mapCollection;
	}

	public void setInfoStore(Endpoint infosStore) {
		this.infoStore = infosStore;
	}

	public void setInfoCollection(Endpoint infosCollection) {
		this.infoCollection = infosCollection;
	}

	public Endpoint getInfoStore() {
		return infoStore;
	}

	public Endpoint getInfoCollection() {
		return infoCollection;
	}

}
