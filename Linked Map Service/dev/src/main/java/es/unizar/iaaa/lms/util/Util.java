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
package es.unizar.iaaa.lms.util;

import java.util.Enumeration;
import java.util.List;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

public class Util {

    /**
     * Extract headers from request.
     */
    public static HttpHeaders getHeaders(final HttpServletRequest request) {
        HttpHeaders headers = new HttpHeaders();
        Enumeration<String> en = request.getHeaderNames();
        while (en.hasMoreElements()) {
            String key = en.nextElement();
            headers.add(key, request.getHeader(key));
        }
        return headers;
    }

    /**
     * Remove a header.
     */
    public static HttpHeaders removeHeaders(final HttpHeaders original,
            final String... headers) {
        HttpHeaders hh = new HttpHeaders();
        hh.putAll(original);
        for (String header : headers) {
            hh.remove(header);
        }
        return HttpHeaders.readOnlyHttpHeaders(hh);
    }

    /**
     * Add a header.
     */
    public static HttpHeaders addHeaders(final HttpHeaders original,
            final String header, final String... values) {
        HttpHeaders hh = new HttpHeaders();
        hh.putAll(original);
        for (String value : values) {
            hh.add(header, value);
        }
        return HttpHeaders.readOnlyHttpHeaders(hh);
    }

    /**
     * Merge headers.
     * 
     * @param response
     * @param headers
     */
    public static void mergeHeaders(HttpServletResponse response,
            HttpHeaders headers) {
        for (Entry<String, List<String>> h : headers.entrySet()) {
            for (String v : h.getValue()) {
                if (response.containsHeader(h.getKey())
                        && !response.getHeaders(h.getKey()).contains(v)) {
                    response.setHeader(h.getKey(),
                        response.getHeader(h.getKey()) + ", " + v);
                } else {
                    response.setHeader(h.getKey(), v);
                }
            }
        }
    }

    /**
     * Remove media type parameters.
     */
    public static MediaType removeParameters(final MediaType mt) {
        return new MediaType(mt.getType(), mt.getSubtype());
    }

    /**
     * This is an utility class, so the constructor is private.
     */
    private Util() {
    }

    public static String baseUrl(HttpServletRequest request) {
        StringBuffer url = request.getRequestURL();
        String uri = request.getRequestURI();
        String ctx = request.getContextPath();
        return url.substring(0, url.length() - uri.length() + ctx.length());
    }

    public static String endpointUrl(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String ctx = request.getContextPath();
        return uri.substring(ctx.length());
    }
}
