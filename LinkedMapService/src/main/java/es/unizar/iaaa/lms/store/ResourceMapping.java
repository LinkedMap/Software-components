package es.unizar.iaaa.lms.store;

import java.util.List;

public class ResourceMapping {

	private float measure;
	private List<ResourceComment> comments;
	private ResourceDetails relatedResource;
	private String mappingId;
	
		
	public String getMappingId() {
		return mappingId;
	}
	public void setMappingId(String mappingId) {
		this.mappingId = mappingId;
	}
	public float getMeasure() {
		return measure;
	}
	public void setMeasure(float measure) {
		this.measure = measure;
	}
	public List<ResourceComment> getComments() {
		return comments;
	}
	public void setComments(List<ResourceComment> comments) {
		this.comments = comments;
	}
	public ResourceDetails getRelatedResource() {
		return relatedResource;
	}
	public void setRelatedResource(ResourceDetails relatedResource) {
		this.relatedResource = relatedResource;
	}
	
	
	
}
