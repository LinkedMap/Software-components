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
package es.unizar.iaaa.lms.aat;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import es.unizar.iaaa.lms.config.CoreConfig;
import es.unizar.iaaa.lms.config.PersistenceConfig;
import es.unizar.iaaa.lms.config.WebConfig;
import es.unizar.iaaa.lms.pubby.PubbyRepository;
import es.unizar.iaaa.lms.store.Datastore;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration()
@ContextConfiguration(classes = {
        PersistenceConfig.class, CoreConfig.class,
        WebConfig.class })
public class Requirement69Bootstrap {

    @Autowired
    public PubbyRepository pubby;
    
    @Autowired
    public Datastore datastore;

    MockMvc mockMvc;

    @Autowired
    WebApplicationContext wac;

    @Before
    public void setup() throws IOException {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac)
                .build();
        datastore.cleanInit();
    }
    
    @Test
    public void existsCapabilitiesDefinedRepository() {
        assertNotNull("Expected named dataset not defined",
            pubby.getNamedDataset("http://linkedmap.unizar.es/conf/wms"));
    }

    @Test
    public void isEmpty() {
        assertFalse("Resource must not be found",
            datastore.exists("http://localhost/api/servers/ign_base?version=1.3.0&service=WMS&request=GetCapabilities"));
    }

    @Test
    public void isFound() throws Exception {
        mockMvc
        .perform(
            get("/api/servers/ign_base?version=1.3.0&service=WMS&request=GetCapabilities"));
        assertTrue("Resource must be found",
            datastore.exists("http://localhost/api/servers/ign_base?version=1.3.0&service=WMS&request=GetCapabilities"));
    }

    // @Test
    // public void existsResource() {
    // LinkedContainer lc = new LinkedContainer("http://localhost/id/servers");
    // assertEquals(1, lc.size());
    // LinkedResource lr = lc.get(0);
    // assertEquals("http://localhost/id/servers/ign_base", lr.getURI());
    // assertEquals("http://www.ign.es/wms-inspire/ign-base?SERVICE=WMS&", lr
    // .getAs(MapServer.class).getOnlineResource());
    // }
    //
    // @Test
    // public void resourceHasCapabilitiesFormats() {
    // MapServer map = new LinkedResource(
    // "http://localhost/id/servers/ign_base",
    // pubby).getAs(MapServer.class);
    // MapServerOperation capabilites = map.getCapabilities();
    // List<MediaType> formats = capabilites.getFormats();
    // assertTrue(formats.contains(MediaType.TEXT_XML));
    // }
    //
    // @Test
    // public void resourceHasMapFormats() {
    // MapServer map = new LinkedResource(
    // "http://localhost/id/servers/ign_base",
    // pubby).getAs(MapServer.class);
    // MapServerOperation capabilites = map.getMap();
    // List<MediaType> formats = capabilites.getFormats();
    // assertTrue(formats.contains(MediaType.IMAGE_PNG));
    // assertTrue(formats.contains(MediaType.IMAGE_GIF));
    // assertTrue(formats.contains(MediaType.IMAGE_JPEG));
    // }
    //
    // @Test
    // public void resourceHasFeatureInfoFormats() {
    // MapServer map = new LinkedResource(
    // "http://localhost/id/servers/ign_base",
    // pubby).getAs(MapServer.class);
    // MapServerOperation capabilites = map.getFeatureInfo();
    // List<MediaType> formats = capabilites.getFormats();
    // assertTrue(formats.contains(MediaType.TEXT_PLAIN));
    // assertTrue(formats.contains(MediaType.TEXT_HTML));
    // assertTrue(formats.contains(MediaType.APPLICATION_JSON));
    // }
}
