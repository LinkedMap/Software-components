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
package es.unizar.iaaa.lms.core.description;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.shared.impl.PrefixMappingImpl;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.XSD;

import es.unizar.iaaa.lms.pubby.HypermediaControls;
import es.unizar.iaaa.lms.pubby.PubbyRepository;
import es.unizar.iaaa.lms.pubby.URIPrefixer;
import es.unizar.iaaa.lms.pubby.VocabularyStore.CachedPropertyCollection;

/**
 * A convenient interface to an RDF description of a resource. Provides access
 * to its label, a textual comment, detailed representations of its properties,
 * and so on.
 * 
 * TODO: Sort out who constructs these. Perhaps only the data source and this
 * class itself should?
 */
public class ResourceDescription {
    private final static int HIGH_DEGREE_CUTOFF = 10;

    final HypermediaControls hypermediaResource;
    final Model model;
    private final Resource resource;
    final PubbyRepository config;
    private final Map<Property, Integer> highIndegreeProperties;
    private final Map<Property, Integer> highOutdegreeProperties;
    private PrefixMapping prefixes = null;
    private List<ResourceProperty> properties = null;

    public ResourceDescription(HypermediaControls controller, Model model,
            PubbyRepository config) {
        this(controller, model, null, null, config, false);
    }

    public ResourceDescription(HypermediaControls controller, Model model,
            Map<Property, Integer> highIndegreeProperties,
            Map<Property, Integer> highOutdegreeProperties,
            PubbyRepository config) {
        this(controller, model, highIndegreeProperties,
            highOutdegreeProperties, config, true);
    }

    private ResourceDescription(HypermediaControls controller, Model model,
            Map<Property, Integer> highIndegreeProperties,
            Map<Property, Integer> highOutdegreeProperties,
            PubbyRepository config, boolean learnHighDegreeProps) {
        this.hypermediaResource = controller;
        this.model = model;
        this.resource = model.getResource(controller.getAbsoluteIRI());
        this.config = config;
        this.highIndegreeProperties = highIndegreeProperties == null ? Collections
            .<Property, Integer> emptyMap()
                : highIndegreeProperties;
        this.highOutdegreeProperties = highOutdegreeProperties == null ? Collections
            .<Property, Integer> emptyMap()
                : highOutdegreeProperties;
        if (learnHighDegreeProps) {
            learnHighDegreeProperties(true, HIGH_DEGREE_CUTOFF);
            learnHighDegreeProperties(false, HIGH_DEGREE_CUTOFF);
        }
    }

    ResourceDescription(Resource resource, Model model,
            PubbyRepository config) {
        this.hypermediaResource = null;
        this.model = model;
        this.resource = resource;
        this.config = config;
        this.highIndegreeProperties = Collections
            .<Property, Integer> emptyMap();
        this.highOutdegreeProperties = Collections
            .<Property, Integer> emptyMap();
    }

    public String getURI() {
        return resource.getURI();
    }

    public Model getModel() {
        return model;
    }

    /**
     * If {@link #getLabel()} is non null, return the label. If it is null,
     * generate an attempt at a human-readable title from the URI. If the
     * resource is blank, return null.
     */
    public String getTitle() {
        if (!resource.isResource())
            return null;
        Literal l = getLabel();
        String label = l == null ? null : l.getLexicalForm();
        String lang = l == null ? null : l.getLanguage();
        if (label == null) {
            label = new URIPrefixer(resource, getPrefixes()).getLocalName();
        }
        if ("".equals(label)) { // Prefix mapping assigns an empty local name
            label = resource.getURI();
            lang = null;
        }
        return toTitleCase(label, lang);
    }

    public Literal getLabel() {
        Collection<RDFNode> candidates = getValuesFromMultipleProperties(config
            .getLabelProperties());
        return getBestLanguageMatch(candidates, config.getDefaultLanguage());
    }

    public String getComment() {
        Collection<RDFNode> candidates = getValuesFromMultipleProperties(config
            .getCommentProperties());
        Literal l = getBestLanguageMatch(candidates,
            config.getDefaultLanguage());
        if (l == null)
            return null;
        return toSentenceCase(l.getLexicalForm(), l.getLanguage());
    }

    public String getImageURL() {
        Collection<RDFNode> candidates = getValuesFromMultipleProperties(config
            .getImageProperties());
        Iterator<RDFNode> it = candidates.iterator();
        while (it.hasNext()) {
            RDFNode candidate = it.next();
            if (candidate.isURIResource()) {
                return candidate.as(Resource.class).getURI();
            }
        }
        return null;
    }

    public ResourceProperty getProperty(Property property, boolean isInverse) {
        for (ResourceProperty p : getProperties()) {
            if (p.getURI().equals(property.getURI())
                    && p.isInverse() == isInverse) {
                return p;
            }
        }
        return null;
    }

    public List<ResourceProperty> getProperties() {
        if (properties == null) {
            properties = buildProperties();
        }
        return properties;
    }

    private List<ResourceProperty> buildProperties() {
        Map<String, PropertyBuilder> propertyBuilders = new HashMap<String, PropertyBuilder>();
        StmtIterator it = resource.listProperties();
        while (it.hasNext()) {
            Statement stmt = it.nextStatement();
            if (isEmptyLiteral(stmt.getObject()))
                continue;
            Property predicate = stmt.getPredicate();
            String key = "=>" + predicate;
            if (!propertyBuilders.containsKey(key)) {
                propertyBuilders.put(key, new PropertyBuilder(this, predicate, false,
                    config.getVocabularyStore()));
            }
            // TODO: Should distinguish clearly here between adding a
            // simple value, adding a complex (inlined) value, and
            // omitting a value. But how to decide whether blank nodes
            // are omitted or included as complex value? The decision has
            // already been made earlier when the model was built.
            propertyBuilders.get(key).addValue(stmt.getObject());
        }
        it = model.listStatements(null, null, resource);
        while (it.hasNext()) {
            Statement stmt = it.nextStatement();
            Property predicate = stmt.getPredicate();
            String key = "<=" + predicate;
            if (!propertyBuilders.containsKey(key)) {
                propertyBuilders.put(key, new PropertyBuilder(this, predicate, true,
                    config.getVocabularyStore()));
            }
            // TODO: See TODO above
            propertyBuilders.get(key).addValue(stmt.getSubject());
        }
        for (Property p : highIndegreeProperties.keySet()) {
            String key = "<=" + p;
            if (!propertyBuilders.containsKey(key)) {
                propertyBuilders.put(
                    key,
                    new PropertyBuilder(this, p, true, config
                        .getVocabularyStore()));
            }
            propertyBuilders.get(key).addHighDegreeArcs(
                highIndegreeProperties.get(p));
        }
        for (Property p : highOutdegreeProperties.keySet()) {
            String key = "=>" + p;
            if (!propertyBuilders.containsKey(key)) {
                propertyBuilders.put(
                    key,
                    new PropertyBuilder(this, p, false, config
                        .getVocabularyStore()));
            }
            propertyBuilders.get(key).addHighDegreeArcs(
                highOutdegreeProperties.get(p));
        }
        List<ResourceProperty> results = new ArrayList<ResourceProperty>();
        Iterator<PropertyBuilder> it2 = propertyBuilders.values().iterator();
        while (it2.hasNext()) {
            PropertyBuilder propertyBuilder = it2.next();
            results.add(propertyBuilder.toProperty());
        }
        Collections.sort(results);
        return results;
    }

    /**
     * Checks whether a node is an empty literal that should better be skipped.
     * The logic is that those literals are probably an error on the data
     * producer side and are best not shown to the user in HTML views.
     */
    private boolean isEmptyLiteral(RDFNode node) {
        if (!node.isLiteral())
            return false;
        Literal l = node.asLiteral();
        if (l.getDatatypeURI() == null
                || l.getDatatypeURI().startsWith(RDF.getURI())
                || l.getDatatypeURI().startsWith(XSD.getURI())) {
            if ("".equals(l.getLexicalForm()))
                return true;
        }
        return false;
    }

    /**
     * Returns a prefix mapping containing all prefixes from the input model and
     * from the configuration, with the configuration taking precedence.
     */
    PrefixMapping getPrefixes() {
        if (prefixes == null) {
            prefixes = new PrefixMappingImpl();
            prefixes.setNsPrefixes(model);
            for (String prefix : config.getPrefixes().getNsPrefixMap().keySet()) {
                prefixes.setNsPrefix(prefix, config.getPrefixes()
                    .getNsPrefixURI(prefix));
            }
        }
        return prefixes;
    }

    private Collection<RDFNode> getValuesFromMultipleProperties(
            Collection<Property> properties) {
        Collection<RDFNode> results = new ArrayList<RDFNode>();
        Iterator<Property> it = properties.iterator();
        while (it.hasNext()) {
            com.hp.hpl.jena.rdf.model.Property property = it
                .next();
            StmtIterator labelIt = resource.listProperties(property);
            while (labelIt.hasNext()) {
                RDFNode label = labelIt.nextStatement().getObject();
                results.add(label);
            }
        }
        return results;
    }

    // TODO: There is some better (?) code doing the same in
    // VocabularyStore.I18nStringValueCache
    private Literal getBestLanguageMatch(Collection<RDFNode> nodes, String lang) {
        Iterator<RDFNode> it = nodes.iterator();
        Literal aLiteral = null;
        while (it.hasNext()) {
            RDFNode candidate = it.next();
            if (!candidate.isLiteral())
                continue;
            Literal literal = candidate.asLiteral();
            if (lang == null || lang.equals(literal.getLanguage())) {
                return literal;
            }
            aLiteral = literal;
        }
        return aLiteral;
    }

    /**
     * Converts a string to Sentence Case. In our implementation, this simply
     * means the first letter is uppercased if it isn't already. Also trims
     * surrounding whitespace.
     */
    public String toSentenceCase(String s, String lang) {
        if (s == null)
            return null;
        s = s.trim();
        if ("".equals(s))
            return null;
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    /**
     * Converts a string to Title Case. Also trims surrounding whitespace and
     * collapses consecutive whitespace characters within into a single space.
     * If the language is English or null, English rules are used. Also splits
     * CamelCase into separate words to better deal with poor labels.
     */
    public String toTitleCase(String s, String lang) {
        if (s == null)
            return null;
        if (lang == null) {
            lang = config.getDefaultLanguage();
        }
        s = camelCaseBoundaryPattern.matcher(s).replaceAll(" ");
        s = s.replace(" - ", " \u2013 ");
        Set<String> uncapitalizedWords = Collections.emptySet();
        if (lang == null || "".equals(lang) || english.matcher(lang).matches()) {
            uncapitalizedWords = englishUncapitalizedWords;
        }
        StringBuffer result = new StringBuffer();
        Matcher matcher = wordPattern.matcher(s);
        boolean first = true;
        int offset = 0;
        while (matcher.find()) {
            result.append(normalizeWhitespace(
                s.substring(offset, matcher.start()), first));
            offset = matcher.end();
            String word = matcher.group();
            if ("".equals(word))
                continue;
            if (first || !uncapitalizedWords.contains(word.toLowerCase())) {
                word = word.substring(0, 1).toUpperCase() + word.substring(1);
            } else {
                word = word.substring(0, 1).toLowerCase() + word.substring(1);
            }
            result.append(word);
            first = false;
        }
        result.append(normalizeWhitespace(s.substring(offset), true));
        return result.toString();
    }

    private static Pattern wordPattern = Pattern.compile("[^ \t\r\n-]+|");
    private static Pattern camelCaseBoundaryPattern = Pattern
        .compile("(?<=(\\p{javaLowerCase}|\\p{javaUpperCase})\\p{javaLowerCase})"
                + "(?=\\p{javaUpperCase})");
    private static Pattern english = Pattern.compile("^en(-.*)?$",
        Pattern.CASE_INSENSITIVE);
    private static Set<String> englishUncapitalizedWords = new HashSet<String>(
        Arrays.asList(
            // Prepositions
            "above", "about", "across", "against", "along", "among",
            "around", "at", "before", "behind", "below", "beneath",
            "beside", "between", "beyond", "by", "down", "during",
            "except", "for", "from", "in", "inside", "into", "like",
            "near", "of", "off", "on", "since", "to", "toward",
            "through", "under", "until", "up", "upon", "with",
            "within",
            // Articles
            "a", "an", "the",
            // Conjunctions
            "and", "but", "for", "nor", "or", "so", "yet"));

    private String normalizeWhitespace(String s, boolean squash) {
        s = s.replaceAll("[ \t\r\n]+", " ");
        if (squash && " ".equals(s))
            return "";
        return s;
    }

    private void learnHighDegreeProperties(boolean isInverse, int limit) {
        CachedPropertyCollection knownHighProps = isInverse ? config
            .getVocabularyStore().getHighIndegreeProperties() : config
            .getVocabularyStore().getHighOutdegreeProperties();
        Map<Property, Integer> highCounts = isInverse ? highIndegreeProperties
                : highOutdegreeProperties;
        StmtIterator it = isInverse ? model
            .listStatements(null, null, resource) : resource
            .listProperties();
        Map<Property, Integer> valueCounts = new HashMap<Property, Integer>();
        while (it.hasNext()) {
            Property p = it.next().getPredicate();
            if (!valueCounts.containsKey(p)) {
                valueCounts.put(p, 0);
            }
            valueCounts.put(p, valueCounts.get(p) + 1);
        }
        for (Property p : valueCounts.keySet()) {
            if (valueCounts.get(p) <= limit)
                continue;
            knownHighProps.reportAdditional(p);
            if (isInverse) {
                model.removeAll(null, p, resource);
            } else {
                resource.removeAll(p);
            }
            highCounts.put(p, valueCounts.get(p));
        }
    }

}
