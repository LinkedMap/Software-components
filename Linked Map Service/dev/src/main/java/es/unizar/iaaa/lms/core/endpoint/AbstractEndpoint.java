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

import static es.unizar.iaaa.lms.util.Util.baseUrl;
import static org.springframework.http.MediaType.TEXT_HTML;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriTemplate;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;

import es.unizar.iaaa.lms.core.description.ResourceDescription;
import es.unizar.iaaa.lms.pubby.HypermediaControls;
import es.unizar.iaaa.lms.pubby.PubbyRepository;
import es.unizar.iaaa.lms.pubby.negotiation.ContentTypeNegotiator;
import es.unizar.iaaa.lms.pubby.negotiation.MediaRangeSpec;
import es.unizar.iaaa.lms.store.RdfConfiguration;
import es.unizar.iaaa.lms.store.RdfRepresentation;
import es.unizar.iaaa.lms.store.RdfResource;
import es.unizar.iaaa.lms.util.Link;
import es.unizar.iaaa.lms.web.matcher.HttpServletRequestMatcher;
import es.unizar.iaaa.lms.web.matcher.HttpServletRequestMatcherConfiguration;
import es.unizar.iaaa.lms.web.matcher.RepresentationMatcher;

public abstract class AbstractEndpoint implements Endpoint {

    public static final String TEXT_TURTLE_VALUE = "text/turtle";
    public static final String APPLICATION_JSON_VALUE = "application/json";
    public static final String IMAGE_JPEG_VALUE="image/jpeg";

    public final static MediaType TEXT_TURTLE = MediaType.parseMediaType(TEXT_TURTLE_VALUE);
    
    public final static MediaType APPLICATION_JSON=MediaType.parseMediaType(APPLICATION_JSON_VALUE);
    
    public final static MediaType IMAGE_JPEG=MediaType.parseMediaType(IMAGE_JPEG_VALUE);

    private final String method;

    private final String description;

    private String notes;

    private MediaType defaultContent;

    private ContentTypeNegotiator negotiator = new ContentTypeNegotiator();

    private Map<MediaType, String> extensions = new HashMap<MediaType, String>();

    private List<MediaType> mediaTypes = new ArrayList<MediaType>();

    private HttpServletRequestMatcherConfiguration configuration = new HttpServletRequestMatcherConfiguration();

    private HttpServletRequestMatcher matcher = new RepresentationMatcher(configuration);

    private PubbyRepository repository;

    public AbstractEndpoint(String method, String path, String description) {
        super();
        this.method = method;
        configuration.setFromTemplate(path);
        configuration.setRepresentationTemplate(path);
        configuration.setResourceTemplate(path);
        this.description = description;
        this.setDefaultResponseContentType(TEXT_TURTLE, ".ttl");
        this.addAcceptsResponseContentType(TEXT_TURTLE, ".ttl");
        this.addAcceptsResponseContentType(TEXT_HTML, ".html");
        this.addAcceptsResponseContentType(APPLICATION_JSON, ".json");
        this.addAcceptsResponseContentType(IMAGE_JPEG, ".jpeg");
    }

    @Override
    public boolean accepts(HttpServletRequest request) {
    	MediaRangeSpec bestMatch = null;
        String accept = request.getHeader("Accept");
        String name = new File(request.getRequestURI()).getName();
        if (accept != null) {
            bestMatch = negotiator.getBestMatch(accept);
            if (bestMatch == null) {
                return false;
            } else {
                if (name.contains(".")) {
                    String extension = extensions.get(MediaType
                        .parseMediaType(bestMatch.getMediaType()));
                    return name.endsWith(extension);
                } else {
                    return true;
                }
            }
        }
        if (name.contains(".")) {
            for (String value : extensions.values()) {
                if (name.endsWith(value)) {
                    return true;
                }
            }
            return false;
        }
        return true;
    }

    public void addAcceptsResponseContentType(MediaType media, String extension) {
        negotiator.addVariant(media.toString());
        extensions.put(media, extension);
        mediaTypes.add(media);
    }

    @Override
    public RdfResource description(HttpServletRequest request, MediaType mediaType, RdfConfiguration config) {
        String representationURI = null;
        RdfResource lr;
        String describedResource = getPrimaryTopicUri(request);
       
        if (isRedirect()) {
            StringBuffer sb = new StringBuffer(getDocumentUri(request));
            String extension=getPermanentUri(request).replace(describedResource, "").trim();
            if(extension.trim().length()==0){
            String accept = request.getHeader("Accept");
            if (accept != null) {
                MediaType best = MediaType.parseMediaType(negotiator.getBestMatch(
                    accept, request.getHeader("User-Agent"))
                    .getMediaType());
                sb.append(extensions.get(best));
            }
            }else{
            	if(extension.toLowerCase().endsWith(".json")){
            		sb.append(extensions.get(APPLICATION_JSON));
            	}else if(extension.toLowerCase().endsWith(".ttl")){
            		sb.append(extensions.get(TEXT_TURTLE));
            	}else {
            		sb.append(extensions.get(TEXT_HTML));
            	}
            }
            sb.append(getNormalizedQueryPart(request));
            representationURI = sb.toString();

            lr = new RdfResource(describedResource, config);
            RdfRepresentation ld = lr.createRepresentation(representationURI);
            declareOutputFormats(request, ld);
            ld.hasFormat(getPreferredRepresentationUri(request));

        } else {
            representationURI = getPermanentUri(request);
            HypermediaControls hc = repository.getControls(describedResource, false);
            //HypermediaControls hc = repository.getControls(representationURI, false);
            if (hc == null)
                return null;
            if (hc.getResourceDescription() == null)
                return null;
            ResourceDescription rd = hc.getResourceDescription();
            lr = new RdfResource(rd.getURI(), config, rd.getModel());
            RdfRepresentation ld = lr.createRepresentation(representationURI);

            if (extensions.containsKey(mediaType)) {
                ld.setCanonical(getPreferredRepresentationUri(request, extensions.get(mediaType)));
            } else {
                ld.setCanonical(getPreferredRepresentationUri(request));
            }
            declareOutputFormats(request, ld);
            ld.hasFormat(getPreferredRepresentationUri(request));
        }
        return lr;
    }

    @Override
    public void enrich(RdfResource description,
            HttpServletRequest request, HttpHeaders headers) {
        Model m = description.getModel();
        Resource document = m
            .createResource(request.getRequestURL().toString());
        String base = baseUrl(request);
        if (headers.containsKey("Link")) {
            for (String linkValue : headers.get("Link")) {
                Link link = Link.valueOf(linkValue);
                m.add(m.createStatement(document,
                    m.createProperty(base + "/def/lms/" + link.getRelation()),
                    m.createResource(link.getTarget())));
            }
        }
        m.add(m.createStatement(document,
            m.createProperty(base + "/def/lms/service"),
            m.createResource(base + "/def/lms/this")));
    }

    /**
     * @see "API Formatting Graphs, Linked Data API 1.0"
     * @param model
     * @param document
     * @param request
     */
    private void declareOutputFormats(HttpServletRequest request, RdfRepresentation document) {
        if (document.isSpecificRepresentation()) {
            document
                .isFormatOf(getDocumentUri(request));
        }
        for (String value : extensions.values()) {
            document.hasFormat(getDocumentUri(request) + value);
        }
    }

    @Override
    public List<MediaType> getContentTypes() {
        return mediaTypes;
    }

    @Override
    public MediaType getDefaultContentType() {
        return defaultContent;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getImplementationNotes() {
        return notes;
    }

    @Override
    public String getMethod() {
        return method;
    }

    @Override
    public List<String> getParameters() {
        return Collections.unmodifiableList(configuration.getAllowedParameters());
    }

    @Override
    public String getPath() {
    	if(method.equals("POST")){
    		return configuration.getFromTemplate().toString().substring(0,configuration.getFromTemplate().toString().indexOf("{"));
    	}else{
    		return configuration.getFromTemplate() + (configuration.getAllowedParameters().size() > 0 ? "?{query}" : "");
    	}
    }

    @Override
    public String getExtension(MediaType parse) {
        return extensions.get(parse);
    }

    @Override
    public boolean matches(HttpServletRequest request) {
        return matcher.matches(request);
    }

    @Override
    public ResponseEntity<?> proxy(HttpServletRequest request,
            RdfResource description) throws Exception {
        throw new UnsupportedOperationException();
 	
    }

    public void setDefaultResponseContentType(MediaType media, String extension) {
        defaultContent = media;
        configuration.setPreferredExtension(extension);
        negotiator.setDefaultAccept(media.toString());
    }

    public void setImplementationNotes(String notes) {
        this.notes = notes;
    }

    public void setRepository(PubbyRepository pubby) {
        repository = pubby;
    }

    public void setResourceTemplate(String rewrite) {
        configuration.setResourceTemplate(rewrite);
    }

    @Override
    public String getResourceTemplateAsString() {
        return configuration.getResourceTemplate() == null ? null : configuration.getResourceTemplate().toString();
    }

    @Override
    public boolean showLabels() {
        return repository.showLabels();
    }

    @Override
    public boolean hasRepository() {
        return repository != null;
    }

    public void setRedirect(boolean redirect) {
        configuration.setRedirectionMode(redirect);
    }

    @Override
    public boolean isRedirect() {
        return configuration.isRedirectionMode();
    }

    @Override
    public UriTemplate getRepresentationTemplate() {
        return configuration.getRepresentationTemplate();
    }

    @Override
    public UriTemplate getFromTemplate() {
        return configuration.getFromTemplate();
    }

    @Override
    public String getRequestedUri(HttpServletRequest request) {
        return matcher.getRequestedUri(request);
    }

    @Override
    public String getPermanentUri(HttpServletRequest request) {
        return matcher.getPermanentUri(request);
    }

    @Override
    public String getPreferredRepresentationUri(HttpServletRequest request) {
        return matcher.getPreferredRepresentationUri(request);
    }

    @Override
    public String getPrimaryTopicUri(HttpServletRequest request) {
        return matcher.getPrimaryTopicUri(request);
    }

    @Override
    public String getDocumentUri(HttpServletRequest request) {
        return matcher.getDocumentUri(request);
    }

    @Override
    public String getNormalizedQueryPart(HttpServletRequest request) {
        return matcher.getNormalizedQueryPart(request);
    }

    @Override
    public String getPreferredRepresentationUri(HttpServletRequest request, String extension) {
        return matcher.getPreferredRepresentationUri(request, extension);
    }

    public void setRepresentationTemplate(String template) {
        configuration.setRepresentationTemplate(template);
    }

    public void setMatcher(Class<HttpServletRequestMatcher> clazz) throws InstantiationException,
            IllegalAccessException {
        matcher = clazz.newInstance();
        matcher.setConfiguration(configuration);
    }

    @Override
    public void setConfiguration(HttpServletRequestMatcherConfiguration configuration) {
        this.configuration = configuration;
        matcher.setConfiguration(configuration);
    }

}
