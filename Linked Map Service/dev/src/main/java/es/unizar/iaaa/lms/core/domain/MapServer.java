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

import com.hp.hpl.jena.rdf.model.Model;

import es.unizar.iaaa.lms.store.RdfConfiguration;
import es.unizar.iaaa.lms.store.RdfResource;

public class MapServer extends RdfResource {

    public MapServer(String uri, Model model, RdfConfiguration config) {
        super(uri, config, model);
    }

    @Deprecated
    public String getOnlineResource() {
        return new ResourceSet(resource).find("onlineResource").first().asResource()
            .getURI();
    }

    @Deprecated
    public MapServerOperation getCapabilities() {
        return new MapServerOperation(new ResourceSet(resource).find("hasOperation")
            .filter("name", "GetCapabilities").first().asResource());
    }

    @Deprecated
    public MapServerOperation getMap() {
        return new MapServerOperation(new ResourceSet(resource).find("hasOperation")
            .filter("name", "GetMap").first().asResource());
    }

    @Deprecated
    public MapServerOperation getFeatureInfo() {
        return new MapServerOperation(new ResourceSet(resource).find("hasOperation")
            .filter("name", "GetFeatureInfo").first().asResource());
    }

}
