package es.unizar.iaaa.lms.web.matcher;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.junit.Before;
import org.junit.Test;

public class RepresentationMatcherTest {

    HttpServletRequestMatcher matcher;

    HttpServletRequest request;

    Map<String, String[]> PARAMETERS_A_B_C;

    @Before
    public void setup() {
        HttpServletRequestMatcherConfiguration config = new HttpServletRequestMatcherConfiguration();
        config.setFromTemplate("/doc/{alpha}/{beta}");
        config.setResourceTemplate("/id/{alpha}/{beta}");
        config.setRepresentationTemplate("/doc/{alpha}/{beta}");
        config.setAllowedParameters(Arrays.asList("a", "b"));
        config.setPreferredExtension(".txt");

        matcher = new RepresentationMatcher(config);
        request = mock(HttpServletRequest.class);

        PARAMETERS_A_B_C = new HashMap<String, String[]>();
        PARAMETERS_A_B_C.put("c", new String[] { "2" });
        PARAMETERS_A_B_C.put("a", new String[] { "3" });
        PARAMETERS_A_B_C.put("b", new String[] { "5" });
    }

    @Test
    public void checkMatches() {
        when(request.getContextPath()).thenReturn("/context");
        when(request.getRequestURI()).thenReturn("/context/doc/a/b");
        when(request.getRequestURL()).thenReturn(new StringBuffer("http://localhost/context/doc/a/b"));
        when(request.getParameterMap()).thenReturn(Collections.<String, String[]> emptyMap());
        assertTrue(matcher.matches(request));

        // No link, current request
        assertEquals("http://localhost/context/doc/a/b", matcher.getRequestedUri(request));

        // Link self: the right URI
        assertEquals("http://localhost/context/doc/a/b", matcher.getPermanentUri(request));

        // Link about: the topic the request is about
        assertEquals("http://localhost/context/id/a/b", matcher.getPrimaryTopicUri(request));

        // Link canonical: the preferred representation of the resource
        assertEquals("http://localhost/context/doc/a/b.txt", matcher.getPreferredRepresentationUri(request));

        // Link alternative: the base for alternative documents
        assertEquals("http://localhost/context/doc/a/b", matcher.getDocumentUri(request));
        assertEquals("", matcher.getNormalizedQueryPart(request));
    }

    @Test
    public void checkMatchesWithQuery() {
        when(request.getContextPath()).thenReturn("/context");
        when(request.getRequestURI()).thenReturn("/context/doc/a/b");
        when(request.getRequestURL()).thenReturn(new StringBuffer("http://localhost/context/doc/a/b"));
        when(request.getQueryString()).thenReturn("c=2&a=3&b=5");
        when(request.getParameterMap()).thenReturn(PARAMETERS_A_B_C);
        assertTrue(matcher.matches(request));

        // No link, current request
        assertEquals("http://localhost/context/doc/a/b?c=2&a=3&b=5", matcher.getRequestedUri(request));

        // Link self: the right URI
        assertEquals("http://localhost/context/doc/a/b?a=3&b=5", matcher.getPermanentUri(request));

        // Link about: the topic the request is about
        assertEquals("http://localhost/context/id/a/b", matcher.getPrimaryTopicUri(request));

        // Link canonical: the preferred representation of the resource
        assertEquals("http://localhost/context/doc/a/b.txt?a=3&b=5", matcher.getPreferredRepresentationUri(request));

        // Link alternative: the base for alternative documents
        assertEquals("http://localhost/context/doc/a/b", matcher.getDocumentUri(request));
        assertEquals("?a=3&b=5", matcher.getNormalizedQueryPart(request));
    }

    @Test
    public void checkMatchesWithQueryAndExtension() {
        when(request.getContextPath()).thenReturn("/context");
        when(request.getRequestURI()).thenReturn("/context/doc/a/b.doc");
        when(request.getRequestURL()).thenReturn(new StringBuffer("http://localhost/context/doc/a/b.doc"));
        when(request.getQueryString()).thenReturn("c=2&a=3&b=5");
        when(request.getParameterMap()).thenReturn(PARAMETERS_A_B_C);
        assertTrue(matcher.matches(request));

        // Link alternative: the base for alternative documents
        assertEquals("http://localhost/context/doc/a/b", matcher.getDocumentUri(request));
        assertEquals("?a=3&b=5", matcher.getNormalizedQueryPart(request));

        // No link, current request
        assertEquals("http://localhost/context/doc/a/b.doc?c=2&a=3&b=5", matcher.getRequestedUri(request));

        // Link self: the right URI
        assertEquals("http://localhost/context/doc/a/b.doc?a=3&b=5", matcher.getPermanentUri(request));

        // Link about: the topic the request is about
        assertEquals("http://localhost/context/id/a/b", matcher.getPrimaryTopicUri(request));

        // Link canonical: the preferred representation of the resource
        assertEquals("http://localhost/context/doc/a/b.doc?a=3&b=5", matcher.getPreferredRepresentationUri(request));

    }

    @Test
    public void checkMatchesWithExtension() {
        when(request.getContextPath()).thenReturn("/context");
        when(request.getRequestURI()).thenReturn("/context/doc/a/b.doc");
        when(request.getRequestURL()).thenReturn(new StringBuffer("http://localhost/context/doc/a/b.doc"));
        when(request.getParameterMap()).thenReturn(Collections.<String, String[]> emptyMap());
        assertTrue(matcher.matches(request));

        // Link alternative: the base for alternative documents
        assertEquals("http://localhost/context/doc/a/b", matcher.getDocumentUri(request));
        assertEquals("", matcher.getNormalizedQueryPart(request));

        // No link, current request
        assertEquals("http://localhost/context/doc/a/b.doc", matcher.getRequestedUri(request));

        // Link self: the right URI
        assertEquals("http://localhost/context/doc/a/b.doc", matcher.getPermanentUri(request));

        // Link about: the topic the request is about
        assertEquals("http://localhost/context/id/a/b", matcher.getPrimaryTopicUri(request));

        // Link canonical: the preferred representation of the resource
        assertEquals("http://localhost/context/doc/a/b.doc", matcher.getPreferredRepresentationUri(request));

    }
}
