package es.unizar.iaaa.lms.web.matcher;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;

import org.junit.Before;
import org.junit.Test;

public class GetFeatureInfoMatcherTest {

    GetFeatureInfoMatcher matcher;

    HttpServletRequest request;

    Map<String, String[]> PARAMETERS;
    String QUERY;
    String EXPECTED_QUERY = "?version=1.3.0&"
            + "request=GetFeatureInfo&layers=SombreadoPenBal,NucleosPob_mayores,RedHidrografica_S,Laguna,Humedal,"
            + "CuerposAgua,ConstruccionHidrografica_S,ConstruccionHidrografica_L,CauceArtificial,Rio,RedHidrografica,"
            + "FFCC_AVE,FC_Convencional_B,FC_Convencional,EnlaceCarreteras,CarreteraNacional,CarreteraConvencional,"
            + "CarreteraAutonomica,VialUrbano,Autovia,Autopista_Autovia,Autopista,TN.TransportNetwork.RunwayArea,"
            + "Aeropuertos,Municipio&styles=,,,,,,,,,,,,,,,,,,,,,,,,&crs=EPSG:25830&bbox=648531.27015522,4597343.1368003,700634.8413783,4627293.6534597&"
            + "width=1009&height=580&format=image/png&"
            + "bgcolor=0xFFFFFF&query_layers=Municipio&info_format=text/plain&i=504&j=290&exceptions=INIMAGE";

    Map<String, String[]> PARAMETERS_A_B_C;

    @Before
    public void setup() {
        HttpServletRequestMatcherConfiguration config = new HttpServletRequestMatcherConfiguration();
        config.setFromTemplate("/api/servers/{beta}");
        config.setResourceTemplate("/id/infos/{beta}");
        config.setRepresentationTemplate("/doc/infos/{beta}");
        config.setPreferredExtension(".txt");
        config.setIdentifierKey("beta");

        matcher = new GetFeatureInfoMatcher(config);
        request = mock(HttpServletRequest.class);

        PARAMETERS = new HashMap<String, String[]>();
        for (String kv : EXPECTED_QUERY.substring(1).split("&")) {
            String v[] = kv.split("=");
            PARAMETERS.put(v[0], new String[] { v[1] });
        }
        StringBuffer sb = new StringBuffer();
        for (Entry<String, String[]> p : PARAMETERS.entrySet()) {
            if (sb.length() > 0) {
                sb.append("&");
            }
            sb.append(p.getKey());
            sb.append("=");
            sb.append(p.getValue()[0]);
        }
        QUERY = sb.toString();
        PARAMETERS_A_B_C = new HashMap<String, String[]>();
        PARAMETERS_A_B_C.put("c", new String[] { "2" });
        PARAMETERS_A_B_C.put("a", new String[] { "3" });
        PARAMETERS_A_B_C.put("b", new String[] { "5" });
    }

    @Test
    public void checkMatchesWithoutQuery() {
        when(request.getContextPath()).thenReturn("/context");
        when(request.getRequestURI()).thenReturn("/context/api/servers/b");
        when(request.getRequestURL()).thenReturn(new StringBuffer("http://localhost/context/api/servers/b"));
        when(request.getParameterMap()).thenReturn(Collections.<String, String[]> emptyMap());
        assertFalse(matcher.matches(request));
    }

    @Test
    public void checkMatchesWithExtension() {
        when(request.getContextPath()).thenReturn("/context");
        when(request.getRequestURI()).thenReturn("/context/api/servers/b.doc");
        when(request.getRequestURL()).thenReturn(new StringBuffer("http://localhost/context/api/servers/b.doc"));
        when(request.getParameterMap()).thenReturn(Collections.<String, String[]> emptyMap());
        assertFalse(matcher.matches(request));
    }

    @Test
    public void checkMatchesWithQueryWithExtension() {
        when(request.getContextPath()).thenReturn("/context");
        when(request.getRequestURI()).thenReturn("/context/api/servers/b.doc");
        when(request.getRequestURL()).thenReturn(new StringBuffer("http://localhost/context/api/servers/b.doc"));
        when(request.getParameterMap()).thenReturn(PARAMETERS);
        assertFalse(matcher.matches(request));
    }

    @Test
    public void checkMatchesWithQuery() {
        when(request.getContextPath()).thenReturn("/context");
        when(request.getRequestURI()).thenReturn("/context/api/servers/b");
        when(request.getRequestURL()).thenReturn(new StringBuffer("http://localhost/context/api/servers/b"));
        when(request.getQueryString()).thenReturn(QUERY);
        when(request.getParameterMap()).thenReturn(PARAMETERS);
        assertTrue(matcher.matches(request));

        // No link, current request
        assertEquals("http://localhost/context/api/servers/b?" + QUERY, matcher.getRequestedUri(request));

        // Link self: the right URI
        assertEquals("http://localhost/context/api/servers/b" + EXPECTED_QUERY, matcher.getPermanentUri(request));

        // Link about: the topic the request is about
        assertEquals("http://localhost/context/id/infos/60297c85-f335-3835-a7b2-87ebf89da3f6",
            matcher.getPrimaryTopicUri(request));

        // Link canonical: the preferred representation of the resource
        assertEquals("http://localhost/context/api/servers/b" + EXPECTED_QUERY,
            matcher.getPreferredRepresentationUri(request));

        // Link alternative: the base for alternative documents
        assertEquals("http://localhost/context/doc/infos/60297c85-f335-3835-a7b2-87ebf89da3f6",
            matcher.getDocumentUri(request));
        assertEquals("", matcher.getNormalizedQueryPart(request));
    }

    @Test
    public void checkMatchesWithFaultQuery() {
        when(request.getContextPath()).thenReturn("/context");
        when(request.getRequestURI()).thenReturn("/context/api/servers/b");
        when(request.getRequestURL()).thenReturn(new StringBuffer("http://localhost/context/api/servers/b"));
        when(request.getQueryString()).thenReturn("c=2&a=3&b=5");
        when(request.getParameterMap()).thenReturn(PARAMETERS_A_B_C);
        assertFalse(matcher.matches(request));
    }
}
