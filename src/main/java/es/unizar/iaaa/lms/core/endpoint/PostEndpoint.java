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
package es.unizar.iaaa.lms.core.endpoint;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.IOUtils;
import org.springframework.http.MediaType;

import com.hp.hpl.jena.sparql.engine.http.HttpQuery;

import es.unizar.iaaa.lms.store.RdfConfiguration;
import es.unizar.iaaa.lms.store.RdfRepresentation;
import es.unizar.iaaa.lms.store.RdfResource;

public class PostEndpoint extends AbstractEndpoint {

	String sparqlEndpoint;

	String userEditComment = "<${MAPPINGID}:${CREATOR}:${DATE}:${HASH}:${VALUE}> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://linkedmap.unizar.es/def/comments/UserEdit> .\n"
			+ "<${MAPPINGID}:${CREATOR}:${DATE}:${HASH}:${VALUE}> <http://purl.org/dc/elements/1.1/creator> \"${CREATOR}\" .\n"
			+ "<${MAPPINGID}:${CREATOR}:${DATE}:${HASH}:${VALUE}> <http://www.w3.org/2000/01/rdf-schema#value> \"${VALUE}\" .\n"
			+ "<${MAPPINGID}:${CREATOR}:${DATE}:${HASH}:${VALUE}> <http://www.w3.org/2000/01/rdf-schema#comment> \"${COMMENT}\" . \n"
			+ "<${MAPPINGID}:${CREATOR}:${DATE}:${HASH}:${VALUE}> <http://purl.org/dc/elements/1.1/date> \"${DATE}\" . \n"
			+ "<${MAPPINGID}:${CREATOR}:${DATE}:${HASH}:${VALUE}> <http://xmlns.com/foaf/0.1/primaryTopic> <${MAPPINGID}> .";

	String targetGraph = "http://linkedmap.unizar.es/graph/UserEdits";
	String filePath="C:/apache-tomcat-7.0.54/webapps/userEdits/";
	String fileName="userEdits.log";

	ArrayList<String> editComments = new ArrayList<String>();

	public PostEndpoint(String path, String description, String sparqlEndpoint) {
		super("POST", path, description);
		editComments.add(userEditComment);
		this.sparqlEndpoint = sparqlEndpoint;
	}

	@Override
	public RdfResource description(HttpServletRequest request,
			MediaType mediaType, RdfConfiguration config) {
		String userEditCommentToInsert = userEditComment;
		Enumeration<String> pNames = request.getParameterNames();
		String mappingID=null;
		String comment="";
		Calendar c=Calendar.getInstance();
		File f=new File(filePath+c.get(Calendar.YEAR)+"_"+String.valueOf(c.get(Calendar.MONTH)+1)+"_"+c.get(Calendar.DAY_OF_MONTH)+"_"+fileName);
		f.getParentFile().mkdirs();
		if(!f.exists()){
			try {
				f.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		FileOutputStream fos=null;
		
		
		try{
		
		while (pNames.hasMoreElements()) {
			String pName = pNames.nextElement();
			userEditCommentToInsert = userEditCommentToInsert.replace("${"
					+ pName.toUpperCase() + "}", URLDecoder.decode(request.getParameter(pName),"UTF-8"));
			if(pName.equalsIgnoreCase("mappingId")){
				mappingID=request.getParameter(pName);
			}
			if(pName.equalsIgnoreCase("comment")){
				comment=request.getParameter(pName);
			}
		}
		}catch(Exception e){}
		try {
			userEditCommentToInsert=userEditCommentToInsert.replace("${HASH}", String.valueOf(URLDecoder.decode(comment,"UTF-8").hashCode()));
		} catch (UnsupportedEncodingException e2) {
			// TODO Auto-generated catch block
			userEditCommentToInsert=userEditCommentToInsert.replace("${HASH}", "NO"+String.valueOf(comment.hashCode()));
		}
		try {
			
			fos=new FileOutputStream(f,true);
			OutputStreamWriter osw=new OutputStreamWriter(fos,"UTF-8");
			osw.write(userEditCommentToInsert+"\n");
			osw.flush();
			osw.close();
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		HttpQuery httpQuery = new HttpQuery(sparqlEndpoint);
		httpQuery.setForcePOST();
		httpQuery.setBasicAuthentication("endpoint", "3ndpo1nt".toCharArray());
		httpQuery.addParam("format", "Turtle");
		httpQuery.addParam("graph", targetGraph);
		httpQuery.addParam("data", userEditCommentToInsert);
		httpQuery.addParam("dsubmit", "Store+Input");
		httpQuery.addParam("view", "HTML");
		try {
			InputStream in = httpQuery.exec();
			System.out.println(IOUtils.toString(in));
		} catch (Exception e) {
			e.printStackTrace();
		}
	
		

        RdfResource lr = new RdfResource(getRequestedUri(request), config);
        RdfRepresentation ld;
		try {
			ld = lr.createRepresentation(URLDecoder.decode(mappingID,"UTF-8"));
			return ld;
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
		
	}

}
