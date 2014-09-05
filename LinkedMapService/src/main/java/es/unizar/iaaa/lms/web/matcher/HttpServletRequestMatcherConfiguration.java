package es.unizar.iaaa.lms.web.matcher;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.web.util.UriTemplate;

public class HttpServletRequestMatcherConfiguration {

    private UriTemplate fromTemplate;
    private UriTemplate resourceTemplate;
    private UriTemplate representationTemplate;
    private String preferredExtension;
    private boolean redirection = false;
    private String identifierKey;

    public void setFromTemplate(String template) {
        fromTemplate = new UriTemplate(template);
        ArrayList<String> allowedParameters= new ArrayList<String>(fromTemplate.getVariableNames());
        /*if (allowedParameters.size()>0) {
        	allowedParameters.add("query");
        }*/
    }

    public void setResourceTemplate(String template) {
        resourceTemplate = new UriTemplate(template);
    }

    public void setAllowedParameters(List<String> list) {
       

    }

    public UriTemplate getFromTemplate() {
        return fromTemplate;
    }

    public List<String> getAllowedParameters() {
        ArrayList<String> params=new ArrayList<String>();
        params.addAll(fromTemplate.getVariableNames());
    	if (params == null || params.size() ==0) {
            return Collections.emptyList();
        }
    	//params.add("query");
        return params;
    }

    public UriTemplate getResourceTemplate() {
        return resourceTemplate;
    }

    public void setPreferredExtension(String extension) {
        preferredExtension = extension;
    }

    public String getPreferredExtension() {
        return preferredExtension;
    }

    public void setRedirectionMode(boolean mode) {
        redirection = mode;
    }

    public boolean isRedirectionMode() {
        return redirection;
    }

    public void setRepresentationTemplate(String template) {
        representationTemplate = new UriTemplate(template);
    }

    public UriTemplate getRepresentationTemplate() {
        return representationTemplate;
    }

    public void setIdentifierKey(String key) {
        identifierKey = key;
    }

    public String getIdentifierKey() {
        return identifierKey;
    }
}
