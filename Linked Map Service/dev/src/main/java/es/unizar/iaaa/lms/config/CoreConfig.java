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
package es.unizar.iaaa.lms.config;

import static es.unizar.iaaa.lms.core.endpoint.EndpointBuilder.deleteEndpoint;
import static es.unizar.iaaa.lms.core.endpoint.EndpointBuilder.getEndpoint;
import static es.unizar.iaaa.lms.core.endpoint.EndpointBuilder.kvpEndpoint;
import static es.unizar.iaaa.lms.core.endpoint.EndpointBuilder.patchEndpoint;
import static es.unizar.iaaa.lms.core.endpoint.EndpointBuilder.postEndpoint;
import static es.unizar.iaaa.lms.core.endpoint.EndpointBuilder.putEndpoint;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;

import es.unizar.iaaa.lms.core.domain.ResourceClass;
import es.unizar.iaaa.lms.core.domain.ServerConfiguration;
import es.unizar.iaaa.lms.core.endpoint.Endpoint;
import es.unizar.iaaa.lms.core.report.CheckKvpConfiguration;
import es.unizar.iaaa.lms.core.report.RequireRepositoryButRedirect;
import es.unizar.iaaa.lms.core.report.RequireRewrite;
import es.unizar.iaaa.lms.core.report.ServerConfigurationReporter;
import es.unizar.iaaa.lms.pubby.PubbyRepository;
import es.unizar.iaaa.lms.web.matcher.WmsMatcher;

@Configuration
public class CoreConfig {

    @Autowired
    public PubbyRepository pubby;
    
    @Autowired
    private Environment env;
    
    @Bean
    public Endpoint apiEndpoint() {
        return kvpEndpoint("/api/servers/{serverId}", "Server endpoint").
            implementationNotes(
                "Invoke a WMS server using a WMS KVP request")
            .resourceRewrite("/id/servers/{serverId}")
            .representationRewrite("/doc/servers/{serverId}")
            .matcher(WmsMatcher.class)
            .repository(pubby)
            .mapStore(mapStore())
            .mapCollection(mapCollection())
            .infoStore(infoStore())
            .infoCollection(infoCollection())
            .endpoint();
    }

    

    @Bean
    public Endpoint mapCollection() {
        return getEndpoint("/doc/maps/{mapsId}", "Describe a map").
            implementationNotes(
                "Return the description of a map")
            .resourceRewrite("/id/maps/{layerId}")
            .accepts(MediaType.IMAGE_PNG, ".png")
            .repository(pubby)
            .endpoint();
    }

    @Bean
    public Endpoint infoStore() {
        return getEndpoint("/doc/infos", "List infos").
            implementationNotes(
                "Return the list of known feature infos")
            .resourceRewrite("/id/infos")
            .repository(pubby)
            .endpoint();
    }

    @Bean
    public Endpoint infoCollection() {
        return getEndpoint("/doc/infos/{infoId}", "Describe a feature info").
            implementationNotes(
                "Return the description of a feature info")
            .resourceRewrite("/id/infos/{infoId}")
            .accepts(MediaType.TEXT_PLAIN, ".txt")
            .repository(pubby)
            .endpoint();
    };
    
    @Bean
    public Endpoint bcnStore() {
        return getEndpoint("/doc/bcn", "List of bnc").
            implementationNotes(
                "Return the list of known bcn")
            .resourceRewrite("/id/bcn/{layerId}")
            .repository(pubby)
            .endpoint();
    }
    
    @Bean
    public ResourceClass bcn() {
        Endpoint[] endpoints = new Endpoint[] {
                getEndpoint("/id/bcn", "Collection identifier")
                    .implementationNotes("Forwards to /doc/bcn")
                    .redirect("/doc/bcn")
                    .endpoint(),
                getEndpoint("/id/bcn/{rid}", "Resource identifier")
                    .implementationNotes(
                        "Forward to /doc/bcn/{rid}")
                    .redirect("/doc/bcn/{rid}")
                    .endpoint(),
                mapStore(),
                mapCollection(),
        };
        return new ResourceClass("bcn", "Operations about get BCN resources",
            Arrays.asList(endpoints));
    }

    @Bean
    public Endpoint mapStore() {
        return getEndpoint("/doc/maps", "List maps").
            implementationNotes(
                "Return the list of known maps")
            .resourceRewrite("/id/maps/{layerId}")
            .repository(pubby)
            .endpoint();
    }
    
    @Bean
    public ResourceClass maps() {
        Endpoint[] endpoints = new Endpoint[] {
                getEndpoint("/id/maps", "Collection identifier")
                    .implementationNotes("Forwards to /doc/maps")
                    .redirect("/doc/maps")
                    .endpoint(),
                getEndpoint("/id/maps/{mapId}", "Map identifier")
                    .implementationNotes(
                        "Forward to /doc/maps/{mapId}")
                    .redirect("/doc/maps/{mapId}")
                    .endpoint(),
                mapStore(),
                mapCollection(),
        };
        return new ResourceClass("maps", "Operations about get maps requests",
            Arrays.asList(endpoints));
    }

    @Bean
    public ResourceClass infos() {
        Endpoint[] endpoints = new Endpoint[] {
                getEndpoint("/id/infos", "Collection identifier")
                    .implementationNotes("Forwards to /doc/infos")
                    .redirect("/idoc/infos")
                    .endpoint(),
                getEndpoint("/id/infos/{infoId}", "Info identifier")
                    .implementationNotes(
                        "Forward to /doc/infos/{infoId}")
                    .redirect("/doc/infos/{infoId}")
                    .endpoint(),
                infoStore(),
                infoCollection(), };
        return new ResourceClass("infos",
            "Operations about get feature info requests",
            Arrays.asList(endpoints));
    }

    @Bean
    public ResourceClass users() {
    	String storeEndpoint=env.getRequiredProperty("datastore.store");
        Endpoint[] endpoints = new Endpoint[] {
               /* getEndpoint("/id/users", "Collection identifier")
                    .implementationNotes("Forwards to /doc/users")
                    .redirect("/doc/users")
                    .endpoint(),*/
                getEndpoint("/id/users/{userId}", "User identifier")
                    .implementationNotes(
                        "Forward to /doc/users/{userId}")
                    .redirect("/doc/users/{userId}")
                    .endpoint(),
                /*getEndpoint("/doc/users", "List users").
                    implementationNotes(
                        "Return the list of users")
                    .resourceRewrite("/id/users")
                    .repository(pubby)
                    .endpoint(),*/
                getEndpoint("/doc/users/{userId}", "Describe user").
                    implementationNotes(
                        "Return the description of a user")
                    .resourceRewrite("/id/users/{userId}")
                    .repository(pubby)
                    .endpoint(),
                /*postEndpoint("/doc/users", "Add new user",storeEndpoint).
                    implementationNotes(
                        "Add a new user")
                    .resourceRewrite("/id/users/{userId}")
                    .repository(pubby)
                    .endpoint(),*/
                /*putEndpoint("/doc/users/{usersId}", "Update existing user",storeEndpoint).
                    implementationNotes(
                        "Update an existing user")
                    .resourceRewrite("/id/users/{userId}")
                    .repository(pubby)
                    .endpoint(),*/
                /*patchEndpoint("/doc/users/{usersId}",
                    "Update partially existing user").
                    implementationNotes(
                        "Update partially an existing user")
                    .resourceRewrite("/id/users/{userId}")
                    .repository(pubby)
                    .endpoint(),*/
                /*deleteEndpoint("/doc/users/{usersId}",
                    "Delete existing edit").
                    implementationNotes(
                        "Delete an existing user")
                    .resourceRewrite("/id/users/{userId}")
                    .repository(pubby)
                    .endpoint(),*/ };
        return new ResourceClass("users", "Operations about users",
            Arrays.asList(endpoints));
    }

    @Bean
    public ResourceClass edits() {
    	String storeEndpoint=env.getRequiredProperty("datastore.store");
        Endpoint[] endpoints = new Endpoint[] {
                /*getEndpoint("/id/edits", "Collection identifier")
                    .implementationNotes("Forwards to /doc/edits")
                    .redirect("/doc/edits")
                    .endpoint(),*/
                /*getEndpoint("/id/edits/{editId}", "Edit identifier")
                    .implementationNotes(
                        "Forward to /doc/edits/{editId}")
                    .redirect("/doc/edits/{editId}")
                    .endpoint(),*/
                /*getEndpoint("/doc/edits", "List edits").
                    implementationNotes(
                        "Return the list of edits")
                    .resourceRewrite("/id/edits")
                    .repository(pubby)
                    .endpoint(),*/
                /*getEndpoint("/doc/edits/{editId}", "Describe edit").
                    implementationNotes(
                        "Return the description of an edit")
                    .resourceRewrite("/id/edits/{editId}")
                    .repository(pubby)
                    .endpoint(),*/
                postEndpoint("/doc/edits{mappingID}{creator}{date}{comment}{value}", "Add new edit",storeEndpoint).
                    implementationNotes(
                        "Add a new edit")
                    .resourceRewrite("/id/edits")
                    .repository(pubby)
                    .endpoint(),
                /*putEndpoint("/doc/edits/{editsId}", "Update existing edit",storeEndpoint).
                    implementationNotes(
                        "Update an existing edit")
                    .resourceRewrite("/id/edits/{editsId}")
                    .repository(pubby)
                    .endpoint(),*/
                /*patchEndpoint("/doc/edits/{editsId}",
                    "Update partially existing edit").
                    implementationNotes(
                        "Update partially an existing edit")
                    .resourceRewrite("/id/edits/{editsId}")
                    .repository(pubby)
                    .endpoint(),*/
                /*deleteEndpoint("/doc/edits/{editsId}",
                    "Delete existing edit").
                    implementationNotes(
                        "Delete an existing edit")
                    .resourceRewrite("/id/edits/{editsId}")
                    .repository(pubby)
                    .endpoint(),*/
        };
        return new ResourceClass("edits", "Operations about users' edits",
            Arrays.asList(endpoints));
    }

    protected Endpoint[] enpointTemplate(String base, String name, String keySingular, String keyPlural) {
        Endpoint[] endpoints = new Endpoint[] {
                getEndpoint("/" + base + "/" + keyPlural, "Collection identifier")
                    .implementationNotes("Forwards to /doc/" + keyPlural)
                    .redirect("/doc/" + keyPlural)
                    .endpoint(),
                getEndpoint("/" + base + "/" + keyPlural + "/{" + keySingular + "Id}", name + " identifier")
                    .implementationNotes(
                        "Forward to /doc/" + keyPlural + "/{" + keySingular + "Id}")
                    .redirect("/doc/" + keyPlural + "/{" + keySingular + "Id}")
                    .endpoint(),
                /*getEndpoint("/" + base + "/" + keyPlural + "/{" + keySingular + "Id}"+"/{version}", name + " identifier")
                    .implementationNotes(
                        "Forward to /doc/" + keyPlural + "/{" + keySingular + "Id}/{version}")
                    .redirect("/doc/" + keyPlural + "/{" + keySingular + "Id}/{version}")
                    .endpoint(),*/
                getEndpoint("/doc/" + keyPlural, "List " + keyPlural).
                    implementationNotes(
                        "Return the list of available " + keyPlural)
                    .resourceRewrite("/" + base + "/" + keyPlural)
                    .repository(pubby)
                    .endpoint(),
                getEndpoint("/doc/" + keyPlural + "/{" + keySingular + "Id}", "Describe " + name).
                    implementationNotes(
                        "Return the description of a " + name)
                    .resourceRewrite("/" + base + "/" + keyPlural + "/{" + keySingular + "Id}")
                    .repository(pubby)
                    .endpoint(), };
        return endpoints;
    }

    @Bean
    public ResourceClass layers() {
        Endpoint[] endpoints = enpointTemplate("id", "Layer", "layer", "layers");
        return new ResourceClass("layers", "Operations about layers offered",
            Arrays.asList(endpoints));
    }

    @Bean
    public ResourceClass datasets() {
        Endpoint[] endpoints = enpointTemplate("id", "Dataset", "dataset", "datasets");
        return new ResourceClass("datasets", "Operations about datasets",
            Arrays.asList(endpoints));
    }

    @Bean
    public ResourceClass data() {
        Endpoint[] endpoints = enpointTemplate("id", "Spatial Object", "data", "data");
        return new ResourceClass("data", "Operations about spatial object",
            Arrays.asList(endpoints));
    }

    /*@Bean
    public ResourceClass links() {
        Endpoint[] endpoints = enpointTemplate("id", "Link", "link", "links");
        return new ResourceClass("link", "Operations about links",
            Arrays.asList(endpoints));
    }*/

    @Bean
    public ResourceClass changes() {
        Endpoint[] endpoints = enpointTemplate("id", "Change set", "change", "changes");
        return new ResourceClass("changes", "Operations about change sets",
            Arrays.asList(endpoints));
    }

    @Bean
    public ResourceClass resources() {
        Endpoint[] endpoints = enpointTemplate("def", "Resource", "context", "{contextId}");
        return new ResourceClass("resources", "Operations about other resource",
            Arrays.asList(endpoints));
    }

    @Bean
    public ResourceClass servers() {
        Endpoint[] endpoints = enpointTemplate("id", "WMS server", "server", "servers");
        List<Endpoint> list = new ArrayList<Endpoint>(Arrays.asList(endpoints));
        list.add(apiEndpoint());
        return new ResourceClass("servers", "Operations about WMS servers", list);
    }

    @Bean
    public ServerConfiguration serverConfiguration() {
        ResourceClass[] resources = new ResourceClass[] { servers(), /*layers(),*/
                /*maps(),*/ /*infos(),*/ datasets(), data(), /*links(),*/ users(), edits(),
                /*changes(),*/ /*resources(),*/ /*bcn()*/  };
        return new ServerConfiguration(Arrays.asList(resources));
    }

    @Bean
    public ServerConfigurationReporter serverConfigurationReporter() {
        ServerConfigurationReporter scr = new ServerConfigurationReporter(
            serverConfiguration());
        scr.reporter(new RequireRewrite(), new RequireRepositoryButRedirect(),
            new CheckKvpConfiguration());
        scr.analyze();
        return scr;
    }

}
