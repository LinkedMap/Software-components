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

import static org.springframework.test.util.AssertionErrors.assertTrue;

import java.io.StringReader;

import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

class ModelResultMatcher {

    private String base;

    private String lang;

    public ModelResultMatcher(String base, String lang) {
        this.base = base;
        this.lang = lang;
    }

    public ResultMatcher subject(final String subject) {
        return new ResultMatcher() {

            @Override
            public void match(MvcResult result) throws Exception {
                Model m = ModelFactory.createDefaultModel();
                m.read(new StringReader(result.getResponse()
                    .getContentAsString()), base, lang);
                assertTrue("Resource " + subject + " not found",
                    m.contains(m.createResource(subject), null));
            }

        };
    }

    public static ModelResultMatcher model(String base, String lang) {
        return new ModelResultMatcher(base, lang);
    }

    public ResourceResultMatcher resource(String resource) {
        return new ResourceResultMatcher(resource, base, lang);
    }

}
