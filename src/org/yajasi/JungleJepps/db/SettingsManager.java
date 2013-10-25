package org.yajasi.JungleJepps.db;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import org.yajasi.JungleJepps.Field;

public class SettingsManager {
	
	private static final File DB;
	private static final Properties connection;
	private static final String LABEL_PREFIX = "label.";
	private static final String DEFAULTS_PREFIX = "defaults.";
	private static final String LIST_SEPERATOR = ";";
	
	/**
	 * The static reference to the instance. Used to keep only one 
	 * instance during runtime.
	 */
	static {
		DB = new File("settings.properties.db");
		connection = new Properties();
	}
	
	private static SettingsManager instance;
	
	public static void main(String[] args){
		getInstance().save();
		
	}
		
	/**
	 * Singleton model. Use to retrieve an instance of this class. 
	 * Used to prevent multiple instances trying to modify or read from a single file.
	 * @return SettingsManager instance
	 */
	public static SettingsManager getInstance(){
		if( instance == null)
			instance = new SettingsManager();
		return instance;
	}
	
	/**
	 * Private constructor 
	 */
	private SettingsManager(){
		if( DB.exists() )
		{
			try {
				connection.load( new FileInputStream( DB ) );
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				initialize();
			} catch (IOException e) {
				e.printStackTrace();
				initialize();
			}
		}
		else
		{
			initialize();
		}
	}
	
	public void save(){
		try {
			connection.store(new FileOutputStream(DB), "Jungle Jepps Settings List");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	private void initialize(){
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
		
		/* Initialize List Defalts */
		
		/* Initialize Settings Defaults */
		String defaultHomeDirectory = System.getProperty("user.home") + File.separator + "JungleJepps" + File.separator;
		setValue(Settings.IS_PRIMARY, 				true);
		setValue(Settings.REPOSITORY_PATH, 			defaultHomeDirectory);
		setValue(Settings.PRIMARY_JDBC_URI, 		"jdbc:sqlite:JJDB.db");
		setValue(Settings.PRIMARY_JDBC_CLASS_PATH, 	"org.sqlite.JDBC");
		setValue(Settings.IS_OPERATIONS_DB, 		false);
		setValue(Settings.ALITUDE_UNITS, 			"ft");
		setValue(Settings.WEIGHT_UNITS, 			"kg");
		setValue(Settings.DIMENSION_UNITS, 			"nm");
		setValue(Settings.DISTANCE_UNITS, 			"m");
		setValue(Settings.DISTANCE_CONVERT_FACTOR, 	1.852);
		setValue(Settings.PAGE_1_DISCLAIMER, 		"Use of this diagram is strictly prohibited for aviation operators other than Jungle Jepps mission users. This diagram MAY CONTAIN ERRORS.");
		setValue(Settings.PAGE_2_DISCLAIMER, 		"No diagram can substitute for a proper runway checkout. Use of this diagram is strictly prohibited for aviation operators other than Jungle Jepps mission users. This diagram MAY CONTAIN ERRORS. Report all safety concerns to the Chief Pilot or designated Safety Officer.");
		setValue(Settings.DEFAULT_EXPIRATION_PERIOD, 12);
		
	}
	
	/* GETTERS AND SETTERS */
	/**
	 * Get literal string for key 
	 * @param key
	 * @return
	 */
	private String getStringForKey(String key){
		return connection.getProperty(key, "");
	}
		
	/**
	 * Set literal string for key
	 * @param key
	 * @param value
	 */
	private void setValue(String key, String value){
		connection.put(key, value);
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
	 * Get the field label for a given field 
	 * @param field
	 * @return field label
	 */
	public String getLabel(Field field){
		return getStringForKey(LABEL_PREFIX + field.toString() );
	}
	
	
	/**
	 * Set label for field
	 * @param field
	 * @param label
	 */
	public void setLabel(Field field, String label){
		setValue(LABEL_PREFIX + field.toString(), label);
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
		return Integer.parseInt( getStringForKey(key.toString()) );	
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
		return Double.parseDouble( getStringForKey(key.toString()) );	
	}
	
	/**
	 * Set settings to double value 
	 * @param key
	 * @param value
	 */
	public void setValue(Settings key, double value){
		setValue(key.toString(), String.valueOf(value) );
	}


}
