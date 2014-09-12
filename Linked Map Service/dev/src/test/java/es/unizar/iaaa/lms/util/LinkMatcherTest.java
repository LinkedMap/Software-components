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

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class LinkMatcherTest {

    @Test
    public void matchesCommas1() {
        assertTrue(new LinkMatcher(new Link(
            "http://example.com/TheBook/chapter2?a=a,b,c").withAboutRel())
            .matches("<http://example.com/TheBook/chapter2?a=a,b,c>;rel=\"about\""));
    }

    @Test
    public void matchesCommas2() {
        assertTrue(new LinkMatcher(new Link(
            "http://example.com/TheBook/chapter2?a=b,c,a").withAboutRel())
            .matches("<http://example.com/TheBook/chapter2?a=a,b,c>;rel=\"about\","
                    + "<http://example.com/TheBook/chapter2?a=b,c,a>;rel=\"about\""));
    }

    @Test
    public void matchesCommas3() {
        assertTrue(new LinkMatcher(new Link(
            "http://example.com/TheBook/chapter2?a=b,c,a").withAboutRel())
            .matches("<http://example.com/TheBook/chapter2?a=a,b,c>;rel=\"about\","
                    + "<http://example.com/TheBook/chapter2?a=c,b,a>;rel=\"about\","
                    + "<http://example.com/TheBook/chapter2?a=b,c,a>;rel=\"about\""));
    }
}
