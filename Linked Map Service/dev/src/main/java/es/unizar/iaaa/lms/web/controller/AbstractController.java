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

import java.util.List;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import es.unizar.iaaa.lms.core.domain.ServerConfiguration;
import es.unizar.iaaa.lms.core.endpoint.Endpoint;
import es.unizar.iaaa.lms.util.Link;

public abstract class AbstractController {

    @Autowired
    protected ServerConfiguration configuration;
    protected Endpoint endpoint;

    public AbstractController() {
        super();
    }

    public void mergeHeadersFromResponseEntity(HttpHeaders headers, ResponseEntity<?> proxiedResponse) {
        for (Entry<String, List<String>> header : proxiedResponse.getHeaders()
            .entrySet()) {
            for (String value : header.getValue()) {
                headers.add(header.getKey(), value);
            }
        }
    }

    public void mergeHeadersFromHttpServletResponse(HttpHeaders headers, HttpServletResponse response) {
        for (String header : response.getHeaderNames()) {
            for (String value : response.getHeaders(header)) {
                headers.add(header, value);
            }
        }
    }

    public void addCanonicalHeader(HttpHeaders headers, HttpServletRequest request, String extension) {
        StringBuffer sb = new StringBuffer(request.getRequestURL().toString());
        if (!sb.toString().endsWith(extension)) {
            sb.append(extension);
        }
        headers.add("Link", new Link(sb.toString()).withCanonicalRel()
            .toString());
    }

    public HttpHeaders linearize(HttpHeaders headers) {
        HttpHeaders hh = new HttpHeaders();
        for (Entry<String, List<String>> header : headers.entrySet()) {
            StringBuffer sb = new StringBuffer();
            for (String value : header.getValue()) {
                sb.append(sb.length() == 0 ? "" : ", ");
                sb.append(value);
            }
            hh.set(header.getKey(), sb.toString());
        }
        return HttpHeaders.readOnlyHttpHeaders(hh);
    }

    protected ResponseEntity<String> notFound(Endpoint endpoint, HttpServletRequest request,
            HttpServletResponse response, boolean varying) {
        HttpHeaders headers = new HttpHeaders();
        mergeHeadersFromHttpServletResponse(headers, response);
        if (varying) {
            headers.add("Vary", "Accept");
        }
        return new ResponseEntity<String>(
            "Not found the the resource for the representation URI "
                    + request.getRequestURL(), linearize(headers),
            HttpStatus.NOT_FOUND);
    }

    protected ResponseEntity<String> notAcceptable(Endpoint endpoint, HttpServletRequest request,
            HttpServletResponse response, boolean varying) {
        HttpHeaders headers = new HttpHeaders();
        mergeHeadersFromHttpServletResponse(headers, response);
        if (varying) {
            headers.add("Vary", "Accept");
        }
        return new ResponseEntity<String>(
            "The requested format is not acceptable for the representation URI "
                    + request.getRequestURL(), linearize(headers),
            HttpStatus.NOT_ACCEPTABLE);
    }

    protected ResponseEntity<String> notHandled(HttpServletRequest request, HttpServletResponse response) {
        return new ResponseEntity<String>(
            "Not handler found for the representation URI "
                    + request.getRequestURL(), HttpStatus.NOT_FOUND);
    }

    public Endpoint getEndpoint() {
        return endpoint;
    }

}
