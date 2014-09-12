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

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriTemplate;

import es.unizar.iaaa.lms.store.RdfConfiguration;
import es.unizar.iaaa.lms.store.RdfResource;
import es.unizar.iaaa.lms.web.matcher.HttpServletRequestMatcher;

public interface Endpoint extends HttpServletRequestMatcher {

    public boolean accepts(HttpServletRequest request);

    public RdfResource description(HttpServletRequest request, MediaType mediaType, RdfConfiguration config);

    public List<MediaType> getContentTypes();

    public MediaType getDefaultContentType();

    public String getDescription();

    public UriTemplate getFromTemplate();

    public String getImplementationNotes();

    public String getMethod();

    public List<String> getParameters();

    public String getPath();

    public UriTemplate getRepresentationTemplate();

    public String getResourceTemplateAsString();

    public boolean hasRepository();

    public boolean isRedirect();

    public ResponseEntity<?> proxy(HttpServletRequest request,
        RdfResource description) throws Exception;

    public boolean showLabels();

    @Deprecated
    public void enrich(RdfResource description,
            HttpServletRequest request, HttpHeaders headers);

    public String getExtension(MediaType parse);

}
