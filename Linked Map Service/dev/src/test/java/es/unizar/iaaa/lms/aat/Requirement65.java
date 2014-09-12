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
package es.unizar.iaaa.lms.aat;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * This class tests the LMS server must add a {@code Link} header conveying the
 * URI that should be considered as the right URI for retrieving the resource.
 * The link relation must be specified as "self".
 * 
 * @author Francisco J Lopez-Pellicer
 * @see "Planet Data D15.1 Call 2: Linked Map requirements definition and conceptual architecture"
 * 
 */

@RunWith(Suite.class)
@Suite.SuiteClasses({
        Requirement65Api.class,
        Requirement65Redirection.class,
        Requirement65Service.class,
})
public class Requirement65 {
}
