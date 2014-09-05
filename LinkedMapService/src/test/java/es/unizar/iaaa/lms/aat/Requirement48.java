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

import static es.unizar.iaaa.lms.util.Util.removeParameters;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.xpath;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.WebApplicationContext;

import es.unizar.iaaa.lms.config.CoreConfig;
import es.unizar.iaaa.lms.config.PersistenceConfig;
import es.unizar.iaaa.lms.config.WebConfig;

/**
 * This class tests that the form of a WMS request to an online resource URI is
 * exactly the same as in the WMS specification. The LMS server routes the
 * request to a WMS server and waits the response. Finally, the LMS server
 * returns the response to the client.
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
public class Requirement48 {

    static final String CONTAINER_MAP_SERVERS = "/api/servers";
    static final String GET_MAP_REQUEST_1 = "version=1.3.0&"
            + "service=WMS&request=GetMap&&layers=EL.Elevation&crs=EPSG:25830&"
            + "bbox=648531.27015522,4597343.1368003,700634.8413783,4627293.6534597&"
            + "styles=&bgcolor=0xFFFFFF&exceptions=INIMAGE&format=image/png&width=1009&height=580";
    static final String GET_CAPABILITIES_REQUEST_1 = "version=1.3.0&service=WMS&request=GetCapabilities";
    static final String GET_FEATURE_INFO_REQUEST_1 = "version=1.3.0&"
            + "service=WMS&request=GetFeatureInfo&layers=EL.Elevation&query_layers=EL.Elevation&crs=EPSG:25830&"
            + "bbox=648531.27015522,4597343.1368003,700634.8413783,4627293.6534597&styles=&"
            + "bgcolor=0xFFFFFF&exceptions=INIMAGE&format=image/png&width=1009&"
            + "height=580&i=504&j=290&info_format=text/plain";

    public static final String LSM_MOCK_IGN_BASE = CONTAINER_MAP_SERVERS
            + "/ign_base";
    public static final String LSM_MOCK_IGN_BASE_ONLINE_RESOURCE = "http://localhost"
            + LSM_MOCK_IGN_BASE + "?";
    public static final String LSM_MOCK_IGN_BASE_GET_FEATURE_INFO_REQUEST_1 = LSM_MOCK_IGN_BASE_ONLINE_RESOURCE
            + GET_FEATURE_INFO_REQUEST_1;
    public static final String LSM_MOCK_IGN_BASE_GET_MAP_REQUEST_1 = LSM_MOCK_IGN_BASE_ONLINE_RESOURCE
            + GET_MAP_REQUEST_1;
    public static final String LSM_MOCK_IGN_BASE_GET_CAPABILITIES_REQUEST_1 = LSM_MOCK_IGN_BASE_ONLINE_RESOURCE
            + GET_CAPABILITIES_REQUEST_1;

    public static final String LSM_TEST_IGN_BASE = "http://localhost:8080"
            + CONTAINER_MAP_SERVERS + "/ign_base";
    public static final String LSM_TEST_IGN_BASE_ONLINE_RESOURCE = LSM_TEST_IGN_BASE
            + "?";

    public static final String LSM_TEST_IGN_BASE_GET_FEATURE_INFO_REQUEST_1 = LSM_TEST_IGN_BASE_ONLINE_RESOURCE
            + GET_FEATURE_INFO_REQUEST_1;
    public static final String LSM_TEST_IGN_BASE_GET_MAP_REQUEST_1 = LSM_TEST_IGN_BASE_ONLINE_RESOURCE
            + GET_MAP_REQUEST_1;
    public static final String LSM_TEST_IGN_BASE_GET_CAPABILITIES_REQUEST_1 = LSM_TEST_IGN_BASE_ONLINE_RESOURCE
            + GET_CAPABILITIES_REQUEST_1;

    public static final String WMS_IGN_BASE = "http://www.ign.es/wms-inspire/ign-base";
    public static final String WMS_IGN_BASE_ONLINE_RESOURCE = WMS_IGN_BASE
            + "?";

    public static final String WMS_IGN_BASE_GET_FEATURE_INFO_REQUEST_1 = WMS_IGN_BASE_ONLINE_RESOURCE
            + GET_FEATURE_INFO_REQUEST_1;
    public static final String WMS_IGN_BASE_GET_MAP_REQUEST_1 = WMS_IGN_BASE_ONLINE_RESOURCE
            + GET_MAP_REQUEST_1;
    public static final String WMS_IGN_BASE_GET_CAPABILITIES_REQUEST_1 = WMS_IGN_BASE_ONLINE_RESOURCE
            + GET_CAPABILITIES_REQUEST_1;
    /**
     * Web application context.
     */
    @Autowired
    private WebApplicationContext wac;

    /**
     * Mock MVC helper.
     */
    private MockMvc mockMvc;

    /**
     * Initializer of the mock MVC helper.
     */
    @Before
    public final void init() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

    /**
     * KVP WMS GetFeatureInfo requests must behave exactly as in the WMS
     * specification.
     * 
     * @throws Exception
     *             if I/O fails
     * @see Integration test
     */
    @Test
    public final void proxyRequestGetFeatureInfo() throws Exception {
        String body = getFeatureInfo(WMS_IGN_BASE_GET_FEATURE_INFO_REQUEST_1);

        mockMvc.perform(get(LSM_MOCK_IGN_BASE_GET_FEATURE_INFO_REQUEST_1))
            .andExpect(status().isOk())
            .andExpect(
                content().contentTypeCompatibleWith(
                    MediaType.TEXT_PLAIN))
            .andExpect(content().string(body));
    }

    /**
     * KVP WMS GetMap requests must behave exactly as in the WMS specification.
     * 
     * @throws Exception
     *             when I/O fails
     * @see Integration test
     */
    @Test
    public final void proxyRequestGetMap() throws Exception {
        byte[] body = getMap(WMS_IGN_BASE_GET_MAP_REQUEST_1);

        mockMvc.perform(get(LSM_MOCK_IGN_BASE_GET_MAP_REQUEST_1))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.IMAGE_PNG))
            .andExpect(content().bytes(body));
    }

    /**
     * KVP WMS GetCapabilities requests must behave exactly as in the WMS
     * specification.
     * 
     * @throws Exception
     *             TODO Integration test
     */
    @Test
    public final void proxyRequestGetCapabilties() throws Exception {
        mockMvc.perform(get(LSM_MOCK_IGN_BASE_GET_CAPABILITIES_REQUEST_1))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.TEXT_XML))
            .andExpect(
                xpath("//HTTP//@href").string(
                    LSM_MOCK_IGN_BASE_ONLINE_RESOURCE));
    }

    /**
     * Ensures a GetFeatureInfo request.
     * 
     * @param url
     *            a GetFeatureInfo request
     * @return the description of a feature
     */
    protected final String getFeatureInfo(final String url) {
        ResponseEntity<String> re = new RestTemplate().getForEntity(url,
            String.class);
        assertEquals(HttpStatus.OK, re.getStatusCode());
        assertEquals("Server is not behaving as expected",
            MediaType.TEXT_PLAIN, removeParameters(re.getHeaders()
                .getContentType()));
        String expectedContent = re.getBody();
        assertEquals("no features were found",
            expectedContent.split("\n")[0]);
        return expectedContent;
    }

    /**
     * Ensures a GetCapabilities request.
     * 
     * @param url
     *            a GetCapabilities request
     * @return a XML document
     */
    protected final String getCapabilities(final String url) {
        ResponseEntity<String> re = new RestTemplate().getForEntity(url,
            String.class);
        assertEquals(HttpStatus.OK, re.getStatusCode());
        assertEquals(MediaType.TEXT_XML, removeParameters(re.getHeaders()
            .getContentType()));
        String expectedContent = re.getBody();
        assertTrue(expectedContent.split("\n")[1]
            .startsWith("<WMS_Capabilities"));
        return expectedContent;
    }

    /**
     * Ensures a GetMap request that returns a PNG image.
     * 
     * @param url
     *            a GetMap request
     * @return an image as a byte array
     */
    protected final byte[] getMap(final String url) {
        ResponseEntity<byte[]> re = new RestTemplate().getForEntity(url,
            byte[].class);
        assertEquals(HttpStatus.OK, re.getStatusCode());
        assertEquals("Server is not behaving as expected", MediaType.IMAGE_PNG,
            re.getHeaders().getContentType());
        return re.getBody();
    }
}
