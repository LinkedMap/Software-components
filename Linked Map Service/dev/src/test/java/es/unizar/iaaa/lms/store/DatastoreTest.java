package es.unizar.iaaa.lms.store;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.context.WebApplicationContext;

import es.unizar.iaaa.lms.config.ContainerIntegrationTestPersistenceConfig;
import es.unizar.iaaa.lms.config.CoreConfig;
import es.unizar.iaaa.lms.config.WebConfig;
import es.unizar.iaaa.lms.core.domain.Layer;
import es.unizar.iaaa.lms.core.domain.MapServer;
import es.unizar.iaaa.lms.store.Datastore;
import es.unizar.iaaa.lms.store.RdfContainer;
import es.unizar.iaaa.lms.store.RdfResource;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration()
@ContextConfiguration(classes = {
        ContainerIntegrationTestPersistenceConfig.class, CoreConfig.class,
        WebConfig.class, DatastoreTest.class })
@Configuration
public class DatastoreTest {

    @Autowired
    WebApplicationContext wac;

    @Autowired
    Datastore datastore;

    @Bean
    public Datastore datastore() {
        Datastore ds = new Datastore();
        ds.setLocation("/WEB-INF/datastore");
        ds.setTargetBase("http://localhost/");
        ds.setDatasetBase("http://linkedmap.unizar.es/");
        return ds;
    }
    
    @Before
    public void setup() throws Exception {
        datastore.cleanInit();
        datastore.loadWMS("classpath:ign_base.xml");
    }

    @Test
    public void existsServer() throws Exception {
        assertTrue(datastore.exists("http://localhost/id/servers/ign_base"));
        RdfResource r = datastore.get("http://localhost/id/servers/ign_base");
        assertFalse(r.isContainer());
        assertTrue(r.getModel().contains(r.getModel().createResource("http://localhost/id/servers/ign_base"), null));

        RdfResource map = r.asInstance(MapServer.class);
        assertEquals("Mapa base de España del Instituto Geográfico Nacional", map.getTitle());
        // assertTrue(map.getDescription().startsWith("Servicio Web de Mapas conforme al perfil"));

        assertFalse(datastore.exists("http://localhost/id/servers/other"));
    }

    @Test
    public void existsServers() {
        assertTrue(datastore.exists("http://localhost/id/servers"));
        RdfResource r = datastore.get("http://localhost/id/servers");
        assertTrue(r.isContainer());
        RdfContainer c = r.asContainer();
        assertEquals(1, c.size());
    }

    @Test
    public void existsLayer() throws Exception {
        assertTrue(datastore.exists("http://localhost/id/layers/ign_base_5"));
        RdfResource r = datastore.get("http://localhost/id/layers/ign_base_5");
        assertFalse(r.isContainer());
        assertTrue(r.getModel().contains(r.getModel().createResource("http://localhost/id/layers/ign_base_5"), null));
        assertFalse(datastore.exists("http://localhost/id/layers/other"));

        Layer map = r.asInstance(Layer.class);
        assertEquals("Redes de transporte", map.getTitle());
        assertEquals("TN.TransportNetwork", map.getIdentifier());
        // assertTrue(map.getDescription().startsWith("Representación de objetos geográficos correspondientes "));
    }

    @Test
    public void existsLayers() {
        assertTrue(datastore.exists("http://localhost/id/layers"));
        RdfResource r = datastore.get("http://localhost/id/layers");
        assertTrue(r.isContainer());
        RdfContainer c = r.asContainer();
        assertEquals(26, c.size());
    }

}
