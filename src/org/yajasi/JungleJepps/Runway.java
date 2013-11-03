package org.yajasi.JungleJepps;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import org.yajasi.JungleJepps.db.DatabaseManager;
import org.yajasi.JungleJepps.pdf.HtmlPreparer;

public class Runway extends HashMap<Field, String> implements ValueByEnum {

	private boolean isModified;
	
	public Runway(){
		super();
		 isModified = false;
	}
	
	public void removeAll(){
		for(Field key : this.keySet())
			this.remove(key);
	}
	
	@Override
	public String get(Object key){
		Field field;
		if(key instanceof Field)
			field  = (Field) key;
		else if(key instanceof String)
			field = Field.valueOf( (String) key);
		else
			throw new ClassCastException("Key is not of type enum org.yajasi.JungleJepps.Field or java.lang.String");
		
		String val = super.get(field);
                return val == null ? "" : val; //Return empty string instead of null
	}
	
	@Override
	public String put(Field field, String value){
		if( isFieldReadonly(field) )
			throw new java.lang.IllegalAccessError("Field is readonly. Field: " + field.toString());
		
		//Store null instead of empty string
		if(value != null && value.isEmpty())
			value = null;
		
		value = super.put(field, value);
		
		isModified = true;
		
		return value;
	}
	
	/**
	 * Used to determine if the settings indicate that this field
	 * is to be pulled from an operations database and therefore is
	 * a readonly field.
	 * @param field
	 * @return
	 */
	public boolean isFieldReadonly(Field field){
		return false;//DatabaseManager.getSettings().isFieldOverridden(field);
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
		// isModified = false;		
	}
	
	/**
	 * Used to publish the Runway PDF to the repository.
	 * @return publishedFile
	 * @throws IOException 
	 */
	public File publish() throws IOException{
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

	@Override
	public String get(Enum key) {
		if(key instanceof Field)
			return get( (Object) key );
		
		return null; 
	}
	

	
}
