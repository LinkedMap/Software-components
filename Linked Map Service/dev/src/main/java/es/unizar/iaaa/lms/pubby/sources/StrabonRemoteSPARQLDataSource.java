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

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.WebContent;
import org.json.JSONArray;
import org.json.JSONObject;

import com.hp.hpl.jena.query.QueryException;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.impl.StatementImpl;
import com.hp.hpl.jena.sparql.engine.http.HttpQuery;
import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP;

import es.unizar.iaaa.lms.pubby.PubbyIRIEscaper;
import es.unizar.iaaa.lms.pubby.PubbyRepositoryException;
import es.unizar.iaaa.lms.pubby.VocabularyStore.CachedPropertyCollection;

/**
 * A data source backed by a SPARQL endpoint accessed through the SPARQL
 * protocol.
 */
public class StrabonRemoteSPARQLDataSource extends RemoteSPARQLDataSource {
	private final String endpointURL;
	private final String defaultGraphURI;
	private final boolean supportsSPARQL11;

	private final Set<String> resourceQueries;
	private final Set<String> propertyQueries;
	private final Set<String> inversePropertyQueries;
	private final Set<String> anonPropertyQueries;
	private final Set<String> anonInversePropertyQueries;

	private final CachedPropertyCollection highIndegreeProperties;
	private final CachedPropertyCollection highOutdegreeProperties;

	private String previousDescribeQuery;
	private String contentType = null;
	private final Set<String[]> queryParamsSelect = new HashSet<String[]>();
	private final Set<String[]> queryParamsGraph = new HashSet<String[]>();

	private String dbpediaReplaceUris = "http://es.dbpedia.org/data";
	private String dbpediaResourceOriginalSufix = "http://es.dbpedia.org/resource";
	private String dbpediaResourceExtension = ".ntriples";
	private String dbpediaRedirectPrefix ="http://linkedmap.unizar.es/id/data/DBPEDIA-";

	private HashMap<String, String> strabonEndpoints = new HashMap<String, String>();

	public StrabonRemoteSPARQLDataSource(String endpointURL,
			String defaultGraphURI) {
		this(endpointURL, defaultGraphURI, false, null, null, null, null, null,
				null, null, null);

	}

	public StrabonRemoteSPARQLDataSource(String endpointURL,
			String defaultGraphURI, boolean supportsSPARQL11,
			Set<String> resourceQueries, Set<String> propertyQueries,
			Set<String> inversePropertyQueries,
			Set<String> anonPropertyQueries,
			Set<String> anonInversePropertyQueries,
			CachedPropertyCollection highIndegreeProperties,
			CachedPropertyCollection highOutdegreeProperties,
			HashMap<String, String> distributedEndpoints) {
		super(endpointURL, defaultGraphURI, supportsSPARQL11, resourceQueries,
				propertyQueries, inversePropertyQueries, anonPropertyQueries,
				anonInversePropertyQueries, highIndegreeProperties,
				highOutdegreeProperties);
		if (distributedEndpoints != null) {
			strabonEndpoints = distributedEndpoints;
		}
		this.endpointURL = endpointURL;
		this.defaultGraphURI = defaultGraphURI;
		this.supportsSPARQL11 = supportsSPARQL11;
		if (resourceQueries == null || resourceQueries.isEmpty()) {
			resourceQueries = supportsSPARQL11 ? new HashSet<String>(
					Arrays.asList(new String[] {
							"CONSTRUCT {?__this__ ?p ?o} WHERE {?__this__ ?p ?o. FILTER (?p NOT IN ?__high_outdegree_properties__)}",
							"CONSTRUCT {?s ?p ?__this__} WHERE {?s ?p ?__this__. FILTER (?p NOT IN ?__high_indegree_properties__)}" }))
					: Collections.singleton("DESCRIBE ?__this__");
		}
		if (propertyQueries == null || propertyQueries.isEmpty()) {
			propertyQueries = Collections
					.singleton("CONSTRUCT {?__this__ ?__property__ ?x} WHERE {?__this__ ?__property__ ?x. FILTER (!isBlank(?x))}");
		}
		if (inversePropertyQueries == null || inversePropertyQueries.isEmpty()) {
			inversePropertyQueries = Collections
					.singleton("CONSTRUCT {?x ?__property__ ?__this__} WHERE {?x ?__property__ ?__this__. FILTER (!isBlank(?x))}");
		}
		if (anonPropertyQueries == null || anonPropertyQueries.isEmpty()) {
			anonPropertyQueries = Collections
					.singleton("DESCRIBE ?x WHERE {?__this__ ?__property__ ?x. FILTER (isBlank(?x))}");
		}
		if (anonInversePropertyQueries == null
				|| anonInversePropertyQueries.isEmpty()) {
			anonInversePropertyQueries = Collections
					.singleton("DESCRIBE ?x WHERE {?x ?__property__ ?__this__. FILTER (isBlank(?x))}");
		}
		this.resourceQueries = resourceQueries;
		this.propertyQueries = propertyQueries;
		this.inversePropertyQueries = inversePropertyQueries;
		this.anonPropertyQueries = anonPropertyQueries;
		this.anonInversePropertyQueries = anonInversePropertyQueries;

		this.highIndegreeProperties = highIndegreeProperties;
		this.highOutdegreeProperties = highOutdegreeProperties;
	}

	/**
	 * Sets the content type to ask for in graph requests (CONSTRUCT and
	 * DESCRIBE) to the remote SPARQL endpoint.
	 */
	public void setGraphContentType(String mediaType) {
		this.contentType = mediaType;
	}

	public void addGraphQueryParam(String param) {
		queryParamsGraph.add(parseQueryParam(param));
	}

	public void addSelectQueryParam(String param) {
		queryParamsSelect.add(parseQueryParam(param));
	}

	private String[] parseQueryParam(String param) {
		Matcher match = queryParamPattern.matcher(param);
		if (!match.matches()) {
			throw new PubbyRepositoryException("Query parameter \"" + param
					+ "\" is not in \"param=value\" form");
		}
		return new String[] { match.group(1), match.group(2) };
	}

	private Pattern queryParamPattern = Pattern.compile("(.*?)=(.*)");

	@Override
	public boolean canDescribe(String absoluteIRI) {
		if (absoluteIRI.contains("/def/")) {
			return false;
		}
		return true;
	}

	@Override
	public Model describeResource(String resourceURI) {
		// Loop over resource description queries, join results in a single
		// model.
		// Process each query to replace place-holders of the given resource.

		resourceURI = PubbyIRIEscaper.escapeSpecialCharacters(resourceURI);
		Model model = ModelFactory.createDefaultModel();
		if (resourceURI.startsWith("http://linkedmap.unizar.es/id/data/")) {
			try {
				URL url = new URL(resourceURI);
				String query = url.getQuery();
				if (query != null && query.trim().length() > 0) {
					query = query.replace("query=", "");
					query = URLDecoder.decode(query, "UTF-8");
					Model result = execQueryGraph(query);
					model.add(result);
					return model;
				} else {
					return deepDescribe(resourceURI);
				}

			} catch (MalformedURLException e1) {
				return null;
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				return null;
			}
		}
		if (resourceURI.startsWith("http://linkedmap.unizar.es/id/users/")) {
			return getUserData(resourceURI);
		}
		return model;

	}

	private Model getUserData(String resourceURI) {
		Model model = ModelFactory.createDefaultModel();
		String sparqlQuery="select ?idFeat ?date ?commentsId1 ?commentsId2 ?date where{ ?id <http://xmlns.com/foaf/0.1/primaryTopic> ?idFeat . ?id <http://purl.org/dc/elements/1.1/date> ?date . ?id <http://purl.org/dc/elements/1.1/creator>	?creator . OPTIONAL {    ?idFeat <http://knowledgeweb.semanticweb.org/heterogeneity/alignment#entity1> ?commentsId1 .    ?idFeat <http://knowledgeweb.semanticweb.org/heterogeneity/alignment#entity2> ?commentsId2 . } FILTER (regex (?creator, \"${USERID}\", \"i\"))}";
		String sparqlGetDatasetAndLabel="select ?label ?dataset where{ ${ID} <http://www.w3.org/2000/01/rdf-schema#label> ?label . ${ID} <http://purl.org/dc/terms/isPartOf> ?dataset }";
		String effectiveEndpointURL = endpointURL;
		if (!endpointURL.endsWith("/")) {
			effectiveEndpointURL = endpointURL + "/";
		}
		
		String user=resourceURI.substring(resourceURI.lastIndexOf("/")+1);
		sparqlQuery=sparqlQuery.replace("${USERID}", user);

		HttpQuery httpQuery = new HttpQuery(effectiveEndpointURL + "Query");
		httpQuery.addParam("query", sparqlQuery);

		/*
		 * añadimos parámetros para strabon
		 */

		httpQuery.addParam("view", "HTML");
		httpQuery.addParam("format", "SPARQL/JSON");
		httpQuery.addParam("handle", "download");

		try {
			InputStream in = httpQuery.exec();
			String obtained = IOUtils.toString(in,"UTF-8");
			JSONObject json = new JSONObject(obtained);
			
			JSONObject results=json.getJSONObject("results");
			JSONArray bindings=results.getJSONArray("bindings");
			HashMap<String, String> featuresCommented=new HashMap<String, String>();
			for(int i=0;i<bindings.length();i++){
				JSONObject binding=bindings.getJSONObject(i);
				//uri
				JSONObject idFeat=binding.getJSONObject("idFeat");
				String uri=idFeat.getString("value");
				
				//date
				JSONObject dateO=binding.getJSONObject("date");
				String date=dateO.getString("value");
				
				// if uri represents a mapping, we need to obtaing features involved
				String relatedFeat1=null;
				String relatedFeat2=null;
				try{
					JSONObject rel1=binding.getJSONObject("commentsId1");
					if(rel1 !=null){
						relatedFeat1=rel1.getString("value");
					}
					
					JSONObject rel2=binding.getJSONObject("commentsId2");
					if(rel2 !=null){
						relatedFeat2=rel2.getString("value");
					}
				}catch(Exception e){}
				if(relatedFeat1!=null || relatedFeat2!=null){
					if(relatedFeat1!=null){
						featuresCommented.put(date+"-"+relatedFeat1, relatedFeat1);
					}
					if(relatedFeat2!=null){
						featuresCommented.put(date+"-"+relatedFeat2, relatedFeat2);
					}
				}else{
					featuresCommented.put(date+"-"+uri, uri);
				}
			}
			
			JSONObject toReturn=new JSONObject();
			JSONArray jaFeatures=new JSONArray();
			Iterator<String> featIt=featuresCommented.keySet().iterator();
			while(featIt.hasNext()){
				String key=featIt.next();
				Iterator<String> keys = strabonEndpoints.keySet().iterator();
				String strabonendpoint=null;
				String label=null;
				String dataset=null;
				String feat=featuresCommented.get(key);
				while (keys.hasNext() && strabonendpoint == null) {
					String ep = keys.next();
					
					if (feat.startsWith(ep)) {
						strabonendpoint = strabonEndpoints.get(ep);
					}
				}
				if(strabonendpoint==null){
					if(feat.startsWith(dbpediaResourceOriginalSufix)){
						ArrayList<String> dbpediaids=new ArrayList<String>();
						dbpediaids.add(feat);
						JSONArray jsonDbpedia=getDBPediaFeatures(dbpediaids);
						JSONObject featureDbpedia=jsonDbpedia.getJSONObject(0);
						JSONObject properties=featureDbpedia.getJSONObject("properties");
						label=properties.getString("label");
						dataset=properties.getString("p");
					}
					else{
						strabonendpoint=effectiveEndpointURL;
					}
				}
				if(strabonendpoint!=null){
					String sparqlGetDatasetAndLabelReplaced=sparqlGetDatasetAndLabel.replace("${ID}", "<"+feat+">");
					if(!strabonendpoint.endsWith("/")){
						strabonendpoint=strabonendpoint+"/";
					}
					HttpQuery httpQuery2 = new HttpQuery(strabonendpoint + "Query");
					httpQuery2.addParam("query", sparqlGetDatasetAndLabelReplaced);

					/*
					 * añadimos parámetros para strabon
					 */

					httpQuery2.addParam("view", "HTML");
					httpQuery2.addParam("format", "SPARQL/JSON");
					httpQuery2.addParam("handle", "download");

					try {
						in = httpQuery2.exec();
						obtained = IOUtils.toString(in,"UTF-8");
						json = new JSONObject(obtained);
						
						results=json.getJSONObject("results");
						bindings=results.getJSONArray("bindings");
						JSONObject binding=bindings.getJSONObject(0);
						JSONObject labelO=binding.getJSONObject("label");
						label=labelO.getString("value");
						JSONObject datasetO=binding.getJSONObject("dataset");
						dataset=datasetO.getString("value");
					}catch(Exception e){}
				}
				
				
				
				String uri=featuresCommented.get(key);
				if(uri.startsWith(dbpediaResourceOriginalSufix)){
					uri=uri.replace(dbpediaResourceOriginalSufix+"/", dbpediaRedirectPrefix);
					key=key.replace(dbpediaResourceOriginalSufix+"/", dbpediaRedirectPrefix);
					
				}
				String date=key.replace(uri, "");
				date=date.replace("-", "");
				JSONObject position=new JSONObject();
				
				position.put("uri", uri);
				position.put("date", date);
				position.put("label", label);
				position.put("dataset", dataset);
				jaFeatures.put(position);
			}
			toReturn.put("featuresCommented", jaFeatures);
			Resource r = model.getResource(resourceURI);
			Property p = model
					.getProperty("http://linkedmap.unizar.es/lms#jsonRepresentation");
			model.add(r, p, StringEscapeUtils.escapeJson(toReturn.toString()));
			return model;
		}catch(Exception e){}
		return null;
	}

	public Model deepDescribe(String resourceURI) {
		String originalURI=resourceURI;
		String selectedEndpoint = null;
		if(resourceURI.startsWith("http://linkedmap.unizar.es/id/data/DBPEDIA-")){
			resourceURI=resourceURI.replace("http://linkedmap.unizar.es/id/data/DBPEDIA-", dbpediaResourceOriginalSufix+"/");
		}
		if (strabonEndpoints != null && !strabonEndpoints.isEmpty()) {
			Iterator<String> keys = strabonEndpoints.keySet().iterator();
			while (keys.hasNext() && selectedEndpoint == null) {
				String key = keys.next();
				if (resourceURI.startsWith(key)) {
					selectedEndpoint = strabonEndpoints.get(key);
				}
			}
		}

		if (selectedEndpoint == null) {
			selectedEndpoint = endpointURL;
		}

		Model model = ModelFactory.createDefaultModel();
		/*for (String query : resourceQueries) {
			Model result = execQueryGraph(preProcessQuery(query, resourceURI),
					selectedEndpoint);
			model.add(result);
			try {
				String provenance = result
						.getProperty(
								result.getResource(resourceURI),
								result.getProperty("http://linkedmap.unizar.es/def/prov-li#primaryTopic"))
						.asTriple().getObject().toString();
				Model prov = execQueryGraph(preProcessQuery(query, provenance));
				model.add(prov);
			} catch (Exception e) {
				// error al obtener la provenance
			}
			try {
				String geometry = result
						.getProperty(
								result.getResource(resourceURI),
								result.getProperty("http://www.opengis.net/ont/geosparql#hasGeometry"))
						.asTriple().getObject().toString();
				Model geom = execQueryGraph(preProcessQuery(query, geometry));
				model.add(geom);
			} catch (Exception e) {
				// error al obtener la geometria
			}
			try {
				String mappingsQuery = "CONSTRUCT"
						+ "{"
						+ " ?id ?p ?ent"
						+ "}WHERE{"
						+ "            		{"
						+ "            		 ?id a <http://knowledgeweb.semanticweb.org/heterogeneity/alignment#Cell> ."
						+ "            		 ?id <http://knowledgeweb.semanticweb.org/heterogeneity/alignment#entity2>   <"
						+ resourceURI
						+ "> ."
						+ "            		 ?id ?p ?ent ."
						+ "            		}UNION{"
						+ "            		 ?id a <http://knowledgeweb.semanticweb.org/heterogeneity/alignment#Cell> ."
						+ "            		 ?id <http://knowledgeweb.semanticweb.org/heterogeneity/alignment#entity1>  <"
						+ resourceURI + "> ." + "            		 ?id ?p ?ent"
						+ "            		}" + "            		}";
				Model mappings = execQueryGraph(mappingsQuery);
				model.add(mappings);

			} catch (Exception e) {
				// error al obtener los mappings
			}
			model.setNsPrefixes(result);
		}
*/
		String jsonResult = null;

		List<String> featuresToDescribe = new ArrayList<String>();
		List<String> dbPediaFeatures = new ArrayList<String>();
		if(resourceURI.startsWith(dbpediaResourceOriginalSufix)){
			dbPediaFeatures.add(resourceURI);
		}else{
			featuresToDescribe.add(resourceURI);
		}

		String relatedFeatures = findRelatedEntities(resourceURI);
		if (relatedFeatures != null) {
			JSONObject json = new JSONObject(relatedFeatures);
			json = json.getJSONObject("results");
			JSONArray bindings = json.getJSONArray("bindings");
			for (int i = 0; i < bindings.length(); i++) {
				JSONObject binding = bindings.getJSONObject(i);
				JSONObject ent = binding.getJSONObject("ent");
				String relatedFeature = ent.getString("value");
				if (relatedFeature.startsWith(dbpediaResourceOriginalSufix)) {
					dbPediaFeatures.add(relatedFeature);
				} else {
					featuresToDescribe.add(relatedFeature);
				}
			}
			jsonResult = "\"mappings\":" + relatedFeatures;
		}

		String describedFeatures = getFeaturesGeoJSON(featuresToDescribe);
		JSONArray describedDBPediaFeatures = getDBPediaFeatures(dbPediaFeatures);
		if (describedFeatures != null) {
			if (describedDBPediaFeatures != null
					&& describedDBPediaFeatures.length() > 0) {
				JSONObject descFeat = new JSONObject(describedFeatures);
				JSONArray features = descFeat.getJSONArray("features");
				for (int i = 0; i < describedDBPediaFeatures.length(); i++) {
					features.put(describedDBPediaFeatures.get(i));
				}
				describedFeatures =  descFeat.toString();
			}

			if (jsonResult == null) {
				jsonResult = "\"features\":" + describedFeatures + "";
			} else {
				jsonResult = "\"features\":" + describedFeatures + ","
						+ jsonResult;
			}
		}

		if (jsonResult != null) {
			jsonResult = "{ \"view\": \"resourceDetail\", " + jsonResult + "}";
			Resource r = model.getResource(originalURI);
			Property p = model
					.getProperty("http://linkedmap.unizar.es/lms#jsonRepresentation");
			model.add(r, p, StringEscapeUtils.escapeJson(jsonResult));
		}
		return model;
	}

	public JSONArray getDBPediaFeatures(List<String> features) {
		JSONArray toReturn = new JSONArray();
		Iterator<String> featuresIt = features.iterator();
		while (featuresIt.hasNext()) {
			String originalFeature = featuresIt.next();
			String feature = originalFeature;
			feature = feature.replace(dbpediaResourceOriginalSufix,
					dbpediaReplaceUris);
			feature = feature.trim() + dbpediaResourceExtension;
			HttpQuery httpQuery = new HttpQuery(feature);
			httpQuery
					.setAccept("text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
			Model model = ModelFactory.createDefaultModel();
			InputStream in = httpQuery.exec();
			RDFDataMgr.read(model, in, Lang.NTRIPLES);
			if (!model.isEmpty()) {
				JSONObject obj = new JSONObject();
				obj.put("type", "Feature");
				JSONObject crs = new JSONObject();
				JSONObject crsProps = new JSONObject();
				crsProps.put("name", "EPSG:4326");
				crs.put("properties", crsProps);
				crs.put("type", "name");
				obj.put("crs", crs);
				JSONObject geometry = new JSONObject();
				geometry.put("type", "Point");
				JSONArray coordinates = new JSONArray();
				String point = model
						.getProperty(
								model.getResource(originalFeature),
								model.getProperty("http://www.georss.org/georss/point"))
						.asTriple().getObject().getLiteralValue().toString();
				if (point != null) {
					point = point.trim().replaceFirst(" ", ",");
					String[] pointV = point.split("\\,");
					for (int i = pointV.length - 1; i >= 0; i--) {
						coordinates.put(Float.parseFloat(pointV[i].trim()));
					}
				}
				geometry.put("coordinates", coordinates);
				obj.put("geometry", geometry);
				JSONObject properties = new JSONObject();
				properties.put("id", originalFeature);
				String label = model
						.getProperty(
								model.getResource(originalFeature),
								model.getProperty("http://www.w3.org/2000/01/rdf-schema#label"))
						.asTriple().getObject().getLiteralValue().toString();
				properties.put("label", label);
				properties.put("prov", "DBPedia feature, see " + feature);
				properties.put("t", "http://www.w3.org/2002/07/owl#Thing");
				properties.put("p", "http://es.dbpedia.org");
				List<String> commentsIds = getComments(originalFeature);
				JSONArray jarray = new JSONArray();
				for (int j = 0; j < commentsIds.size(); j++) {
					JSONArray comment = describeComment(commentsIds.get(j));
					for (int m = 0; m < comment.length(); m++) {
						jarray.put(comment.get(m));
					}
				}
				properties.put("comments", jarray);
				obj.put("properties", properties);
				obj.put("id", "fid_" + originalFeature);
				toReturn.put(obj);
			}
		}
		return toReturn;
	}

	public String getFeaturesGeoJSON(List<String> features) {
		HashMap<String, List<String>> queries = new HashMap<String, List<String>>();
		if (strabonEndpoints != null && !strabonEndpoints.isEmpty()) {
			Iterator<String> keys = strabonEndpoints.keySet().iterator();
			while (keys.hasNext()) {
				String key = keys.next();
				ArrayList<String> featuresCopy = new ArrayList<String>(features);
				Iterator<String> featuresIt = featuresCopy.iterator();
				while (featuresIt.hasNext()) {
					String feature = featuresIt.next();
					if (feature.startsWith(key)) {
						features.remove(feature);
						String endpoint = strabonEndpoints.get(key);
						if (queries.containsKey(endpoint)) {
							queries.get(endpoint).add(feature);
						} else {
							ArrayList<String> feats = new ArrayList<String>();
							feats.add(feature);
							queries.put(endpoint, feats);
						}
					}
				}
			}
		}
		if (!features.isEmpty()) {
			if (queries.containsKey(endpointURL)) {
				queries.get(endpointURL).addAll(features);
			} else {
				queries.put(endpointURL, features);
			}
		}

		// -- once endpoints for each feature is selected, sparql queries are
		// launched:
		Iterator<String> endpointsIt = queries.keySet().iterator();
		ArrayList<JSONObject> objs = new ArrayList<JSONObject>();
		ArrayList<String> processedFeatures=new ArrayList<String>();
		while (endpointsIt.hasNext()) {
			String endpoint = endpointsIt.next();
			List<String> selectedFeatures = queries.get(endpoint);
			String sparqlQuery = "select ?id ?label ?p ?y ?t ?prov where{   ?id <http://www.opengis.net/ont/geosparql#hasGeometry> ?fGeom.   ?id <http://www.w3.org/2000/01/rdf-schema#label> ?label .   ?id <http://purl.org/dc/terms/isPartOf> ?p .   ?fGeom <http://www.opengis.net/ont/geosparql#asWKT> ?y .   ?id <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ?t .   ?id <http://linkedmap.unizar.es/def/prov-li#primaryTopic> ?pEnt .   ?pEnt <http://www.w3.org/ns/prov#value> ?prov . ${FILTER}  }limit 100";

			String filter = "FILTER ((?t != <http://www.opengis.net/ont/geosparql#Feature>)&&(${FEATURES_IDS}))";

			String featuresId = null;
			Iterator<String> featuresIt = selectedFeatures.iterator();
			while (featuresIt.hasNext()) {
				String feature = featuresIt.next();

				if (featuresId == null) {
					featuresId = "(?id = <" + feature + ">)";
				} else {
					featuresId = featuresId + " || " + "(?id = <" + feature
							+ ">)";
				}
			}

			filter = filter.replace("${FEATURES_IDS}", featuresId);
			sparqlQuery = sparqlQuery.replace("${FILTER}", filter);

			String effectiveEndpointURL = endpoint;
			if (!endpointURL.endsWith("/")) {
				effectiveEndpointURL = endpoint + "/";
			}

			HttpQuery httpQuery = new HttpQuery(effectiveEndpointURL + "Query");
			httpQuery.addParam("query", sparqlQuery);

			/*
			 * añadimos parámetros para strabon
			 */

			httpQuery.addParam("view", "HTML");
			httpQuery.addParam("format", "GeoJSON");
			httpQuery.addParam("handle", "download");

			try {
				InputStream in = httpQuery.exec();
				String toReturn = IOUtils.toString(in,"UTF-8");

				JSONObject obj = new JSONObject(toReturn);
				JSONArray featuresArray = obj.getJSONArray("features");
				JSONArray featuresToIntroduce = new JSONArray();
				for (int i = 0; i < featuresArray.length(); i++) {
					JSONObject feature = featuresArray.getJSONObject(i);
					JSONObject properties = feature.getJSONObject("properties");
					String id = properties.getString("id");
					if(!processedFeatures.contains(id)){
						processedFeatures.add(id);
						List<String> commentsIds = getComments(id);
						JSONArray jarray = new JSONArray();
						for (int j = 0; j < commentsIds.size(); j++) {
							JSONArray comment = describeComment(commentsIds.get(j));
							for (int m = 0; m < comment.length(); m++) {
								jarray.put(comment.get(m));
							}
						}
						properties.put("comments", jarray);
						featuresToIntroduce.put(feature);
					}
				}
				JSONObject objProcessed=new JSONObject();
				objProcessed.put("features",featuresToIntroduce);
				objProcessed.put("type", "FeatureCollection");
				//objProcessed.append(key, value)("features", featuresToIntroduce);
				objs.add(objProcessed);
			} catch (Exception e) {
			}
		}

		if (objs != null && objs.size() == 1) {
			return objs.get(0).toString();
		} else if (objs != null) {
			JSONObject toReturn = objs.get(0);
			for (int i = 1; i < objs.size(); i++) {
				JSONObject obj = objs.get(i);
				JSONArray featuresArray = obj.getJSONArray("features");
				for(int j=0;j<featuresArray.length();j++){
					toReturn.getJSONArray("features").put(featuresArray.get(j));
				}
			}
			return toReturn.toString();
		}

		// -- falta merge de objs
		return null;
	}

	public String findRelatedEntities(String resourceURI) {
		String sparqlQuery = "select ?ent ?measure ?mapId { {  ?mapId a <http://knowledgeweb.semanticweb.org/heterogeneity/alignment#Cell> .  ?mapId <http://knowledgeweb.semanticweb.org/heterogeneity/alignment#entity2>  ${resourceURI} .  ?mapId <http://knowledgeweb.semanticweb.org/heterogeneity/alignment#entity1> ?ent . ?mapId <http://knowledgeweb.semanticweb.org/heterogeneity/alignment#measure>  ?measure }UNION{  ?mapId a <http://knowledgeweb.semanticweb.org/heterogeneity/alignment#Cell> .  ?mapId <http://knowledgeweb.semanticweb.org/heterogeneity/alignment#entity1>  ${resourceURI} .  ?mapId <http://knowledgeweb.semanticweb.org/heterogeneity/alignment#entity2> ?ent . ?mapId <http://knowledgeweb.semanticweb.org/heterogeneity/alignment#measure>  ?measure }}";

		sparqlQuery = sparqlQuery.replace("${resourceURI}", "<" + resourceURI
				+ ">");

		String effectiveEndpointURL = endpointURL;
		if (!endpointURL.endsWith("/")) {
			effectiveEndpointURL = endpointURL + "/";
		}

		HttpQuery httpQuery = new HttpQuery(effectiveEndpointURL + "Query");
		httpQuery.addParam("query", sparqlQuery);

		/*
		 * añadimos parámetros para strabon
		 */

		httpQuery.addParam("view", "HTML");
		httpQuery.addParam("format", "SPARQL/JSON");
		httpQuery.addParam("handle", "download");

		try {
			InputStream in = httpQuery.exec();
			String toReturn = IOUtils.toString(in,"UTF-8");
			JSONObject json = new JSONObject(toReturn);

			JSONObject results = json.getJSONObject("results");
			JSONArray bindings = results.getJSONArray("bindings");
			for (int i = 0; i < bindings.length(); i++) {
				JSONObject obj = bindings.getJSONObject(i);
				JSONObject mapIdObj = obj.getJSONObject("mapId");
				String mapId = mapIdObj.getString("value");
				List<String> commentsIds = getComments(mapId);
				JSONArray jarray = new JSONArray();
				for (int j = 0; j < commentsIds.size(); j++) {
					JSONArray comment = describeComment(commentsIds.get(j));
					for (int m = 0; m < comment.length(); m++) {
						jarray.put(comment.get(m));
					}
				}
				obj.put("comments", jarray);
			}

			return json.toString();
		} catch (Exception e) {
		}

		return null;

	}

	public JSONArray describeComment(String commentId) {
		String sparqlQuery = "select ?value ?comment ?creator ?date where{ <${commentId}> <http://www.w3.org/2000/01/rdf-schema#value> ?value .  <${commentId}> <http://www.w3.org/2000/01/rdf-schema#comment> ?comment .  <${commentId}> <http://purl.org/dc/elements/1.1/creator> ?creator . <${commentId}> <http://purl.org/dc/elements/1.1/date> ?date . }";

		sparqlQuery = sparqlQuery.replace("${commentId}", commentId);

		String effectiveEndpointURL = endpointURL;
		if (!endpointURL.endsWith("/")) {
			effectiveEndpointURL = endpointURL + "/";
		}

		HttpQuery httpQuery = new HttpQuery(effectiveEndpointURL + "Query");
		httpQuery.addParam("query", sparqlQuery);

		/*
		 * añadimos parámetros para strabon
		 */

		httpQuery.addParam("view", "HTML");
		httpQuery.addParam("format", "SPARQL/JSON");
		httpQuery.addParam("handle", "download");

		try {
			InputStream in = httpQuery.exec();
			String toReturn = IOUtils.toString(in,"UTF-8");
			JSONObject json = new JSONObject(toReturn);
			JSONObject results = json.getJSONObject("results");
			JSONArray bindings = results.getJSONArray("bindings");
			return bindings;
		} catch (Exception e) {
		}
		return null;
	}

	public List<String> getComments(String mapId) {
		String sparqlQuery = "select ?commentId where{  ?commentId <http://xmlns.com/foaf/0.1/primaryTopic> <${mapId}> }";

		sparqlQuery = sparqlQuery.replace("${mapId}", mapId);

		String effectiveEndpointURL = endpointURL;
		if (!endpointURL.endsWith("/")) {
			effectiveEndpointURL = endpointURL + "/";
		}

		HttpQuery httpQuery = new HttpQuery(effectiveEndpointURL + "Query");
		httpQuery.addParam("query", sparqlQuery);

		/*
		 * añadimos parámetros para strabon
		 */

		httpQuery.addParam("view", "HTML");
		httpQuery.addParam("format", "SPARQL/JSON");
		httpQuery.addParam("handle", "download");

		try {
			InputStream in = httpQuery.exec();
			String toReturn = IOUtils.toString(in,"UTF-8");
			JSONObject json = new JSONObject(toReturn);
			ArrayList<String> commentsId = new ArrayList<String>();
			JSONObject results = json.getJSONObject("results");
			JSONArray bindings = results.getJSONArray("bindings");
			for (int i = 0; i < bindings.length(); i++) {
				JSONObject res = bindings.getJSONObject(i);
				JSONObject commentObj = res.getJSONObject("commentId");
				commentsId.add(commentObj.getString("value"));
			}
			return commentsId;
		} catch (Exception e) {
		}
		return new ArrayList<String>();
	}

	@Override
	public Map<Property, Integer> getHighIndegreeProperties(String resourceURI) {
		return getHighDegreeProperties("SELECT ?p (COUNT(?s) AS ?count) "
				+ "WHERE { " + "  ?s ?p ?__this__. "
				+ "  FILTER (?p IN ?__high_indegree_properties__)" + "}"
				+ "GROUP BY ?p", resourceURI);
	}

	@Override
	public Map<Property, Integer> getHighOutdegreeProperties(String resourceURI) {
		return getHighDegreeProperties("SELECT ?p (COUNT(?o) AS ?count) "
				+ "WHERE { " + "  ?__this__ ?p ?o. "
				+ "  FILTER (?p IN ?__high_outdegree_properties__)" + "}"
				+ "GROUP BY ?p", resourceURI);
	}

	private Map<Property, Integer> getHighDegreeProperties(String query,
			String resourceURI) {
		if (!supportsSPARQL11)
			return null;
		query = preProcessQuery(query, resourceURI);
		ResultSet rs = execQuerySelect(query);
		Map<Property, Integer> results = new HashMap<Property, Integer>();
		while (rs.hasNext()) {
			QuerySolution solution = rs.next();
			if (!solution.contains("p") || !solution.contains("count"))
				continue;
			Resource p = solution.get("p").asResource();
			int count = solution.get("count").asLiteral().getInt();
			results.put(ResourceFactory.createProperty(p.getURI()), count);
		}
		return results;
	}

	@Override
	public Model listPropertyValues(String resourceURI, Property property,
			boolean isInverse) {
		// Loop over the queries, join results in a single model.
		// Process each query to replace place-holders of the given resource and
		// property.
		List<String> queries = new ArrayList<String>();
		queries.addAll(isInverse ? inversePropertyQueries : propertyQueries);
		queries.addAll(isInverse ? anonInversePropertyQueries
				: anonPropertyQueries);
		Model model = ModelFactory.createDefaultModel();
		for (String query : queries) {
			String preprocessed = preProcessQuery(query, resourceURI, property);
			Model result = execQueryGraph(preprocessed);
			model.add(result);
			model.setNsPrefixes(result);
		}
		return model;
	}

	@Override
	public List<Resource> getIndex() {

		List<Resource> result = new ArrayList<Resource>();
		try {
			ResultSet rs = execQuerySelect("SELECT DISTINCT ?s { "
					+ "?s ?p ?o " + "FILTER (isURI(?s)) " + "} LIMIT "
					+ DataSource.MAX_INDEX_SIZE, 10000);
			while (rs.hasNext()) {
				result.add(rs.next().getResource("s"));
			}
			if (result.size() < DataSource.MAX_INDEX_SIZE) {
				rs = execQuerySelect("SELECT DISTINCT ?o { " + "?s ?p ?o "
						+ "FILTER (isURI(?o)) " + "} LIMIT "
						+ (DataSource.MAX_INDEX_SIZE - result.size()));
				while (rs.hasNext()) {
					result.add(rs.next().getResource("o"));
				}
			}
		} catch (Exception e) {
			// if an exception is thrown then we return an empty list
			result = new ArrayList<Resource>();
		}
		return result;
	}

	public String getPreviousDescribeQuery() {
		return previousDescribeQuery;
	}

	private Model execQueryGraph(String query) {
		return execQueryGraph(query, endpointURL);
	}

	private Model execQueryGraph(String query, String endpoint) {
		Model model = ModelFactory.createDefaultModel();
		previousDescribeQuery = query;

		// Since we don't know the exact query type (e.g. DESCRIBE or
		// CONSTRUCT),
		// and com.hp.hpl.jena.query.QueryFactory could throw exceptions on
		// vendor-specific sections of the query, we use the lower-level
		// com.hp.hpl.jena.sparql.engine.http.HttpQuery to execute the query and
		// read the results into model.
		String effectiveEndpointURL = endpoint;
		if (!endpointURL.endsWith("/")) {
			effectiveEndpointURL = endpointURL + "/";
		}

		HttpQuery httpQuery = new HttpQuery(effectiveEndpointURL + "Describe");
		httpQuery.addParam("query", query);

		/*
		 * añadimos parámetros para strabon
		 */

		httpQuery.addParam("view", "HTML");
		httpQuery.addParam("format", "RDF/XML");
		httpQuery.addParam("handle", "download");

		if (defaultGraphURI != null) {
			httpQuery.addParam("default-graph-uri", defaultGraphURI);
		}
		for (String[] param : queryParamsGraph) {
			httpQuery.addParam(param[0], param[1]);
		}

		// The rest is more or less a copy of QueryEngineHTTP.execModel()
		httpQuery.setAccept(contentType);
		try {
			InputStream in = httpQuery.exec();

			// Don't assume the endpoint actually gives back the content type we
			// asked for
			String actualContentType = httpQuery.getContentType();

			// If the server fails to return a Content-Type then we will assume
			// the server returned the type we asked for
			if (actualContentType == null || actualContentType.equals("")) {
				actualContentType = contentType;
			}

			// Try to select language appropriately here based on the model
			// content
			// type
			Lang lang = WebContent.contentTypeToLang(actualContentType);
			if (!RDFLanguages.isTriples(lang))
				throw new QueryException("Endpoint <" + endpointURL
						+ "> returned Content Type: " + actualContentType
						+ " which is not a supported RDF graph syntax");
			RDFDataMgr.read(model, in, lang);

			// Skip prefixes ns1, ns2, etc, which are usually
			// auto-assigned by the endpoint and do more harm than good
			for (String prefix : model.getNsPrefixMap().keySet()) {
				if (prefix.matches("^ns[0-9]+$")) {
					model.removeNsPrefix(prefix);
				}
			}
		} catch (Exception e) {
		}
		return model;
	}

	private ResultSet execQuerySelect(String query) {
		return execQuerySelect(query, -1);
	}

	private ResultSet execQuerySelect(String query, long timeOut) {
		String effectiveEndpointURL = endpointURL;
		if (!endpointURL.endsWith("/")) {
			effectiveEndpointURL = endpointURL + "/";
		}
		QueryEngineHTTP endpoint = new QueryEngineHTTP(effectiveEndpointURL
				+ "Query", query);
		if (timeOut > 0) {
			endpoint.setTimeout(timeOut);
		}
		if (defaultGraphURI != null) {
			endpoint.setDefaultGraphURIs(Collections
					.singletonList(defaultGraphURI));
		}
		for (String[] param : queryParamsSelect) {
			endpoint.addParam(param[0], param[1]);
		}
		endpoint.addParam("view", "HTML");
		endpoint.addParam("format", "SPARQL/JSON");
		endpoint.addParam("handle", "download");
		// endpoint.addParam("query", query);
		return endpoint.execSelect();
	}

	private String preProcessQuery(String query, String resourceURI) {
		return preProcessQuery(query, resourceURI, null);
	}

	private String preProcessQuery(String query, String resourceURI,
			Property property) {
		String result = replaceString(query, "?__this__", "<" + resourceURI
				+ ">");
		if (property != null) {
			result = replaceString(result, "?__property__",
					"<" + property.getURI() + ">");
		}
		result = replaceString(result, "?__high_indegree_properties__",
				toSPARQLArgumentList(highIndegreeProperties == null ? null
						: highIndegreeProperties.get()));
		result = replaceString(result, "?__high_outdegree_properties__",
				toSPARQLArgumentList(highOutdegreeProperties == null ? null
						: highOutdegreeProperties.get()));
		return result;
	}

	private String replaceString(String text, String searchString,
			String replacement) {
		int start = 0;
		int end = text.indexOf(searchString, start);
		if (end == -1) {
			return text;
		}

		int replacementLength = searchString.length();
		StringBuffer buf = new StringBuffer();
		while (end != -1) {
			buf.append(text.substring(start, end)).append(replacement);
			start = end + replacementLength;
			end = text.indexOf(searchString, start);
		}
		buf.append(text.substring(start));
		return buf.toString();
	}

	private String toSPARQLArgumentList(Collection<? extends RDFNode> values) {
		if (values == null)
			return "()";
		StringBuilder result = new StringBuilder();
		result.append('(');
		boolean isFirst = true;
		for (RDFNode term : values) {
			if (!isFirst) {
				result.append(", ");
			}
			if (term.isURIResource()) {
				result.append('<');
				result.append(term.asResource().getURI());
				result.append('>');
			} else {
				throw new IllegalArgumentException(
						"toSPARQLArgumentList is only implemented for URIs; "
								+ "called with term " + term);
			}
			isFirst = false;
		}
		result.append(')');
		return result.toString();
	}

}
