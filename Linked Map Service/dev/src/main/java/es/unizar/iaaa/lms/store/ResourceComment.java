package es.unizar.iaaa.lms.store;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;


public class ResourceComment implements Comparable<ResourceComment>{

	private boolean valid;
	private String comment;
	private String user;
	private String date;
	
	public boolean isValid() {

		return valid;
	}
	public void setValid(boolean valid) {
		this.valid = valid;
	}
	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}
	public String getUser() {
		return user;
	}
	public void setUser(String user) {
		this.user = user;
	}
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	@Override
	public int compareTo(ResourceComment o) {
		if(date==null){
			return -1;
		}else if(o.getDate()==null){
			return 1;
		}
		else{
			Date local=parseDate(date);
			Date external=parseDate(o.getDate());
			if(local.getTime()==external.getTime()){
				return 0;
			}else if(local.getTime()<external.getTime()){
				return -1;
			}else{
				return 1;
			}
		}
	}
	
	public static Date parseDate(String s) {
		try {
			DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
			return df.parse(s);
		} catch (Exception e) {
			try{
				DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
				return df.parse(s);
			}catch(Exception ex){
				return null;
			}
		}
		// return df.format(d);
	}
	
	
}
