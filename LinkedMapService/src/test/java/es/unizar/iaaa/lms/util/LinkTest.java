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

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class LinkTest {

    @Test
    public void parseLink() {
        Link l = Link
            .valueOf("<http://example.com/TheBook/chapter2>; rel=\"previous\";title=\"previous chapter\"");
        assertEquals("http://example.com/TheBook/chapter2", l.getTarget());
        assertEquals("previous", l.getRelation());
        assertEquals("<http://example.com/TheBook/chapter2>;rel=\"previous\"",
            l.toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void targetMustBeAbsoluteIfBaseNotProvided() {
        Link.valueOf("<TheBook/chapter2>;rel=\"previous\";title=\"previous chapter\"");
    }

    @Test
    public void serviceLink() {
        Link l = new Link("http://linkedmap.unizar.es/def/lms/this")
            .withServiceRel();
        assertEquals(
            "<http://linkedmap.unizar.es/def/lms/this>;rel=\"service\"",
            l.toString());
    }

    @Test
    public void selfLink() {
        Link l = new Link("http://linkedmap.unizar.es/def/lms/this")
            .withSelfRel();
        assertEquals("<http://linkedmap.unizar.es/def/lms/this>;rel=\"self\"",
            l.toString());
    }

    @Test
    public void aboutLink() {
        Link l = new Link("http://linkedmap.unizar.es/def/lms/this")
            .withAboutRel();
        assertEquals("<http://linkedmap.unizar.es/def/lms/this>;rel=\"about\"",
            l.toString());
    }

    @Test
    public void canonicalLink() {
        Link l = new Link("http://linkedmap.unizar.es/def/lms/this")
            .withCanonicalRel();
        assertEquals(
            "<http://linkedmap.unizar.es/def/lms/this>;rel=\"canonical\"",
            l.toString());
    }

    @Test
    public void alternateLink() {
        Link l = new Link("http://linkedmap.unizar.es/def/lms/this")
            .withAlternateRel();
        assertEquals(
            "<http://linkedmap.unizar.es/def/lms/this>;rel=\"alternate\"",
            l.toString());
    }

    @Test
    public void printLink1() {
        Link l = new Link("http://linkedmap.unizar.es/def/lms/this")
            .withAlternateRel();
        assertEquals(
            "<http://linkedmap.unizar.es/def/lms/this>;rel=\"alternate\"",
            Link.toFormatedString(l));
    }

    @Test
    public void linkWithComma() {
        Link l = Link
            .valueOf("<http://linkedmap.unizar.es/def/lms/this?a=a,b,c>;rel=\"alternate\"");
        assertEquals(
            "<http://linkedmap.unizar.es/def/lms/this?a=a,b,c>;rel=\"alternate\"",
            Link.toFormatedString(l));
    }

    @Test
    public void printLink2() {
        Link l1 = new Link("http://linkedmap.unizar.es/def/lms/this")
            .withAlternateRel();
        Link l2 = new Link("http://linkedmap.unizar.es/def/lms/this")
            .withCanonicalRel();
        assertEquals(
            "<http://linkedmap.unizar.es/def/lms/this>;rel=\"alternate\","
                    + "<http://linkedmap.unizar.es/def/lms/this>;rel=\"canonical\"",
            Link.toFormatedString(l1, l2));
    }

}
