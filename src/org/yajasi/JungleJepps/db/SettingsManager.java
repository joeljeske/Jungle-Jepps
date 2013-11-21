/////////////////////////////////////////////////////////////////////////
// Author: Joel Jeske
// File: SettingsManager.java
// Class: org.yajasi.JungleJepps.db.SettingsManager
//
// Target Platform: Java Virtual Machine 
// Development Platform: Apple OS X 10.9
// Development Environment: Eclipse Kepler SDK
// 
// Project: Jungle Jepps - Desktop
// Copyright 2013 YAJASI. All rights reserved. 
// 
// Objective: This class is used to store all the persistent settings. This 
// class makes use of the Java properties persistence using a simple key-
// value pairing storage in plain text. This class has wrapper methods 
// such that other classes can store arrays or field labels. Note the 
// warying difference when using a Settings ENUM or a Field ENUM as the 
// primary key.
//
/////////////////////////////////////////////////////////////////////////

package org.yajasi.JungleJepps.db;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.yajasi.JungleJepps.Field;
import org.yajasi.JungleJepps.ValueByEnum;

/**
 * This class makes use of the singleton model having exactly one instance
 * at a time during runtime.  
 * 
 * @author Joel Jeske
 *
 */
public class SettingsManager implements ValueByEnum{
	
	/**
	 * The file where the .properties file will be stored
	 */
	private static final File DB;
	
	/**
	 * The connection to the properties file where data transfer occurs 
	 */
	private static final Properties connection;
	
	/**
	 * Each field has a label. This is the prefix to differentiate 
	 * the field label from the field defaults or the field override. 
	 */
	private static final String LABEL_PREFIX = "label.";
	
	/**
	 * Some fields are a drop down list that has default options. 
	 * This is the prefix to differentiate the field defaults from 
	 * the field label or the field override. 
	 */
	private static final String DEFAULTS_PREFIX = "defaults.";
	
	/**
	 * Some fields may be overridden from a 3rd party db.
	 * This is the prefix to differentiate the field override 
	 * from the field label or the field defaults. 
	 */
	private static final String OVERRIDE_PREFIX = "override.";
	
	/**
	 * The field defaults are stored a separated list with the 
	 * following string as the separator. 
	 */
	private static final String LIST_SEPERATOR = ";";
	
	/////////////////////////////////////////////////////////////////////
	/**
	 * The handle to the settings manager instance
	 */
	private static SettingsManager instance;

	
	/**
	 * The static reference to the instance. Used to keep only one 
	 * instance during runtime.
	 */
	static {
		System.out.println("Loading settings...");
		DB = new File("settings.properties.txt");
		connection = new Properties();
	}
	
	/**
	 * This class overrides the use of the persistent file and instead loads from the 
	 * Server primary instance relayed by the client class. 
	 * @param stream
	 */
	public static void loadFromInputStream(InputStream stream){
		//Verbose 
		System.out.println("Loading settings from primary source...");

		try {
			//Load the stream into runtime and override most fields
			connection.load(stream);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// Overriding fields from the primary source that do not apply to the secondary instance.
		instance.setValue(Settings.IS_PRIMARY, false);
	}
	
	/**
	 * Called from the server when the secondary instance needs the raw stream 
	 * too the persistant file. 
	 * @return
	 */
	public static InputStream getSettingsStream(){
		//Will hold our stream from the file
		InputStream is = null;
		
		//Make sure all changes are saved in the file before loading it. 
		getInstance().save();
		
		try {
			//Create the input stream to the raw file
			is = new FileInputStream(DB);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		//Give the input stream so it can be used to load the settings on the client
		return is;
	}
	
	/**
	 * Get the raw length of the settings file. Used so the HTTP server can
	 * accurately define the length of its message to the client.
	 * @return Long the length in bytes of the settings file
	 */
	public static Long getLength(){
		//Make sure everything is saved
		getInstance().save();
		
		//Return the length of the file in bytes
		return DB.length();
	}

	///////////////////////////////////////////////////////////////////////////	
	/**
	 * Singleton model. Use to retrieve an instance of this class. 
	 * Used to prevent multiple instances trying to modify or read from a single file.
	 * @return SettingsManager instance
	 */
	public static SettingsManager getInstance(){
		//Only create an instance if one does not exist
		if( instance == null)
			instance = new SettingsManager();
		return instance;
	}
	
	/**
	 * Private constructor. Sets up connection and initializes defaults if
	 * no file is found.
	 */
	private SettingsManager(){
		//If the settings file exists and we dont need initialization
		if( DB.exists() )
		{
			//Load the 
			InputStream is;
			try {
				//Create an input stream from the settings file
				is = new FileInputStream( DB );
				
				//Load this stream into our settings
				connection.load( is );
				
				//Good housekeeping
				is.close();
			} catch (Exception e) {
				e.printStackTrace();
				
				//Reset all the settings
				initialize();
				
				//Save this clean version
				save();
			}
		}
		else
		{
			//Reset all the settings
			initialize();
			
			//Save this new clean set of settings
			save();
		}
	}
	
	/**
	 * Use to make changes persistent
	 */
	public void save(){
		try {
			//Save this using the Java properties save method. 
			connection.store(new FileOutputStream(DB), "Jungle Jepps Settings List");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Called to setup defaults settings
	 */
	private void initialize(){
		System.out.println("Loading default settings!");
		/* Initialize Labels */
		setLabel(Field.RUNWAY_IDENTIFIER, 		"Runway");
		setLabel(Field.RUNWAY_NAME, 			"");
		setLabel(Field.AIRCRAFT_IDENTIFIER, 	"Aircraft");
		setLabel(Field.LONGITUDE, 				"Longitude");
		setLabel(Field.LATITUDE, 				"Latitude");
		setLabel(Field.INSPECTION_NA, 			"N/A");
		setLabel(Field.INSPECTION_DATE, 		"Completed");
		setLabel(Field.INSPECTOR_NAME, 			"Pilot");
		setLabel(Field.INSPECTION_DUE, 			"Expiration");
		setLabel(Field.CLASSIFICATION, 			"Classification");
		setLabel(Field.FREQUENCY_1, 			"VHF");
		setLabel(Field.FREQUENCY_2, 			"HF");
		setLabel(Field.LANGUAGE_GREET, 			"Language / Greeting");
		setLabel(Field.ELEVATION, 				"Elevation");
		setLabel(Field.LENGTH, 					"Length");
		setLabel(Field.WIDTH_TEXT, 				"Width");
		setLabel(Field.TDZ_SLOPE, 				"TD Zone Slope");
		setLabel(Field.IAS_ADJUSTMENT, 			"Gnd Speed");
		setLabel(Field.PRECIPITATION_ON_SCREEN, "Land with Precipation");
		setLabel(Field.RUNWAY_A, 				"Runway");
		setLabel(Field.A_TAKEOFF_RESTRICTION, 	"Takeoff Weight");
		setLabel(Field.A_TAKEOFF_NOTE,			 "");
		setLabel(Field.A_LANDING_RESTRICTION, 	"Landing Weight");
		setLabel(Field.A_LANDING_NOTE, 			"");
		setLabel(Field.RUNWAY_B, 				"Runway");
		setLabel(Field.B_TAKEOFF_RESTRICTION, 	"Takeoff Weight");
		setLabel(Field.B_TAKEOFF_NOTE, 			"");
		setLabel(Field.B_LANDING_RESTRICTION, 	"Landing Weight");
		setLabel(Field.B_LANDING_NOTE, 			"");
		setLabel(Field.P1_TEXT_1, 				"Committal Point");
		setLabel(Field.P1_TEXT_2, 				"Go Around");
		setLabel(Field.P1_TEXT_3, 				"Emergency Stop After Landing");
		setLabel(Field.P1_TEXT_4, 				"Surface Description");
		setLabel(Field.P1_TEXT_5, 				"Hazards and Additional Info");
		setLabel(Field.P1_TEXT_6, 				"Aborted Takeoff");
		setLabel(Field.P1_TEXT_7, 				"Departure Engine Failure Option(s)");
		setLabel(Field.P2_TEXT_1, 				"Language Group and Greeting");
		setLabel(Field.P2_TEXT_2, 				"Weather Patterns");
		setLabel(Field.P2_TEXT_3, 				"Explanation of Restrictions");
		setLabel(Field.P2_TEXT_4, 				"Chief Pilot Comments");
		setLabel(Field.P2_TEXT_5, 				"Minumum Number/Type of Wind Indicators");
		setLabel(Field.P2_TEXT_6, 				"Runway Minimum Maintainance Standard");
		setLabel(Field.P2_TEXT_7, 				"Pilot Authority for Runway Below Standard");
		
		/* Initialize List Defaults */
		setDefaults(Field.CLASSIFICATION, new String[]{"YES", "NO"});
		/* TODO: Find all defaults needed in SRS */
		
		/* Initialize Settings Defaults */
		String defaultHomeDirectory = System.getProperty("user.home") + File.separator + "JungleJepps" + File.separator;
		
		setValue(Settings.IS_PRIMARY, 				true);
		setValue(Settings.REPOSITORY_PATH, 			defaultHomeDirectory);
		setValue(Settings.PRIMARY_JDBC_URI, 		"jdbc:sqlite:JJDB.db");
		setValue(Settings.PRIMARY_JDBC_CLASS_PATH, 	"org.sqlite.JDBC");
		setValue(Settings.IS_OPERATIONS_DB, 		false);
		setValue(Settings.ALTITUDE_UNITS, 			"ft");
		setValue(Settings.WEIGHT_UNITS, 			"kg");
		setValue(Settings.DIMENSION_UNITS, 			"m");
		setValue(Settings.DISTANCE_UNITS, 			"nm");
		setValue(Settings.DISTANCE_CONVERT_FACTOR, 	1.852);
		setValue(Settings.PAGE_1_DISCLAIMER, 		"Use of this diagram is strictly prohibited for aviation operators other than Jungle Jepps mission users. This diagram MAY CONTAIN ERRORS.");
		setValue(Settings.PAGE_2_DISCLAIMER, 		"No diagram can substitute for a proper runway checkout. Use of this diagram is strictly prohibited for aviation operators other than Jungle Jepps mission users. This diagram MAY CONTAIN ERRORS. Report all safety concerns to the Chief Pilot or designated Safety Officer.");
		setValue(Settings.DEFAULT_EXPIRATION_PERIOD, 12);
		setValue(Settings.WEB_ROOT, 				"src/xhtml/");
	}
	
	//////////////////////////////////////////////////////////////////
	/* Methods to interact with properties files */
	/**
	 * Get literal string for key 
	 * @param key
	 * @return
	 */
	private String getStringForKey(String key){
		//Get the string from settings and default to an empty string
		return connection.getProperty(key, "");
	}
		
	/**
	 * Set literal string for key
	 * @param key
	 * @param value
	 */
	private void setValue(String key, String value){
		//Set the value directly
		connection.put(key, value);
	}
	
	//////////////////////////////////////////////////////////////////
	/* GETTERS AND SETTERS */
	/**
	 * Get the field label for a given field 
	 * @param field
	 * @return field label
	 */
	public String getLabel(Field field){
		//Add the label prefix, "LABEL." to differentiate this setting
		return getStringForKey(LABEL_PREFIX + field.toString() );
	}
	
	/**
	 * Set label for field
	 * @param field
	 * @param label
	 */
	public void setLabel(Field field, String label){
		//Get the label using the same prefix used to settings
		setValue(LABEL_PREFIX + field.toString(), label);
	}
	
	/**
	 * Get the defaults for a particular field
	 * @param field
	 * @return
	 */
	public String[] getDefaults(Field field){
		//Get the array as a string
		String defaults = getStringForKey(DEFAULTS_PREFIX + field.toString());
		
		//Split up back into array for easy use
		return defaults.split( LIST_SEPERATOR );
	}
	
	/**
	 * Used to set the defaults for a drop down list. 
	 * Defaults are passed in as a String[]
	 * @param field
	 * @param defaults
	 */
	public void setDefaults(Field field, String[] defaults){
		//
		StringBuilder joined = new StringBuilder();
				
		//If there are any defaults to add
		if(defaults.length > 0)
		{
			//Loop through defaults
			for(String str : defaults)
			{
				//Add the item
				joined.append(str);
				
				//And add the delimiter
				joined.append(LIST_SEPERATOR);
			}
		
			//
			//Remove the last delimiter
			joined.substring(0, joined.length() - 1);
		}
		
		//Save this joined String
		setValue(DEFAULTS_PREFIX + field.toString(), joined.toString());
	}
	
	/**
	 * Used to get the name of the column of the overriding field
	 * in the operations database. If the field is not being overridden, 
	 * the method returns an empty string.
	 * @param field
	 * @return columnName
	 */
	public String getOverrideColumn(Field field){
		//Get the key with the correct prefix
		return getStringForKey( OVERRIDE_PREFIX + field.toString() );
	}
	
	/**
	 * Used to set the name of the column of the overriding field
	 * in the operations database. 
	 * @param field
	 * @return columnName
	 */
	public void setOverrideColumn(Field field, String columnName){
		//Set the key with the correct prefix
		setValue(OVERRIDE_PREFIX + field.toString(), columnName );
	}
	
	public boolean isFieldOverridden(Field field){
		//Field is overridden when there is a non-empty string for its override value
		return !getOverrideColumn(field).isEmpty();
	}
	
	/**
	 * Get string value for key
	 * @param key
	 * @return
	 */
	public String getStringForKey(Settings key){
		return getStringForKey(key.toString());
	}
	
	/**
	 * Set settings to string
	 * @param key
	 * @param value
	 */
	public void setValue(Settings key, String value){
		setValue(key.toString(), value);
	}

	/**
	 * Get int value for key
	 * @param key
	 * @return
	 */
	public int getIntegerForKey(Settings key){
		String val = getStringForKey(key.toString());
		val = val.isEmpty() ? "0" : val;
		return Integer.parseInt( val );	
	}

	/**
	 * Set settings to int value 
	 * @param key
	 * @param value
	 */
	public void setValue(Settings key, int value){
		setValue(key.toString(), String.valueOf( value ));
	}

	/**
	 * Get boolean value for key
	 * @param key
	 * @return
	 */
	public boolean getBooleanForKey(Settings key){
		return Boolean.parseBoolean( getStringForKey(key.toString()) );
	}

	/**
	 * Set settings to boolean value
	 * @param key
	 * @param value
	 */
	public void setValue(Settings key, boolean value){
		setValue(key.toString(), String.valueOf(value) );
	}

	/**
	 * Get double value for key
	 * @param key
	 * @return value
	 */
	public double getDoubleForKey(Settings key){
		String val = getStringForKey(key.toString());
		val = val.isEmpty() ? "0" : val;
		return Double.parseDouble( val );	
	}
	
	/**
	 * Set settings to double value 
	 * @param key
	 * @param value
	 */
	public void setValue(Settings key, double value){
		setValue(key.toString(), String.valueOf(value) );
	}

	///////////////////////////////////////////////////////////////////////
	/**
	 * Test method for running class
	 * @param args
	 */
	public static void main(String[] args){
		SettingsManager settings = getInstance();
		System.out.println( settings.getLabel(Field.IAS_ADJUSTMENT));
	}

	@Override
	public String get(Enum key) {
		if(key instanceof Field)
			return getLabel( (Field) key );
		
		if(key instanceof Settings)
			return getStringForKey( (Settings) key);
		
		return "";
	}

}
