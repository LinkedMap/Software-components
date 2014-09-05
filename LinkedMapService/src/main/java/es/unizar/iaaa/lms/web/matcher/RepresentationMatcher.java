package es.unizar.iaaa.lms.web.matcher;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.util.UriTemplate;

public class RepresentationMatcher implements HttpServletRequestMatcher {

    HttpServletRequestMatcherConfiguration config;

    public RepresentationMatcher(HttpServletRequestMatcherConfiguration config) {
        this.config = config;
    }

    @Override
    public boolean matches(HttpServletRequest request) {
        String base = request.getRequestURI();
      /*  if (config.isRedirectionMode()
                && (removeExtension(base).length() != base.length() || !request.getParameterMap().isEmpty())) {
            return false;
        }*/
        
        base = removeExtension(base.substring(request.getContextPath().length(), base.length()));
        return config.getFromTemplate().matches(base);
    }

    @Override
    public String getRequestedUri(HttpServletRequest request) {
        StringBuffer sb = new StringBuffer(request.getRequestURL());
        if (request.getQueryString() != null) {
            sb.append("?");
            sb.append(request.getQueryString());
        }
        return sb.toString();
    }

    @Override
    public String getPermanentUri(HttpServletRequest request) {
        return request.getRequestURL() + computeQuery(request, getAllowedParameters());
    }

    @Override
    public String getPreferredRepresentationUri(HttpServletRequest request) {
        return getPreferredRepresentationUri(request, config.getPreferredExtension());
    }

    @Override
    public String getPreferredRepresentationUri(HttpServletRequest request, String extension) {
        StringBuffer sb = new StringBuffer();
        sb.append(getDocumentUri(request));
        UriTemplate template = new UriTemplate("{base}.{extension}");
        sb.append(template.matches(request.getRequestURI()) ? "."
                + template.match(request.getRequestURI()).get("extension") : extension);
        sb.append(computeQuery(request, getAllowedParameters()));
        return sb.toString();
    }

    @Override
    public String getPrimaryTopicUri(HttpServletRequest request) {
        return computeUri(request, config.getResourceTemplate());
    }

    @Override
    public String getDocumentUri(HttpServletRequest request) {
        return computeUri(request, config.getRepresentationTemplate());
    }

    protected String computeUri(HttpServletRequest request, UriTemplate uriTemplate) {
        String base1 = removeExtension(request.getRequestURI());
        String base2 = removeExtension(request.getRequestURL().toString());
        if(base2.contains(base1)){
        	base2 = base2.substring(0, base2.length() - base1.length() + request.getContextPath().length());
        }
        base1 = base1.substring(request.getContextPath().length(), base1.length());
    	return base2 + uriTemplate.expand(computeIdentifier(request, base1)).toString();
    }

    protected Map<String, String> computeIdentifier(HttpServletRequest request, String uri) {
        return config.getFromTemplate().match(uri);
    }

    protected String removeExtension(String uri) {
    	try {
			URL url=new URL(uri);
			String path=url.getPath();
			UriTemplate template = new UriTemplate("{base}.{extension}");
			if( template.matches(path)){
				 String base=template.match(path).get("base");
				 return uri.replace(path, base);
			}else{
				return uri;
			}
		} catch (MalformedURLException e) {
			UriTemplate template = new UriTemplate("{base}.{extension}");
	        return template.matches(uri) ? template.match(uri).get("base") : uri;
		}
        
    }

    @Override
    public String getNormalizedQueryPart(HttpServletRequest request) {
        return computeQuery(request, getAllowedParameters());
    }

    protected String computeQuery(HttpServletRequest request, List<String> list) {
        StringBuffer sb = new StringBuffer();
        for (String param : list) {
            for (String candidate : request.getParameterMap().keySet()) {
                if (param.equalsIgnoreCase(candidate)) {
                    if (sb.length() == 0) {
                        sb.append("?");
                    } else {
                        sb.append("&");
                    }
                    sb.append(param);
                    sb.append("=");
                    if (request.getParameterMap().get(candidate).length > 0) {
                        try {
                        	String paramValue=URLEncoder.encode(request.getParameterMap().get(candidate)[0],"UTF-8");
                        	paramValue=paramValue.replace("#", "%23");
							sb.append(paramValue);
						} catch (UnsupportedEncodingException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
                    }
                    break;
                }
            }
        }
        return sb.toString();
    }

    protected List<String> getAllowedParameters() {
        return config.getAllowedParameters();
    }

    @Override
    public void setConfiguration(HttpServletRequestMatcherConfiguration configuration) {
        config = configuration;
    }

}
