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

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.util.StringUtils;
import org.springframework.web.util.UriTemplate;

/**
 * Value object for RFC-5899 Link headers.
 * 
 * @author Francisco J Lopez-Pellicer (based on a previous work of Oliver
 *         Gierke)
 */
public class Link {

    public static final String REL_SELF = "self";
    public static final String REL_ABOUT = "about";
    public static final String REL_ALTERNATE = "alternate";
    public static final String REL_CANONICAL = "canonical";
    public static final String REL_SERVICE = "service";
    final private String rel;
    final private String target;

    /**
     * Creates a new link to the given URI with the self rel.
     * 
     * @see #REL_SELF
     * @param href
     *            must not be {@literal null} or empty.
     */
    public Link(String target) {
        this(target, REL_SELF);
    }

    /**
     * Creates a new Link from the given {@link UriTemplate} and rel.
     * 
     * @param template
     *            must not be {@literal null}.
     * @param rel
     *            must not be {@literal null} or empty.
     */
    public Link(String target, String rel) {
        this.target = target;
        this.rel = rel;
        if (!URI.create(target).isAbsolute())
            throw new IllegalArgumentException("The target " + target
                    + " must be absolute");
    }

    /**
     * Factory method to easily create {@link Link} instances from RFC-5988
     * compatible {@link String} representations of a link. Will return
     * {@literal null} if an empty or {@literal null} {@link String} is given.
     * 
     * @param element
     *            an RFC-5899 compatible representation of a link.
     * @throws IllegalArgumentException
     *             if a non-empty {@link String} was given that does not adhere
     *             to RFC-5899.
     * @return
     */
    public static Link valueOf(String element) {

        if (!StringUtils.hasText(element)) {
            return null;
        }

        Pattern uriAndAttributes = Pattern.compile("<([^>]*)>;(.*)");
        Matcher matcher = uriAndAttributes.matcher(element);

        if (matcher.find()) {

            Map<String, String> attributes = getAttributeMap(matcher.group(2));

            if (!attributes.containsKey("rel")) {
                throw new IllegalArgumentException(
                    "Link does not provide a rel attribute!");
            }

            return new Link(matcher.group(1), attributes.get("rel"));

        } else {
            throw new IllegalArgumentException(String.format(
                "Given link header %s is not RFC5988 compliant!", element));
        }
    }

    /**
     * Parses the links attributes from the given source {@link String}.
     * 
     * @param source
     * @return
     */
    private static Map<String, String> getAttributeMap(String source) {

        if (!StringUtils.hasText(source)) {
            return Collections.emptyMap();
        }

        Map<String, String> attributes = new HashMap<String, String>();
        Pattern keyAndValue = Pattern.compile("(\\w+)=\\\"(\\p{Alnum}*)\"");
        Matcher matcher = keyAndValue.matcher(source);

        while (matcher.find()) {
            attributes.put(matcher.group(1), matcher.group(2));
        }

        return attributes;
    }

    /**
     * The target of the {@link Link}.
     * 
     * @return an absolute URI reference
     */
    public String getTarget() {
        return target;
    }

    /**
     * The relation type of the {@link Link}.
     * 
     * @return a representation of the relation type
     */
    public String getRelation() {
        return rel;
    }

    /**
     * Create a new {@link Link} of relation type "service".
     * 
     * @return a new {@link Link}
     */
    public Link withServiceRel() {
        return new Link(this.target, REL_SERVICE);
    }

    /**
     * Create a new {@link Link} of relation type "self".
     * 
     * @return a new {@link Link}
     */
    public Link withSelfRel() {
        return new Link(this.target, REL_SELF);
    }

    /**
     * Create a new {@link Link} of relation type "about".
     * 
     * @return a new {@link Link}
     */
    public Link withAboutRel() {
        return new Link(this.target, REL_ABOUT);
    }

    /**
     * Create a new {@link Link} of relation type "canonical".
     * 
     * @return a new {@link Link}
     */
    public Link withCanonicalRel() {
        return new Link(this.target, REL_CANONICAL);
    }

    /**
     * Create a new {@link Link} of relation type "alternate".
     * 
     * @return a new {@link Link}
     */
    public Link withAlternateRel() {
        return new Link(this.target, REL_ALTERNATE);
    }

    /**
     * Create a {@link String} representation of an array of {@link Link}
     * separated by ",".
     */
    public static String toFormatedString(Link... links) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < links.length; i++) {
            sb.append(links[i].toString());
            if (i < links.length - 1) {
                sb.append(',');
            }
        }
        return sb.toString();
    }

    /**
     * Create a {@link String} representation of a {@link Link} as
     * <code>&lt;target&gt;;rel="type"</code>.
     */
    @Override
    public String toString() {
        return String.format("<%s>;rel=\"%s\"", target, rel);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((rel == null) ? 0 : rel.hashCode());
        result = prime * result + ((target == null) ? 0 : target.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Link other = (Link) obj;
        if (rel == null) {
            if (other.rel != null)
                return false;
        } else if (!rel.equals(other.rel))
            return false;
        if (target == null) {
            if (other.target != null)
                return false;
        } else if (!target.equals(other.target))
            return false;
        return true;
    }

}
