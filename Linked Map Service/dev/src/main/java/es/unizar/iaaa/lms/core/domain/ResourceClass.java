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

import es.unizar.iaaa.lms.core.endpoint.Endpoint;

public class ResourceClass {

    private final String name;

    private final String description;

    private final List<Endpoint> endpoints;

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public ResourceClass(String name, String description,
            List<Endpoint> endpoints) {
        super();
        this.name = name;
        this.description = description;
        this.endpoints = endpoints;
    }

    public List<Endpoint> getEndpoints() {
        return endpoints;
    }

}
