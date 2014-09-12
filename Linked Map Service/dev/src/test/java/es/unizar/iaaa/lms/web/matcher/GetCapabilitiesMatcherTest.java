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

public class GetCapabilitiesMatcherTest {

    BaseWmsMatcher matcher;

    HttpServletRequest request;

    Map<String, String[]> PARAMETERS;
    String QUERY;
    String EXPECTED_QUERY = "?version=1.3.0&service=WMS&request=GetCapabilities";
    Map<String, String[]> PARAMETERS_A_B_C;

    @Before
    public void setup() {
        HttpServletRequestMatcherConfiguration config = new HttpServletRequestMatcherConfiguration();
        config.setFromTemplate("/api/servers/{beta}");
        config.setResourceTemplate("/id/servers/{beta}");
        config.setRepresentationTemplate("/doc/servers/{beta}");
        config.setPreferredExtension(".txt");

        matcher = new GetCapabilitiesMatcher(config);
        request = mock(HttpServletRequest.class);

        PARAMETERS = new HashMap<String, String[]>();
        PARAMETERS.put("request", new String[] { "GetCapabilities" });
        PARAMETERS.put("service", new String[] { "WMS" });
        PARAMETERS.put("version", new String[] { "1.3.0" });
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
        assertEquals("http://localhost/context/id/servers/b", matcher.getPrimaryTopicUri(request));

        // Link canonical: the preferred representation of the resource
        assertEquals("http://localhost/context/doc/servers/b" + EXPECTED_QUERY,
            matcher.getPreferredRepresentationUri(request));

        // Link alternative: the base for alternative documents
        assertEquals("http://localhost/context/doc/servers/b", matcher.getDocumentUri(request));
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
