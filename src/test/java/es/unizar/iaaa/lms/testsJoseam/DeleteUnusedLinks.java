package es.unizar.iaaa.lms.testsJoseam;

import java.io.InputStream;

import junit.framework.TestCase;

import org.apache.commons.io.IOUtils;

import com.hp.hpl.jena.sparql.engine.http.HttpQuery;

public class DeleteUnusedLinks extends TestCase {

	public void test1(){
		String auxDeleteEditTriples="delete { ?id ?p ?o}  where {  ?id ?p ?o . FILTER(?id = <http://linkedmap.unizar.es/id/data/BCN-3410694:joseam:1/8/2014:1406887277006>) }";
		HttpQuery httpQuery = new HttpQuery("http://descartes.cps.unizar.es:8080/strabonendpoint/Query");
		httpQuery.setBasicAuthentication("endpoint", "3ndpo1nt".toCharArray());
		httpQuery.addParam("format", "HTML");
		httpQuery.addParam("query", auxDeleteEditTriples);
		httpQuery.addParam("submit", "Update");
		httpQuery.addParam("view", "HTML");
		httpQuery.addParam("handle", "plain");
		try {
			InputStream in = httpQuery.exec();
			System.out.println(IOUtils.toString(in));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
}
