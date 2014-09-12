package es.unizar.iaaa.lms.web.matcher;

import javax.servlet.http.HttpServletRequest;

public class WmsMatcher implements HttpServletRequestMatcher {

    private HttpServletRequestMatcher[] matchers;

    public HttpServletRequestMatcher find(HttpServletRequest request) {
        for (HttpServletRequestMatcher m : matchers) {
            if (m.matches(request)) {
                return m;
            }
        }
        return null;
    }

    @Override
    public boolean matches(HttpServletRequest request) {
        return find(request) != null;
    }

    @Override
    public String getRequestedUri(HttpServletRequest request) {
        return find(request).getRequestedUri(request);
    }

    @Override
    public String getPermanentUri(HttpServletRequest request) {
        return find(request).getPermanentUri(request);
    }

    @Override
    public String getPreferredRepresentationUri(HttpServletRequest request) {
        return find(request).getPreferredRepresentationUri(request);
    }

    @Override
    public String getPrimaryTopicUri(HttpServletRequest request) {
        return find(request).getPrimaryTopicUri(request);
    }

    @Override
    public String getDocumentUri(HttpServletRequest request) {
        return find(request).getDocumentUri(request);
    }

    @Override
    public String getNormalizedQueryPart(HttpServletRequest request) {
        return find(request).getNormalizedQueryPart(request);
    }

    @Override
    public void setConfiguration(HttpServletRequestMatcherConfiguration config) {
        matchers = new HttpServletRequestMatcher[] { new GetCapabilitiesMatcher(config), new GetMapMatcher(config),
                new GetFeatureInfoMatcher(config) };
    }

    @Override
    public String getPreferredRepresentationUri(HttpServletRequest request, String extension) {
        return find(request).getPreferredRepresentationUri(request, extension);
    }

}
