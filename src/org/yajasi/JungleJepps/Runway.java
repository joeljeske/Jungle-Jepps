package org.yajasi.JungleJepps;

import java.io.File;
import java.sql.Date;
import java.util.HashMap;
import java.util.Map;

import org.yajasi.JungleJepps.pdf.HtmlPreparer;

public class Runway {
	
	private Map<Field, String> runway;
	private boolean isModified;
	
	public Runway(){
		 runway = new HashMap<Field, String>();
		 isModified = false;
	}
	
	public void empty(){
		runway = new HashMap<Field, String>();
	}
	
	public String getField(Field field){
		String val = runway.get(field);
		return val == null ? "" : val; 
	}
	
	public void putField(Field field, String value){
		value = value == "" ? null : value;
		runway.put(field, value);
	}
	

	
	/**
	 * Used to determine if the settings indicate that this field
	 * is to be pulled from an operations database and therefore is
	 * a readonly field.
	 * @param field
	 * @return
	 */
	public boolean isFieldReadonly(Field field){
		throw new UnsupportedOperationException();	
	}
	
	/**
	 * Used to indicate if this Runway object in current runtime reflects
	 * what is stored in the database. 
	 * @return
	 */
	public boolean isModified(){
		return isModified;
	}
	
	/**
	 * Used to save the current state of the runway to the database
	 */
	public void save(){
		throw new UnsupportedOperationException();		
	}
	
	/**
	 * Used to publish the Runway PDF to the repository.
	 * @return publishedFile
	 */
	public File publish(){
		return HtmlPreparer.publish(this);		
	}

	/**
	 * Create a PDF preview of the Runway and store it temporarily.
	 * @return publishedFile
	 */
	public File preview(){
		File temp = new File("temp.pdf");
		return HtmlPreparer.publish(this, temp);		
	}
	

	
}
