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

import java.net.URI;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import es.unizar.iaaa.lms.store.RdfConfiguration;
import es.unizar.iaaa.lms.store.RdfResource;
import es.unizar.iaaa.lms.util.Link;

@Controller
public class RedirectionController extends AbstractController {

    @Autowired
    private RdfConfiguration config;

    @RequestMapping(value = { "/id/{container}", "/def/{container}",
            "/id/{container}/{resource}", "/def/{container}/{resource}", "/id/{container}/{resource}/{version}" })
    public HttpEntity<String> redirect(HttpServletRequest request,
            HttpServletResponse response) {
    	
        endpoint = configuration.findEndpoint(request);
        if (endpoint == null) {
            return notHandled(request, response);
        }
        /*if (!endpoint.accepts(request)) {
            return notAcceptable(endpoint, request, response, true);
        }*/

        RdfResource description = endpoint.description(request, null, config);
        HttpHeaders headers = new HttpHeaders();
        headers
            .add("Link", new Link(description.getResource().getURI()).toString());

        mergeHeadersFromHttpServletResponse(headers, response);
        headers.add("Vary", "Accept");
        headers.setLocation(URI.create(description.getRepresentation().getResource().getURI()));
        return new ResponseEntity<String>(null, linearize(headers),
            HttpStatus.SEE_OTHER);
    }
}
