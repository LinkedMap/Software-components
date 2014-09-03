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

import static org.hamcrest.CoreMatchers.not;
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
import org.springframework.http.MediaType;
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
import es.unizar.iaaa.lms.store.RdfConfiguration;
import es.unizar.iaaa.lms.store.RdfRepresentation;
import es.unizar.iaaa.lms.util.LinkMatcher;
import es.unizar.iaaa.lms.util.LinkRelMatcher;
import es.unizar.iaaa.lms.web.controller.ServiceController;

/**
 * This class tests a LMS server must add a {@code Link} header advertising the
 * URI of the preferred representation of a resource. The link relation must be
 * specified as "canonical".
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
public class Requirement67 {
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
        mockMvc
            .perform(get("/id/servers"))
            .andExpect(
                header().string(
                    "Link",
                    not(new LinkRelMatcher("canonical"))));
    }

    @Test
    public void documentsAboutDef() throws Exception {
        mockMvc
            .perform(get("/def/definition"))
            .andExpect(
                header().string(
                    "Link",
                    not(new LinkRelMatcher("canonical"))));
    }

    @Test
    public void documentsAboutDocHtml1() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURL()).thenReturn(new StringBuffer("http://localhost/doc/definition"));
        when(request.getRequestURI()).thenReturn("/doc/definition");
        when(request.getContextPath()).thenReturn("");
        when(request.getParameter("Accept")).thenReturn(MediaType.TEXT_HTML_VALUE);
        mockMvc
            .perform(get("/doc/definition").accept(MediaType.TEXT_HTML))
            .andExpect(
                header().string(
                    "Link",
                    new LinkMatcher("http://localhost/doc/definition.html")
                        .withCanonicalRel()));
        RdfRepresentation lr = controller.getEndpoint().description(request, MediaType.TEXT_HTML, config)
            .getRepresentation();
        assertEquals("http://localhost/doc/definition.html", lr.getCanonical());
    }

    @Test
    public void documentsAboutDocTurtle1() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURL()).thenReturn(new StringBuffer("http://localhost/doc/definition"));
        when(request.getRequestURI()).thenReturn("/doc/definition");
        when(request.getContextPath()).thenReturn("");
        when(request.getParameter("Accept")).thenReturn(AbstractEndpoint.TEXT_TURTLE_VALUE);
        mockMvc
            .perform(
                get("/doc/definition").accept(AbstractEndpoint.TEXT_TURTLE))
            .andExpect(
                header().string(
                    "Link",
                    new LinkMatcher("http://localhost/doc/definition.ttl")
                        .withCanonicalRel()));
        RdfRepresentation lr = controller.getEndpoint().description(request, AbstractEndpoint.TEXT_TURTLE, config)
            .getRepresentation();
        assertEquals("http://localhost/doc/definition.ttl", lr.getCanonical());
    }

    @Test
    public void documentsAboutDocTurtle2() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURL()).thenReturn(new StringBuffer("http://localhost/doc/definition.ttl"));
        when(request.getRequestURI()).thenReturn("/doc/definition.ttl");
        when(request.getContextPath()).thenReturn("");
        mockMvc
            .perform(get("/doc/definition.ttl"))
            .andExpect(
                header().string(
                    "Link",
                    new LinkMatcher("http://localhost/doc/definition.ttl")
                        .withCanonicalRel()));
        RdfRepresentation lr = controller.getEndpoint().description(request, null, config).getRepresentation();
        assertEquals("http://localhost/doc/definition.ttl", lr.getCanonical());
    }

    @Test
    public void documentsAboutDocHtml2() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURL()).thenReturn(new StringBuffer("http://localhost/doc/definition.html"));
        when(request.getRequestURI()).thenReturn("/doc/definition.html");
        when(request.getContextPath()).thenReturn("");
        mockMvc
            .perform(get("/doc/definition.html"))
            .andExpect(
                header().string(
                    "Link",
                    new LinkMatcher("http://localhost/doc/definition.html")
                        .withCanonicalRel()));
        RdfRepresentation lr = controller.getEndpoint().description(request, null, config).getRepresentation();
        assertEquals("http://localhost/doc/definition.html", lr.getCanonical());
    }

}
