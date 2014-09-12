package es.unizar.iaaa.lms.store;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ReadWrite;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.tdb.TDB;
import com.hp.hpl.jena.tdb.TDBFactory;
import com.hp.hpl.jena.vocabulary.RDF;

import es.unizar.iaaa.lms.pubby.IRIRewriter;
import es.unizar.iaaa.lms.pubby.vocab.LDP;

public class Datastore implements InitializingBean {

    private String location;
    private String target;
    private String base;
    private Dataset dataset;
    private Logger log = Logger.getLogger(Datastore.class);
    private IRIRewriter rewriter = IRIRewriter.identity;
    private boolean initClean;
    
    @Autowired
    private ApplicationContext ctx;
    
    @Autowired
    private RdfConfiguration config;
    

    public void setLocation(String location) {
        this.location = location;
    }

    public void setTargetBase(String namespace) {
        this.target = namespace;
    }

    public void setDatasetBase(String base) {
        this.base = base;
    }

    public void init() throws IOException {
        if (dataset != null) {
            log.info("Releasing previous TDB");
            dataset.close();
        }
        Resource directory = ctx.getResource(location);
        log.info("Creating TDB at " + directory);
        dataset = TDBFactory.createDataset(directory.getFile().toString());
    }

    public void cleanInit() throws IOException {
        if (dataset != null) {
            log.info("Releasing previous TDB");
            dataset.close();
        }
        Resource directory = ctx.getResource(location);
        if (directory.exists()) {
            log.info("Cleaning existing TDB at " + directory);
            FileUtils.deleteDirectory(directory.getFile());
        }
        log.info("Creating TDB at " + directory);
        dataset = TDBFactory.createDataset(directory.getFile().toString());
        rewriter = IRIRewriter.createNamespaceBased(base, target);

    }

    public void loadWMS(String location) throws Exception {
        Resource file = ctx.getResource(location);
        InputStream is = file.getInputStream();
        WmsCapabilitiesParser wcp = new WmsCapabilitiesParser(base, file.getFilename().split("\\.")[0]);
        wcp.parseDocument(is);
        dataset.begin(ReadWrite.WRITE);
        try {
            dataset.addNamedModel(wcp.getServerContainerId(), wcp.getServerContainerModel());
            dataset.addNamedModel(wcp.getServiceId(), wcp.getServiceModel());
            dataset.addNamedModel(wcp.getLayerContainerId(), wcp.getLayerContainerModel());
            List<String> layers = wcp.getLayerIds();
            List<Model> layerModels = wcp.getLayerModels();
            for (int i = 0; i < layers.size(); i++) {
                dataset.addNamedModel(layers.get(i), layerModels.get(i));
            }
            dataset.commit();
        } finally {
            dataset.end();
        }
        is.close();

    }

    public boolean exists(String uri) {
        dataset.begin(ReadWrite.READ);
        try {
            return dataset.getNamedModel(rewriter.unrewrite(uri)).size() > 0;
        } finally {
            dataset.end();
        }
    }

    public RdfResource get(String uri) {
        dataset.begin(ReadWrite.READ);
        try {
            String targetUri = rewriter.unrewrite(uri);
            Model resultModel = rewriter.rewrite(dataset.getNamedModel(targetUri));
            if (resultModel.contains(resultModel.createResource(uri), RDF.type, LDP.Container)) {
                String queryString = "PREFIX ldp: <" + LDP.NS + "> " +
                        "CONSTRUCT {?a ldp:contains ?b} WHERE {?a ldp:contains ?b}";
                Query query = QueryFactory.create(queryString);
                QueryExecution qexec = QueryExecutionFactory.create(query, dataset);
                qexec.getContext().set(TDB.symUnionDefaultGraph, true);
                resultModel.add(rewriter.rewrite(qexec.execConstruct()));
                qexec.close();
                return new RdfContainer(uri, config, resultModel);
            } else {
                return new RdfResource(uri, config, resultModel);
            }
        } finally {
            dataset.end();
        }
    }

    public void set(String topic, RdfResource description) {
        dataset.begin(ReadWrite.WRITE);
        try {
            dataset.addNamedModel(topic, description.getModel());
            dataset.commit();
        } finally {
            dataset.end();
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (initClean) {
            cleanInit();
        } else {
            init();
        }
    }

    public void setInitClean(boolean value) {
        initClean = value;
    }

}
