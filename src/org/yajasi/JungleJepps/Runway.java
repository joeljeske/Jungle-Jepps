package org.yajasi.JungleJepps;

import java.io.File;
import java.sql.Date;

import org.yajasi.JungleJepps.pdf.HtmlPreparer;

public class Runway {
	
	private boolean isModified;
	
	public Runway(){	
	}
	
	/**
	 * Used to get the current value of Field for the loaded runway.
	 * It will return the most recently edited value, not necessarily 
	 * the value in the database. 
	 * @param field
	 * @return fieldValue
	 */
	public String getField(Field field){
		throw new UnsupportedOperationException();
	}
	
	/**
	 * Used to get the label for a specific field. If the field does not have a 
	 * label it returns an empty string.
	 * @param field
	 * @return label
	 */
	public String getLabel(Field field){
		throw new UnsupportedOperationException();
	}
	
	/**
	 * Used to change the value of a field. Pass new value for the
	 * for the field. Returns true if a change occurred and false if no
	 * change occurred. 
	 * @param field
	 * @param value
	 * @return modified
	 */
	public boolean setField(Field field, String value){
		throw new UnsupportedOperationException();
	}
	
	/**
	 * Used to change the value of a field. Pass new value for the
	 * for the field. Returns true if a change occurred and false if no
	 * change occurred. 
	 * @param field
	 * @param value
	 * @return modified
	 */
	public boolean setField(Field field, Boolean value){
		throw new UnsupportedOperationException();
	}
	
	/**
	 * Used to change the value of a field. Pass new value for the
	 * for the field. Returns true if a change occurred and false if no
	 * change occurred. 
	 * @param field
	 * @param value
	 * @return modified
	 */
	public boolean setField(Field field, Date value){
		throw new UnsupportedOperationException();	
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
