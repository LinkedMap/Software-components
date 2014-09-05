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

import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.StringReader;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import es.unizar.iaaa.lms.core.endpoint.AbstractEndpoint;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration()
@ContextConfiguration(classes = {
        ContainerIntegrationTestPersistenceConfig.class, CoreConfig.class,
        WebConfig.class })
public class ContainerIntegrationTest {
    MockMvc mockMvc;

    @Autowired
    WebApplicationContext wac;

    @Before
    public void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac)
            .build();
    }

    @Test
    public void retrieveServersCollection() throws Exception {
        MockHttpServletResponse response = mockMvc
            .perform(get("/doc/servers").accept(AbstractEndpoint.TEXT_TURTLE))
            .andExpect(status().isOk())
            .andReturn().getResponse();

        Model modelA = ModelFactory.createDefaultModel();
        modelA.read(new StringReader(response.getContentAsString()),
            "http://localhost/doc/servers", "TURTLE");

        Model modelB = ModelFactory.createDefaultModel();
        modelB.read(wac.getResource("classpath:ContainerIntegrationTest1.ttl")
            .getInputStream(), "http://localhost/doc/servers", "TURTLE");

        assertTrue(modelA.containsAll(modelB));
    }

    @Test
    public void retrieveServersCollectionTurtle() throws Exception {
        MockHttpServletResponse response = mockMvc
            .perform(get("/doc/servers.ttl"))
            .andExpect(status().isOk())
            .andReturn().getResponse();

        Model modelA = ModelFactory.createDefaultModel();
        modelA.read(new StringReader(response.getContentAsString()),
            "http://localhost/doc/servers", "TURTLE");

        Model modelB = ModelFactory.createDefaultModel();
        modelB.read(wac.getResource("classpath:ContainerIntegrationTest1.ttl")
            .getInputStream(), "http://localhost/doc/servers.ttl", "TURTLE");

        assertTrue(modelA.containsAll(modelB));
    }

}
