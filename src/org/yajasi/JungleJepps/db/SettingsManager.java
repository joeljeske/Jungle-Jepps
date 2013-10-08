package org.yajasi.JungleJepps.db;

public class SettingsManager {
	
	/**
	 * The file name for the configurations.
	 */
	private static final String CONFIG_NAME = "config.properties";
	
	/**
	 * The static reference to the instance. Used to keep only one 
	 * instance during runtime.
	 */
	private static SettingsManager instance;
	
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
	 * Private constructor sets all property readers
	 */
	private SettingsManager(){
		//Do all setup work here
	}
	
	
	public int getIntegerForKey(String key){
		throw new UnsupportedOperationException();
	}
	
	public boolean getBooleanForKey(String key){
		throw new UnsupportedOperationException();
	}
	
	public float getFloatForKey(String key){
		throw new UnsupportedOperationException();
	}
	
	public String getStringForKey(String key){
		throw new UnsupportedOperationException();
	}

}
