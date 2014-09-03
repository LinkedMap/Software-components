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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import es.unizar.iaaa.lms.store.Datastore;
import es.unizar.iaaa.lms.store.RdfConfiguration;
import es.unizar.iaaa.lms.store.RdfRepresentation;
import es.unizar.iaaa.lms.store.RdfResource;
import es.unizar.iaaa.lms.util.Link;

/**
 * A LMS server acts as reverse proxy for WMS servers, that its, the LSM server
 * sits in front of a set of WMS servers and determines where to route a
 * particular request and the LMS server returns its response to the client.
 * Thus, from the point of view of a GIS client, the LMS is a WMS server
 * 
 * @author Francisco J Lopez-Pellicer
 * 
 */

@Controller
public class ApiController extends AbstractController {

    @Autowired
    private Datastore datastore;
    
    @Autowired
    private RdfConfiguration config;

    @RequestMapping("/api/servers/{resource}")
  
    public ResponseEntity<?> data(HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        endpoint = configuration.findEndpoint(request);
        if (endpoint == null) {
            return notHandled(request, response);
        }
        if (!endpoint.accepts(request)) {
            return notAcceptable(endpoint, request, response, false);
        }
        
        String topic = endpoint.getPrimaryTopicUri(request);
        RdfResource description = null;
        if (!datastore.exists(topic)) {
            description = endpoint.description(request, null, config);
            if (description == null) {
                return notFound(endpoint, request, response, false);
            }
            datastore.set(topic, description);
        } else {
            description = datastore.get(topic);
        }

        HttpHeaders headers = new HttpHeaders();
        RdfRepresentation lr = description.getRepresentation();
        try{
        	headers
            	.add("Link", new Link(lr.getResource().getURI()).toString());
        }catch(Exception e){}
        try{
        headers.add("Link", new Link(lr.getPrimaryTopic()).withAboutRel()
            .toString());
        }catch(Exception e){}
        try{
        headers.add("Link", new Link(lr.getCanonical()).withCanonicalRel()
            .toString());
        }catch(Exception e){}
        try{
        for (String al : lr.getHasFormats()) {
            headers.add("Link", new Link(al).withAlternateRel()
                .toString());
        }
        }catch(Exception e){}
        
        mergeHeadersFromHttpServletResponse(headers, response);
        ResponseEntity<?> proxiedResponse = endpoint
            .proxy(request, description);
        mergeHeadersFromResponseEntity(headers, proxiedResponse);
        return new ResponseEntity<Object>(proxiedResponse.getBody(),
            linearize(headers), proxiedResponse.getStatusCode());
    }
}
