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
package es.unizar.iaaa.lms.web.interceptor;

import static es.unizar.iaaa.lms.util.Util.baseUrl;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import es.unizar.iaaa.lms.util.Link;

/**
 * Filter that adds a RFC 5899 Link that advertises the service.
 * 
 * This filter implements the requirement that LMS server should advertise its
 * presence in all responses to URIs that it manages by adding a Link header
 * with a target URI of http://{base}/def/lms/this and a link relation of type
 * service.
 * 
 * @author Francisco J Lopez Pellicer
 * 
 */
public class DiscoveryInterceptor extends HandlerInterceptorAdapter {

    @Override
    public void postHandle(HttpServletRequest request,
            HttpServletResponse response, Object handler,
            ModelAndView modelAndView) throws Exception {
        Link link = new Link(
            baseUrl(request) + "/def/lms/this").withServiceRel();
        if (!response.containsHeader("Link")
                || !response.getHeaders("Link").contains(link.toString())) {
            response
                .addHeader("Link",
                    link.toString());
        }
    }

}
