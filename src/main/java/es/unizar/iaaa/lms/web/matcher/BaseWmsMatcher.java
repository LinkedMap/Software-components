package es.unizar.iaaa.lms.web.matcher;

import javax.servlet.http.HttpServletRequest;

public class BaseWmsMatcher extends RepresentationMatcher {

    public BaseWmsMatcher(HttpServletRequestMatcherConfiguration config) {
        super(config);
    }

    protected boolean exists(HttpServletRequest request, String key, String value) {
        for (String candidate : request.getParameterMap().keySet()) {
            if (key.equalsIgnoreCase(candidate) && value.equals(request.getParameterMap().get(candidate)[0])) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String getPreferredRepresentationUri(HttpServletRequest request) {
        return getPermanentUri(request);
    }

    @Override
    public String getNormalizedQueryPart(HttpServletRequest request) {
        return "";
    }

    protected boolean exists(HttpServletRequest request, String key) {
        for (String candidate : request.getParameterMap().keySet()) {
            if (key.equalsIgnoreCase(candidate)) {
                return true;
            }
        }
        return false;
    }

}
