package es.unizar.iaaa.lms.web.matcher;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

public class GetFeatureInfoMatcher extends BaseWmsMatcher {

    private final String[] GET_FEATURE_INFO_PARAMS = { "version",
            "request", "layers", "styles", "crs", "bbox", "width", "height", "format", "transparent", "bgcolor",
            "time", "elevation", "query_layers", "info_format", "feature_count", "i", "j", "exceptions"
    };

    private final String[] GET_ID_PARAMS = { "version",
            "layers", "styles", "crs", "bbox", "width", "height", "format", "transparent", "bgcolor",
            "time", "elevation", "query_layers", "info_format", "feature_count", "i", "j"
    };

    public GetFeatureInfoMatcher(HttpServletRequestMatcherConfiguration config) {
        super(config);
    }

    @Override
    public boolean matches(HttpServletRequest request) {
        String base = request.getRequestURI();
        if (removeExtension(base).length() != base.length() || request.getParameterMap().isEmpty()
                || !exists(request, "version", "1.3.0")
                || !exists(request, "request", "GetFeatureInfo")
                || !exists(request, "layers")
                || !exists(request, "styles")
                || !exists(request, "crs")
                || !exists(request, "bbox")
                || !exists(request, "width")
                || !exists(request, "height")
                || !exists(request, "format")
                || !exists(request, "query_layers")
                || !exists(request, "info_format")
                || !exists(request, "i")
                || !exists(request, "j")) {
            return false;
        }
        base = base.substring(request.getContextPath().length(), base.length());
        return config.getFromTemplate().matches(base);
    }

    @Override
    protected List<String> getAllowedParameters() {
        return Arrays.asList(GET_FEATURE_INFO_PARAMS);
    }

    @Override
    protected Map<String, String> computeIdentifier(HttpServletRequest request, String uri) {
        Map<String, String> params = super.computeIdentifier(request, uri);

        String value = params.get(config.getIdentifierKey()) + computeQuery(request, Arrays.asList(GET_ID_PARAMS));

        params.put(config.getIdentifierKey(), UUID.nameUUIDFromBytes(value.getBytes(Charset.forName("UTF-8")))
            .toString());

        return params;
    }
}
