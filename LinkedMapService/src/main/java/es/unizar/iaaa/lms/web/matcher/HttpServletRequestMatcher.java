package es.unizar.iaaa.lms.web.matcher;

import javax.servlet.http.HttpServletRequest;

public interface HttpServletRequestMatcher {

    public boolean matches(HttpServletRequest request);

    /**
     * @param request
     * @return
     */
    public String getRequestedUri(HttpServletRequest request);

    /**
     * The {@code String URI} that should be considered as the permanent URI for
     * future references of this resource.
     * 
     * @param request
     * @return
     */
    public String getPermanentUri(HttpServletRequest request);

    /**
     * The {@code String URI} that should be considered as the permanent URI for
     * future references of the preferred representation of this resource.
     * 
     * @param request
     * @return
     */
    public String getPreferredRepresentationUri(HttpServletRequest request);

    /**
     * The {@code String URI} that should be considered as the permanent URI for
     * future references of the primary topic of this resource.
     * 
     * @param request
     * @return
     */
    public String getPrimaryTopicUri(HttpServletRequest request);

    /**
     * The {@code String URI} that should be considered as the base URI for
     * future references of alternative representations. The {@code String} must
     * not end in an extension.
     * 
     * @param request
     * @return
     */
    public String getDocumentUri(HttpServletRequest request);

    /**
     * The {@code String} that contains the normalized query string including
     * the character '{@code ?}'. Otherwise this method returns an empty
     * {@code String}.
     * 
     * @param request
     * @return
     */
    public String getNormalizedQueryPart(HttpServletRequest request);

    /**
     * Modifies the configuration.
     * 
     * @param configuration
     */
    public void setConfiguration(HttpServletRequestMatcherConfiguration configuration);

    /**
     * The {@code String URI} that should be considered as the permanent URI for
     * future references of the preferred representation of this resource.
     * 
     * @param request
     * @return
     */
    public String getPreferredRepresentationUri(HttpServletRequest request, String extension);

}
