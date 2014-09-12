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
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import es.unizar.iaaa.lms.store.RdfResource;
import es.unizar.iaaa.lms.util.LinkMatcher;
import es.unizar.iaaa.lms.web.controller.RedirectionController;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration()
@ContextConfiguration(classes = {
        PersistenceConfig.class, CoreConfig.class,
        WebConfig.class })
public class Requirement65Redirection {
    private static final String HTTP_LOCALHOST_ID_SERVERS = "http://localhost/id/servers";

    MockMvc mockMvc;

    @Autowired
    WebApplicationContext wac;

    @Autowired
    RdfConfiguration config;

    RedirectionController controller;

    HttpServletRequest request;

    @Before
    public void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac)
            .build();
        request = mock(HttpServletRequest.class);
        when(request.getRequestURL()).thenReturn(new StringBuffer(HTTP_LOCALHOST_ID_SERVERS));
        when(request.getRequestURI()).thenReturn("/id/servers");
        when(request.getContextPath()).thenReturn("");
        controller = wac.getBean(RedirectionController.class);
    }

    @Test
    public void redirectionWithParameters() throws Exception {
        mockMvc
            .perform(get("/id/servers?b=1&a=2"))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    public void redirectionWithoutParameters()
            throws Exception {
        mockMvc
            .perform(get("/id/servers"))
            .andDo(print())
            .andExpect(
                header().string("Link",
                    new LinkMatcher(HTTP_LOCALHOST_ID_SERVERS)));

        RdfResource ld = controller.getEndpoint().description(request, null, config);
        assertEquals(HTTP_LOCALHOST_ID_SERVERS, ld.getResource().getURI());
    }

}
