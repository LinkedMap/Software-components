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

import java.util.HashMap;
import java.util.Map;

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
import es.unizar.iaaa.lms.core.endpoint.KvpEndpoint;
import es.unizar.iaaa.lms.store.RdfConfiguration;
import es.unizar.iaaa.lms.store.RdfRepresentation;
import es.unizar.iaaa.lms.util.LinkMatcher;
import es.unizar.iaaa.lms.web.controller.AbstractController;
import es.unizar.iaaa.lms.web.controller.ApiController;
import es.unizar.iaaa.lms.web.matcher.HttpServletRequestMatcher;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration()
@ContextConfiguration(classes = {
        PersistenceConfig.class, CoreConfig.class,
        WebConfig.class })
public class Requirement68GetMap {
    MockMvc mockMvc;

    static final String GET_MAP_REQUEST = "version=1.3.0&"
            + "request=GetMap&&layers=SombreadoPenBal,NucleosPob_mayores,RedHidrografica_S,Laguna,Humedal,"
            + "CuerposAgua,ConstruccionHidrografica_S,ConstruccionHidrografica_L,CauceArtificial,Rio,RedHidrografica,"
            + "FFCC_AVE,FC_Convencional_B,FC_Convencional,EnlaceCarreteras,CarreteraNacional,CarreteraConvencional,"
            + "CarreteraAutonomica,VialUrbano,Autovia,Autopista_Autovia,Autopista,TN.TransportNetwork.RunwayArea,"
            + "Aeropuertos,Municipio&crs=EPSG:25830&bbox=648531.27015522,4597343.1368003,700634.8413783,4627293.6534597&"
            + "styles=,,,,,,,,,,,,,,,,,,,,,,,,&bgcolor=0xFFFFFF&exceptions=INIMAGE&format=image/png&width=1009&height=580";
    @Autowired
    WebApplicationContext wac;

    @Autowired
    RdfConfiguration config;

    AbstractController controller;

    HttpServletRequest request;

    HttpServletRequestMatcher matcher;

    @Before
    public void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac)
            .build();
        controller = wac.getBean(ApiController.class);

        matcher = wac.getBean(KvpEndpoint.class);

    }

    @Test
    public void representationUriGetMap() throws Exception {
        Map<String, String[]> map = new HashMap<String, String[]>();
        map.put("request", new String[] { "GetMap" });
        map.put("version", new String[] { "1.3.0" });
        map.put("layers", new String[] { "SombreadoPenBal,NucleosPob_mayores,RedHidrografica_S,Laguna,"
                + "Humedal,CuerposAgua,ConstruccionHidrografica_S,ConstruccionHidrografica_L,CauceArtificial,Rio,"
                + "RedHidrografica,FFCC_AVE,FC_Convencional_B,FC_Convencional,EnlaceCarreteras,CarreteraNacional,"
                + "CarreteraConvencional,CarreteraAutonomica,VialUrbano,Autovia,Autopista_Autovia,Autopista,"
                + "TN.TransportNetwork.RunwayArea,Aeropuertos,Municipio" });
        map.put("crs", new String[] { "EPSG:25830" });
        map.put("bbox", new String[] { "648531.27015522,4597343.1368003,700634.8413783,4627293.6534597" });
        map.put("styles", new String[] { ",,,,,,,,,,,,,,,,,,,,,,,," });
        map.put("bgcolor", new String[] { "0xFFFFFF" });
        map.put("exceptions", new String[] { "INIMAGE" });
        map.put("format", new String[] { "image/png" });
        map.put("width", new String[] { "1009" });
        map.put("height", new String[] { "580" });

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURL()).thenReturn(new StringBuffer("http://localhost/api/servers/ign_base"));
        when(request.getRequestURI()).thenReturn("/api/servers/ign_base");
        when(request.getContextPath()).thenReturn("");
        when(request.getParameterMap()).thenReturn(map);
        when(request.getParameter("request")).thenReturn("GetMap");
        mockMvc
            .perform(
                get("/api/servers/ign_base?" + GET_MAP_REQUEST))
            .andExpect(
                header()
                    .string(
                        "Link",
                        new LinkMatcher(matcher.getPreferredRepresentationUri(request))
                            .withCanonicalRel()));
        RdfRepresentation lr = controller.getEndpoint().description(request, null, config).getRepresentation();
        assertEquals(matcher.getPreferredRepresentationUri(request), lr.getCanonical());
    }
}
