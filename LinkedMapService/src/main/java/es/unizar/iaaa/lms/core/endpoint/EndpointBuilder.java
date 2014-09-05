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

import org.springframework.http.MediaType;

import es.unizar.iaaa.lms.pubby.PubbyRepository;
import es.unizar.iaaa.lms.web.matcher.HttpServletRequestMatcher;

public class EndpointBuilder {

    private AbstractEndpoint endpoint;

    public EndpointBuilder(AbstractEndpoint endpoint) {
        this.endpoint = endpoint;
    }

    public static EndpointBuilder getEndpoint(String path, String description) {
        return new EndpointBuilder(new GetEndpoint(path, description));
    }

    public static EndpointBuilder putEndpoint(String path, String description, String sparqlPoint) {
        return new EndpointBuilder(new PutEndpoint(path, description,sparqlPoint));
    }

    public static EndpointBuilder postEndpoint(String path, String description, String sparqlPoint) {
        return new EndpointBuilder(new PostEndpoint(path, description,sparqlPoint));
    }

    public static EndpointBuilder deleteEndpoint(String path, String description) {
        return new EndpointBuilder(new DeleteEndpoint(path, description));
    }

    public static EndpointBuilder patchEndpoint(String path, String description) {
        return new EndpointBuilder(new PatchEndpoint(path, description));
    }

    public static EndpointBuilder kvpEndpoint(String path, String description) {
        return new EndpointBuilder(new KvpEndpoint(path, description));
    }

    public static EndpointBuilder xmlEndpoint(String path, String description) {
        return new EndpointBuilder(new XmlEndpoint(path, description));
    }

    public Endpoint endpoint() {
        return endpoint;
    }

    public EndpointBuilder implementationNotes(String notes) {
        endpoint.setImplementationNotes(notes);
        return this;
    }

    public EndpointBuilder preferredContentType(MediaType media,
            String extension) {
        endpoint.setDefaultResponseContentType(media, extension);
        endpoint.addAcceptsResponseContentType(media, extension);
        return this;
    }

    public EndpointBuilder accepts(MediaType media, String extension) {
        endpoint.addAcceptsResponseContentType(media, extension);
        return this;
    }

    public EndpointBuilder repository(PubbyRepository pubby) {
        endpoint.setRepository(pubby);
        return this;
    }

    public EndpointBuilder resourceRewrite(String template) {
        endpoint.setResourceTemplate(template);
        return this;
    }

    public EndpointBuilder representationRewrite(String template) {
        endpoint.setRepresentationTemplate(template);
        return this;
    }

    public EndpointBuilder redirect(String template) {
        endpoint.setRedirect(true);
        endpoint.setRepresentationTemplate(template);
        endpoint.setResourceTemplate(endpoint.getFromTemplate().toString());
        return this;
    }

    public EndpointBuilder mapStore(Endpoint mapsStore) {
        if (endpoint instanceof KvpEndpoint) {
            ((KvpEndpoint) endpoint).setMapStore(mapsStore);
        }
        return this;
    }

    public EndpointBuilder mapCollection(Endpoint mapsCollection) {
        if (endpoint instanceof KvpEndpoint) {
            ((KvpEndpoint) endpoint).setMapCollection(mapsCollection);
        }
        return this;
    }

    public EndpointBuilder infoStore(Endpoint infoStore) {
        if (endpoint instanceof KvpEndpoint) {
            ((KvpEndpoint) endpoint).setInfoStore(infoStore);
        }
        return this;
    }

    public EndpointBuilder infoCollection(Endpoint infoCollection) {
        if (endpoint instanceof KvpEndpoint) {
            ((KvpEndpoint) endpoint).setInfoCollection(infoCollection);
        }
        return this;
    }

    @SuppressWarnings("unchecked")
    public EndpointBuilder matcher(Class<? extends HttpServletRequestMatcher> clazz) {
        try {
            endpoint.setMatcher((Class<HttpServletRequestMatcher>) clazz);
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return this;
    }

}
