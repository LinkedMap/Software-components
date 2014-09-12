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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

public class LinkMatcher extends BaseMatcher<String> {

    private final Link link;

    public LinkMatcher(String target) {
        this.link = new Link(target);
    }

    public LinkMatcher(Link link) {
        this.link = link;
    }

    @Override
    public boolean matches(Object item) {
        if (item == null)
            return false;
        if (!(item instanceof String))
            return false;
        Pattern p = Pattern.compile("(<[^>]*>[^<]+)((?:,\\s*)(.*))?");
        Matcher m = p.matcher(item.toString());
        while (m.matches()) {
            if (Link.valueOf(m.group(1)).equals(link)) {
                return true;
            }
            if (m.group(3) == null) {
                return false;
            }
            m = p.matcher(m.group(3));
        }
        return false;
    }

    @Override
    public void describeTo(Description description) {
        description.appendText(link.toString());
    }

    public LinkMatcher withServiceRel() {
        return new LinkMatcher(link.withServiceRel());
    }

    public LinkMatcher withAboutRel() {
        return new LinkMatcher(link.withAboutRel());
    }

    public LinkMatcher withCanonicalRel() {
        return new LinkMatcher(link.withCanonicalRel());
    }

    public org.hamcrest.Matcher<? super String> withAlternateRel() {
        return new LinkMatcher(link.withAlternateRel());
    }

}
