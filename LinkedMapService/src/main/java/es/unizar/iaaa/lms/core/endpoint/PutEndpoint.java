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

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.IOUtils;
import org.springframework.http.MediaType;

import com.hp.hpl.jena.sparql.engine.http.HttpQuery;

import es.unizar.iaaa.lms.store.RdfConfiguration;
import es.unizar.iaaa.lms.store.RdfResource;

public class PutEndpoint extends AbstractEndpoint {

	String sparqlEndpoint;

	String deleteEditTriples = "DELETE {?id ?p ?o} WHERE {?id ?p ?o . FILTER (?id = <${editID}>)}";
	String insertEditTriples = "INSERT DATA INTO <http://linkedmap.unizar.es/graph/UserEdits> { ${triples} }";

	String targetGraph = "http://linkedmap.unizar.es/graph/UserEdits";

	ArrayList<String> editComments = new ArrayList<String>();
	
    public PutEndpoint(String path, String description, String sparqlEndpoint) {
        super("PUT", path, description);
        editComments.add(deleteEditTriples);
		this.sparqlEndpoint = sparqlEndpoint;
    }
    
    @Override
	public RdfResource description(HttpServletRequest request,
			MediaType mediaType, RdfConfiguration config) {
		String s=null;
		try {
			s = IOUtils.toString(request.getInputStream(),"UTF-8");
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		String describedResource = getPrimaryTopicUri(request);
		try {
			describedResource=URLDecoder.decode(describedResource.substring(describedResource.lastIndexOf("/")+1),"UTF-8");
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		String auxDeleteEditTriples=deleteEditTriples.replace("${editID}", describedResource);
		HttpQuery httpQuery = new HttpQuery(sparqlEndpoint);
		httpQuery.setBasicAuthentication("endpoint", "3ndpo1nt".toCharArray());
		httpQuery.addParam("format", "HTML");
		httpQuery.addParam("query", auxDeleteEditTriples);
		httpQuery.addParam("submit", "Update");
		httpQuery.addParam("view", "HTML");
		httpQuery.addParam("handle", "plain");
		try {
			InputStream in = httpQuery.exec();
			System.out.println(IOUtils.toString(in));
		} catch (Exception e) {
			e.printStackTrace();
		}

		return super.description(request, mediaType, config);
	}

}
