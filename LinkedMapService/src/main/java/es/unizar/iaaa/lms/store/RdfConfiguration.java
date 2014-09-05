package es.unizar.iaaa.lms.store;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.shared.impl.PrefixMappingImpl;

import es.unizar.iaaa.lms.pubby.VocabularyStore;


public class RdfConfiguration {
    
    private String defaultLanguage;
    private PrefixMapping prefixes;
    private Model model; 
    private List<Property> labelProperties;
    private List<Property> commentProperties;
    private List<Property> imageProperties;
    private VocabularyStore vocabularyStore;


    public RdfConfiguration() {
        prefixes = new PrefixMappingImpl();
        model = ModelFactory.createDefaultModel();
        model.setNsPrefixes(prefixes);
        labelProperties = new ArrayList<Property>();
        commentProperties = new ArrayList<Property>();
        imageProperties = new ArrayList<Property>();
    }

    public Collection<Property> getLabelProperties() {
        return labelProperties;
    }

    public String getDefaultLanguage() {
        return defaultLanguage;
    }

    public PrefixMapping getPrefixes() {
        return prefixes;
    }

    public Collection<Property> getCommentProperties() {
        return commentProperties;
    }

    public Collection<Property> getImageProperties() {
        return imageProperties;
    }

    public void setDefaultLenguage(String language) {
        defaultLanguage = language;
    }

    public void addPrefix(String prefix, String uri) {
        prefixes.setNsPrefix(prefix, uri);
    }

    public void addLabelProperty(String uri) {
        labelProperties.add(model.createProperty(prefixes.expandPrefix(uri)));
    }

    public void addCommentProperty(String uri) {
        commentProperties.add(model.createProperty(prefixes.expandPrefix(uri)));
    }
    public void addImageProperty(String uri) {
        imageProperties.add(model.createProperty(prefixes.expandPrefix(uri)));
    }

    public VocabularyStore getVocabularyStore() {
        return vocabularyStore;
    }

    public void setVocabularyStore(VocabularyStore vocabularyStore) {
        this.vocabularyStore = vocabularyStore;
    }
}
