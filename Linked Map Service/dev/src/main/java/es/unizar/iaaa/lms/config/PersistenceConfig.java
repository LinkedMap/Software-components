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
package es.unizar.iaaa.lms.config;

import java.io.IOException;

import javax.servlet.ServletContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import es.unizar.iaaa.lms.pubby.PubbyRepository;
import es.unizar.iaaa.lms.pubby.VocabularyStore;
import es.unizar.iaaa.lms.pubby.sources.DataSource;
import es.unizar.iaaa.lms.pubby.sources.MergeDataSource;
import es.unizar.iaaa.lms.pubby.sources.ModelDataSource;
import es.unizar.iaaa.lms.pubby.vocab.CONF;
import es.unizar.iaaa.lms.pubby.vocab.LMS;
import es.unizar.iaaa.lms.store.Datastore;
import es.unizar.iaaa.lms.store.RdfConfiguration;

@Configuration
@PropertySource("classpath:app.properties")
public class PersistenceConfig {

    @Autowired
    private Environment env;
    
    @Autowired
    private ServletContext servletContext;

    @Autowired
    private ApplicationContext ctx;

    @Bean
    public PubbyRepository pubbyRepository() throws IOException {
        Resource configFile = ctx.getResource(env.getRequiredProperty("pubby.config.location"));
        Model m = ModelFactory.createDefaultModel();
        m.read(configFile.getInputStream(), configFile.getURL().toString(),
            env.getProperty("pubby.config.language","TURTLE"));
        return PubbyRepository.create(m, ctx, servletContext);
    }

    @Bean
    public Datastore datastore() {
        Datastore ds = new Datastore();
        ds.setLocation(env.getRequiredProperty("datastore.location"));
        ds.setTargetBase(env.getRequiredProperty("datastore.targetBase"));
        ds.setDatasetBase(env.getRequiredProperty("datastore.datasetBase"));
        ds.setInitClean(env.getProperty("datastore.initClean", Boolean.class, false));
        return ds;
    }
    
    @Bean
    public RdfConfiguration rdfConfiguration() throws IOException {
        RdfConfiguration conf = new RdfConfiguration();
        conf.setDefaultLenguage(env.getRequiredProperty("model.defaultLanguage"));
        for(String prefix: env.getRequiredProperty("model.prefix").split(",")) {
            prefix = prefix.trim();
            conf.addPrefix(prefix, env.getRequiredProperty("model.prefix."+prefix).trim());
        }
        for(String key: env.getRequiredProperty("model.labelProperties").split(",")) {
            conf.addLabelProperty(key.trim());
        }
        for(String key: env.getRequiredProperty("model.commentProperties").split(",")) {
            conf.addCommentProperty(key.trim());
        }
        for(String key: env.getRequiredProperty("model.imageProperties").split(",")) {
            conf.addImageProperty(key.trim());
        }
        VocabularyStore vocabularyStore = new VocabularyStore();
        DataSource vocabularyDataSource = new MergeDataSource(
            new ModelDataSource(pubbyRepository().getModel()),pubbyRepository().getDataSource());
        vocabularyStore.setDataSource(vocabularyDataSource);
        vocabularyStore.setDefaultLanguage(pubbyRepository().getDefaultLanguage());
        conf.setVocabularyStore(vocabularyStore);
        
        CONF.doNotRewrite.add(LMS.sparqlEndpoint);
        CONF.doNotRewrite.add(LMS.defaultGraph);
        
        return conf;
    }
}
