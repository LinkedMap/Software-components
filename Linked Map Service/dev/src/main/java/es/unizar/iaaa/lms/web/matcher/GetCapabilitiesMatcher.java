package es.unizar.iaaa.lms.web.matcher;

import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

public class GetCapabilitiesMatcher extends BaseWmsMatcher {

    final String[] GET_CAPABILITIES_PARAMS = { "version",
            "service", "request", "format", "updatesequence"
    };

    public GetCapabilitiesMatcher(HttpServletRequestMatcherConfiguration config) {
        super(config);
    }

    @Override
    protected List<String> getAllowedParameters() {
        return Arrays.asList(GET_CAPABILITIES_PARAMS);
    }

    @Override
    public boolean matches(HttpServletRequest request) {
        String base = request.getRequestURI();
        if (removeExtension(base).length() != base.length() || request.getParameterMap().isEmpty()
                || !exists(request, "service", "WMS")
                || !exists(request, "request", "GetCapabilities")) {
            return false;
        }
        base = base.substring(request.getContextPath().length(), base.length());
        return config.getFromTemplate().matches(base);
    }
}
