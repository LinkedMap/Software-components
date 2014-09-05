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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;

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
import es.unizar.iaaa.lms.util.LinkMatcher;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration()
@ContextConfiguration(classes = {
        PersistenceConfig.class, CoreConfig.class,
        WebConfig.class })
public class Requirement69GetFeatureInfo {
    MockMvc mockMvc;

    static final String GET_FEATURE_INFO_REQUEST = "version=1.3.0&"
            + "service=wms&request=GetFeatureInfo&layers=SombreadoPenBal,NucleosPob_mayores,RedHidrografica_S,Laguna,"
            + "Humedal,CuerposAgua,ConstruccionHidrografica_S,ConstruccionHidrografica_L,CauceArtificial,Rio,"
            + "RedHidrografica,FFCC_AVE,FC_Convencional_B,FC_Convencional,EnlaceCarreteras,CarreteraNacional,"
            + "CarreteraConvencional,CarreteraAutonomica,VialUrbano,Autovia,Autopista_Autovia,Autopista,"
            + "TN.TransportNetwork.RunwayArea,Aeropuertos,Municipio&query_layers=Municipio&crs=EPSG:25830&"
            + "bbox=648531.27015522,4597343.1368003,700634.8413783,4627293.6534597&styles=,,,,,,,,,,,,,,,,,,,,,,,,&"
            + "bgcolor=0xFFFFFF&exceptions=INIMAGE&format=image/png&width=1009&height=580&i=504&j=290&info_format=text/plain";
    @Autowired
    WebApplicationContext wac;

    @Before
    public void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac)
            .build();
    }

    @Test
    public void alternateApi() throws Exception {
        mockMvc
            .perform(
                get("/api/servers/ign_base?" + GET_FEATURE_INFO_REQUEST))
            .andExpect(
                header()
                    .string(
                        "Link",
                        new LinkMatcher(
                            "http://localhost/doc/infos/ign_base")
                            .withCanonicalRel()))
            .andExpect(
                header()
                    .string(
                        "Link",
                        not(new LinkMatcher(
                            "http://localhost/doc/infos/ign_base")
                            .withAlternateRel())))
            .andExpect(
                header()
                    .string(
                        "Link",
                        new LinkMatcher(
                            "http://localhost/doc/infos/ign_base.ttl")
                            .withAlternateRel()))
            .andExpect(
                header()
                    .string(
                        "Link",
                        new LinkMatcher(
                            "http://localhost/doc/infos/ign_base.html")
                            .withAlternateRel()));
    }

    @Test
    public void alternateDocTurtle() throws Exception {
        mockMvc
            .perform(
                get("/doc/infos/ign_base.ttl"))
            .andExpect(
                header()
                    .string(
                        "Link",
                        new LinkMatcher(
                            "http://localhost/doc/infos/ign_base.ttl")
                            .withCanonicalRel()))
            .andExpect(
                header()
                    .string(
                        "Link",
                        new LinkMatcher(
                            "http://localhost/doc/infos/ign_base.html")
                            .withAlternateRel()))
            .andExpect(
                header()
                    .string(
                        "Link",
                        new LinkMatcher(
                            "http://localhost/api/servers/ign_base?" + GET_FEATURE_INFO_REQUEST)
                            .withAlternateRel()));
    }

    @Test
    public void alternateDocHtml() throws Exception {
        mockMvc
            .perform(
                get("/doc/infos/ign_base.html"))
            .andExpect(
                header()
                    .string(
                        "Link",
                        new LinkMatcher(
                            "http://localhost/doc/infos/ign_base.html")
                            .withCanonicalRel()))
            .andExpect(
                header()
                    .string(
                        "Link",
                        new LinkMatcher(
                            "http://localhost/doc/infos/ign_base.ttl")
                            .withAlternateRel()))
            .andExpect(
                header()
                    .string(
                        "Link",
                        new LinkMatcher(
                            "http://localhost/api/servers/ign_base?" + GET_FEATURE_INFO_REQUEST)
                            .withAlternateRel()));

    }

    @Test
    public void alternateDoc() throws Exception {
        mockMvc
            .perform(
                get("/doc/infos/ign_base"))
            .andExpect(
                header()
                    .string(
                        "Link",
                        new LinkMatcher(
                            "http://localhost/doc/infos/ign_base.html")
                            .withAlternateRel()))
            .andExpect(
                header()
                    .string(
                        "Link",
                        new LinkMatcher(
                            "http://localhost/doc/infos/ign_base.ttl")
                            .withAlternateRel()))
            .andExpect(
                header()
                    .string(
                        "Link",
                        new LinkMatcher(
                            "http://localhost/api/servers/ign_base?" + GET_FEATURE_INFO_REQUEST)
                            .withAlternateRel()));

    }
}
