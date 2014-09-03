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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;

import javax.servlet.http.HttpServletRequest;

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
import es.unizar.iaaa.lms.store.RdfConfiguration;
import es.unizar.iaaa.lms.store.RdfRepresentation;
import es.unizar.iaaa.lms.util.LinkMatcher;
import es.unizar.iaaa.lms.web.controller.ServiceController;

/**
 * This class tests a LMS server must add a {@code Link} header referring to the
 * URI of the resource that is the primary topic of the response. The link
 * relation must be specified as "about".
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
public class Requirement66 {
    MockMvc mockMvc;

    @Autowired
    WebApplicationContext wac;

    @Autowired
    RdfConfiguration config;

    ServiceController controller;

    @Before
    public void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac)
            .build();
        controller = wac.getBean(ServiceController.class);
    }

    @Test
    public void documentsAboutId() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURL()).thenReturn(new StringBuffer("http://localhost/doc/servers"));
        when(request.getRequestURI()).thenReturn("/doc/servers");
        when(request.getContextPath()).thenReturn("");
        mockMvc
            .perform(get("/doc/servers"))
            .andExpect(
                header().string(
                    "Link",
                    new LinkMatcher("http://localhost/id/servers")
                        .withAboutRel()));

        RdfRepresentation lr = controller.getEndpoint().description(request, null, config).getRepresentation();
        assertEquals("http://localhost/id/servers", lr.getPrimaryTopic());
    }

    @Test
    public void documentsAboutDef() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURL()).thenReturn(new StringBuffer("http://localhost/doc/definition"));
        when(request.getRequestURI()).thenReturn("/doc/definition");
        when(request.getContextPath()).thenReturn("");
        mockMvc
            .perform(get("/doc/definition"))
            .andExpect(
                header().string(
                    "Link",
                    new LinkMatcher("http://localhost/def/definition")
                        .withAboutRel()));

        RdfRepresentation lr = controller.getEndpoint().description(request, null, config).getRepresentation();
        assertEquals("http://localhost/def/definition", lr.getPrimaryTopic());
    }
}
