package es.unizar.iaaa.lms.store;

import java.util.ArrayList;
import java.util.List;

public class ResourceDetails {
	
	
	private String title;
	private double x;
	private double y;
	private String provenance;
	private String dataset;
	private List<String> type;
	private List<ResourceComment> comments;
	private List<ResourceMapping> mappings;
	private String srs;
	private String url;
	private String replaces;
	
	public String getReplaces() {
		return replaces;
	}
	public void setReplaces(String replaces) {
		this.replaces = replaces;
	}
	
	public String getUrl() {
		if(url==null){
			return "";
		}
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getSrs() {
		if(srs==null){
			return "";
		}
		return srs;
	}
	public void setSrs(String srs) {
		this.srs = srs;
	}
	public String getTitle() {
		if(title==null){
			return "";
		}
		return title;
		
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public double getX() {
		return x;
	}
	public void setX(double d) {
		this.x = d;
	}
	public double getY() {
		return y;
	}
	public void setY(double d) {
		this.y = d;
	}
	public String getProvenance() {
		if(provenance==null){
			return "";
		}
		return provenance;
	}
	public void setProvenance(String provenance) {
		this.provenance = provenance;
	}
	public String getDataset() {
		if(dataset==null){
			return "";
		}
		return dataset;
	}
	public void setDataset(String dataset) {
		this.dataset = dataset;
	}
	public List<String> getType() {
		if(type==null){
			return new ArrayList<String>();
		}
		return type;
	}
	public void setType(List<String> type) {
		this.type = type;
	}
	public List<ResourceComment> getComments() {
		if(comments==null){
			return new ArrayList<ResourceComment>();
		}
		return comments;
	}
	public void setComments(List<ResourceComment> comments) {
		this.comments = comments;
	}
	public List<ResourceMapping> getMappings() {
		if(mappings==null){
			return new ArrayList<ResourceMapping>();
		}
		return mappings;
	}
	public void setMappings(List<ResourceMapping> mappings) {
		this.mappings = mappings;
	}
	
	

}
