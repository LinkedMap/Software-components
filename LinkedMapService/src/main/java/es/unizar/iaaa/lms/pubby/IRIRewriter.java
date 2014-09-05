/**
 * Copyright (C) 2007 Richard Cyganiak (richard@cyganiak.de)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package es.unizar.iaaa.lms.pubby;

import java.util.HashMap;
import java.util.Map;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

import es.unizar.iaaa.lms.pubby.vocab.CONF;

/**
 * Rewrites IRI in an 1:1 fashion. For any syntactically valid IRI
 * <code>x</code>, the following expressions must either be <code>true</code> or
 * throw an {@link IllegalArgumentException}:
 * 
 * <ul>
 * <li><code>unrewrite(rewrite(x)).equals(x)</code></li>
 * <li><code>rewrite(unrewrite(x)).equals(x)</code></li>
 * </ul>
 */
public abstract class IRIRewriter {

    /**
     * The trivial IRI rewriter that returns IRIs unmodified. We override all
     * methods for efficiency.
     */
    public final static IRIRewriter identity = new IRIRewriter() {
        @Override
        public String rewrite(String absoluteIRI) {
            return absoluteIRI;
        }

        @Override
        public String unrewrite(String absoluteIRI) {
            return absoluteIRI;
        }

        @Override
        public Property rewrite(Property original) {
            return original;
        }

        @Override
        public Property unrewrite(Property rewritten) {
            return rewritten;
        }

        @Override
        public Resource rewrite(Resource original) {
            return original;
        }

        @Override
        public Resource unrewrite(Resource rewritten) {
            return rewritten;
        }

        @Override
        public Model rewrite(Model original) {
            return original;
        }

        @Override
        public Map<Property, Integer> rewrite(Map<Property, Integer> original) {
            return original;
        }
    };

    /**
     * Rewrites an IRI.
     * 
     * @param Any
     *            absolute IRI
     * @return The rewritten form of the IRI
     */
    public abstract String rewrite(String absoluteIRI);

    /**
     * Rewrites an IRI. For any valid IRI,
     * 
     * @param Any
     *            absolute IRI
     * @return The rewritten form of the IRI
     */
    public abstract String unrewrite(String absoluteIRI);

    public Property rewrite(Property original) {
        String rewritten = rewrite(original.getURI());
        if (rewritten.equals(original.getURI())) {
            return original;
        }
        return ResourceFactory.createProperty(rewritten);
    }

    public Property unrewrite(Property rewritten) {
        String original = unrewrite(rewritten.getURI());
        if (original.equals(rewritten.getURI())) {
            return rewritten;
        }
        return ResourceFactory.createProperty(original);
    }

    public Resource rewrite(Resource original) {
        String rewritten = rewrite(original.getURI());
        if (rewritten.equals(original.getURI())) {
            return original;
        }
        return ResourceFactory.createResource(rewritten);
    }

    public Resource unrewrite(Resource rewritten) {
        String original = unrewrite(rewritten.getURI());
        if (original.equals(rewritten.getURI())) {
            return rewritten;
        }
        return ResourceFactory.createResource(original);
    }

    /**
     * Rewrites the RDF graph in a Jena model by returning a new in-memory model
     * that contains all statements from the original with any IRIs rewritten.
     * It rewrites IRIs in subject, predicate, and object position. Also
     * rewrites the namespace prefix mappings, if any are present.
     * 
     * @param original
     *            An RDF graph in Jena model form
     * @return Rewritten version of the graph
     */
    public Model rewrite(Model original) {
        Model result = ModelFactory.createDefaultModel();
        for (String prefix : original.getNsPrefixMap().keySet()) {
            String uri = original.getNsPrefixURI(prefix);
            result.setNsPrefix(prefix, rewrite(uri));
        }
        StmtIterator it = original.listStatements();
        while (it.hasNext()) {
            Statement stmt = it.nextStatement();
            Resource s = stmt.getSubject();
            Property p=stmt.getPredicate();
            RDFNode o = stmt.getObject();
            
            if(!CONF.doNotRewrite.contains(p)){
            	 if (o.isURIResource()) {
                     o = result.createResource(rewrite(o.asResource().getURI()));
                 }
            	 p = result.createProperty(rewrite(stmt.getPredicate()
                         .getURI()));
                 
            }
            if (s.isURIResource()) {
                s = result.createResource(rewrite(s.getURI()));
            }
            
             
            result.add(s, p, o);
        }
        return result;
    }

    public Map<Property, Integer> rewrite(Map<Property, Integer> original) {
        if (original == null)
            return null;
        Map<Property, Integer> result = new HashMap<Property, Integer>();
        for (Property p : original.keySet()) {
            result.put(rewrite(p), original.get(p));
        }
        return result;
    }

    /**
     * Creates a new rewriter that rewrites all IRIs starting with a given
     * namespace by replacing that namespace with another namespace. Any IRIs
     * that don't start with theat namespace are returned unchanged. For
     * example, if original namespace is <code>https://example.com/</code> and
     * replacement namespace is <code>http://localhost:8080/</code>, then the
     * IRI <code>https://example.com/foo/bar</code> will be rewritten to
     * <code>http://localhost:8080/foo/bar</code>, and the IRI
     * <code>https://foo.example.org/bar</code> will be rewritten to itself as
     * it doesn't start with the original namespace.
     * 
     * @param originalNamespace
     *            The namespace to be replaced
     * @param rewrittenNamespace
     *            The replacement namespace
     * @return
     */
    public static IRIRewriter createNamespaceBased(
            final String originalNamespace, final String rewrittenNamespace) {
        if (originalNamespace.equals(rewrittenNamespace)) {
            return identity;
        }
       /* if (originalNamespace.startsWith(rewrittenNamespace)
                || rewrittenNamespace.startsWith(originalNamespace)) {
            throw new IllegalArgumentException(
                "Cannot rewrite overlapping namespaces, "
                        + "this would be ambiguous: " + originalNamespace
                        + " => " + rewrittenNamespace);
        }*/
        return new IRIRewriter() {
            @Override
            public String rewrite(String absoluteIRI) {
                if (absoluteIRI.startsWith(originalNamespace)) {
                    return rewrittenNamespace
                            + absoluteIRI.substring(originalNamespace.length());
                }
                if (absoluteIRI.startsWith(rewrittenNamespace)) {
                    throw new IllegalArgumentException(
                        "Can't rewrite already rewritten IRI: "
                                + absoluteIRI);
                }
                return absoluteIRI;
            }

            @Override
            public String unrewrite(String absoluteIRI) {
                if (absoluteIRI.startsWith(rewrittenNamespace)) {
                    return originalNamespace
                            + absoluteIRI
                                .substring(rewrittenNamespace.length());
                }
                if (absoluteIRI.startsWith(originalNamespace)) {
                    throw new IllegalArgumentException(
                        "Can't unrewrite IRI that already is in the original namespace: "
                                + absoluteIRI);
                }
                return absoluteIRI;
            }
        };
    }

    /**
     * Creates an {@link IRIRewriter} that applies two existing IRIRewriters.
     */
    public static IRIRewriter chain(final IRIRewriter rewriter1,
            final IRIRewriter rewriter2) {
        if (rewriter1 == identity)
            return rewriter2;
        if (rewriter2 == identity)
            return rewriter1;
        return new IRIRewriter() {
            @Override
            public String rewrite(String absoluteIRI) {
                return rewriter2.rewrite(rewriter1.rewrite(absoluteIRI));
            }

            @Override
            public String unrewrite(String absoluteIRI) {
                return rewriter1.unrewrite(rewriter2.unrewrite(absoluteIRI));
            }
        };
    }
}
