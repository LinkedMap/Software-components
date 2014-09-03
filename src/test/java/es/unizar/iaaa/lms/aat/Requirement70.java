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

import static es.unizar.iaaa.lms.aat.ModelResultMatcher.model;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

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
import es.unizar.iaaa.lms.core.endpoint.AbstractEndpoint;

/**
 * This class tests each link relation may be equivalent to the presence of a
 * triple in the resource description.
 * 
 * @author Francisco J Lopez-Pellicer
 * @see "Planet Data D15.1 Call 2: Linked Map requirements definition and conceptual architecture"
 * 
 */

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration()
@ContextConfiguration(classes = {
        PersistenceConfig.class, CoreConfig.class,
        WebConfig.class })
public class Requirement70 {
    MockMvc mockMvc;

    @Autowired
    WebApplicationContext wac;

    @Before
    public void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac)
            .build();
    }

    @Test
    public void serviceInModel() throws Exception {
        mockMvc
            .perform(
                get("/doc/definition").accept(AbstractEndpoint.TEXT_TURTLE))
            .andExpect(
                model("http://localhost", "TURTLE").resource(
                    "http://localhost/doc/definition").hasProperty(
                    "http://localhost/def/lms/service",
                    "http://localhost/def/lms/this"));
    }

    @Test
    public void canonicalInModel() throws Exception {
        mockMvc
            .perform(
                get("/doc/definition").accept(AbstractEndpoint.TEXT_TURTLE))
            .andExpect(
                model("http://localhost", "TURTLE").resource(
                    "http://localhost/doc/definition").hasProperty(
                    "http://localhost/def/lms/canonical",
                    "http://localhost/doc/definition.ttl"));
    }

    @Test
    public void aboutInModel() throws Exception {
        mockMvc
            .perform(
                get("/doc/definition").accept(AbstractEndpoint.TEXT_TURTLE))
            .andExpect(
                model("http://localhost", "TURTLE").resource(
                    "http://localhost/doc/definition").hasProperty(
                    "http://localhost/def/lms/about",
                    "http://localhost/def/definition"));
    }

    @Test
    public void selfInModel() throws Exception {
        mockMvc
            .perform(
                get("/doc/definition").accept(AbstractEndpoint.TEXT_TURTLE))
            .andExpect(
                model("http://localhost", "TURTLE").resource(
                    "http://localhost/doc/definition").hasProperty(
                    "http://localhost/def/lms/self",
                    "http://localhost/doc/definition"));
    }
}
