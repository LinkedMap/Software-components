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

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

public class LinkRelMatcher extends BaseMatcher<String> {

    private final String relation;

    public LinkRelMatcher(String target) {
        relation = target;
    }

    @Override
    public boolean matches(Object item) {
        if (item == null)
            return false;
        if (!(item instanceof String))
            return false;
        for (String candidate : item.toString().split(",")) {
            if (Link.valueOf(candidate.trim()).getRelation().equals(relation)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void describeTo(Description description) {
        description.appendText(relation);
    }
}
