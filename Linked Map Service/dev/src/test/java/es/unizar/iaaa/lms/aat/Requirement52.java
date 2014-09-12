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
import es.unizar.iaaa.lms.web.controller.NotFoundException;

/**
 * This class tests a dereferencing a document URI or a representation URI must
 * return a {@code 200 OK} response if the LMS server recognizes the URI as the
 * URI of the description of a known resource. However, if the LMS server knows
 * that the resource has been deleted the response may be {@code 410 Gone}.
 * Otherwise the response must be {@code 404 Not Found}.
 * 
 * FIXME: 410 Gone support
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
public class Requirement52 {
    MockMvc mockMvc;

    @Autowired
    WebApplicationContext webApplicationContext;

    @Before
    public void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
            .build();
    }

    @Test
    public void knownDocumentResourceReturnsOk() throws Exception {
        mockMvc
            .perform(get("/doc/servers/ign_base"))
            .andDo(print())
            .andExpect(status().isOk());
    }

    @Test
    public void knownRepresentationResourceReturnsOk() throws Exception {
        mockMvc
            .perform(get("/doc/servers/ign_base.ttl"))
            .andDo(print())
            .andExpect(status().isOk());
    }

    @Test(expected = NotFoundException.class)
    // FIXME This request should be handled by a data handler, not a html
    // handler
            public
            void unknownDocumentResourceReturnsNotFound() throws Exception {
        mockMvc
            .perform(get("/doc/servers/ign_base2"))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    public void unknownRepresentationResourceReturnsNotFound() throws Exception {
        mockMvc
            .perform(get("/doc/servers/ign_base2.ttl"))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

}
