package es.unizar.iaaa.lms.store;

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
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.shared.impl.PrefixMappingImpl;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.XSD;

import es.unizar.iaaa.lms.core.domain.ResourceSet;
import es.unizar.iaaa.lms.pubby.URIPrefixer;

public class RdfResource {

    protected String uri;

    protected Model model;

    protected Resource resource;

    protected RdfRepresentation representation;

    protected RdfConfiguration config;

    private PrefixMapping prefixes = null;

    private List<RdfResourceProperty> properties = null;

    public RdfResource(String uri, RdfConfiguration config, Model model) {
        this.uri = uri;
        this.model = model;
        this.config = config;
        this.resource = model.createResource(uri);
    }

    public RdfResource(String uri, RdfConfiguration config) {
        this(uri, config, ModelFactory.createDefaultModel());
    }

    public Model getModel() {
        return model;
    }

    public boolean isContainer() {
        return false;
    }

    public RdfContainer asContainer() {
        throw new UnsupportedOperationException();
    }

    public <T> T asInstance(Class<T> clazz) throws Exception {
        return clazz.getConstructor(String.class, Model.class).newInstance(uri, model);
    }

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

    public String getIdentifier() {
        return new ResourceSet(resource).
            find(DCTerms.identifier).
            asLiteral().
            getString();
    }

    public RdfRepresentation createRepresentation(String representationUri) {
        if (representation == null) {
            representation = new RdfRepresentation(representationUri, uri, config);
        }
        return representation;
    }

    public Resource getResource() {
        return resource;
    }

    public RdfRepresentation getRepresentation() {
        return representation;
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

    public List<RdfResourceProperty> getProperties() {
        if (properties == null) {
            properties = buildProperties();
        }
        return properties;
    }

    private List<RdfResourceProperty> buildProperties() {
        Map<String, RdfPropertyBuilder> propertyBuilders = new HashMap<String, RdfPropertyBuilder>();
        StmtIterator it = resource.listProperties();
        while (it.hasNext()) {
            Statement stmt = it.nextStatement();
            if (isEmptyLiteral(stmt.getObject()))
                continue;
            Property predicate = stmt.getPredicate();
            String key = "=>" + predicate;
            if (!propertyBuilders.containsKey(key)) {
                propertyBuilders.put(key, new RdfPropertyBuilder(this, predicate, false,
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
                propertyBuilders.put(key, new RdfPropertyBuilder(this, predicate, true,
                    config.getVocabularyStore()));
            }
            // TODO: See TODO above
            propertyBuilders.get(key).addValue(stmt.getSubject());
        }
//        for (Property p : highIndegreeProperties.keySet()) {
//            String key = "<=" + p;
//            if (!propertyBuilders.containsKey(key)) {
//                propertyBuilders.put(
//                    key,
//                    new PropertyBuilder(this, p, true, config
//                        .getVocabularyStore()));
//            }
//            propertyBuilders.get(key).addHighDegreeArcs(
//                highIndegreeProperties.get(p));
//        }
//        for (Property p : highOutdegreeProperties.keySet()) {
//            String key = "=>" + p;
//            if (!propertyBuilders.containsKey(key)) {
//                propertyBuilders.put(
//                    key,
//                    new PropertyBuilder(this, p, false, config
//                        .getVocabularyStore()));
//            }
//            propertyBuilders.get(key).addHighDegreeArcs(
//                highOutdegreeProperties.get(p));
//        }
        List<RdfResourceProperty> results = new ArrayList<RdfResourceProperty>();
        Iterator<RdfPropertyBuilder> it2 = propertyBuilders.values().iterator();
        while (it2.hasNext()) {
            RdfPropertyBuilder propertyBuilder = it2.next();
            results.add(propertyBuilder.toProperty());
        }
        Collections.sort(results);
        return results;
    }

    public Model getModelWithRepresentation() {
        if (representation == null)
            return resource.getModel();
        Model m = ModelFactory.createDefaultModel();
        return m.add(resource.getModel()).add(representation.getResource().getModel());
    }

    public Literal getLabel() {
        Collection<RDFNode> candidates = getValuesFromMultipleProperties(config
            .getLabelProperties());
        return getBestLanguageMatch(candidates, config.getDefaultLanguage());
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
}
