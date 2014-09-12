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
package es.unizar.iaaa.lms.core.domain;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import es.unizar.iaaa.lms.core.endpoint.Endpoint;

public class ServerConfiguration {

    private List<ResourceClass> resources;

    public ServerConfiguration(List<ResourceClass> resources) {
        super();
        this.resources = resources;
    }

    public List<ResourceClass> getResources() {
        return resources;
    }

    public Endpoint findEndpoint(HttpServletRequest request) {
        for (ResourceClass rc : resources) {
            for (Endpoint e : rc.getEndpoints()) {
                if (e.matches(request)) {
                	if(e.getMethod().equalsIgnoreCase(request.getMethod())){
                		return e;
                	}
                }
            }
        }
        return null;
    }
}
