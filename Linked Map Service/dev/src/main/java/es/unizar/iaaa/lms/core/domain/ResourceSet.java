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
package es.unizar.iaaa.lms.core.domain;

import java.util.ArrayList;
import java.util.List;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

public class ResourceSet {
    private List<RDFNode> list = new ArrayList<RDFNode>();

    public ResourceSet(RDFNode res) {
        if (res != null) {
            list.add(res);
        }
    }

    public ResourceSet(List<RDFNode> res) {
        list.addAll(res);
    }

    public ResourceSet find(String property) {
        List<RDFNode> newList = new ArrayList<RDFNode>();
        for (RDFNode res : list) {
            if (res.isResource()) {
                StmtIterator it = res.asResource().listProperties();
                while (it.hasNext()) {
                    Statement st = it.next();
                    if (st.getPredicate().toString().endsWith("/" + property)) {
                        newList.add(st.getObject());
                    }
                }
            }
        }
        return new ResourceSet(newList);
    }

    public ResourceSet first() {
        List<RDFNode> newList = new ArrayList<RDFNode>();
        if (list.size() > 0) {
            newList.add(list.get(0));
        }
        return new ResourceSet(newList);
    }

    public Resource asResource() {
        return (list.size() > 0 && list.get(0).isResource()) ? list.get(0)
            .asResource() : null;
    }

    public Literal asLiteral() {
        return (list.size() > 0 && list.get(0).isLiteral()) ? list.get(0)
            .asLiteral() : null;
    }

    public ResourceSet filter(String property, String literal) {
        List<RDFNode> newList = new ArrayList<RDFNode>();
        for (RDFNode res : list) {
            if (res.isResource()) {
                StmtIterator it = res.asResource().listProperties();
                while (it.hasNext()) {
                    Statement st = it.next();
                    if (st.getPredicate().toString().endsWith("/" + property)
                            && st.getString().equals(literal)) {
                        newList.add(res);
                    }
                }
            }
        }
        return new ResourceSet(newList);
    }

    public List<String> toStringArray() {
        List<String> newList = new ArrayList<String>();
        for (RDFNode res : list) {
            if (res.isResource()) {
                newList.add(res.asResource().getURI());
            } else if (res.isLiteral()) {
                newList.add(res.asLiteral().getString());
            }
        }
        return newList;
    }

    public boolean exists() {
        return list.size() > 0;
    }

    public ResourceSet find(Property property) {
        List<RDFNode> newList = new ArrayList<RDFNode>();
        for (RDFNode res : list) {
            if (res.isResource()) {
                StmtIterator it = res.asResource().listProperties(property);
                while (it.hasNext()) {
                    newList.add(it.next().getObject());
                }
            }
        }
        return new ResourceSet(newList);
    }
}
