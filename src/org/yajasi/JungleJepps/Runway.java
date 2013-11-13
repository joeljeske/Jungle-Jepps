package org.yajasi.JungleJepps;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.yajasi.JungleJepps.db.DatabaseException;
import org.yajasi.JungleJepps.db.DatabaseManager;
import org.yajasi.JungleJepps.pdf.HtmlPreparer;
import org.yajasi.JungleJepps.pdf.Repository;

public class Runway extends HashMap<Field, String> implements ValueByEnum {

	private Set<Field> modifiedFields;
	
	public Runway(){
		super();
		 modifiedFields = new HashSet<Field>();
	}
	
	/**
	 * Removes all Fields from the runway making it essentially a new Runway object
	 */
	public void removeAll(){
		for(Field key : this.keySet())
			this.remove(key);
		modifiedFields.clear();
	}
	
	/**
	 * Get a Field from the Runway
	 * @param key Field key
	 * @return String value for field
	 */
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
	
	/**
	 * Set a new value for a field
	 * @param field
	 * @param value
	 * @return
	 */
	@Override
	public String put(Field field, String value){
		if( isFieldReadonly(field) )
			throw new java.lang.IllegalAccessError("Field is readonly. Field: " + field.toString());
		
		//Store null instead of empty string
		if(value != null && value.isEmpty())
			value = null;
		
		String oldValue = super.put(field, value);
		
		// Old value cannot null unless new value is null and 
		if( equals(oldValue, value) )
			modifiedFields.add( field ); // This field is now modified
		
		
		return oldValue;
	}
	
	/**
	 * Used to determine if the settings indicate that this field
	 * is to be pulled from an operations database and therefore is
	 * a readonly field.
	 * @param field
	 * @return
	 */
	public boolean isFieldReadonly(Field field){
		return DatabaseManager.getSettings().isFieldOverridden(field);
	}
	
	/** 
	 * Used to indicate if this Runway object in current runtime reflects
	 * what is stored in the database. 
	 * @return
	 */
	public boolean isModified(){
		// Must contain at least one entry to be modified
		return !modifiedFields.isEmpty();
	}
	
	/**
	 * Used to save the current state of the runway to the database
	 */
	public void save(){
		modifiedFields.add(Field.AIRCRAFT_IDENTIFIER);
		modifiedFields.add(Field.RUNWAY_IDENTIFIER);
		
		Runway run = generateModifiedRunway();
		
		try {
			DatabaseManager.getDatabase().updateRunway(run);
			modifiedFields.clear();
		} catch (DatabaseException e) {
			e.printStackTrace();
		}		
	}
	
	/**
	 * Create a new runway object that only contains the fields and 
	 * the values for Fields that are modified in the current runway object.
	 * Used to save resources when updating items in the database. 
	 * @return Runway object with only modified fields
	 */
	private Runway generateModifiedRunway(){
		Runway run = new Runway();
		run.putAll( getMapOfKeys(modifiedFields) );
		return run;
	}
	
	/**
	 * Returns a new map of all the fields and values using the keys in 
	 * this set 
	 * @param keys set of fields
	 * @return Map<Field, String> new map of keys from set
	 */
	private Map<Field, String> getMapOfKeys(Set<Field>  keys){
		Map<Field, String> data = new HashMap<Field, String>();
		for(Field f : keys)
		{
			data.put( f, this.get(f) );
		}
		return data;
	}
	
	/**
	 * Used to publish the Runway PDF to the repository.
	 * @return publishedFile
	 * @throws IOException 
	 */
	public File publish() throws IOException{
		File published =  HtmlPreparer.publish(this);
		File copy = Repository.getArchiveLocation(this);
		
	    InputStream is = null;
	    OutputStream os = null;
	    try {
	        is = new FileInputStream(published);
	        os = new FileOutputStream(copy);
	        
	        byte[] buffer = new byte[1024];
	        int length;
	        while ((length = is.read(buffer)) > 0) {
	            os.write(buffer, 0, length);
	        }
	    } finally {
	        is.close();
	        os.close();
	    }
	    
	    return published;
	}

	/**
	 * Create a PDF preview of the Runway and store it temporarily.
	 * @return publishedFile
	 */
	public File preview(){
		File temp = new File("temp.pdf");
		return HtmlPreparer.publish(this, temp);		
	}

	/**
	 * 
	 * @param key
	 * @return
	 */
	@Override
	public String get(Enum key) {
		if(key instanceof Field)
			return get( (Object) key );
		
		return null; 
	}
	
	/**
	 * Are the two strings equal or both null
	 * @param str
	 * @param str2
	 * @return true if they are equal
	 */
	private boolean equals(String str, String str2){
		// The strings are the same reference or they are both not null and they
		// contain identical characters.
		return (str == str2) ||
				(str != null && str2 != null && str.equals(str2));
	}

	
}
