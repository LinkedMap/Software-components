/**
 * This file is part of Linked Map Service (LMS).
 *
 * Linked Map Service (LMS) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Linked Map Service (LMS) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Linked Map Service (LMS).  If not, see <http://www.gnu.org/licenses/>.
 */
package es.unizar.iaaa.lms.web.controller;

import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.springframework.core.style.StylerUtils;
import org.springframework.web.util.UrlPathHelper;

@SuppressWarnings("serial")
public class NotFoundException extends ServletException {

    private String methodName;

    /**
     * Create a new NoSuchRequestHandlingMethodException for the given request.
     * 
     * @param request
     *            the offending HTTP request
     */
    public NotFoundException(HttpServletRequest request) {
        this(new UrlPathHelper().getRequestUri(request), request.getMethod(),
            request.getParameterMap());
    }

    /**
     * Create a new NoSuchRequestHandlingMethodException.
     * 
     * @param urlPath
     *            the request URI that has been used for handler lookup
     * @param method
     *            the HTTP request method of the request
     * @param parameterMap
     *            the request's parameters as map
     */
    public NotFoundException(String urlPath, String method,
            Map<String, String[]> parameterMap) {
        super("No matching handler method found for servlet request: path '"
                + urlPath +
                "', method '" + method + "', parameters "
                + StylerUtils.style(parameterMap));
    }

    /**
     * Create a new NoSuchRequestHandlingMethodException for the given request.
     * 
     * @param methodName
     *            the name of the handler method that wasn't found
     * @param controllerClass
     *            the class the handler method was expected to be in
     */
    public NotFoundException(String methodName, Class<?> controllerClass) {
        super("No request handling method with name '" + methodName +
                "' in class [" + controllerClass.getName() + "]");
        this.methodName = methodName;
    }

    /**
     * Return the name of the offending method, if known.
     */
    public String getMethodName() {
        return this.methodName;
    }

}
