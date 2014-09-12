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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
 * This class tests a LMS server must support be able to return data encoded in
 * Turtle format.
 * 
 * @author Francisco J Lopez-Pellicer
 * @see "Planet Data D15.1 Call 2: Linked Map requirements definition and conceptual architecture"
 * 
 */

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = {
        PersistenceConfig.class, CoreConfig.class,
        WebConfig.class })
public class Requirement54 {
    MockMvc mockMvc;

    @Autowired
    WebApplicationContext webApplicationContext;

    @Before
    public void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
            .build();
    }

    @Test
    public void requestTurtleByExtension() throws Exception {
        mockMvc
            .perform(get("/doc/servers/ign_base.ttl"))
            .andDo(print())
            .andExpect(
                content().contentTypeCompatibleWith(
                    AbstractEndpoint.TEXT_TURTLE))
            .andExpect(status().isOk());
    }

    @Test
    public void requestTurtleRemoteDataByExtension() throws Exception {
        mockMvc
            .perform(get("/doc/data/BCN-4024602.ttl"))
            .andDo(print())
            .andExpect(
                content().contentTypeCompatibleWith(
                    AbstractEndpoint.TEXT_TURTLE))
            .andExpect(status().isOk());
    }

    @Test
    public void requestTurtleByAccept() throws Exception {
        mockMvc
            .perform(
                get("/doc/servers/ign_base").accept(
                    AbstractEndpoint.TEXT_TURTLE))
            .andDo(print())
            .andExpect(
                content().contentTypeCompatibleWith(
                    AbstractEndpoint.TEXT_TURTLE))
            .andExpect(status().isOk());
    }
}
