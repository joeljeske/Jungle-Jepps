package org.yajasi.JungleJepps.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.yajasi.JungleJepps.Field;

public class SettingsManager {
	
	private static final String JDBC_DRIVER_CLASSPATH = "org.sqlite.JDBC";
	private static final String JDBC_PREFIX = "jdbc:sqlite:";
	private static final String DB_NAME = "settings.db";
	private static final String LABEL_PREFIX = "label_";
	private static final String GENERAL_SETTINGS_TABLE = "gen_settings";
	private static final String KEY_COLUMN = "key";
	private static final String VALUE_COLUMN = "value";
	
	/**
	 * The static reference to the instance. Used to keep only one 
	 * instance during runtime.
	 */
	private static SettingsManager instance;
	
	private Connection connection; 
	
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
		try {
			Class.forName( JDBC_DRIVER_CLASSPATH );
			
			connection = DriverManager.getConnection(JDBC_PREFIX + DB_NAME);
			initialize();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private void initialize(){
		Statement statement;
		try {
			statement = connection.createStatement();
			statement.setQueryTimeout(5);
			
			statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + GENERAL_SETTINGS_TABLE + "(" + KEY_COLUMN + "string, " + VALUE_COLUMN + "string)" );
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Get the field label for a given field 
	 * @param field
	 * @return field label
	 */
	public String getLabel(Field field){
		return getLabel( field.toString() );
	}
	
	/**
	 * Get the field label for a given field 
	 * @param field
	 * @return field label
	 */
	public String getLabel(String field){
		return getStringForKey( LABEL_PREFIX + field );
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
		ResultSet result = getKey(key);
		String val;
		
		if(result == null) 
			val = "";
		else
		{
			try {
				val = result.getString(VALUE_COLUMN);
			} catch (SQLException e) {
				val = "";
			}
		}
		
		return val;
	}
	
	private ResultSet getKey(String key) {
		String sql = "SELECT * FROM " + GENERAL_SETTINGS_TABLE + "  WHERE " + KEY_COLUMN + " = '" + key + "'";
		Statement statement;
		ResultSet result;
		
		try {
			statement = connection.createStatement();
			result = statement.executeQuery(sql);
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
		return result;
	}
	


}
