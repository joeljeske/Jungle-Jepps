/////////////////////////////////////////////////////////////////////////
// Author: Joel Jeske
// File: Runway.java
// Class: org.yajasi.JungleJepps.Runway
//
// Target Platform: Java Virtual Machine 
// Development Platform: Apple OS X 10.9
// Development Environment: Eclipse Kepler SDK
// 
// Project: Jungle Jepps - Desktop
// Copyright 2013 YAJASI. All rights reserved. 
// 
// Objective: This class is used to store all the data about a Runway 
// during runtime. This class will not be persistent except by calling 
// the save(), publish(), or preview() methods, where this class 
// calls methods on the database and local filesystem to store information
// either as PDF binary or in a database. This class extends a Map<Field, String> 
// and can therefore only contain values for chosen Fields as speficied in 
// the Field enum.
//
/////////////////////////////////////////////////////////////////////////

package org.yajasi.JungleJepps;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.yajasi.JungleJepps.db.DatabaseException;
import org.yajasi.JungleJepps.db.DatabaseManager;
import org.yajasi.JungleJepps.db.SettingsManager;
import org.yajasi.JungleJepps.pdf.HtmlPreparer;
import org.yajasi.JungleJepps.pdf.Repository;

public class Runway extends HashMap<Field, String> implements ValueByEnum {
	/**
	 * Serial verision for Runway object
	 */
	private static final long serialVersionUID = -5658099536565799785L;
	
	//This will hold all the fields that have been changed since the last call to save it 
	private Set<Field> modifiedFields;
	
	/**
	 * This method can be called to initialize a runway with fields. 
	 * <b>Note:</b> This method can initialize a runway with fields that 
	 * are readonly. This should be used when constructing a runway from 
	 * readonly data (e.g. from the th 3rd party database)
	 * @param readonlyFields
	 * @return Runway initialized runway object
	 */
	public static Runway initialize(Map<Field, String> readonlyFields){
		Runway runway = new Runway();
		
		//Add all the fields using a private override method
		for(Field f : readonlyFields.keySet())
			runway.putOverride(f, readonlyFields.get(f));
		
		//Return the runway with all the fields set, regardless if they are readonly
		return runway;
	}
	
	/**
	 * Constructs a new and empty Runway object.
	 */
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
	 * @return String the previous value if any
	 */
	@Override
	public String put(Field field, String value){
		if( isFieldReadonly(field) )
			throw new java.lang.IllegalAccessError("Field is readonly. Field: " + field.toString());
		
		return putOverride(field, value);
	}
	
	/**
	 * This method is private and does the leg work for putting a field
	 * in the runway object. It does not check if the field is overridden
	 * @param field
	 * @param value
	 * @return Stirng The previous value if any
	 */
	private String putOverride(Field field, String value){
		//Store null instead of empty string
		if(value != null && value.isEmpty())
			value = null;
		
		//Use the HashMap method to store the value 
		String oldValue = super.put(field, value);
		
		// Old value cannot null unless new value is null and 
		if( equals(oldValue, value) )
			modifiedFields.add( field ); // This field is now modified
		
		
		//Return the value that was previously set for this key
		return oldValue;
	}
	
	/**
	 * Used to determine if the settings indicate that this field
	 * is to be pulled from an operations database and therefore is
	 * a readonly field.
	 * @param field
	 * @return Boolean true if the field is overridden and therefore readonly
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
	 * @return File the file pointer to the location of the newly published PDF 
	 * @throws IOException 
	 */
	public File publish() throws IOException {
		//Publish and retrieve the file in one call
		File published =  HtmlPreparer.publish(this);
		
		//Now that the file is published, we need to copy it to the archive location
		//for record keeping history
		
		//Get the location where the copy should be stored
		File copy = Repository.getArchiveLocation(this);
		
	    InputStream is = null; //to read from the published file
	    OutputStream os = null; //to write to the copy file
	    try {
	    	//Construct File Streams for copying
	        is = new FileInputStream(published); 
	        os = new FileOutputStream(copy);
	        
	        //Copy 1KB at a type
	        byte[] buffer = new byte[1024];
	        int length; //The length read
	        
	        //Read from the input stream until nothing is read
	        while ((length = is.read(buffer)) > 0) 
	            os.write(buffer, 0, length); //Write out what is read without an offset
	       
	    } finally {
	    	//Good housekeeping 
	        is.close(); 
	        os.close();
	    }
	    
	    //Return the original file of the published PDF after we are done copying
	    return published;
	}

	/**
	 * Create a PDF preview of the Runway and store it temporarily.
	 * @return publishedFile
	 */
	public File preview(){
		//Construct a simple temp file and publish to it
		File temp = new File("temp.pdf");
		return HtmlPreparer.publish(this, temp);
	}

	/**
	 * Same as method <code>public String get(Field field)</code>
	 * Needed to comply with interface, ValueByEnum. 
	 * @param key
	 * @return
	 */
	@Override
	public String get(Enum key) {
		//Cast as object to avoid recursive method calls.
		return get( (Object) key ); 
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

	
	//Test method
	public static void main(String[] args){
		SettingsManager settings = DatabaseManager.getSettings();
		
		Runway r = new Runway();
		
		settings.setOverrideColumn(Field.A_LANDING_NOTE, ""); //Not overriden 
		try {
			r.put(Field.A_LANDING_NOTE, "Test 1 (Valid)");
			assert true;
		} catch(java.lang.IllegalAccessError e){
			e.printStackTrace();
			assert false;
		}
			
		settings.setOverrideColumn(Field.A_LANDING_NOTE, "THIS IS OVERRIDDEN");
		try {
			r.put(Field.A_LANDING_NOTE, "Test 2 (Invalid)"); //Should have exception
			assert false;
		} catch(java.lang.IllegalAccessError e){
			e.printStackTrace();
			assert true;
		}
		
		
		
		
	}
}
